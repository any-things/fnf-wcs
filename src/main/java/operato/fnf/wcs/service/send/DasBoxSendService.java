package operato.fnf.wcs.service.send;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.DasBoxCancel;
import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.RfidBoxResult;
import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.entity.WmsMheBox;
import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * DAS 박스 실적 전송 서비스
 * 
 * @author shortstop
 */
@Component
public class DasBoxSendService extends AbstractQueryService {
	
	/**
	 * REF_NO 조회 쿼리
	 */
	private static final String WCS_FIND_REF_NO_QUERY = "select distinct ref_no, outb_tcd from mhe_dr where wh_cd = :whCd and work_unit = :batchId and outb_no = :orderNo";
	/**
	 * RFID 박스 취소 I/F 순서 쿼리
	 */
	private static final String RFID_BOX_DEL_IF_SEQ = "SELECT SEQ_IF_PASDELIVERY_DEL_RECV.NEXTVAL FROM DUAL";
	/**
	 * RFID 박스 취소 정보 전송 쿼리
	 */
	private static final String RFID_DELETE_SQL = "INSERT INTO RFID_IF.IF_PASDELIVERY_DEL_RECV(DT_IF_DATE, NO_IF_SEQ, CD_WAREHOUSE, CD_BRAND, TP_MACHINE, NO_BOX, NO_WAYBILL, TP_STATUS, DM_BF_RECV) VALUES(:workDate, :ifSeqNo, :whCd, :brandCd, :equipType, :boxId, :invoiceId, '0', :currentTime)";
	
	/**
	 * 박스 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	@SuppressWarnings("rawtypes")
	public void sendBoxResults(Domain domain, JobBatch batch) {
		
		Long domainId = batch.getDomainId();
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidBoxItem.class);
		DasBoxSendService boxSendSvc = BeanUtil.get(DasBoxSendService.class);
		
		// 1. 박스 완료 실적 조회
		List<WcsMheBox> boxOrderList = this.searchBoxedOrderList(batch);
		// 2. WMS에 전송
		for(WcsMheBox order : boxOrderList) {
			// 2.1 WCS 주문별 실적 정보 
			List<WcsMheBox> boxedOrders = this.searchBoxResult(order.getWorkUnit(), order.getOutbNo());
			// 2.2 WCS 주문별 실적 정보를 WMS 패킹 정보로 복사
			boxSendSvc.sendPackingsToWms(domainId, wmsQueryMgr, boxedOrders);
			// 2.3 WCS 주문별 실적 정보를 RFID 패킹 정보로 복사
			boxSendSvc.sendPackingsToRfid(domainId, wmsQueryMgr, boxedOrders);
		}

		// 3. 박스 취소 실적 조회
		List<DasBoxCancel> cancelBoxList = this.searchCancelBoxList(batch);
		// 4. RFID에 박스 취소 전송
		for(DasBoxCancel cancelBox : cancelBoxList) {
			boxSendSvc.sendCancelPackingsToRfid(rfidQueryMgr, batch, cancelBox);
		}
		
		// 5. RFID로 부터 새로운 박스 검수 결과 조회
		List<Map> rfidBoxResults = this.searchRfidBoxResults(rfidQueryMgr, batch);
		// 4. RFID로 부터 박스 결과 수신
		for(Map rfidBoxResult : rfidBoxResults) {
			boxSendSvc.receiveRfidBoxResult(rfidQueryMgr, rfidBoxResult);
		}
	}
	
	/**
	 * 박싱된 주문 리스트 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<WcsMheBox> searchBoxedOrderList(JobBatch batch) {
		Query condition = new Query();
		condition.addSelect("work_unit", "outb_no", "box_no");
		condition.addFilter("whCd", FnFConstants.WH_CD_ICF);
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("ifYn", LogisConstants.NOT_EQUAL, LogisConstants.Y_CAP_STRING);
		condition.addOrder("outbNo", true);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheBox.class, condition);
	}
	
	/**
	 * 취소된 박싱 주문 리스트 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<DasBoxCancel> searchCancelBoxList(JobBatch batch) {
		Query condition = new Query();
		condition.addFilter("whCd", FnFConstants.WH_CD_ICF);
		condition.addFilter("batchNo", batch.getId());
		condition.addFilter("prcsYn", LogisConstants.NOT_EQUAL, LogisConstants.Y_CAP_STRING);
		condition.addOrder("createdAt", true);
		return this.queryManager.selectList(DasBoxCancel.class, condition);		
	}

	/**
	 * 박스 실적 주문 조회
	 * 
	 * @param batchId
	 * @param orderId
	 * @return
	 */
	private List<WcsMheBox> searchBoxResult(String batchId, String orderId) {
		Query condition = new Query();
		condition.addFilter("whCd", FnFConstants.WH_CD_ICF);
		condition.addFilter("workUnit", batchId);
		condition.addFilter("ifYn", LogisConstants.NOT_EQUAL, LogisConstants.Y_CAP_STRING);
		condition.addFilter("outbNo", orderId);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheBox.class, condition);
	}
	
	/**
	 * WCS 박스 실적으로 부터 WMS 박스 실적 전송
	 * 
	 * @param domainId
	 * @param wmsQueryMgr
	 * @param boxedOrders
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingsToWms(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheBox> boxedOrders) {
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			List<WmsMheBox> toBoxList = new ArrayList<WmsMheBox>(boxedOrders.size());
			Date currentTime = new Date();
			
			for(WcsMheBox boxedOrder : boxedOrders) {
				// WMS 전송 데이터 생성 
				WmsMheBox toBox = ValueUtil.populate(boxedOrder, new WmsMheBox());
				toBoxList.add(toBox);
				
				// WCS From Box 상태 업데이트
				boxedOrder.setIfYn(LogisConstants.Y_CAP_STRING);
				boxedOrder.setIfDatetime(currentTime);
			}
			
			// WMS 박스 실적 전송
			wmsQueryMgr.insertBatch(toBoxList);
			
			// WCS 박스 실적 상태 변경
			this.queryManager.updateBatch(boxedOrders);
		}
	}
	
	/**
	 * WCS 박스 실적으로 부터 RFID 실적 전송
	 * 
	 * @param domainId
	 * @param wmsQueryMgr
	 * @param boxedOrders
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingsToRfid(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheBox> boxedOrders) {
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			List<RfidBoxItem> toRfidBoxList = new ArrayList<RfidBoxItem>(boxedOrders.size());
			
			for(WcsMheBox boxedOrder : boxedOrders) {
				// WMS 전송 데이터 생성 
				RfidBoxItem rfidBox = this.newDasRfidBoxItem(boxedOrder);
				toRfidBoxList.add(rfidBox);
			}
			
			wmsQueryMgr.insertBatch(toRfidBoxList);
		}
	}
	
	/**
	 * DAS RFID 박스 정보 생성
	 * 
	 * @param fromBox
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private RfidBoxItem newDasRfidBoxItem(WcsMheBox fromBox) {
		
		RfidBoxItem rfidBoxItem = new RfidBoxItem();
		rfidBoxItem.setCdWarehouse(fromBox.getWhCd());
		rfidBoxItem.setCdBrand(fromBox.getStrrId());
		rfidBoxItem.setTpMachine("2");
		rfidBoxItem.setDtDelivery(fromBox.getWorkDate());
		rfidBoxItem.setDsBatchNo(fromBox.getWorkUnit());
		rfidBoxItem.setNoBox(fromBox.getBoxNo());
		// TODO 운송장 번호가 박스 번호 맞는지 체크
		rfidBoxItem.setNoWaybill(fromBox.getBoxNo());
		rfidBoxItem.setIfCdItem(fromBox.getItemCd());
		rfidBoxItem.setYnAssort(LogisConstants.N_CAP_STRING);
		rfidBoxItem.setCdShop(fromBox.getShiptoId());
		rfidBoxItem.setTpDelivery("1");
		rfidBoxItem.setDsShuteno(null);
		rfidBoxItem.setOutbNo(fromBox.getOutbNo());
		rfidBoxItem.setQtDelivery(fromBox.getCmptQty());
		rfidBoxItem.setDmBfRecv(DateUtil.dateStr(new Date(), "YYYYMMDDHHMMSS"));
		rfidBoxItem.setYnCancel(LogisConstants.N_CAP_STRING);
		rfidBoxItem.setTpWeight("0");
		rfidBoxItem.setTpSend("0");
		rfidBoxItem.setNoWeight(null);
		
		Map<String, Object> params = ValueUtil.newMap("whCd,batchId,orderNo", fromBox.getWhCd(), fromBox.getWorkUnit(), fromBox.getOutbNo());
		Map<String, Object> order = this.queryManager.selectBySql(WCS_FIND_REF_NO_QUERY, params, Map.class);
		rfidBoxItem.setRefNo(ValueUtil.toString(order.get("ref_no")));
		rfidBoxItem.setOutbTcd(ValueUtil.toString(order.get("outb_tcd")));
		
		return rfidBoxItem;
	}
	
	/**
	 * WCS 박스 취소 정보로 부터 RFID 삭제 정보 전송
	 * 
	 * @param rfidQueryMgr
	 * @param batch
	 * @param cancelBox
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendCancelPackingsToRfid(IQueryManager rfidQueryMgr, JobBatch batch, DasBoxCancel cancelBox) {
		
		// 1. RFID에 전송할 IF_SEQ 생성  
		Integer ifSeqNo = rfidQueryMgr.selectBySql(RFID_BOX_DEL_IF_SEQ, new HashMap<String, Object>(), Integer.class);
		
		// 2. RFID에 박스 삭제 데이터 전송 
		String jobDate = batch.getJobDate().replaceAll(SysConstants.DASH, SysConstants.EMPTY_STRING);
		Date currentTime = new Date();
		String currentTimeStr = DateUtil.dateStr(currentTime, "YYYYMMDDHHMMSS");
		// TODO 운송장 번호가 박스 번호 맞는지 체크
		Map<String, Object> delBox = ValueUtil.newMap("workDate,ifSeqNo,whCd,brandCd,equipType,boxId,invoiceId,currentTime", jobDate, ifSeqNo, FnFConstants.WH_CD_ICF, cancelBox.getStrrId(), cancelBox.getBoxId(), cancelBox.getBoxId(), currentTimeStr);	
		rfidQueryMgr.executeBySql(RFID_DELETE_SQL, delBox);
		
		// 3. RFID 프로시셔 호출을 위한 파라미터 생성 
		Map<String, Object> params = ValueUtil.newMap("IN_DT_IF_DATE,IN_NO_IF_SEQ,IN_CD_COMPANY", jobDate, ifSeqNo, "FNF");
		// 4. 작업 지시 프로시져 콜 
		Map<?, ?> result = rfidQueryMgr.callReturnProcedure("RFID_IF.PRO_IF_PASDELIVERY_DEL_RECV", params, Map.class);
		// 5. 결과 파싱 
		String successYn = ValueUtil.toString(result.get("OUT_YN_SUCCESS"));
		
		if(ValueUtil.isEqualIgnoreCase(successYn, LogisConstants.N_CAP_STRING)) {
			String errorMsg = ValueUtil.isNotEmpty(result.get("OUT_ERRMSG")) ? result.get("OUT_ERRMSG").toString() : "Failed to DPS RFID Delete Confirm Procedure!";
			throw new ElidomRuntimeException(errorMsg);
		}
		
		// 6. WCS 박스 취소 정보 수행
		Query condition = new Query();
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("boxId", cancelBox.getBoxId());
		WcsMheBox cancelBoxResult = this.queryManager.selectByCondition(WcsMheBox.class, condition);
		cancelBoxResult.setDelYn(LogisConstants.Y_CAP_STRING);
		cancelBoxResult.setDelDatetime(currentTime);
		this.queryManager.update(cancelBoxResult, "delYn", "delDatetime");
		
		// 7. 박스 취소 정보 실행 플래그 업데이트
		cancelBox.setPrcsYn(LogisConstants.Y_CAP_STRING);
		this.queryManager.update("prcsYn");
	}

	/**
	 * RFID로 부터 검수 실적을 조회
	 * 
	 * @param rfidQueryMgr
	 * @param batch
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> searchRfidBoxResults(IQueryManager rfidQueryMgr, JobBatch batch) {
		String sql = "SELECT * FROM RFID_IF.IF_PASDELIVERY_SEND	WHERE DS_BATCH_NO = :batchId AND TP_STATUS = '0'";
		return rfidQueryMgr.selectListBySql(sql, new HashMap<String, Object>(), Map.class, 0, 0);
	}

	/**
	 * RFID로 부터 검수 실적을 수신
	 * 
	 * @param rfidQueryMgr
	 * @param batch
	 * @param rfidBoxResult
	 */
	@SuppressWarnings("rawtypes")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void receiveRfidBoxResult(IQueryManager rfidQueryMgr, Map rfidBox) {
		
		RfidBoxResult wcsBoxResult = new RfidBoxResult();
		wcsBoxResult.setBatchId(ValueUtil.toString(rfidBox.get("ds_batch_no")));
		wcsBoxResult.setBoxId(ValueUtil.toString(rfidBox.get("no_box")));
		wcsBoxResult.setBrandCd(ValueUtil.toString(rfidBox.get("cd_brand")));
		wcsBoxResult.setCreatedAt(new Date());
		// 1 : PAS, 2 : DAS, 3 : DAS 반품 
		wcsBoxResult.setEquipType("2");
		wcsBoxResult.setInspResult(ValueUtil.toString(rfidBox.get("result_st")));
		wcsBoxResult.setInvoiceId(ValueUtil.toString(rfidBox.get("no_waybill")));
		wcsBoxResult.setOutDate(ValueUtil.toString(rfidBox.get("dt_delivery")));
		wcsBoxResult.setWhCd(FnFConstants.WH_CD_ICF);
		this.queryManager.insert(wcsBoxResult);
	}

}
