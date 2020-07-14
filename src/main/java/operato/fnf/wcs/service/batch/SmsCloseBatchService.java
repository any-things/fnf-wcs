package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WcsMheDasRtnBoxRslt;
import operato.fnf.wcs.entity.WmsRtnSortDr;
import operato.fnf.wcs.entity.WmsRtnSortHr;
import operato.fnf.wcs.service.send.SmsInspSendService;
import operato.logis.sms.SmsConstants;
import operato.logis.wcs.service.impl.WcsBatchProgressService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.util.ValueUtil;

/**
 * SMS 작업 배치 종료 서비스
 * 
 * 
 */
@Component
public class SmsCloseBatchService extends AbstractQueryService {
	/**
	 * DAS 작업 서머리 서비스
	 */
	@Autowired
	private JobSummaryService jobSummarySvc;
	
	/**
	 * 반품 검수완료 Box 전송 서비스
	 */
	@Autowired
	private SmsInspSendService smsInspSendSvc;
	
	/**
	 * WCS 배치 생산성 정보 업데이트 서비스 
	 */
	@Autowired
	private WcsBatchProgressService progressSvc;
	
	/**
	 * SMS 작업 배치 종료
	 * 
	 * @param batch
	 */
	public void closeBatch(JobBatch batch) {
		// 1. 10분 생산성 최종 마감
		// 작업해야함 실적을 가지고 쿼리 생성해야한다.
		this.closeProductivity(batch);
		
		// 2. 배치에 반영
		this.setBatchInfoOnClosing(batch);
		
		this.sendInspBoxScanResultToWms(batch);
		
		// 3. WMS MHE_HR 테이블에 마감 전송
		Query query = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		query.addFilter("batchGroupId", batch.getBatchGroupId());
		List<JobBatch> jobBatches = this.queryManager.selectList(JobBatch.class, query);
		
		for (JobBatch jobBatch : jobBatches) {
			WmsRtnSortHr rtnSortHr = new WmsRtnSortHr();
			rtnSortHr.setWhCd(FnFConstants.WH_CD_ICF);
			rtnSortHr.setMheNo(jobBatch.getEquipCd());
			rtnSortHr.setStrrId(jobBatch.getBrandCd());
			//분류일자 언제를 말하는건지???
			rtnSortHr.setSortDate(DateUtil.dateStr(new Date(), "yyyyMMdd"));
			rtnSortHr.setSortSeq(jobBatch.getJobSeq());
			rtnSortHr.setStatus("A");
			rtnSortHr.setInsDatetime(new Date());
			this.getDataSourceQueryManager(WmsRtnSortHr.class).insert(rtnSortHr);
		}
		
		Query wmsCondition = new Query();
		wmsCondition.addFilter("WH_CD", FnFConstants.WH_CD_ICF);
		wmsCondition.addFilter("BATCH_NO", batch.getBatchGroupId());
		wmsCondition.addFilter("DEL_YN", LogisConstants.N_CAP_STRING);
		List<WcsMheDasRtnBoxRslt> boxList = this.queryManager.selectList(WcsMheDasRtnBoxRslt.class, wmsCondition);
		List<WmsRtnSortDr> rtnSortDrList = new ArrayList<WmsRtnSortDr>(boxList.size());
		
		for (WcsMheDasRtnBoxRslt rtnBox : boxList) {
			WmsRtnSortDr sortDr = new WmsRtnSortDr();
			sortDr.setWhCd(FnFConstants.WH_CD_ICF);
			sortDr.setMheNo(rtnBox.getMheNo());
			sortDr.setStrrId(rtnBox.getStrrId());
			sortDr.setSortDate(rtnBox.getSortDate());
			sortDr.setSortSeq(batch.getJobSeq());
			sortDr.setItemCd(rtnBox.getItemCd());
			sortDr.setBoxNo(rtnBox.getBoxNo());
			sortDr.setCmptQty(rtnBox.getCmptQty());
			sortDr.setInsDatetime(new Date());
			
			rtnSortDrList.add(sortDr);
		}
		this.getDataSourceQueryManager(WmsRtnSortDr.class).updateBatch(rtnSortDrList);
	}
	
	/**
	 * 배치 종료시에 설정할 정보 설정
	 * 
	 * @param batch
	 */
	private void setBatchInfoOnClosing(JobBatch batch) {
		this.progressSvc.updateBatchProductionResult(batch, batch.getFinishedAt());
		batch.setStatus(JobBatch.STATUS_END);		
		this.queryManager.update(batch, "status", "finishedAt", "resultBoxQty", "resultOrderQty", "resultPcs", "progressRate", "equipRuntime", "uph", "updatedAt");
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
		
		// 작업 해야함.
		this.jobSummarySvc.summaryTotalBatchJobs(batch);
	}
	
	/**
	 * 소터 실적으로 검수정보가 없는 정보들은 매장 반품예정 검수 스캔결과(WMT_UIF_IMP_MHE_RTN_SCAN) 테이블로 전송
	 */
	private void sendInspBoxScanResultToWms(JobBatch batch) {
		if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SRTN, batch.getJobType())) {
			this.smsInspSendSvc.sendInspBoxScanResultToWms(batch);
		}
	}

}
