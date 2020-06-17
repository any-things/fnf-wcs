package operato.fnf.wcs.service.batch;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.logis.wcs.service.impl.WcsBatchProgressService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 작업 배치 종료 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsCloseBatchService extends AbstractQueryService {
	/**
	 * DAS 작업 서머리 서비스
	 */
	@Autowired
	private JobSummaryService jobSummarySvc;
	/**
	 * WCS 배치 생산성 정보 업데이트 서비스 
	 */
	@Autowired
	private WcsBatchProgressService progressSvc;
	
	/**
	 * DPS 작업 배치 종료
	 * 
	 * @param batch
	 */
	public void closeBatch(JobBatch batch) {
		// 1. 10분 생산성 최종 마감
		this.closeProductivity(batch);
		
		// 2. 배치에 반영
		this.setBatchInfoOnClosing(batch);
		
		// 3. WMS MHE_HR 테이블에 마감 전송
		String sql = "update mhe_hr set cmpt_qty = :pickedQty, status = :status, cnf_datetime = :finishedAt where wh_cd = :whCd and work_unit = :batchId";
		Map<String, Object> params = ValueUtil.newMap("whCd,batchId,status,pickedQty,finishedAt", FnFConstants.WH_CD_ICF, batch.getId(), "F", batch.getResultPcs(), batch.getFinishedAt());
		this.getDataSourceQueryManager(WmsMheHr.class).executeBySql(sql, params);
		
		// 4. WCS MHE_HR 테이블에 마감 전송
		sql = "update mhe_hr set status = :status, cmpt_qty = :pickedQty, cnf_datetime = :finishedAt, prcs_yn = 'Y', prcs_datetime = :finishedAt where wh_cd = :whCd and work_unit = :batchId";
		this.queryManager.executeBySql(sql, params);
		
		// 5. 트레이 상태 리셋 
		sql = "update tray_boxes set status = :status where domain_id = :domainId";
		this.queryManager.executeBySql(sql, ValueUtil.newMap("domainId,status", batch.getDomainId(), LogisConstants.JOB_STATUS_WAIT));
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
		
		this.jobSummarySvc.summaryTotalBatchJobs(batch);
	}

}
