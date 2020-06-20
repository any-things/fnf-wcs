package operato.logis.dps.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.entity.WmsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.logis.dps.DpsConstants;
import operato.logis.dps.query.store.DpsBatchQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 주문 수신용 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsReceiveBatchService extends AbstractQueryService {

	/**
	 * 작업 유형
	 */
	private String DPS_JOB_TYPE = "SHIPBYDPS";
	/**
	 * 배치 관련 쿼리 스토어 
	 */
	@Autowired
	private DpsBatchQueryStore dpsQueryStore;

	/**
	 * 주문 정보 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 10 and #event.eventStep == 1 and #event.jobType == 'DPS'")
	public void handleReadyToReceive(BatchReceiveEvent event) { 
		BatchReceipt receipt = event.getReceiptData();
		receipt = this.createReadyToReceiveData(receipt);
		event.setReceiptData(receipt);
		event.setExecuted(true);
	}
	
	/**
	 * 배치 수신 서머리 데이터 생성 
	 * 
	 * @param receipt
	 * @param params
	 * @return
	 */
	private BatchReceipt createReadyToReceiveData(BatchReceipt receipt, Object ... params) {
		// 1. WMS IF 테이블에서 수신 대상 데이터 확인
		List<BatchReceiptItem> receiptItems = this.getWmfIfToReceiptItems(receipt);
		
		// 2 수신 아이템 데이터 생성 
		for(BatchReceiptItem item : receiptItems) {
			item.setBatchId(item.getWmsBatchNo());
			item.setBatchReceiptId(receipt.getId());
			this.queryManager.insert(item);
		}
		
		// 3. 수신 아이템 설정 및 리턴
		receipt.setItems(receiptItems);
		return receipt;
	}
	
	/**
	 * WMS IF 테이블에서 수신 대상 데이터 확인
	 * 
	 * @param receipt
	 * @return
	 */
	private List<BatchReceiptItem> getWmfIfToReceiptItems(BatchReceipt receipt) {
		String workDate = receipt.getJobDate().replace(LogisConstants.DASH, LogisConstants.EMPTY_STRING);
		Map<String, Object> params = ValueUtil.newMap("whCd,jobType,jobDate,status", FnFConstants.WH_CD_ICF, this.DPS_JOB_TYPE, workDate, "A");
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		String sql = this.dpsQueryStore.getOrderSummaryToReceive();
		return dsQueryManager.selectListBySql(sql, params, BatchReceiptItem.class, 0, 0);
	}
	
	/**
	 * 주문 정보 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 20 and #event.eventStep == 1 and (#event.jobType == 'DPS')")
	public void handleStartToReceive(BatchReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		List<BatchReceiptItem> items = receipt.getItems();
		
		for(BatchReceiptItem item : items) {
			if(ValueUtil.isEqualIgnoreCase(DpsConstants.JOB_TYPE_DPS, item.getJobType())) {
				this.startToReceiveData(receipt, item);
			}
		}
		
		event.setExecuted(true);
	}
	
	/**
	 * 배치, 작업 수신
	 * 
	 * @param receipt
	 * @param item
	 * @param params
	 * @return
	 */
	private BatchReceipt startToReceiveData(BatchReceipt receipt, BatchReceiptItem item, Object ... params) {
		boolean exceptionOccurred = false;
		
		try {
			// 1. skip 이면 pass
			if(item.getSkipFlag()) {
				item.updateStatusImmediately(LogisConstants.COMMON_STATUS_SKIPPED, item.getMessage());
				return receipt;
			}
						
			// 2. BatchReceiptItem 상태 업데이트 - 진행 중 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_RUNNING, item.getMessage());
			
			// 3. JobBatch 생성 
			JobBatch batch = JobBatch.createJobBatch(item.getBatchId(), ValueUtil.toString(item.getJobSeq()), receipt, item);
			
			// 4. WMS의 주문 데이터를 WCS의 주문 I/F 테이블에 복사
			this.cloneData(receipt, item);
			
			// 5. JobBatch 상태 변경  
			batch.updateStatusImmediately(LogisConstants.isB2CJobType(batch.getJobType())? JobBatch.STATUS_READY : JobBatch.STATUS_WAIT);
			
			// 6. batchReceiptItem 상태 업데이트 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_FINISHED, null);
						
		} catch(Exception e) {
			exceptionOccurred = true;
			String errMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			errMsg = errMsg.length() > 400 ? errMsg.substring(0, 400) : errMsg;
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_ERROR, errMsg);
		}
		
		// 7. 에러 발생인 경우 수신 상태 에러로 업데이트
		if(exceptionOccurred) {
			receipt.updateStatusImmediately(LogisConstants.COMMON_STATUS_ERROR);
		}
		
		// 8. 배치 리턴
		return receipt;
	}
	
	/**
	 * 데이터 복제
	 * 
	 * @param receipt
	 * @param item
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW) 
	private void cloneData(BatchReceipt receipt, BatchReceiptItem item) throws Exception {
		// 1. WMS 데이터소스 조회 
		Query condition = new Query();
		condition.addFilter("wh_cd", FnFConstants.WH_CD_ICF);
		condition.addFilter("work_unit", item.getWmsBatchNo());
		
		// 2. WMS로 부터 배치 정보 조회
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		WmsMheHr wmsBatch = dsQueryManager.selectByCondition(WmsMheHr.class, condition);
		// 3. WMS로 부터 주문 정보 조회
		List<WmsMheDr> wmsOrderDetails = dsQueryManager.selectList(WmsMheDr.class, condition);
		
		if(ValueUtil.isNotEmpty(wmsOrderDetails)) {
			// 4. WCS에 배치 정보 복사 
			WcsMheHr orderMaster = ValueUtil.populate(wmsBatch, new WcsMheHr());
			orderMaster.setId(UUID.randomUUID().toString());
			this.queryManager.insert(orderMaster);
			
			// 5. WCS에 주문 정보 복사
			List<WcsMheDr> orderDestList = new ArrayList<WcsMheDr>(wmsOrderDetails.size());
			
			for(WmsMheDr orderSrc : wmsOrderDetails) {
				WcsMheDr orderDest = ValueUtil.populate(orderSrc, new WcsMheDr());
				orderDest.setId(UUID.randomUUID().toString());
				orderDest.setMheNo(orderMaster.getMheNo());
				orderDest.setBizType(wmsBatch.getBizType());
				orderDest.setDpsAssignYn(LogisConstants.N_CAP_STRING);
				orderDest.setBoxInputSeq(0);
				orderDestList.add(orderDest);
			}
			
			if(ValueUtil.isNotEmpty(orderDestList)) {
				AnyOrmUtil.insertBatch(orderDestList, 100);
			}
			
			// 6. WMS 배치 정보 수신 플래그 업데이트
			wmsBatch.setStatus("B");
			dsQueryManager.update(wmsBatch);
		}
	}
	
	/**
	 * 주문 수신 취소
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 30 and #event.eventStep == 1 and #event.jobType == 'DPS'")
	public void handleCancelReceived(BatchReceiveEvent event) {
		// 1. 작업 배치 추출 
		JobBatch batch = event.getJobBatch();
		
		// 2. 배치 상태 체크
		String currentStatus = AnyEntityUtil.findItemOneColumn(batch.getDomainId(), true, String.class, JobBatch.class, DpsConstants.ENTITY_FIELD_STATUS, DpsConstants.ENTITY_FIELD_ID, batch.getId());
		
		if(ValueUtil.isNotEqual(currentStatus, JobBatch.STATUS_WAIT) && ValueUtil.isNotEqual(currentStatus, JobBatch.STATUS_READY)) {
			throw new ElidomRuntimeException("작업 대기 상태에서만 취소가 가능 합니다.");
		}
		
		// 3. 주문 취소시 데이터 유지 여부에 따라서
		boolean isKeepData = BatchJobConfigUtil.isDeleteWhenOrderCancel(batch);
		int cancelledCnt = isKeepData ? this.cancelOrderKeepData(batch) : this.cancelOrderDeleteData(batch);
		event.setResult(cancelledCnt);
	}
	
	/**
	 * 주문 데이터 삭제 update
	 * 
	 * seq = 0
	 * @param batch
	 * @return
	 */
	private int cancelOrderKeepData(JobBatch batch) {
		int cnt = 0;
		
		// 1. 배치 상태  update 
		batch.updateStatus(JobBatch.STATUS_CANCEL);
		
		// 2. 주문 조회 
		List<Order> orderList = AnyEntityUtil.searchEntitiesBy(batch.getDomainId(), false, Order.class, DpsConstants.ENTITY_FIELD_ID, "batchId", batch.getId());
		
		// 3. 취소 상태 , seq = 0 셋팅 
		for(Order order : orderList) {
			order.setStatus(Order.STATUS_CANCEL);
			order.setJobSeq(LogisConstants.ZERO_STRING);
		}
		
		// 4. 배치 update
		AnyOrmUtil.updateBatch(orderList, 100, "jobSeq", DpsConstants.ENTITY_FIELD_STATUS);
		cnt += orderList.size();
		
		// 5. 주문 가공 데이터 삭제  
		cnt += this.deleteBatchPreprocessData(batch);
		return cnt;
	}
	
	/**
	 * 주문 데이터 삭제
	 * 
	 * @param batch
	 * @return
	 */
	private int cancelOrderDeleteData(JobBatch batch) {
		int cnt = 0;
		
		// 1. 삭제 조건 생성 
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		
		// 2. 삭제 실행
		cnt+= this.queryManager.deleteList(Order.class, condition);
		
		// 3. 주문 가공 데이터 삭제 
		cnt += this.deleteBatchPreprocessData(batch);
		
		// 4. 배치 삭제 
		this.queryManager.delete(batch);
		
		return cnt;
	}
	
	/**
	 * 주문 가공 데이터 삭제
	 * 
	 * @param batch
	 * @return
	 */
	private int deleteBatchPreprocessData(JobBatch batch) {
		// 1. 삭제 조건 생성 
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		
		// 2. 삭제 실행
		return this.queryManager.deleteList(OrderPreprocess.class, condition);
	}

}
