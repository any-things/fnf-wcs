package operato.logis.sms.service.impl;

import java.util.ArrayList;
import java.util.Date;
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
import operato.logis.sms.SmsConstants;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.EquipGroup;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * SMS(출고, 반품) 주문 수신용 서비스
 * 
 * @author jyp
 */
@Component
public class SmsReceiveBatchService extends AbstractQueryService {
	

	String dasBizType = "SHIPBYDAS";
	String dpsBizType = "SHIPBYDPS";
	String createStatus = "A";
	String receiveStatus = "B";
	String equipType = "SORTER";
	String mheNo = "94";
	
	/**
	 * SMS 관련 쿼리 스토어
	 */
	@Autowired
	private SmsQueryStore batchQueryStore;
	
	/**
	 * FnF 인터페이스를 위한 데이터소스의 쿼리 매니저
	 */
	private IQueryManager fnfQueryMgr = null;
	
	/**
	 * 주문 정보 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 10 and #event.eventStep == 1 and (#event.jobType == 'SDAS' or #event.jobType == 'SRTN' or #event.jobType == 'SDPS')")
	public void handleReadyToReceive(BatchReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		String jobType = event.getJobType();
		String jobDate = event.getJobDate();
		
		Query query = AnyOrmUtil.newConditionForExecution(receipt.getDomainId());
		query.addFilter("stageCd", event.getStageCd());
		EquipGroup equipGroup = this.queryManager.selectByCondition(EquipGroup.class, query);
		
		if(ValueUtil.isEmpty(equipGroup)) {
			throw new ElidomRuntimeException("선택된 장비가 없습니다.");
		}
		
		this.mheNo = equipGroup.getEquipGroupCd();
		receipt = this.createReadyToReceiveData(receipt, jobType, jobDate, event.getStageCd());
		event.setReceiptData(receipt);
	}
	
	/**
	 * 배치 수신 서머리 데이터 생성 
	 * 
	 * @param receipt
	 * @param jobType
	 * @param params
	 * @return
	 */
	private BatchReceipt createReadyToReceiveData(BatchReceipt receipt, String jobType, Object ... params) {
		
		// 1. 대기 상태 이거나 진행 중인 수신이 있는지 확인
		BatchReceipt runBatchReceipt = this.checkRunningOrderReceipt(receipt,jobType);
		if(runBatchReceipt != null) return runBatchReceipt;
		String jobDate = ValueUtil.toString(params[0]);
		String stageCd = ValueUtil.toString(params[1]);
		
		// 2. WMS IF 테이블에서 수신 대상 데이터 확인
		List<BatchReceiptItem> receiptItems = null;
		if(ValueUtil.isEqual(jobType, SmsConstants.JOB_TYPE_SDAS)) {
			receiptItems = this.getWmfIfToSdasReceiptItems(receipt, jobType);
		} else if(ValueUtil.isEqual(jobType, SmsConstants.JOB_TYPE_SRTN)) {
			receiptItems = this.getWmfIfToSrtnReceiptItems(receipt, jobType, jobDate);
		} else if(ValueUtil.isEqual(jobType, SmsConstants.JOB_TYPE_SDPS)) {
			receiptItems = this.getWmfIfToSdpsReceiptItems(receipt, jobType, jobDate, stageCd);
		}
		
		// 3 수신 아이템 데이터 생성 
		for(BatchReceiptItem item : receiptItems) {
			item.setBatchId(item.getWmsBatchNo());
			item.setBatchReceiptId(receipt.getId());
			this.queryManager.insert(item);
			receipt.addItem(item);
		}
		
		return receipt;
	}
	
	/**
	 * 대기 상태 이거나 진행 중인 수신이 있는지 확인
	 * 
	 * @param receipt
	 * @param jobType
	 * @return
	 */
	private BatchReceipt checkRunningOrderReceipt(BatchReceipt receipt, String jobType) {
		Map<String, Object> paramMap = ValueUtil.newMap("domainId,comCd,areaCd,stageCd,jobDate,status,jobType", 
				receipt.getDomainId(), receipt.getComCd(), receipt.getAreaCd(), receipt.getStageCd(), receipt.getJobDate(),
				ValueUtil.newStringList(LogisConstants.COMMON_STATUS_WAIT, LogisConstants.COMMON_STATUS_RUNNING), jobType);
		
		BatchReceipt receiptData = this.queryManager.selectBySql(this.batchQueryStore.getBatchReceiptOrderTypeStatusQuery(), paramMap, BatchReceipt.class);
		
		// 대기 중 또는 진행 중인 수신 정보 리턴 
		if(receiptData != null) {
			receiptData.setItems(AnyEntityUtil.searchDetails(receipt.getDomainId(), BatchReceiptItem.class, "batchReceiptId", receiptData.getId()));
			return receiptData;
		}
		
		return null;
	}
	
	/**
	 * WMS IF 테이블에서 SDAS 수신 대상 데이터 확인
	 * 
	 * @param receipt
	 * @param jobType
	 * @return
	 */
	private List<BatchReceiptItem> getWmfIfToSdasReceiptItems(BatchReceipt receipt, String jobType) {
		Map<String,Object> sqlParams = ValueUtil.newMap("equipCd,whCd,jobDate,bizType,status", this.mheNo, FnFConstants.WH_CD_ICF, receipt.getJobDate().replaceAll(SysConstants.DASH, SysConstants.EMPTY_STRING), this.dasBizType, this.createStatus);
		return this.getFnfQueryManager().selectListBySql(this.batchQueryStore.getWmsIfToSdasReceiptDataQuery(), sqlParams, BatchReceiptItem.class, 0, 0);
	}
	
	/**
	 * WMS IF 테이블에서 SRTN 수신 대상 데이터 확인
	 * 
	 * @param receipt
	 * @param jobType
	 * @param jobDate
	 * @return
	 */
	private List<BatchReceiptItem> getWmfIfToSrtnReceiptItems(BatchReceipt receipt, String jobType, String jobDate) {
		Query query = AnyOrmUtil.newConditionForExecution(receipt.getDomainId());
		
		String fromDate = DateUtil.dateStr(DateUtil.addDate(DateUtil.parse(jobDate, DateUtil.getDateFormat()), -30), DateUtil.getDateFormat());
		
		String[] betDate = {fromDate, jobDate};
		query.addFilter("jobDate", SysConstants.BETWEEN, betDate);  
		List<Order> orders = this.queryManager.selectList(Order.class, query);
		List<String> batchList = AnyValueUtil.filterValueListBy(orders, "batchId");
		if(ValueUtil.isEmpty(batchList)) {
			batchList.add("1");
		}
		
		Map<String, Object> params = ValueUtil.newMap("equipCd,jobDate,batchId", this.mheNo, jobDate, batchList);
		return this.getFnfQueryManager().selectListBySql(this.batchQueryStore.getWmsIfToSrtnReceiptDataQuery(), params, BatchReceiptItem.class, 0, 0);
	}
	
	/**
	 * WMS IF 테이블에서 SDPS 수신 대상 데이터 확인
	 * 
	 * @param receipt
	 * @param jobType
	 * @param jobDate
	 * @param stageCd
	 * @return
	 */
	private List<BatchReceiptItem> getWmfIfToSdpsReceiptItems(BatchReceipt receipt, String jobType, String jobDate, String stageCd) {
		String workDate = jobDate.replace(LogisConstants.DASH, LogisConstants.EMPTY_STRING);
		Map<String, Object> params = ValueUtil.newMap("stageCd,equipCd,whCd,jobType,jobDate,status", stageCd, this.mheNo, FnFConstants.WH_CD_ICF, this.dpsBizType, workDate, this.createStatus);
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		String sql = this.batchQueryStore.getWmsIfToSdpsReceiptDataQuery();
		return dsQueryManager.selectListBySql(sql, params, BatchReceiptItem.class, 0, 0);
	}
	
	/**
	 * 주문 정보 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 20 and #event.eventStep == 1 and (#event.jobType == 'SDAS' or #event.jobType == 'SRTN' or #event.jobType == 'SDPS')")
	public void handleStartToReceive(BatchReceiveEvent event) {
		BatchReceipt receipt = event.getReceiptData();
		List<BatchReceiptItem> items = receipt.getItems();
		 
		for(BatchReceiptItem item : items) {
			if ((ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SDAS, item.getJobType())
					|| ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SRTN, item.getJobType())
					|| ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SDPS, item.getJobType()))
					&& ValueUtil.isEqualIgnoreCase(item.getStatus(), LogisConstants.COMMON_STATUS_WAIT)) {
				this.startToReceiveData(receipt, item);
			}
		}
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
		SmsReceiveBatchService selfSvc = BeanUtil.get(SmsReceiveBatchService.class);
		
		try {
			// 2. skip 이면 pass
			if(item.getSkipFlag()) {
				item.updateStatusImmediately(LogisConstants.COMMON_STATUS_SKIPPED, item.getMessage());
				return receipt;
			}
						
			// 3. BatchReceiptItem 상태 업데이트  - 진행 중 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_RUNNING, item.getMessage());
			
			// 4. JobBatch 생성 
			JobBatch batch = JobBatch.createJobBatch(item.getBatchId(), ValueUtil.toString(item.getJobSeq()), receipt, item);
			
			// 5. 데이터 복사  
			selfSvc.cloneData(item.getBatchId(), receipt, item);
						
			// 6. JobBatch 상태 변경  
			batch.updateStatusImmediately(LogisConstants.isB2CJobType(batch.getJobType())? JobBatch.STATUS_READY : JobBatch.STATUS_WAIT);
			
			// 7. batchReceiptItem 상태 업데이트 
			item.updateStatusImmediately(LogisConstants.COMMON_STATUS_FINISHED, null);
			
			// 8.WMS 상태 업데이트
			this.updateWmfIfToReceiptItems(batch, item);
			
		} catch(Throwable th) {
			// 9. 수신 에러 처리
			selfSvc.handleReceiveError(th, receipt, item);
		}
		
		return receipt;
	}
	
	/**
	 * 데이터 복제
	 * 
	 * @param batchId
	 * @param receipt
	 * @param item
	 * @throws Exception
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public void cloneData(String batchId, BatchReceipt receipt, BatchReceiptItem item) throws Exception {		
		// 1. 조회  
		if(ValueUtil.isEqual(item.getJobType(), SmsConstants.JOB_TYPE_SDAS)) {
			this.getWmsSdasOrders(batchId, receipt, item);
		} else if(ValueUtil.isEqual(item.getJobType(), SmsConstants.JOB_TYPE_SRTN)) {
			this.getWmsSrtnOrders(batchId, receipt, item);
		} else if(ValueUtil.isEqual(item.getJobType(), SmsConstants.JOB_TYPE_SDPS)) {
			this.getWmsSdpsOrders(receipt, item);
		}
	}
	
	/**
	 * WMS IF 테이블의 수신완료 데이터 상태 변경
	 * 
	 * @param jobBatch
	 * @param item
	 */
	private void updateWmfIfToReceiptItems(JobBatch jobBatch, BatchReceiptItem item) {
		if(ValueUtil.isEqual(item.getJobType(), SmsConstants.JOB_TYPE_SDAS)) {
//			Map<String,Object> params = ValueUtil.newMap("mheNo,status,rcvDatetime,whCd,workUnit",
//					this.mheNo, this.receiveStatus, DateUtil.getDate(), this.whCd, item.getWmsBatchNo());
//			this.getFnfQueryManager().executeBySql(this.batchQueryStore.getWmsIfToSdasReceiptUpdateQuery(), params);
			
		} else if(ValueUtil.isEqual(item.getJobType(), SmsConstants.JOB_TYPE_SRTN)) {
		} else if(ValueUtil.isEqual(item.getJobType(), SmsConstants.JOB_TYPE_SDPS)) {
			
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
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.eventType == 30 and #event.eventStep == 1 and (#event.jobType == 'SDAS' or #event.jobType == 'SRTN')")
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
	 * FnF 인터페이스를 위한 데이터소스의 쿼리 매니저 리턴
	 * 
	 * @return
	 */
	private IQueryManager getFnfQueryManager() {
		if(this.fnfQueryMgr == null) {
			String fnfDataSrcName = SettingUtil.getValue("xyz.elings.db.ref.name");
			this.fnfQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager(fnfDataSrcName);
		}
		
		return this.fnfQueryMgr;
	}
	
	/**
	 * WMS 테이블에서 조회후 SDAS Orders 데이터 생성
	 * 
	 * @param batchId
	 * @param receipt
	 * @param item
	 * @return
	 */
	private void getWmsSdasOrders(String batchId, BatchReceipt receipt, BatchReceiptItem item) {
		Map<String, Object> sqlParams = ValueUtil.newMap(
				"batchId,jobType,orderLineNo,comCd,areaCd,stageCd,equipType,wh_cd,work_unit", batchId, item.getJobType(), 0,
				item.getComCd(), item.getAreaCd(), item.getStageCd(), item.getEquipType(), FnFConstants.WH_CD_ICF,
				item.getWmsBatchNo());
		
		List<Order> wmsOrders = this.getFnfQueryManager().selectListBySql(this.batchQueryStore.getWmsIfToSdasReceiptOrderDataQuery(), sqlParams, Order.class, 0, 0);
		
		if(ValueUtil.isNotEmpty(wmsOrders)) {
			AnyOrmUtil.insertBatch(wmsOrders, 100);
		}
	}
	
	/**
	 * WMS 테이블에서 조회후 SRTN Orders 데이터 생성
	 * 
	 * @param batchId
	 * @param receipt
	 * @param item
	 * @return
	 */
	private void getWmsSrtnOrders(String batchId, BatchReceipt receipt, BatchReceiptItem item) {
		String[] batchInfo = item.getWcsBatchNo().split(SysConstants.DASH);
		if(batchInfo.length == 4) {
			String strrId = batchInfo[0];
			String season = batchInfo[1];
			String rtnType = batchInfo[2];
			String jobSeq = batchInfo[3];
			
			Map<String, Object> sqlParams = ValueUtil.newMap(
					"batchId,orderNo,wmsBatchNo,wcsBatchNo,jobDate,orderDate,brandCd,season,type,jobSeq",
					item.getWcsBatchNo(), item.getWcsBatchNo(), item.getWmsBatchNo(), item.getWcsBatchNo(),
					receipt.getJobDate(), receipt.getJobDate(), strrId, season, rtnType, jobSeq);
			
			List<Order> wmsOrders = this.getFnfQueryManager().selectListBySql(this.batchQueryStore.getWmsIfToSrtnReceiptOrderDataQuery(), sqlParams, Order.class, 0, 0);
			
			if(ValueUtil.isNotEmpty(wmsOrders)) {
				AnyOrmUtil.insertBatch(wmsOrders, 100);
			}
		}
	}
	
	/**
	 * WMS 테이블에서 조회후 SDPS 데이터 복제
	 * 
	 * @param receipt
	 * @param item
	 * @throws Exception
	 */
	private void getWmsSdpsOrders(BatchReceipt receipt, BatchReceiptItem item) {
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
			Date currentTime = new Date();
			wmsBatch.setStatus(this.receiveStatus);
			wmsBatch.setMheNo(this.mheNo);
			wmsBatch.setRcvDatetime(currentTime);
			
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
			
			dsQueryManager.update(wmsBatch, "status", "mheNo", "rcvDatetime");
		}
	}

}
