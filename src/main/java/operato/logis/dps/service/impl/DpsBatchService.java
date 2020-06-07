package operato.logis.dps.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.service.batch.DpsCloseBatchService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.main.BatchCloseEvent;
import xyz.anythings.base.service.api.IBatchService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 작업 배치 서비스
 * 
 * @author shortstop
 */
@Component("dpsBatchService")
public class DpsBatchService extends AbstractLogisService implements IBatchService {

	/**
	 * DPS 배치 종료 서비스
	 */
	@Autowired
	private DpsCloseBatchService dpsCloseBatchSvc;
		
	@Override
	public void isPossibleCloseBatch(JobBatch batch, boolean closeForcibly) {
		// 1. 배치 마감 전 처리 이벤트 전송
		BatchCloseEvent event = new BatchCloseEvent(batch, SysEvent.EVENT_STEP_BEFORE);
		event = (BatchCloseEvent)this.eventPublisher.publishEvent(event);
		
		// 2. 이벤트 취소라면 ...
		if(event.isAfterEventCancel()) {
			return;
		}
		
		// 3. 작업 배치 상태 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
			// 진행 중인 작업배치가 아닙니다
			throw ThrowUtil.newStatusIsNotIng("terms.label.job_batch");
		}

		if(!closeForcibly) {
			// 4. 작업 배치별 처리하지 않은 주문이 존재하는지 체크
			Query condition = new Query();
			condition.addFilter(new Filter("workUnit", batch.getId()));
			condition.addFilter(new Filter("cmptQty", LogisConstants.EQUAL, 0));
			int count = this.queryManager.selectSize(WcsMheDr.class, condition);
			
			if(count > 0) {
				// {0} 등 {1}개의 호기에서 작업이 끝나지 않았습니다.
				String msg = MessageUtil.getMessage("ASSORTING_NOT_FINISHED_IN_RACKS", "{0} 등 {1}개의 호기에서 작업이 끝나지 않았습니다.", ValueUtil.toList(batch.getEquipCd(), "1"));
				throw ThrowUtil.newValidationErrorWithNoLog(msg);
			}
		}
	}

	@Override
	public void closeBatch(JobBatch batch, boolean forcibly) {
		// 1. 작업 마감 가능 여부 체크 
		this.isPossibleCloseBatch(batch, forcibly);

		// 2. 배치 마감 후 처리 이벤트 전송
		BatchCloseEvent event = new BatchCloseEvent(batch, SysEvent.EVENT_STEP_AFTER);
		event = (BatchCloseEvent)this.eventPublisher.publishEvent(event);
		
		// 3. 이벤트 취소라면 ...
		if(event.isAfterEventCancel()) {
			return;
		}
		
		// 4. 해당 배치에 대한 고정식이 아닌 호기들에 소속된 로케이션을 모두 찾아서 리셋
		this.resetRacksAndWorkCells(batch);

		// 5. OREDER_PREPROCESS 삭제
		this.deletePreprocess(batch);

		// 6. JobBatch 상태 변경
		this.updateJobBatchFinished(batch);
	}

	@Override
	public void isPossibleCloseBatchGroup(Long domainId, String batchGroupId, boolean closeForcibly) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int closeBatchGroup(Long domainId, String batchGroupId, boolean forcibly) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void isPossibleCancelBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 해당 배치의 랙, 작업 셀 정보 리셋
	 *
	 * @param batch
	 */
	protected void resetRacksAndWorkCells(JobBatch batch) {
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,batchId", batch.getDomainId(), batch.getEquipType(), batch.getEquipCd(), batch.getId());
		this.queryManager.executeBySql("UPDATE RACKS SET STATUS = null, BATCH_ID = null WHERE DOMAIN_ID = :domainId AND RACK_CD = :equipCd", params);
		this.queryManager.executeBySql("DELETE FROM WORK_CELLS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", params);
		this.queryManager.executeBySql("UPDATE STOCKS SET SKU_CD = null, SKU_BARCD = null, SKU_NM = null, STOCK_QTY = 0, LOAD_QTY = 0, ALLOC_QTY = 0, PICKED_QTY = 0 WHERE DOMAIN_ID = :domainId AND EQUIP_TYPE = :equipType AND EQUIP_CD = :equipCd AND (FIXED_FLAG IS NULL OR FIXED_FLAG = false)", params);
	}
	
	/**
	 * 주문 가공 정보를 모두 삭제한다.
	 *
	 * @param batch
	 * @return
	 */
	protected void deletePreprocess(JobBatch batch) {
		this.queryManager.executeBySql("DELETE FROM ORDER_PREPROCESSES WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId()));
	}
	
	/**
	 * 작업 배치를 마감 처리
	 * 
	 * @param batch
	 */
	protected void updateJobBatchFinished(JobBatch batch) {
		this.dpsCloseBatchSvc.closeBatch(batch);
	}

}
