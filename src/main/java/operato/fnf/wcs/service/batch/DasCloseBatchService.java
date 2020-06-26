package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.entity.WmsMheBox;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.query.store.FnFDasQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.ValueUtil;

/**
 * DAS 작업 배치 종료 서비스
 * 
 * @author shortstop
 */
@Component
public class DasCloseBatchService extends AbstractQueryService {

	/**
	 * FNF 용 DAS 쿼리 스토어
	 */
	@Autowired
	private FnFDasQueryStore fnfDasQueryStore;
	/**
	 * DAS 작업 서머리 서비스
	 */
	@Autowired
	private JobSummaryService jobSummarySvc;
	
	/**
	 * MheHr 정보로 부터 JobBatch에 배치 완료 정보를 반영한다.
	 * 
	 * @param domainId
	 * @param wcsMheHr
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void closeBatch(Long domainId, WcsMheHr wcsMheHr) {
		
		// 1. WCS MHE_HR 정보로 부터 작업 배치를 조회 
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("id", wcsMheHr.getWorkUnit());
		JobBatch batch = this.queryManager.selectByCondition(JobBatch.class, condition);
		
		if(batch == null) {
			return;
		}
		
		// 2. 10분 생산성 최종 마감
		this.closeProductivity(batch);
		
		// 3. 배치에 반영 
		this.setBatchInfoOnClosing(batch);

		// 4. WMS에 박스 실적 한 번에 전송
		this.sendAllBoxToWms(batch);
		
		// 5. WMS MHE_HR 테이블에 반영
		this.closeWmsWave(batch, wcsMheHr);
		
		// 6. WcsMheHr 엔티티에 반영
		this.closeWcsWave(batch, wcsMheHr);		
	}
	
	/**
	 * 배치 종료시에 설정할 정보 설정
	 * 
	 * @param batch
	 */
	private void setBatchInfoOnClosing(JobBatch batch) {
		batch.setStatus(JobBatch.STATUS_END);
		batch.setResultBoxQty(this.calcBatchResultBoxQty(batch));
		batch.setResultOrderQty(this.calcBatchResultOrderQty(batch));
		batch.setResultPcs(this.calcBatchResultPcs(batch));
		batch.setProgressRate(batch.getBatchOrderQty() == 0 ? 0 : ((float)batch.getResultOrderQty() / (float)batch.getBatchOrderQty() * 100.0f));
		batch.setEquipRuntime(this.calcBatchEquipRuntime(batch));
		batch.setUph(this.calcBatchUph(batch));
		this.queryManager.update(batch, "status", "finishedAt", "resultBoxQty", "resultOrderQty", "resultPcs", "progressRate", "equipRuntime", "uph", "updatedAt");
	}

	/**
	 * 작업 배치의 최종 처리 박스 총 수량을 구한다.
	 * 
	 * @param batch
	 * @return
	 */
	private int calcBatchResultBoxQty(JobBatch batch) {
		String sql = "select COALESCE(count(distinct(box_no)), 0) as result from mhe_box where work_unit = :batchId";
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}
	
	/**
	 * 작업 배치의 최종 처리 주문 총 수량을 구한다.
	 * 
	 * @param batch
	 * @return
	 */
	private int calcBatchResultOrderQty(JobBatch batch) {
		String sql = "select COALESCE(count(distinct(shipto_id)), 0) as result from mhe_box where work_unit = :batchId";
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}
	
	/**
	 * 작업 배치의 최종 처리 총 수량을 구한다.
	 * 
	 * @param batch
	 * @return
	 */
	private int calcBatchResultPcs(JobBatch batch) {
		String sql = "select COALESCE(sum(cmpt_qty), 0) as result from mhe_box where work_unit = :batchId";
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}
	
	/**
	 * 작업 배치의 최종 설비 가동율을 구한다.
	 * 
	 * @param batch
	 * @return
	 */
	private float calcBatchEquipRuntime(JobBatch batch) {
		// 배치 총 시간 
		long gap = batch.getFinishedAt().getTime() - batch.getInstructedAt().getTime();
		int totalMin = ValueUtil.toInteger(gap / ValueUtil.toLong(1000 * 60));
		
		// Productivity 정보에서 10분당 실적이 0인 구간을 모두 합쳐서 시간 계산
		String sql = this.fnfDasQueryStore.getDasEquipmentIdleTime();
		int idleMin = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId()), Integer.class);
		
		// duration에서 일하지 않은 총 시간을 빼서 실제 가동 시간을 구함
		return ValueUtil.toFloat(totalMin - idleMin);
	}
	
	/**
	 * 작업 배치의 최종 시간당 생산성을 구한다.
	 * 
	 * @param batch
	 * @return
	 */
	private float calcBatchUph(JobBatch batch) {
		long duration = batch.getFinishedAt().getTime() - batch.getInstructedAt().getTime();
		int pcs = batch.getResultPcs();
		float uph = ValueUtil.toFloat(ValueUtil.toFloat(pcs * 1000 * 60 * 60) / ValueUtil.toFloat(duration));
		return uph;
	}
	
	/**
	 * 작업 배치 생산성 최종 마감
	 * 
	 * @param batch
	 */
	private void closeProductivity(JobBatch batch) {
		if(ValueUtil.isEmpty(batch.getFinishedAt())) {
			batch.setFinishedAt(new Date());
		}
		
		this.jobSummarySvc.summaryTotalBatchJobs(batch);
	}

	/**
	 * 박스 전송 실적을 한 번에 전송
	 * 
	 * @param batch
	 * @return
	 */
	private int sendAllBoxToWms(JobBatch batch) {
		// 배치별 박스 실적 모두 조회
		Query condition = new Query();
		condition.addFilter("workUnit", batch.getId());
		// TODO 취소 실적은 제외 조건
		List<WcsMheBox> wcsBoxList = this.queryManager.selectList(WcsMheBox.class, condition);
		List<WmsMheBox> wmsBoxList = new ArrayList<WmsMheBox>();
		
		// 배치별 박스 실적 WMS로 전송
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheBox.class);
		for(WcsMheBox fromBox : wcsBoxList) {
			WmsMheBox toBox = ValueUtil.populate(fromBox, new WmsMheBox());
			wmsBoxList.add(toBox);
			
			if(wmsBoxList.size() >= 500) {
				wmsQueryMgr.insertBatch(wmsBoxList);
				wmsBoxList.clear();
			}
		}
		
		if(!wmsBoxList.isEmpty()) {
			wmsQueryMgr.insertBatch(wmsBoxList);
			wmsBoxList.clear();
		}
		
		return wcsBoxList.size();
	}
	
	/**
	 * WMS Wave를 마감 처리 
	 * 
	 * @param batch
	 * @param wcsMheHr
	 */
	private void closeWmsWave(JobBatch batch, WcsMheHr wcsMheHr) {
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		Query condition = new Query();
		condition.addFilter("whCd", wcsMheHr.getWhCd());
		condition.addFilter("workUnit", wcsMheHr.getWorkUnit());
		WmsMheHr wmsWave = wmsQueryMgr.selectByCondition(WmsMheHr.class, condition);
		
		if(wmsWave != null) {
			wmsWave.setCmptQty(batch.getResultPcs());
			wmsWave.setStatus("F");
			wmsWave.setCnfDatetime(new Date());
			wmsQueryMgr.update(wmsWave, "cmptQty", "status", "cnfDatetime");
		}		
	}

	/**
	 * WCS Wave 마감 처리
	 * 
	 * @param batch
	 * @param wcsMheHr
	 */
	private void closeWcsWave(JobBatch batch, WcsMheHr wcsMheHr) {
		Date currentTime = new Date();
		wcsMheHr.setStatus("F");
		wcsMheHr.setCnfDatetime(currentTime);
		wcsMheHr.setEndDatetime(currentTime);
		wcsMheHr.setPrcsDatetime(currentTime);
		wcsMheHr.setPrcsYn(LogisConstants.Y_CAP_STRING);
		this.queryManager.update(wcsMheHr, "status", "cnfDatetime", "endDatetime", "prcsDatetime", "prcsYn");		
	}

}
