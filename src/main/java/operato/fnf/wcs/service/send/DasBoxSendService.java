package operato.fnf.wcs.service.send;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.entity.WmsMheBox;
import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
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
	private String findRefNoQuery = "select distinct ref_no, outb_tcd from mhe_dr where wh_cd = :whCd and work_unit = :batchId and outb_no = :orderNo";
	
	/**
	 * 박스 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	public void sendBoxResults(Domain domain, JobBatch batch) {
		// 1. 박스 완료 실적 조회
		List<WcsMheBox> boxedOrderList = this.searchBoxedOrderList(batch);
		
		// TODO 트랜잭션을 위해 로컬 쿼리 매니저로 변경 후 테스트 ...
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		DasBoxSendService boxSendSvc = BeanUtil.get(DasBoxSendService.class);
		
		// 2. WMS에 전송
		for(WcsMheBox order : boxedOrderList) {
			// 2.1 WCS 주문별 실적 정보 
			List<WcsMheBox> boxedOrders = this.searchBoxResult(order.getWorkUnit(), order.getOutbNo());
			// 2.2 WCS 주문별 실적 정보를 WMS 패킹 정보로 복사
			boxSendSvc.sendPackingsToWms(batch.getDomainId(), wmsQueryMgr, boxedOrders);
			// 2.3 WCS 주문별 실적 정보를 RFID 패킹 정보로 복사
			boxSendSvc.sendPackingsToRfid(batch.getDomainId(), wmsQueryMgr, boxedOrders);
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
		condition.addSelect("work_unit", "outb_no");
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("ifYn", LogisConstants.NOT_EQUAL, LogisConstants.Y_CAP_STRING);
		condition.addOrder("outbNo", true);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheBox.class, condition);
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
		condition.addFilter("whCd", "ICF");
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
		Map<String, Object> order = this.queryManager.selectBySql(this.findRefNoQuery, params, Map.class);
		rfidBoxItem.setRefNo(ValueUtil.toString(order.get("ref_no")));
		rfidBoxItem.setOutbTcd(ValueUtil.toString(order.get("outb_tcd")));
		
		return rfidBoxItem;
	}

}
