package operato.fnf.wcs.service.batch;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.query.store.FnFDasQueryStore;
import operato.fnf.wcs.service.model.ResultSummary;
import operato.logis.wcs.entity.Productivity;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.ClassUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 서머리 관리 서비스
 * 
 * @author shortstop
 */
@Component
public class DasJobSummaryService extends AbstractQueryService {

	/**
	 * FnF DAS용 쿼리 스토어
	 */
	@Autowired
	private FnFDasQueryStore fnfDasQueryStore;
	
	/**
	 * 작업 배치별 10분대별 실적 서머리 처리
	 * 
	 * @param batch
	 * @param date
	 * @param hour
	 * @param minute
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void summary10MinuteJobs(JobBatch batch, String date, int hour, int minute) {
		// 1. 시간대, 10분대 계산 
		String hourStr = StringUtils.leftPad(ValueUtil.toString(hour), 2, "0");
		int minuteTo = (ValueUtil.toInteger(Math.floor(minute / 10)) + 1) * 10;
		int minuteFrom = minuteTo - 10;
		
		// 2. 생산성 데이터 조회 (없으면 생성)
		String minStr = ValueUtil.toString(minuteTo);
		Productivity p = this.findProductivity(batch, date, hourStr, minStr);
		
		// 3. 시간, 분대에 대한 실적 조회
		int result = this.calc10MinResult(batch, date, hourStr, StringUtils.leftPad(ValueUtil.toString(minuteFrom), 2, "0"), ValueUtil.toString(minuteTo));
		String fieldName = "m" + minStr + "Result";
		ClassUtil.setFieldValue(p, fieldName, result);
		this.queryManager.update(p, fieldName, "updatedAt");
	}
	
	/**
	 * 작업 배치별 10분대별 실적 서머리 최총 처리
	 * 
	 * @param batch
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void summaryTotalBatchJobs(JobBatch batch) {
		// 1. 시간, 분대에 대한 실적 조회
		String sql = this.fnfDasQueryStore.getDasBatchTotalResultSummary();
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		List<ResultSummary> summaries = this.queryManager.selectListBySql(sql, params, ResultSummary.class, 0, 0);
		
		// 2. 시작 시간, 완료 시간을 구해서 Productivity에 없는 데이터를 일괄적으로 등록한다.
		String startDate = xyz.elidom.util.DateUtil.dateStr(batch.getInstructedAt(), "yyyy-MM-dd");
		String startHour = xyz.elidom.util.DateUtil.dateTimeStr(batch.getInstructedAt(), "HH24");
		String endHour = xyz.elidom.util.DateUtil.dateTimeStr(batch.getFinishedAt(), "HH24");
		
		// startDate, endDate의 차이를 날짜 단위로 계산 
		long diffDate = batch.getFinishedAt().getTime() - batch.getInstructedAt().getTime();
        int diffDays = ValueUtil.toInteger(Math.abs(Math.floor(diffDate / (24 * 60 * 60 * 1000))));
        
        // 시작, 완료 일자가 하루 이상이 나는 경우와 그렇지 않은 경우 처리
        if(diffDays > 0) {
            for(int i = 0 ; i < diffDays ; i++) {
            	String date = xyz.elidom.util.DateUtil.addDateToStr(batch.getInstructedAt(), i);
            	String sHour = (i == 0) ? startHour : "09";				// TODO 업무 시작 시간을 설정으로 
            	String eHour = (i == diffDays - 1) ? endHour : "18";	// TODO 업무 완료 시간을 설정으로 
            	this.createBasicJobSummaries(batch, date, sHour, eHour);
            }
        } else {
        	this.createBasicJobSummaries(batch, startDate, startHour, endHour);
        }
		
		// 3. 시간대별로 다시 업데이트
		for(ResultSummary summary : summaries) {
			String hour = StringUtils.leftPad(summary.getHour(), 2, "0");
			String min = StringUtils.leftPad(summary.getMinute(), 2, "0");
			Productivity p = this.findProductivity(batch, summary.getWorkDate(), hour, min);
			String fieldName = "m" + min + "_result";
			ClassUtil.setFieldValue(p, fieldName, summary.getPickedQty());
			this.queryManager.update(p, fieldName);
		}
	}
	
	/**
	 * 작업 배치에서 시작, 완료 시간에 빠져있는 작업 서머리 정보를 먼저 생성
	 * 
	 * @param batch
	 * @param date
	 * @param startHour
	 * @param endHour
	 */
	private void createBasicJobSummaries(JobBatch batch, String date, String startHour, String endHour) {
		int sHour = Integer.parseInt(startHour);
		int eHour = Integer.parseInt(endHour);

		for(int i = sHour ; i <= eHour ; i++) {
			findProductivity(batch, date, StringUtils.leftPad("" + i, 2, "0"), null);
		}
	}

	/**
	 * 10분대 실적 서머리 조회
	 * 
	 * @param batch
	 * @param date
	 * @param hour
	 * @param min
	 * @return
	 */
	private Productivity findProductivity(JobBatch batch, String date, String hour, String min) {
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("jobDate", date);
		condition.addFilter("hour", hour);
		Productivity p = this.queryManager.selectByCondition(Productivity.class, condition);

		if(p == null) {
			p = ValueUtil.populate(batch, new Productivity());
			p.setAttr01(batch.getBrandCd());
			p.setJobHour(hour);
			p.setM10Result(0);
			p.setM20Result(0);
			p.setM30Result(0);
			p.setM40Result(0);
			p.setM50Result(0);
			p.setM60Result(0);
			this.queryManager.insert(p);
		}
		
		return p;
	}
	
	/**
	 * 작업 배치의 시간대 10분대 실적 합을 구한다.
	 *  
	 * @param batch
	 * @param date
	 * @param hour
	 * @param minFrom
	 * @param minTo
	 * @return
	 */
	private int calc10MinResult(JobBatch batch, String date, String hour, String minFrom, String minTo) {
		String timeFrom = date + " " + hour + ":" + minFrom + ":00";
		String timeTo = date + " " + hour + ":" + minTo + ":00";
		String sql = this.fnfDasQueryStore.getDasCalc10MinuteResultSummary();
		Map<String, Object> params = ValueUtil.newMap("batchId,date,timeFrom,timeTo", batch.getId(), date, timeFrom, timeTo);
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}

}
