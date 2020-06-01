package operato.fnf.wcs.service.send;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
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
	 * 박스 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	public void sendBoxResults(Domain domain, JobBatch batch) {
		// 1. 박스 완료 실적 조회
		List<WcsMheDr> orderList = this.searchBoxedOrderList(batch);
		// TODO 트랜잭션을 위해 로컬 쿼리 매니저로 변경 후 테스트 ...
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		String todayStr = DateUtil.todayStr("yyyyMMdd");
		String currentTimeStr = DateUtil.dateTimeStr(new Date(), "yyyyMMddHHmmss");
		DpsBoxSendService boxSendSvc = BeanUtil.get(DpsBoxSendService.class);
		
		// 2. WMS에 전송
		for(WcsMheDr order : orderList) {
			// 2.1 WCS 주문별 실적 정보 
			List<WcsMheDr> boxedOrders = this.searchBoxResult(order.getWorkUnit(), order.getRefNo());
			// 2.2 WCS 주문별 실적 정보를 WMS 패킹 정보로 복사
			boxSendSvc.sendPackingsToWms(batch.getDomainId(), wmsQueryMgr, boxedOrders, batch.getEquipGroupCd(), todayStr, currentTimeStr);
			// 2.3 WCS 주문별 실적 정보를 RFID 패킹 정보로 복사
			boxSendSvc.sendPackingsToRfid(batch.getDomainId(), wmsQueryMgr, boxedOrders, batch.getEquipGroupCd(), todayStr, currentTimeStr);
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
		condition.addSelect("work_unit", "ref_no");
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("status", "B");
		condition.addOrder("refNo", false);
		condition.addOrder("mheDatetime", false);
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
		condition.addFilter("workUnit", batchId);
		condition.addFilter("status", "B");
		condition.addFilter("refNo", orderId);
		condition.addOrder("mheDatetime", false);
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
	 * @param currentTime
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingsToWms(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheDr> boxedOrders, String mheNo, String todayStr, String currentTime) {
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			for(WcsMheDr boxedOrder : boxedOrders) {
				// 1. 주문 별 박스 번호 생성
				String boxId = ValueUtil.isEmpty(boxedOrder.getBoxNo()) ? this.newBoxId(domainId, mheNo, todayStr) : boxedOrder.getBoxNo();
				boxedOrder.setBoxNo(boxId);
				
				// 2. WMS 박스 실적 전송 
				Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", todayStr, boxedOrder.getWhCd(), boxId, boxedOrder.getRefNo(), boxedOrder.getStrrId(), boxedOrder.getItemCd(), boxedOrder.getCmptQty(), boxedOrder.getOutbEctDate(), currentTime);
				wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
			}
			
			// 3. WCS_MHE_DR 정보에 박스 번호 업데이트
			this.queryManager.updateBatch(boxedOrders, "boxNo");
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendPackingsToRfid(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheDr> boxedOrders, String mheNo, String todayStr, String currentTime) {
		
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			List<RfidBoxItem> boxItems = new ArrayList<RfidBoxItem>(boxedOrders.size());
			
			for(WcsMheDr boxedOrder : boxedOrders) {
				// 1. 주문 별 박스
				String waybillNo = ValueUtil.isEmpty(boxedOrder.getWaybillNo()) ? this.newWaybillNo(boxedOrder) : boxedOrder.getWaybillNo();
				boxedOrder.setWaybillNo(waybillNo);
				boxedOrder.setStatus("S");
				
				// 2. RFID 실적 전송 
				RfidBoxItem boxItem = this.newRfidBoxItem(boxedOrder);
				boxItems.add(boxItem);
			}
			
			wmsQueryMgr.insertBatch(boxItems);
			this.queryManager.updateBatch(boxedOrders, "status", "waybillNo");
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
		waybillReqUrl += "?WH_CD=ICF&BOX_ID=" + boxedOrder.getBoxNo();
		RestTemplate rest = new RestTemplate();
		rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8)));
		WaybillResponse res = rest.getForObject(waybillReqUrl, WaybillResponse.class);
		
		if(res == null || ValueUtil.isNotEqual("OK", res.getErrorMsg())) {
			return res.getWaybillNo();
		} else {
			throw new ElidomRuntimeException("Error When Request Waybill Service To WMS", res.getErrorMsg());
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
		rfidBoxItem.setTpMachine("2");
		rfidBoxItem.setDtDelivery(orderItem.getWorkDate());
		rfidBoxItem.setDsBatchNo(orderItem.getWorkUnit());
		rfidBoxItem.setNoBox(orderItem.getBoxNo());
		rfidBoxItem.setNoWaybill(orderItem.getWaybillNo());
		rfidBoxItem.setIfCdItem(orderItem.getItemCd());
		rfidBoxItem.setYnAssort(LogisConstants.N_CAP_STRING);
		rfidBoxItem.setCdShop(orderItem.getShiptoId());
		rfidBoxItem.setTpDelivery("2");
		rfidBoxItem.setOutbTcd(orderItem.getOutbTcd());
		rfidBoxItem.setDsShuteno(null);
		rfidBoxItem.setOutbNo(orderItem.getOutbNo());
		rfidBoxItem.setRefNo(orderItem.getRefNo());
		rfidBoxItem.setNoWeight(null);
		rfidBoxItem.setQtDelivery(orderItem.getCmptQty());
		rfidBoxItem.setDmBfRecv(DateUtil.dateStr(new Date(), "YYYYMMDDHHMMSS"));
		rfidBoxItem.setYnCancel(LogisConstants.N_CAP_STRING);
		rfidBoxItem.setTpWeight("0");
		rfidBoxItem.setTpSend("0");
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
