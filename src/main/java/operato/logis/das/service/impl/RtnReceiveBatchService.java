package operato.logis.das.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.entity.WmsMheRtnInvn;
import operato.logis.das.query.store.RtnQueryStore;
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
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * FnF 반품 주문 수신용 서비스
 * 
 * @author shortstop
 */
@Component
public class RtnReceiveBatchService extends AbstractQueryService {
	
	/**
	 * FnF 센터 코드
	 */
	private String whCd = "ICF";
	/**
	 * 작업 유형
	 */
	private String RTN_JOB_TYPE = "SHIPBYRTN";
	/**
	 * 반품 관련 쿼리 스토어 
	 */
	@Autowired
	private RtnQueryStore rtnQueryStore;

	/**
	 * 주문 정보 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 10 and #event.eventStep == 1 and (#event.jobType == 'RTN')")
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
		Map<String, Object> params = ValueUtil.newMap("whCd,jobType,jobDate,status", this.whCd, this.RTN_JOB_TYPE, workDate, "A");
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		String sql = this.rtnQueryStore.getOrderSummaryToReceive();
		return dsQueryManager.selectListBySql(sql, params, BatchReceiptItem.class, 0, 0);
	}
	
	
	/**
	 * 주문 정보 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 20 and #event.eventStep == 1 and (#event.jobType == 'RTN')")
	public void handleStartToReceive(BatchReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		List<BatchReceiptItem> items = receipt.getItems();
		 
		for(BatchReceiptItem item : items) {
			if(ValueUtil.isEqualIgnoreCase(LogisConstants.JOB_TYPE_RTN, item.getJobType())) {
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
		
		// 1. 별도 트랜잭션 처리를 위해 컴포넌트 자신의 레퍼런스 준비
		RtnReceiveBatchService selfSvc = BeanUtil.get(RtnReceiveBatchService.class);
		
		try {
			// 2. skip 이면 pass
			if(item.getSkipFlag()) {
				item.updateStatusImmediately(LogisConstants.COMMON_STATUS_SKIPPED, item.getMessage());
				return receipt;
			}
						
			// 3. BatchReceiptItem 상태 업데이트  - 진행 중 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_RUNNING, item.getMessage());
			
			// 4. JobBatch 생성 
			JobBatch batch = JobBatch.createJobBatch(item.getBatchId(), item.getJobSeq(), receipt, item);
			
			// 5. 데이터 복사  
			selfSvc.cloneData(item.getBatchId(), receipt.getJobDate(), item.getJobSeq(), item);
			
			// 6. JobBatch 상태 변경  
			batch.updateStatusImmediately(LogisConstants.isB2CJobType(batch.getJobType())? JobBatch.STATUS_READY : JobBatch.STATUS_WAIT);
			
			// 7. batchReceiptItem 상태 업데이트 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_FINISHED, null);
			
			// 8.Wms_if_order 상태 업데이트
			this.updateWmfIfToReceiptItems(item,receipt.getJobDate());
			
		} catch(Throwable th) {
			// 9. 에러 처리
			selfSvc.handleReceiveError(th, receipt, item);
		}
				
		return receipt;
	}
	
	/**
	 * 데이터 복제
	 * 
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public void cloneData(String batchId, String jobDate, String jobSeq, BatchReceiptItem item) throws Exception {
		
		List<Order> orderList = new ArrayList<Order>();
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheRtnInvn.class);
		Query condition = new Query();
		condition.setFilter("wh_cd" , this.whCd);
		List<WmsMheRtnInvn> wmsOrders = dsQueryManager.selectList(WmsMheRtnInvn.class, condition);
		
		if(ValueUtil.isNotEmpty(wmsOrders)) {
			for(WmsMheRtnInvn wmsOrder : wmsOrders) {
				Order order = new Order();
				order.setShopCd(wmsOrder.getStrrId());
				order.setSkuCd(wmsOrder.getItemCd());
				order.setSkuBarcd(wmsOrder.getBarcode());
				order.setOrderQty(wmsOrder.getInvnQty());
				order.setBatchId(batchId);
				order.setJobDate(jobDate);
				order.setJobType(item.getJobType());
				order.setAreaCd(item.getAreaCd());
				order.setStageCd(item.getStageCd());
				order.setJobSeq(jobSeq);
				order.setComCd(item.getComCd());
				order.setEquipType(item.getEquipType());
				order.setStatus(Order.STATUS_WAIT);
				order.setOrderNo(jobSeq + SysConstants.DASH + wmsOrder.getItemCd());
				order.setOrderLineNo(wmsOrder.getItemCd());
				orderList.add(order);
			}
		}
		
		if(ValueUtil.isNotEmpty(orderList)) {
			AnyOrmUtil.insertBatch(orderList, 100);
		}
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
	
	/**
	 * 주문 수신 취소
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 30 and #event.eventStep == 1 and (#event.jobType == 'RTN')")
	public void handleCancelReceived(BatchReceiveEvent event) {
		// 1. 작업 배치 추출 
		JobBatch batch = event.getJobBatch();
		
		// 2. 배치 상태 체크
		String sql = "select status from job_batches where domain_id = :domainId and id = :id";
		Map<String, Object> params = ValueUtil.newMap("domainId,id", batch.getDomainId(), batch.getId());
		String currentStatus = AnyEntityUtil.findItem(batch.getDomainId(), true, String.class, sql, params);
		
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
		List<Order> orderList = AnyEntityUtil.searchEntitiesBy(batch.getDomainId(), false, Order.class, "id", "batchId", batch.getId());
		
		// 3. 취소 상태 , seq = 0 셋팅 
		for(Order order : orderList) {
			order.setStatus(Order.STATUS_CANCEL);
			order.setJobSeq("0");
		}
		
		// 4. 배치 update
		this.queryManager.updateBatch(orderList, "jobSeq","status");
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
	
	/**
	 * WMS IF 테이블의 수신완료 데이터 상태 변경
	 * 
	 * @param receipt
	 * @return
	 */
	private void updateWmfIfToReceiptItems(BatchReceiptItem item,String jobDate) {
		Map<String,Object> params = ValueUtil.newMap("wcsBatchNo,wmsBatchNo,stageCd,jobSeq,jobDate",
				item.getWcsBatchNo(),item.getWmsBatchNo(),item.getStageCd(),item.getJobSeq(),jobDate);
 
		this.queryManager.executeBySql(this.rtnQueryStore.getWmsIfToReceiptUpdateQuery(), params);
	}
	
}
