package operato.fnf.wcs.service.send;

import java.nio.charset.Charset;
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

import operato.fnf.wcs.entity.RfidDpsInspResult;
import operato.fnf.wcs.entity.RfidResult;
import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.service.model.WaybillResponse;
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
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

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
	private static final String RFID_EXAMED_INSERT_SQL = "INSERT INTO if_rfidhistory_recv(DT_IF_DATE, NO_IF_SEQ, TP_GUBUN, CD_COMPANY, CD_DEPART, DT_DATE, CD_SHOP, CD_BILL, CD_SUBBILL, CD_RFIDUID, TP_HISTORY, TP_STATUS, CD_REGISTER, DM_BF_RECV) VALUES (:today, RFID_IF.SEQ_IF_RFIDHISTORY_RECV.NEXTVAL, 'I', 'FnF', :brandCd, :today, :shopCd, :waybillNo, '1', :rfidUid, '42', '0', :creatorId, sysdate)";	
	
	/**
	 * 박스 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	public void sendBoxResults(Domain domain, JobBatch batch) {
		// 1. 박스 완료 실적 조회
		List<WcsMheDr> orderList = this.searchBoxedOrderList(batch);
		
		if(ValueUtil.isNotEmpty(orderList)) {
			// TODO 트랜잭션을 위해 로컬 쿼리 매니저로 변경 후 테스트 ...
			IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
			
			String todayStr = DateUtil.todayStr("yyyyMMdd");
			DpsBoxSendService boxSendSvc = BeanUtil.get(DpsBoxSendService.class);
			
			// 2. WMS에 전송
			for(WcsMheDr order : orderList) {
				// 2.1 WCS 주문별 실적 정보 
				List<WcsMheDr> boxedOrders = this.searchBoxResultByOrder(order.getWorkUnit(), order.getRefNo());
				// 2.2 WCS 주문별 실적 정보를 WMS 패킹 정보로 복사
				boxSendSvc.sendPackingsToWms(batch.getDomainId(), wmsQueryMgr, boxedOrders, batch.getEquipGroupCd(), todayStr);
			}
		}
	}

	/**
	 * RFID로 검수 완료 정보 전송
	 * 
	 * @param domainId
	 * @param batchId
	 * @param invoiceId
	 */
	public void sendPackingsToRfid(Long domainId, String batchId, String invoiceId) {
		
		List<RfidResult> rfidResults = this.searchRfidResultByInvoice(domainId, batchId, invoiceId);
		
		if(ValueUtil.isNotEmpty(rfidResults)) {
			IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidDpsInspResult.class);
			
			for(RfidResult rfidResult : rfidResults) {
				// RFID 실적 전송
				Map<String, Object> params = ValueUtil.newMap("today,brandCd,shopCd,invoiceId,rfidUid,creatorId", rfidResult.getJobDate(), rfidResult.getBrandCd(), rfidResult.getShopCd(), invoiceId, rfidResult.getRfidId(), "WCS");
				rfidQueryMgr.executeBySql(RFID_EXAMED_INSERT_SQL, params);
			}
			
			// 프로시져 호출 - RFID_IF.PK_IF_RFIDHISTORY_RECV.PRO_IF_RFIDHISTORY_RECV_ONLINE
			this.confirmRfidProcedure(invoiceId);
		}
	}
	
	/**
	 * RFID 프로시져 호출
	 * 
	 * @param invoiceId
	 */
	private void confirmRfidProcedure(String invoiceId) {
		// 1. 조건 생성
		Map<String, Object> params = ValueUtil.newMap("IN_NO_WAYBILL", invoiceId, null, null);
		// 2. 작업 지시 프로시져 콜 
		Map<?, ?> result = this.queryManager.callReturnProcedure("RFID_IF.PK_IF_RFIDHISTORY_RECV.PRO_IF_RFIDHISTORY_RECV_ONLINE", params, Map.class);
		// 3. 결과 파싱 
		String successYn = ValueUtil.toString(result.get("OUT_YN_SUCCESS"));
		String errorMsg = ValueUtil.isNotEmpty(result.get("OUT_ERRMSG")) ? result.get("OUT_ERRMSG").toString() : SysConstants.EMPTY_STRING;
		
		if(ValueUtil.isEqualIgnoreCase(successYn, LogisConstants.N_CAP_STRING)) {
			if(ValueUtil.isNotEmpty(errorMsg)) {
				throw new ElidomRuntimeException(errorMsg);
			} else {
				throw new ElidomRuntimeException("Failed to DPS RFID Confirm Procedure!");
			}
		}
	}
	
	/**
	 * 박싱된 주문 리스트 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<WcsMheDr> searchBoxedOrderList(JobBatch batch) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addSelect("work_unit", "ref_no");
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("status", BoxPack.BOX_STATUS_BOXED);
		condition.addOrder("refNo", true);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheDr.class, condition);
	}
	
	/**
	 * 온라인 주문 번호로 박스 실적 주문 조회
	 * 
	 * @param batchId
	 * @param orderId
	 * @return
	 */
	private List<WcsMheDr> searchBoxResultByOrder(String batchId, String orderId) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batchId);
		condition.addFilter("status", BoxPack.BOX_STATUS_BOXED);
		condition.addFilter("refNo", orderId);
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
	 * WCS 박스 실적으로 부터 WMS 박스 실적 복사 
	 * 
	 * @param domainId
	 * @param wmsQueryMgr
	 * @param boxedOrders
	 * @param mheNo
	 * @param todayStr
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingsToWms(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheDr> boxedOrders, String mheNo, String todayStr) {
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			Date currentTime = new Date();
			String currentTimeStr = DateUtil.dateTimeStr(currentTime, "yyyyMMddHHmmss");
			String boxId = null;
			String waybillNo = null;
			
			for(WcsMheDr boxedOrder : boxedOrders) {
				// 1. 주문 별 박스 번호 생성
				if(ValueUtil.isEmpty(boxId)) {
					// 1.1 박스 Unique ID 생성
					boxId = ValueUtil.isEmpty(boxedOrder.getBoxId()) ? this.newBoxId(domainId, mheNo, todayStr) : boxedOrder.getBoxId();
					// 1.2 송장 번호 생성
					waybillNo = ValueUtil.isEmpty(boxedOrder.getWaybillNo()) ? this.newWaybillNo(boxedOrder) : boxedOrder.getWaybillNo();
				}
				
				// 2. 박스 번호, 박스 전송 시간 설정
				boxedOrder.setBoxId(boxId);
				boxedOrder.setBoxResultIfAt(currentTime);
				boxedOrder.setWaybillNo(waybillNo);
				boxedOrder.setStatus("S");
				
				// 3. WMS 박스 실적 전송 
				Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", todayStr, boxedOrder.getWhCd(), boxId, boxedOrder.getRefNo(), boxedOrder.getStrrId(), boxedOrder.getItemCd(), boxedOrder.getCmptQty(), boxedOrder.getOutbEctDate(), currentTimeStr);
				wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
			}
			
			// 4. 주문 상세 정보 업데이트
			this.queryManager.updateBatch(boxedOrders, "status", "boxId", "waybillNo", "boxResultIfAt");
		}
	}
	
	/**
	 * 운송장 번호 발행
	 * 
	 * @param boxedOrder
	 * @return
	 */
	private String newWaybillNo(WcsMheDr boxedOrder) {
		String waybillReqUrl = SettingUtil.getValue("fnf.waybill_no.request.url", "http://dev.wms.fnf.co.kr/onlineInvoiceMultiPackService/issue_express_waybill");
		waybillReqUrl += "?WH_CD=ICF&BOX_ID=" + boxedOrder.getBoxId();
		RestTemplate rest = new RestTemplate();
		StringHttpMessageConverter shmc = new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8));
		shmc.setSupportedMediaTypes(ValueUtil.toList(MediaType.APPLICATION_JSON_UTF8));
		rest.getMessageConverters().add(0, shmc);
		WaybillResponse res = rest.getForObject(waybillReqUrl, WaybillResponse.class);
		
		if(res == null || ValueUtil.isNotEqual("OK", res.getErrorMsg())) {
			// TODO 내부 개발 서버에서는 더미 API를 개발하여 배포 ...
			throw new ElidomRuntimeException("Error When Request Waybill Service To WMS", res.getErrorMsg());
			//return boxedOrder.getBoxId();
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
	private String newBoxId(Long domainId, String mheNo, String todayStr) {
		//Prefix '70' + YYMM(4자리) + 장비식별(2자리) + Cycle일련번호(6자리)
		//ex) 702005M1000001
		
		String prefix = "70";
		String yearMonth = todayStr.substring(2, 6);
		Integer serialNo = RangedSeq.increaseSequence(domainId, "DPS_UNIQUE_BOX_ID", prefix, "YEAR_MONTH", yearMonth, "MHE_NO", mheNo);
		String serialStr = StringUtils.leftPad(LogisConstants.EMPTY_STRING + serialNo, 6, LogisConstants.ZERO_STRING);
		return prefix + yearMonth + mheNo + serialStr;
	}

}
