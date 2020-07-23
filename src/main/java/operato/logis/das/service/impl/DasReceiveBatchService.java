package operato.logis.das.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.entity.WmsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.logis.das.query.store.DasQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * FnF DAS 출고 주문 수신용 서비스
 * 
 * @author shortstop
 */
@Component
public class DasReceiveBatchService extends AbstractQueryService {
	
	/**
	 * FnF 센터 코드
	 */
	private String whCd = "ICF";
	/**
	 * 작업 유형
	 */
	private String DAS_JOB_TYPE = "SHIPBYDAS";
	/**
	 * 출고 관련 쿼리 스토어 
	 */
	@Autowired
	private DasQueryStore dasQueryStore;
	
	/**
	 * 주문 정보 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 10 and #event.eventStep == 1 and (#event.jobType == 'DAS')")
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
	 * @return
	 */
	private BatchReceipt createReadyToReceiveData(BatchReceipt receipt) {
		// 1. WMS IF 테이블에서 수신 대상 데이터 확인
		List<BatchReceiptItem> receiptItems = this.getWmfIfToReceiptItems(receipt);
		
		// 2. 수신 아이템 데이터 생성 
		for(BatchReceiptItem item : receiptItems) {
			item.setBatchId(item.getWmsBatchNo());
			item.setBatchReceiptId(receipt.getId());
			this.queryManager.insert(item);
			receipt.addItem(item);
		}
		
		// 3. 배치 수신 결과 리턴
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
		Map<String, Object> params = ValueUtil.newMap("whCd,jobType,jobDate,status", this.whCd, this.DAS_JOB_TYPE, workDate, "A");
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		String sql = this.dasQueryStore.getOrderSummaryToReceive();
		return dsQueryManager.selectListBySql(sql, params, BatchReceiptItem.class, 0, 0);
	}
	
	/**
	 * 주문 정보 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 20 and #event.eventStep == 1 and (#event.jobType == 'DAS')")
	public void handleStartToReceive(BatchReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		List<BatchReceiptItem> items = receipt.getItems();
		 
		for(BatchReceiptItem item : items) {
			if(ValueUtil.isEqualIgnoreCase(LogisConstants.JOB_TYPE_DAS, item.getJobType())) {
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
		
		// 별도 트랜잭션 처리를 위해 컴포넌트 자신의 레퍼런스 준비
		DasReceiveBatchService selfSvc = BeanUtil.get(DasReceiveBatchService.class);
		
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
			selfSvc.cloneData(receipt, item);
			
			// 5. JobBatch 상태 변경  
			batch.updateStatusImmediately(LogisConstants.isB2CJobType(batch.getJobType())? JobBatch.STATUS_READY : JobBatch.STATUS_WAIT);
			
			// 6. batchReceiptItem 상태 업데이트 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_FINISHED, null);
						
		} catch(Throwable th) {
			// 7. 수신 에러 처리
			selfSvc.handleReceiveError(th, receipt, item);
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
	public void cloneData(BatchReceipt receipt, BatchReceiptItem item) throws Exception {
		// 1. WMS 데이터소스 조회 
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		Query condition = new Query();
		condition.addFilter("wh_cd", this.whCd);
		condition.addFilter("work_unit", item.getWmsBatchNo());
		
		// 2. WMS로 부터 배치 정보 조회 
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
				orderDest.setBizType(wmsBatch.getBizType());
				orderDestList.add(orderDest);
			}
			
			if(ValueUtil.isNotEmpty(orderDestList)) {
				AnyOrmUtil.insertBatch(orderDestList, 100);
			}
			
			// 6. WMS 배치 정보 수신 플래그 업데이트 (TODO WCS 수신 상태 필요한 지 WMS와 협의 필요)
			wmsBatch.setStatus("W");
			dsQueryManager.update(wmsBatch);
		}
	}
	
	/**
	 * 주문 수신 취소
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 30 and #event.eventStep == 1 and (#event.jobType == 'DAS')")
	public void handleCancelReceived(BatchReceiveEvent event) {
		event.setExecuted(true);
	}

	/**
	 * 주문 수신시 에러 핸들링
	 * 
	 * @param th
	 * @param receipt
	 * @param item
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW) 
	public void handleReceiveError(Throwable th, BatchReceipt receipt, BatchReceiptItem item) {
		String errMsg = th.getCause() != null ? th.getCause().getMessage() : th.getMessage();
		errMsg = errMsg.length() > 400 ? errMsg.substring(0,400) : errMsg;
		item.updateStatusImmediately(LogisConstants.COMMON_STATUS_ERROR, errMsg);
		receipt.updateStatusImmediately(LogisConstants.COMMON_STATUS_ERROR);
	}

}
