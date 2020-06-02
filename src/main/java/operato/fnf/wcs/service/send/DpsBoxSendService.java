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

import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.service.model.WaybillResponse;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dev.entity.RangedSeq;
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
				List<WcsMheDr> boxedOrders = this.searchBoxResult(order.getWorkUnit(), order.getRefNo());
				// 2.2 WCS 주문별 실적 정보를 WMS 패킹 정보로 복사
				boxSendSvc.sendPackingsToWms(batch.getDomainId(), wmsQueryMgr, boxedOrders, batch.getEquipGroupCd(), todayStr);
				// 2.3 WCS 주문별 실적 정보를 RFID 패킹 정보로 복사
				boxSendSvc.sendPackingsToRfid(batch.getDomainId(), wmsQueryMgr, boxedOrders, batch.getEquipGroupCd(), todayStr);
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
	 * 박스 실적 주문 조회
	 * 
	 * @param batchId
	 * @param orderId
	 * @return
	 */
	private List<WcsMheDr> searchBoxResult(String batchId, String orderId) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batchId);
		condition.addFilter("status", BoxPack.BOX_STATUS_BOXED);
		condition.addFilter("refNo", orderId);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheDr.class, condition);
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
			
			for(WcsMheDr boxedOrder : boxedOrders) {
				// 1. 주문 별 박스 번호 생성
				if(ValueUtil.isEmpty(boxId)) {
					boxId = ValueUtil.isEmpty(boxedOrder.getBoxId()) ? this.newBoxId(domainId, mheNo, todayStr) : boxedOrder.getBoxId();
				}
				
				// 2. 박스 번호, 박스 전송 시간 설정
				boxedOrder.setBoxId(boxId);
				boxedOrder.setBoxResultIfAt(currentTime);
				
				// 3. WMS 박스 실적 전송 
				Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", todayStr, boxedOrder.getWhCd(), boxId, boxedOrder.getRefNo(), boxedOrder.getStrrId(), boxedOrder.getItemCd(), boxedOrder.getCmptQty(), boxedOrder.getOutbEctDate(), currentTimeStr);
				wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
			}
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingsToRfid(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheDr> boxedOrders, String mheNo, String todayStr) {
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			List<RfidBoxItem> boxItems = new ArrayList<RfidBoxItem>(boxedOrders.size());
			String waybillNo = null;
			
			for(WcsMheDr boxedOrder : boxedOrders) {
				// 1. 주문 별 박스
				if(ValueUtil.isEmpty(waybillNo)) {
					waybillNo = ValueUtil.isEmpty(boxedOrder.getWaybillNo()) ? this.newWaybillNo(boxedOrder) : boxedOrder.getWaybillNo();
				}
				
				// 2. 송장 번호, 상태 설정
				boxedOrder.setWaybillNo(waybillNo);
				boxedOrder.setStatus("S");
				
				// 3. RFID 실적 생성
				RfidBoxItem boxItem = this.newRfidBoxItem(boxedOrder);
				boxItems.add(boxItem);
			}
			
			// 4. RFID 실적 전송
			wmsQueryMgr.insertBatch(boxItems);
			// 5. 주문 상세 정보 업데이트
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
			//throw new ElidomRuntimeException("Error When Request Waybill Service To WMS", res.getErrorMsg());
			return boxedOrder.getBoxId();
		} else {
			return res.getWaybillNo();
		}
	}
	
	/**
	 * RFID 박스 정보 생성
	 * 
	 * @param orderItem
	 * @return
	 */
	private RfidBoxItem newRfidBoxItem(WcsMheDr orderItem) {
		RfidBoxItem rfidBoxItem = new RfidBoxItem();
		rfidBoxItem.setCdWarehouse(orderItem.getWhCd());
		rfidBoxItem.setCdBrand(orderItem.getStrrId());
		rfidBoxItem.setTpMachine(LogisConstants.TWO_STRING);
		rfidBoxItem.setDtDelivery(orderItem.getWorkDate());
		rfidBoxItem.setDsBatchNo(orderItem.getWorkUnit());
		rfidBoxItem.setNoBox(orderItem.getBoxNo());
		rfidBoxItem.setNoWaybill(orderItem.getWaybillNo());
		rfidBoxItem.setIfCdItem(orderItem.getItemCd());
		rfidBoxItem.setYnAssort(LogisConstants.N_CAP_STRING);
		rfidBoxItem.setCdShop(orderItem.getShiptoId());
		rfidBoxItem.setTpDelivery(LogisConstants.TWO_STRING);
		rfidBoxItem.setOutbTcd(orderItem.getOutbTcd());
		rfidBoxItem.setDsShuteno(null);
		rfidBoxItem.setOutbNo(orderItem.getOutbNo());
		rfidBoxItem.setRefNo(orderItem.getRefNo());
		rfidBoxItem.setNoWeight(null);
		rfidBoxItem.setQtDelivery(orderItem.getCmptQty());
		String currentTime = DateUtil.dateStr(orderItem.getBoxResultIfAt(), "yyyyMMddHHmmss");
		rfidBoxItem.setDmBfRecv(currentTime);
		rfidBoxItem.setYnCancel(LogisConstants.N_CAP_STRING);
		rfidBoxItem.setTpWeight(LogisConstants.ZERO_STRING);
		rfidBoxItem.setTpSend(LogisConstants.ZERO_STRING);
		return rfidBoxItem;
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
		String yearMonth = todayStr.substring(2, 4);
		Integer serialNo = RangedSeq.increaseSequence(domainId, "DPS_UNIQUE_BOX_ID", prefix, "YEAR_MONTH", yearMonth, "MHE_NO", mheNo);
		String serialStr = StringUtils.leftPad(LogisConstants.EMPTY_STRING + serialNo, 6, LogisConstants.ZERO_STRING);
		return prefix + yearMonth + mheNo + serialStr;
	}

}
