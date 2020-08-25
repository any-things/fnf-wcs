package operato.fnf.wcs.service.batch;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.query.store.FnFDasQueryStore;
import operato.fnf.wcs.query.store.FnFDpsQueryStore;
import operato.fnf.wcs.query.store.FnFSmsQueryStore;
import operato.fnf.wcs.service.model.ResultSummary;
import operato.logis.sms.SmsConstants;
import operato.logis.wcs.entity.DailyProdSummary;
import operato.logis.wcs.entity.Productivity;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.ClassUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DAS 작업 서머리 관리 서비스
 * 
 * @author shortstop
 */
@Component
public class JobSummaryService extends AbstractQueryService {

	/**
	 * FnF DAS용 쿼리 스토어
	 */
	@Autowired
	private FnFDasQueryStore fnfDasQueryStore;
	/**
	 * FnF DPS용 쿼리 스토어
	 */
	@Autowired
	private FnFDpsQueryStore fnfDpsQueryStore;
	/**
	 * FnF SMS용 쿼리 스토어
	 */
	@Autowired
	private FnFSmsQueryStore fnfSmsQueryStore;
	
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
		// 1. 시간대 서머리 정보 조회 (없으면 생성)
		Productivity p = this.findProductivity(batch, date, hour);
		
		// 2. 시간대, 10분대 계산 
		int minuteTo = (ValueUtil.toInteger(Math.floor(minute / 10)) + 1) * 10;
		int minuteFrom = minuteTo - 10;
		
		// 3. 시간, 분대에 대한 실적 조회
		int resultQty = this.calc10MinResult(batch, date, hour, minuteFrom, minuteTo);
		
		// 4. 시간, 분대에 대한 실적 업데이트
		this.updateResultSummary(p, minuteTo, resultQty);
	}
	
	/**
	 * 배치의 총 실적 서머리 조회 쿼리 
	 * 
	 * @param batch
	 * @return
	 */
	private String getBatchTotalResultSummaryQuery(JobBatch batch) {
		String jobType = batch.getJobType();
		
		if(LogisConstants.isDasJobType(jobType)) {
			return this.fnfDasQueryStore.getDasBatchTotalResultSummary();
			
		} else if(LogisConstants.isDpsJobType(jobType)) {
			return this.fnfDpsQueryStore.getDpsBatchTotalResultSummary();
			
		} else if(ValueUtil.isEqual(SmsConstants.JOB_TYPE_SRTN, jobType)) {
//			return this.fnfSmsQueryStore.getSrtnBatchTotalResultSummary();
			return null;
		} else {
			return null;
		}
	}
	
	/**
	 * 작업 배치별 10분대별 실적 서머리 최총 처리
	 * 
	 * @param batch
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void summaryTotalBatchJobs(JobBatch batch) {
		// 1. 시간, 분대에 대한 실적 조회
		String sql = this.getBatchTotalResultSummaryQuery(batch);
		if(ValueUtil.isEmpty(sql)) {
			return;
		}
		
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		List<ResultSummary> summaries = this.queryManager.selectListBySql(sql, params, ResultSummary.class, 0, 0);
		
		// 2. 시작 시간, 완료 시간을 구해서 Productivity에 없는 데이터를 일괄적으로 등록한다.
		String startDate = xyz.elidom.util.DateUtil.dateStr(batch.getInstructedAt(), "yyyy-MM-dd");
		String endDate = xyz.elidom.util.DateUtil.dateStr(batch.getFinishedAt(), "yyyy-MM-dd");
		String startHour = xyz.elidom.util.DateUtil.dateTimeStr(batch.getInstructedAt(), "HH");
		String endHour = xyz.elidom.util.DateUtil.dateTimeStr(batch.getFinishedAt(), "HH");
		
		// 시작, 완료 일자가 하루 이상이 나는 경우와 그렇지 않은 경우 처리
		if(ValueUtil.isNotEqual(startDate, endDate)) {
			// startDate, endDate의 차이를 날짜 단위로 계산 
			long diffTime = batch.getFinishedAt().getTime() - batch.getInstructedAt().getTime();
			float diffDay = (diffTime / ValueUtil.toFloat((24 * 60 * 60 * 1000)));
			int diffDays = ValueUtil.toInteger(Math.round(diffDay));

			for(int i = 0 ; i <= diffDays ; i++) {
				String date = xyz.elidom.util.DateUtil.addDateToStr(batch.getInstructedAt(), i);
				String sHour = (i == 0) ? startHour : "09";				// TODO 업무 시작 시간을 설정으로 
				String eHour = (i == diffDays) ? endHour : "18";		// TODO 업무 완료 시간을 설정으로 
				this.createBasicJobSummaries(batch, date, sHour, eHour);
			}
		} else {
			this.createBasicJobSummaries(batch, startDate, startHour, endHour);
		}
		
		// 3. 시간대별로 다시 업데이트
		for(ResultSummary summary : summaries) {
			if(ValueUtil.isNotEmpty(summary.getWorkDate())) {
				Productivity p = this.findProductivity(batch, summary.getWorkDate(), summary.getHour());
				this.updateResultSummary(p, summary.getMinute(), summary.getPickedQty());
			}
		}
	}
	
	/**
	 * 작업 배치별 일별 실적 서머리 최총 처리
	 * 
	 * @param batch
	 * @param date
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void summaryDailyBatchJobs(JobBatch batch, String date) {
		// 1. 이미 일별 서머리가 있다면 삭제
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("jobDate", date);
		this.queryManager.deleteByCondition(DailyProdSummary.class, condition);

		// 2. 일별 서머리 생성
		DailyProdSummary dailySum = ValueUtil.populate(batch, new DailyProdSummary());
		dailySum.setId(null);
		dailySum.setJobDate(date);
		dailySum.setBatchId(batch.getId());
		dailySum.setAttr01(batch.getBrandCd());
		dailySum.setAttr02(batch.getSeasonCd());
		
		String[] dateArr = date.split(LogisConstants.DASH);
		dailySum.setYear(dateArr[0]);
		dailySum.setMonth(dateArr[1]);
		dailySum.setDay(dateArr[2]);
		
		// 3. 시간대별 실적을 계산하여 설정
		for(int i = 0 ; i < 24 ; i++) {
			int resultQty = this.calc1HourResult(batch, date, i);
			String hourStr =  (i >= 9) ? LogisConstants.EMPTY_STRING + (i + 1) : LogisConstants.ZERO_STRING + (i + 1);
			String fieldName = "h" + hourStr + "Result";
			ClassUtil.setFieldValue(dailySum, fieldName, resultQty);
		}
		
		int planQty = batch.getBatchPcs();
		int resultQty = batch.getResultPcs();
		float progressRate = (resultQty == 0 || planQty == 0) ? 0.0f : (ValueUtil.toFloat(resultQty) / ValueUtil.toFloat(planQty) * 1.0f) * 100.0f;
		
		dailySum.setResultQty(resultQty);
		dailySum.setPlanQty(planQty);
		dailySum.setLeftQty(planQty - resultQty);
		dailySum.setProgressRate(progressRate);
		
		// 4. 배치가 종료되었다면 - 배치 총 시간, 설비가동율 등 계산 설정 
		if(ValueUtil.isEqual(batch.getStatus(), JobBatch.STATUS_END)) {
			long gap = batch.getFinishedAt().getTime() - batch.getInstructedAt().getTime();
			float totalMin = ValueUtil.toFloat(gap / ValueUtil.toLong(1000 * 60));
			float equipRtMin = dailySum.getEquipRuntime();
			float equipRate = (totalMin == 0 || equipRtMin == 0) ? 0.0f : (equipRtMin / totalMin) * 100.0f;
			dailySum.setEquipRate(equipRate);
		}
		
		// 5. 일별 서머리 생성
		this.queryManager.insert(dailySum);
	}
	
	/**
	 * 분대별 실적 서머리 업데이트
	 * 
	 * @param p
	 * @param minute
	 * @param resultQty
	 */
	private void updateResultSummary(Productivity p, int minute, int resultQty) {
		if(resultQty > 0) {
			this.updateResultSummary(p, LogisConstants.EMPTY_STRING + minute, resultQty);
		}
	}
	
	/**
	 * 분대별 실적 서머리 업데이트
	 * 
	 * @param p
	 * @param minute
	 * @param resultQty
	 */
	private void updateResultSummary(Productivity p, String minute, int resultQty) {
		if(resultQty > 0) {
			try {
				String minStr = StringUtils.leftPad(minute, 2, LogisConstants.ZERO_STRING);
				String fieldName = "m" + minStr + "Result";
				ClassUtil.setFieldValue(p, fieldName, resultQty);
				this.queryManager.update(p, fieldName);
			} catch (Throwable e) {
				e.printStackTrace();
			}
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
			findProductivity(batch, date, i);
		}
	}

	/**
	 * 10분대 실적 서머리 조회
	 * 
	 * @param batch
	 * @param date
	 * @param hour
	 * @return
	 */
	private Productivity findProductivity(JobBatch batch, String date, int hour) {
		return this.findProductivity(batch, date, ValueUtil.toString(hour));
	}
	
	/**
	 * 10분대 실적 서머리 조회
	 * 
	 * @param batch
	 * @param date
	 * @param hour
	 * @return
	 */
	private Productivity findProductivity(JobBatch batch, String date, String hour) {
		String hourStr = StringUtils.leftPad(hour, 2, LogisConstants.ZERO_STRING);
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("jobDate", date);
		condition.addFilter("jobHour", hourStr);
		Productivity p = this.queryManager.selectByCondition(Productivity.class, condition);

		if(p == null) {
			p = ValueUtil.populate(batch, new Productivity());
			p.setId(null);
			p.setBatchId(batch.getId());
			p.setJobDate(date);
			p.setAttr01(batch.getBrandCd());
			p.setAttr02(batch.getSeasonCd());
			p.setJobHour(hourStr);
			p.setM10Result(0);
			p.setM20Result(0);
			p.setM30Result(0);
			p.setM40Result(0);
			p.setM50Result(0);
			p.setM60Result(0);
			p.setInputWorkers(batch.getInputWorkers());
			p.setTotalWorkers(batch.getTotalWorkers());
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
	private int calc10MinResult(JobBatch batch, String date, int hour, int minFrom, int minTo) {
		String sql = this.getCalc10MinuteResultSummaryQuery(batch);
		if(ValueUtil.isEmpty(sql)) {
			return 0;
		}
		
		String timeFrom = this.getFrom10Minute(date, hour, minFrom);
		String timeTo = this.getTo10Minute(date, hour, minTo);
		Map<String, Object> params = ValueUtil.newMap("batchId,timeFrom,timeTo", batch.getId(), timeFrom, timeTo);
		
		if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SDPS, batch.getJobType())) {
			Query query = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
			query.addFilter("status", LogisConstants.NOT_EQUAL, JobBatch.STATUS_END);
			query.addFilter("batchGroupId", LogisConstants.IN, batch.getBatchGroupId());
			query.addOrder("jobType", false);
			query.addOrder("instructedAt", true);
			List<JobBatch> groupBatch = this.queryManager.selectList(JobBatch.class, query);
			List<String> batchList = AnyValueUtil.filterValueListBy(groupBatch, "id");
			
			params.put("batchList", batchList);
		}
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}
	
	/**
	 * 작업 배치의 시간대 10분대 실적 합을 구한다.
	 *  
	 * @param batch
	 * @param date
	 * @param hour
	 * @return
	 */
	private int calc1HourResult(JobBatch batch, String date, int hour) {
		String sql = this.getDasCalc1HourResultSummaryQuery(batch);
		if(ValueUtil.isEmpty(sql)) {
			return 0;
		}
		
		String timeFrom = this.getFromHour(date, hour);
		String timeTo = this.getToHour(date, hour);
		Map<String, Object> params = ValueUtil.newMap("batchId,timeFrom,timeTo", batch.getId(), timeFrom, timeTo);
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}

	/**
	 * 10분간 실적 서머리 조회 쿼리 
	 * 
	 * @param batch
	 * @return
	 */
	private String getCalc10MinuteResultSummaryQuery(JobBatch batch) {
		String jobType = batch.getJobType();
		
		if(LogisConstants.isDasJobType(jobType)) {
			return this.fnfDasQueryStore.getDasCalc10MinuteResultSummary();
			
		} else if(LogisConstants.isDpsJobType(jobType)) {
			return this.fnfDpsQueryStore.getDpsCalc10MinuteResultSummary();
			
		} else if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SRTN, jobType)) {
			return this.fnfSmsQueryStore.getSrtnCalc10MinuteResultSummary();
		} else if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SDAS, jobType)) {
			return this.fnfSmsQueryStore.getSdasCalc10MinuteResultSummary();
		} else if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SDPS, jobType)) {
			return this.fnfSmsQueryStore.getSdpsCalc10MinuteResultSummary();
		} else {
			return null;
		}
	}
	
	/**
	 * 10분간 실적 서머리 조회 쿼리 
	 * 
	 * @param batch
	 * @return
	 */
	private String getDasCalc1HourResultSummaryQuery(JobBatch batch) {
		String jobType = batch.getJobType();
		
		if(LogisConstants.isDasJobType(jobType)) {
			return this.fnfDasQueryStore.getDasCalc1HourResultSummary();
			
		} else if(LogisConstants.isDpsJobType(jobType)) {
			return this.fnfDpsQueryStore.getDpsCalc1HourResultSummary();
			
		} else {
			return null;
		}
	}
	
	/**
	 * 시간대 10분대 From Time
	 * 
	 * @param date
	 * @param hour
	 * @param minFrom
	 * @return
	 */
	private String getFrom10Minute(String date, int hour, int minFrom) {
		String hourStr = StringUtils.leftPad("" + hour, 2, LogisConstants.ZERO_STRING);
		String minFromStr = StringUtils.leftPad("" + minFrom, 2, LogisConstants.ZERO_STRING);
		return date + " " + hourStr + ":" + minFromStr + ":00.000";
	}
	
	/**
	 * 시간대 10분대 To Time
	 * 
	 * @param date
	 * @param hour
	 * @param minTo
	 * @return
	 */
	private String getTo10Minute(String date, int hour, int minTo) {
		String hourStr = StringUtils.leftPad("" + hour, 2, LogisConstants.ZERO_STRING);
		minTo = minTo - 1;
		String minToStr = StringUtils.leftPad("" + minTo, 2, LogisConstants.ZERO_STRING);
		return date + " " + hourStr + ":" + minToStr + ":59.999";
	}
	
	/**
	 * 시간대 From Hour
	 * 
	 * @param date
	 * @param hour
	 * @return
	 */
	private String getFromHour(String date, int hour) {
		String hourStr = StringUtils.leftPad("" + hour, 2, LogisConstants.ZERO_STRING);
		return date + " " + hourStr + ":00:00.000";
	}
	
	/**
	 * 시간대 To Hour
	 * 
	 * @param date
	 * @param hour
	 * @return
	 */
	private String getToHour(String date, int hour) {
		String hourStr = StringUtils.leftPad("" + hour, 2, LogisConstants.ZERO_STRING);
		return date + " " + hourStr + ":59:59.999";
	}

}
