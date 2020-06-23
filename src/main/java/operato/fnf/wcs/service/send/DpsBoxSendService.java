package operato.fnf.wcs.service.send;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.DpsJobInstance;
import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.RfidDpsInspResult;
import operato.fnf.wcs.entity.RfidResult;
import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WmsDpsActualOrder;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.service.model.WaybillResponse;
import operato.logis.dps.model.DpsInspItem;
import operato.logis.dps.model.DpsInspection;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dev.entity.RangedSeq;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DPS 박스 실적 전송 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsBoxSendService extends AbstractQueryService {

	/**
	 * WMS 박스 실적 정보 추가 쿼리
	 */
	private static final String WMS_PACK_INSERT_SQL = "INSERT INTO MPS_PACKING_CMPT(INTERFACE_CRT_DT, INTERFACE_NO, WH_CD, BOX_ID, REF_NO, STRR_ID, ITEM_CD, PACK_QTY, OUTB_ECT_DATE, IF_CRT_ID, IF_CRT_DTM) VALUES (:today, SEQ_MPS_PACKING_CMPT.NEXTVAL, :whCd, :boxId, :orderNo, :brandCd, :skuCd, :pickedQty, :jobDate, 'wcs', :currentTime)";

	/**
	 * RFID 박스 검수 완료 정보 추가 쿼리
	 */
	private static final String RFID_EXAMED_SELECT_SQL = "SELECT * FROM RFID_IF.IF_RFIDHISTORY_RECV WHERE CD_REGISTER = 'WCS' AND CD_BILL = :waybillNo";	

	/**
	 * RFID 박스 검수 완료 정보 추가 쿼리
	 */
	private static final String RFID_EXAMED_INSERT_SQL = "INSERT INTO RFID_IF.IF_RFIDHISTORY_RECV(DT_IF_DATE, NO_IF_SEQ, TP_GUBUN, CD_COMPANY, CD_DEPART, DT_DATE, CD_SHOP, CD_BILL, CD_SUBBILL, CD_RFIDUID, TP_HISTORY, TP_STATUS, CD_REGISTER, DM_BF_RECV) VALUES (:today, RFID_IF.SEQ_IF_RFIDHISTORY_RECV.NEXTVAL, 'I', 'FNF', :brandCd, :today, :shopCd, :waybillNo, :orderNo, :rfidUid, '42', '0', :creatorId, :currentTime)";	
	
	/**
	 * 주문 번호로 Unique 박스 ID 생성
	 * 
	 * @param batch
	 * @param orderNo
	 */
	public String generateBoxIdByOrderNo(JobBatch batch, String orderNo) {
		
		return this.generateBoxIdByOrderNo(batch.getDomainId(), batch.getId(), batch.getEquipGroupCd(), orderNo);
	}
	
	/**
	 * 주문 번호로 Unique 박스 ID 생성
	 * 
	 * @param domainId
	 * @param batchId
	 * @param equipGroupCd
	 * @param orderNo
	 * @return
	 */
	public String generateBoxIdByOrderNo(Long domainId, String batchId, String equipGroupCd, String orderNo) {
		
		List<DpsJobInstance> jobList = this.searchJobListByOrder(batchId, orderNo, true);
		String boxId = null;
		
		if(ValueUtil.isNotEmpty(jobList)) {
			boxId = jobList.get(0).getBoxId();
			
			if(ValueUtil.isEmpty(boxId)) {
				// 새로운 박스 ID 생성
				boxId = this.newBoxId(domainId, equipGroupCd, DateUtil.todayStr("yyyyMMdd"));
				List<String> orderIdList = new ArrayList<String>();
				
				for(DpsJobInstance job : jobList) {
					// 박스 번호, 박스 전송 시간 설정
					job.setBoxId(boxId);
					orderIdList.add(job.getMheDrId());
				}
				
				// 작업 상세 정보 업데이트
				this.queryManager.updateBatch(jobList, "boxId");
				
				// 주문 정보 업데이트
				String sql = "update mhe_dr set box_id = :boxId where id in (:orderIdList)";
				this.queryManager.executeBySql(sql, ValueUtil.newMap("boxId,orderIdList", boxId, orderIdList));
			}
		}
				
		return boxId;		
	}
	
	/**
	 * 패킹 실적 WMS로 전송
	 * 
	 * @param batch
	 * @param orderNo
	 * @param boxId
	 */
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingToWms(JobBatch batch, String orderNo, String boxId) {
		
		List<WcsMheDr> orderItems = this.searchBoxItemsByOrder(batch.getId(), orderNo, boxId);
		
		if(ValueUtil.isNotEmpty(orderItems)) {
			IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
			String todayStr = DateUtil.todayStr("yyyyMMdd");
			Date currentTime = new Date();
			String currentTimeStr = DateUtil.dateTimeStr(currentTime, "yyyyMMddHHmmss");
			String status = "S";
			
			for(WcsMheDr boxedOrder : orderItems) {
				// 1. 박스 번호, 박스 전송 시간 설정
				boxedOrder.setBoxResultIfAt(currentTime);
				boxedOrder.setStatus(status);
				
				// 2. WMS 박스 실적 전송 
				Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", todayStr, boxedOrder.getWhCd(), boxedOrder.getBoxId(), boxedOrder.getRefNo(), boxedOrder.getStrrId(), boxedOrder.getItemCd(), boxedOrder.getCmptQty(), boxedOrder.getOutbEctDate(), currentTimeStr);
				wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
			}
			
			// 3. 주문 상세 정보 업데이트
			this.queryManager.updateBatch(orderItems, "status", "boxResultIfAt");
			
			// 4. 작업 정보 업데이트
			String sql = "update dps_job_instances set box_result_if_at = now(), status = :status where work_unit = :batchId and ref_no = :orderNo and box_id = :boxId";
			this.queryManager.executeBySql(sql, ValueUtil.newMap("batchId,orderNo,boxId,status", batch.getId(), orderNo, boxId, status));
		}
	}
	
	/**
	 * WMS에 주문 상태(취소된 상태 여부)를 체크 
	 *  
	 * @param domainId
	 * @param refNo
	 * @param boxId
	 */
	public List<DpsInspItem> checkInpectionItemsToWms(DpsInspection inspection) {
		String sql = "select strr_id as brand_cd, item_cd as sku_cd, item_season as sku_season, item_color as sku_color, item_size as sku_size, outb_ect_qty as order_qty, to_pick_qty as picked_qty, done_qty as confirm_qty from dps_actual_order where wh_cd = :whCd and ref_no = :refNo and item_season != 'X'";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsDpsActualOrder.class);
		List<DpsInspItem> inspectionItems = wmsQueryMgr.selectListBySql(sql, ValueUtil.newMap("whCd,refNo", FnFConstants.WH_CD_ICF, inspection.getOrderNo()), DpsInspItem.class, 0, 0);
		
		if(ValueUtil.isNotEmpty(inspectionItems)) {
			List<DpsInspItem> filteredItems = new ArrayList<DpsInspItem>();
			int totalInspectedPcs = 0;
			
			for(DpsInspItem item : inspectionItems) {
				totalInspectedPcs += item.getConfirmQty();
				
				// 검수 수량이 피킹 수량 보다 작으면 검수 대상
				if(item.getPickedQty() > item.getConfirmQty()) {
					item.setOrderQty(item.getPickedQty() - item.getConfirmQty());
					item.setPickedQty(item.getOrderQty());
					item.setConfirmQty(0);
					filteredItems.add(item);
				}				
			}
			
			if(filteredItems.isEmpty()) {
				if(totalInspectedPcs == 0) {
					inspection.setStatus(LogisConstants.JOB_STATUS_CANCEL);
					// throw ThrowUtil.newValidationErrorWithNoLog("해당 주문 [주문번호 : " + inspection.getOrderNo() + "]는 취소되었습니다.");
				} else {
					// throw ThrowUtil.newValidationErrorWithNoLog("해당 주문 [주문번호 : " + inspection.getOrderNo() + "]는 검수할 항목이 남아있지 않습니다.");
				}
			}			
		}
		
		return inspectionItems;
	}
	
	/**
	 * 총 검수해야 할 수량을 계산해서 리턴
	 * 
	 * @param domainId
	 * @param orderNo
	 * @return
	 */
	public int checkTotalToInspectionQty(Long domainId, String orderNo) {
		String sql = "select COALESCE(sum(to_pick_qty - done_qty), 0) as inspection_qty from dps_actual_order where wh_cd = :whCd and ref_no = :refNo and item_season != 'X'";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsDpsActualOrder.class);
		return wmsQueryMgr.selectBySql(sql, ValueUtil.newMap("whCd,refNo", FnFConstants.WH_CD_ICF, orderNo), Integer.class);
	}
	
	/**
	 * WMS에 단포 실적 전송
	 * 
	 * @param domainId
	 * @param today
	 * @param outDate
	 * @param itemCd
	 * @param newBoxId
	 */
	public void sendSinglePackToWms(Long domainId, String today, String outDate, String brandCd, String itemCd, String newBoxId) {
		
		String currentTimeStr = DateUtil.dateTimeStr(new Date(), "yyyyMMddHHmmss");
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", today, FnFConstants.WH_CD_ICF, newBoxId, null, brandCd, itemCd, 1, outDate, currentTimeStr);
		wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
	}
	
	/**
	 * 패킹 실적 WMS로 전송
	 * 
	 * @param batch
	 * @param orderNo
	 * @param boxId
	 * @param jobList
	 */
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingToWmsBySplit(JobBatch batch, String orderNo, String boxId, List<DpsJobInstance> jobList) {
		
		if(ValueUtil.isNotEmpty(jobList)) {
			IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
			String todayStr = DateUtil.todayStr("yyyyMMdd");
			Date currentTime = new Date();
			String currentTimeStr = DateUtil.dateTimeStr(currentTime, "yyyyMMddHHmmss");
			List<String> orderIdList = new ArrayList<String>(jobList.size());
			String status = "S";
			
			for(DpsJobInstance job : jobList) {
				// 1. 박스 번호, 박스 전송 시간 설정
				job.setBoxResultIfAt(currentTime);
				job.setStatus(status);
				
				// 2. 주문 정보 조회
				orderIdList.add(job.getMheDrId());
				
				// 3. WMS 박스 실적 전송 
				Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", todayStr, job.getWhCd(), boxId, orderNo, job.getStrrId(), job.getItemCd(), job.getCmptQty(), job.getOutbEctDate(), currentTimeStr);
				wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
			}
			
			// 4. 작업 정보 업데이트
			this.queryManager.updateBatch(jobList, "status", "boxResultIfAt");
			
			// 5. 작업 상세 정보 업데이트
			String sql = "update mhe_dr set status = :status, box_result_if_at = :boxResultIfAt where id in (:orderIdList)";
			Map<String, Object> params = ValueUtil.newMap("orderIdList,status,boxResultIfAt", orderIdList, status, currentTime);
			this.queryManager.executeBySql(sql, params);
		}
	}
	
	/**
	 * WMS에 송장 발행 요청 
	 * 
	 * @param batch
	 * @param orderNo
	 * @param boxId
	 * @return
	 */
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String requestInvoiceToWms(JobBatch batch, String orderNo, String boxId) {
		List<WcsMheDr> boxedOrders = this.searchBoxItemsByBoxId(batch.getId(), orderNo, boxId);
		String waybillNo = null;
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			WcsMheDr item = boxedOrders.get(0);
			waybillNo = ValueUtil.isEmpty(item.getWaybillNo()) ? this.newWaybillNo(boxId) : item.getWaybillNo();			
			String status = ValueUtil.isEqualIgnoreCase(waybillNo, FnFConstants.ORDER_CANCEL_ALL) ? LogisConstants.JOB_STATUS_CANCEL : BoxPack.BOX_STATUS_EXAMED;
			
			for(WcsMheDr boxedOrder : boxedOrders) {
				boxedOrder.setStatus(status);
				boxedOrder.setWaybillNo(waybillNo);
			}
			
			// mhe_dr에 송장 번호 업데이트
			this.queryManager.updateBatch(boxedOrders, "waybillNo", "status");
			
			// dps_job_instance에 송장 번호 업데이트
			String sql = "update dps_job_instances set waybill_no = :invoiceId, status = :status, inspector_id = :inspectorId, inspected_at = now() where work_unit = :batchId and ref_no = :orderNo and box_id = :boxId";
			this.queryManager.executeBySql(sql, ValueUtil.newMap("batchId,orderNo,boxId,invoiceId,status,inspectorId", batch.getId(), orderNo, boxId, waybillNo, status, User.currentUser() != null ? User.currentUser().getId() : LogisConstants.EMPTY_STRING));
		}
		
		return waybillNo;
	}
	
	/**
	 * WMS에 송장 발행 요청 
	 * 
	 * @param batch
	 * @param orderNo
	 * @param boxId
	 * @param jobList
	 * @return
	 */
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String requestInvoiceToWmsBySplit(JobBatch batch, String orderNo, String boxId, List<DpsJobInstance> jobList) {
		String waybillNo = null;
		
		if(ValueUtil.isNotEmpty(jobList)) {
			DpsJobInstance job = jobList.get(0);
			waybillNo = ValueUtil.isEmpty(job.getWaybillNo()) ? this.newWaybillNo(boxId) : job.getWaybillNo();			
			String status = ValueUtil.isEqualIgnoreCase(waybillNo, FnFConstants.ORDER_CANCEL_ALL) ? LogisConstants.JOB_STATUS_CANCEL : BoxPack.BOX_STATUS_EXAMED;
			List<String> mheDrIdList = new ArrayList<String>();
			String inspectorId = User.currentUser() != null ? User.currentUser().getId() : LogisConstants.EMPTY_STRING;
			Date currentTime = new Date();
			
			for(DpsJobInstance item : jobList) {
				item.setStatus(status);
				item.setWaybillNo(waybillNo);
				item.setInspectorId(inspectorId);
				item.setInspectedAt(currentTime);
				mheDrIdList.add(item.getMheDrId());
			}
				
			// 작업 정보 업데이트
			this.queryManager.updateBatch(jobList, "waybillNo", "status", "inspectorId", "inspectedAt");
			
			// 주문 정보 업데이트
			String sql = "update mhe_dr set waybill_no = :invoiceId, status = :status where id in (:mheDrIdList)";
			this.queryManager.executeBySql(sql, ValueUtil.newMap("invoiceId,mheDrIdList,status", waybillNo, mheDrIdList, status));
		}
		
		// 발행 송장 리턴
		return waybillNo;
	}
		
	/**
	 * RFID ID를 체크
	 * 
	 * @param rfidId
	 * @param exceptionWhenInvalid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> checkRfidId(String rfidId, boolean exceptionWhenInvalid) {
		Map<String, Object> procParams = ValueUtil.newMap("IN_CD_RFIDUID", rfidId);
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidBoxItem.class);
		Map<String, Object> result = rfidQueryMgr.callReturnProcedure("PRO_RFIDUID_STATUS_CHECK", procParams, Map.class);
		
		// 리턴 파라미터 : OUT_CONFIRM, OUT_MSG, OUT_DEPART, OUT_ITEM_CD, OUT_STYLE, OUT_GOODS, OUT_COLOR, OUT_SIZE, OUT_BARCOD
		String successYn = ValueUtil.toString(result.get("OUT_CONFIRM"));
		
		if(ValueUtil.isNotEqual(successYn, LogisConstants.Y_CAP_STRING)) {
			if(exceptionWhenInvalid) {
				String msg = ValueUtil.toString(result.get("OUT_MSG"));
				msg = ValueUtil.isEmpty(msg) ? "스캔한 RFID 코드는 사용할 수 없습니다." : msg;
				throw ThrowUtil.newValidationErrorWithNoLog(msg);
			}
		}
		
		return result;
	}

	/**
	 * 검수 완료 정보 RFID로 전송
	 * 
	 * @param batch
	 * @param invoiceId
	 */
	public void sendPackingToRfid(JobBatch batch, String invoiceId) {
		
		List<RfidResult> rfidResults = this.searchRfidResultByInvoice(batch.getDomainId(), batch.getId(), invoiceId);
		
		if(ValueUtil.isNotEmpty(rfidResults)) {
			IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidDpsInspResult.class);
			// 이미 해당 송장이 RFID에 존재하는지 체크 
			int count = rfidQueryMgr.selectSizeBySql(RFID_EXAMED_SELECT_SQL, ValueUtil.newMap("waybillNo", invoiceId));
			String currentTimeStr = DateUtil.dateTimeStr(new Date(), "yyyyMMddHHmmss");
			
			if(count == 0) {
				for(RfidResult rfidResult : rfidResults) {
					// RFID 실적 전송
					String todayStr = rfidResult.getJobDate().replace(LogisConstants.DASH, LogisConstants.EMPTY_STRING);
					Map<String, Object> params = ValueUtil.newMap("today,brandCd,shopCd,waybillNo,orderNo,rfidUid,creatorId,currentTime", todayStr, rfidResult.getBrandCd(), rfidResult.getShopCd(), invoiceId, rfidResult.getOrderNo(), rfidResult.getRfidId(), "WCS", currentTimeStr);
					rfidQueryMgr.executeBySql(RFID_EXAMED_INSERT_SQL, params);
				}
				
				// 프로시져 호출 - RFID_IF.PK_IF_RFIDHISTORY_RECV.PRO_IF_RFIDHISTORY_RECV_ONLINE
				this.confirmRfidProcedure(invoiceId);
			}
		}
	}
	
	/**
	 * 단포 검수 완료 정보 RFID로 전송
	 * 
	 * @param rfidResult
	 */
	public void sendSinglePackingToRfid(RfidResult rfidResult) {
		
		String todayStr = DateUtil.todayStr("yyyyMMdd");
		String invoiceId = rfidResult.getInvoiceId();
		String currentTimeStr = DateUtil.dateTimeStr(new Date(), "yyyyMMddHHmmss");
		Map<String, Object> params = ValueUtil.newMap("today,brandCd,shopCd,waybillNo,orderNo,rfidUid,creatorId,currentTime", todayStr, rfidResult.getBrandCd(), rfidResult.getShopCd(), invoiceId, rfidResult.getOrderNo(), rfidResult.getRfidId(), "WCS", currentTimeStr);
		
		// RFID 시스템에 단포 실적 전송
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidDpsInspResult.class);
		rfidQueryMgr.executeBySql(RFID_EXAMED_INSERT_SQL, params);

		// 프로시져 호출 - RFID_IF.PK_IF_RFIDHISTORY_RECV.PRO_IF_RFIDHISTORY_RECV_ONLINE
		this.confirmRfidProcedure(invoiceId);
	}
	
	/**
	 * RFID 프로시져 호출
	 * 
	 * @param invoiceId
	 */
	private void confirmRfidProcedure(String invoiceId) {
		// 1. 조건 생성
		Map<String, Object> params = ValueUtil.newMap("IN_NO_WAYBILL", invoiceId);
		// 2. 작업 지시 프로시져 콜 
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidDpsInspResult.class);
		Map<?, ?> result = rfidQueryMgr.callReturnProcedure("PK_IF_RFIDHISTORY_RECV.PRO_IF_RFIDHISTORY_RECV_ONLINE", params, Map.class);
		// 3. 결과 파싱 
		String successYn = ValueUtil.toString(result.get("OUT_YN_SUCCESS"));
		
		if(ValueUtil.isEqualIgnoreCase(successYn, LogisConstants.N_CAP_STRING)) {
			String errorMsg = ValueUtil.isNotEmpty(result.get("OUT_ERRMSG")) ? result.get("OUT_ERRMSG").toString() : SysConstants.EMPTY_STRING;
			
			if(ValueUtil.isNotEmpty(errorMsg)) {
				throw new ElidomRuntimeException(errorMsg);
			} else {
				throw new ElidomRuntimeException("Failed to DPS RFID Confirm Procedure!");
			}
		}
	}
	
	/**
	 * 온라인 주문 번호로 박스 실적 주문 조회
	 * 
	 * @param batchId
	 * @param orderNo
	 * @param boxId
	 * @return
	 */
	private List<DpsJobInstance> searchJobListByOrder(String batchId, String orderNo, boolean isBoxIdEmpty) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batchId);
		condition.addFilter("status", BoxPack.BOX_STATUS_BOXED);
		condition.addFilter("refNo", orderNo);
		if(isBoxIdEmpty) {
			condition.addFilter("boxId", LogisConstants.IS_BLANK, LogisConstants.EMPTY_STRING);
		}
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(DpsJobInstance.class, condition);
	}
	
	/**
	 * 온라인 주문 번호로 박스 실적 주문 조회
	 * 
	 * @param batchId
	 * @param orderNo
	 * @param boxId
	 * @return
	 */
	private List<WcsMheDr> searchBoxItemsByOrder(String batchId, String orderNo, String boxId) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batchId);
		condition.addFilter("status", BoxPack.BOX_STATUS_BOXED);
		condition.addFilter("refNo", orderNo);
		if(ValueUtil.isNotEmpty(boxId)) {
			condition.addFilter("boxId", boxId);
		}
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheDr.class, condition);
	}
	
	/**
	 * 박스 ID로 박스 실적 주문 조회
	 * 
	 * @param batchId
	 * @param orderNo
	 * @param boxId
	 * @return
	 */
	private List<WcsMheDr> searchBoxItemsByBoxId(String batchId, String orderNo, String boxId) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batchId);
		condition.addFilter("status", "S");
		condition.addFilter("refNo", orderNo);
		condition.addFilter("boxId", boxId);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheDr.class, condition);
	}
	
	/**
	 * 송장 번호로 RFID 검수 실적 조회
	 * 
	 * @param domainId
	 * @param batchId
	 * @param invoiceId
	 * @return
	 */
	private List<RfidResult> searchRfidResultByInvoice(Long domainId, String batchId, String invoiceId) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batchId);
		condition.addFilter("invoiceId", invoiceId);
		condition.addOrder("createdAt", true);
		return this.queryManager.selectList(RfidResult.class, condition);
	}
	
	/**
	 * 운송장 번호 발행
	 * 
	 * @param boxId
	 * @return
	 */
	public String newWaybillNo(String boxId) {
		return this.newWaybillNo(boxId, false);
	}
	
	/**
	 * 운송장 번호 발행
	 * 
	 * @param boxId
	 * @param exceptionWhenResNotOk
	 * @return
	 */
	public String newWaybillNo(String boxId, boolean exceptionWhenResNotOk) {
		String waybillReqUrl = SettingUtil.getValue("fnf.waybill_no.request.url", "http://dev.wms.fnf.co.kr/onlineInvoiceMultiPackService/issue_express_waybill");
		waybillReqUrl += "?WH_CD=ICF&BOX_ID=" + boxId;
		RestTemplate rest = new RestTemplate();
		StringHttpMessageConverter shmc = new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8));
		shmc.setSupportedMediaTypes(ValueUtil.toList(MediaType.APPLICATION_JSON_UTF8));
		rest.getMessageConverters().add(0, shmc);
		WaybillResponse res = rest.getForObject(waybillReqUrl, WaybillResponse.class);
		
		if(res == null || ValueUtil.isNotEqual(LogisConstants.OK_STRING.toUpperCase(), res.getErrorMsg())) {
			if(!exceptionWhenResNotOk && ValueUtil.isEqualIgnoreCase(res.getErrorCode(), FnFConstants.INVOICE_RES_CODE_ORDER_CANCEL_ALL)) {
				return FnFConstants.ORDER_CANCEL_ALL;
			} else {
				String msg = ValueUtil.isEmpty(res.getErrorMsg()) ? "WMS에서 송장 발행에 실패했습니다." : res.getErrorMsg();
				throw new ElidomRuntimeException(msg);
			}
		} else {
			return res.getWaybillNo();
		}
	}
	
	/**
	 * WMS I/F를 위한 UNIQUE 박스 ID 생성
	 * 
	 * @param domainId
	 * @param mheNo
	 * @param todayStr
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String newBoxId(Long domainId, String mheNo, String todayStr) {
		// Prefix '70' + YYMM(4자리) + 장비식별(2자리) + Cycle일련번호(6자리) ex) 702005M1000001
		mheNo = ValueUtil.isEmpty(mheNo) ? "M1" : mheNo;
		String prefix = "70";
		String yearMonth = todayStr.substring(2, 6);
		Integer serialNo = RangedSeq.increaseSequence(domainId, "DPS_UNIQUE_BOX_ID", prefix, "YEAR_MONTH", yearMonth, "MHE_NO", mheNo);
		String serialStr = StringUtils.leftPad(LogisConstants.EMPTY_STRING + serialNo, 6, LogisConstants.ZERO_STRING);
		return prefix + yearMonth + mheNo + serialStr;
	}
	
	/**
	 * 단포 일별 박스 투입 순서 생성 
	 * 
	 * @param domainId
	 * @param mheNo
	 * @param todayStr
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Integer newSinglePackBoxInputSeq(Long domainId, String mheNo, String dateStr) {
		mheNo = ValueUtil.isEmpty(mheNo) ? "M1" : mheNo;
		return RangedSeq.increaseSequence(domainId, "DPS_SINGLE_PACK_SEQ", "D", "DATE", dateStr, "MHE_NO", mheNo);
	}

}
