package operato.fnf.wcs.service.send;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dev.entity.RangedSeq;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.DateUtil;
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
	 * 박스 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendBoxResults(Domain domain, JobBatch batch) {
		// 1. 박스 완료 실적 조회
		List<WcsMheDr> orderList = this.searchBoxedOrderList(batch);
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		String todayStr = DateUtil.todayStr("yyyyMMdd");
		String currentTimeStr = DateUtil.dateTimeStr(new Date(), "yyyyMMddHHmmss");
		
		// 2. WMS에 전송
		for(WcsMheDr order : orderList) {
			// 2.1 WCS 주문별 실적 정보 
			List<WcsMheDr> boxedOrders = this.searchBoxResult(order.getWorkUnit(), order.getRefNo());
			// 2.2 WCS 주문별 실적 정보를 WMS 패킹 정보로 복사
			this.createWmsPackings(batch.getDomainId(), wmsQueryMgr, boxedOrders, batch.getEquipGroupCd(), todayStr, currentTimeStr);
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
	private void createWmsPackings(Long domainId, IQueryManager wmsQueryMgr, List<WcsMheDr> boxedOrders, String mheNo, String todayStr, String currentTime) {
		if(ValueUtil.isNotEmpty(boxedOrders)) {
			for(WcsMheDr boxedOrder : boxedOrders) {
				String newBoxId = this.newBoxId(domainId, mheNo, todayStr);
				Map<String, Object> params = ValueUtil.newMap("today,whCd,boxId,orderNo,brandCd,skuCd,pickedQty,jobDate,currentTime", todayStr, boxedOrder.getWhCd(), newBoxId, boxedOrder.getRefNo(), boxedOrder.getStrrId(), boxedOrder.getItemCd(), boxedOrder.getCmptQty(), boxedOrder.getOutbEctDate(), currentTime);
				wmsQueryMgr.executeBySql(WMS_PACK_INSERT_SQL, params);
			}
			
			for(WcsMheDr boxedOrder : boxedOrders) {
				boxedOrder.setStatus("S");
			}
			
			this.queryManager.updateBatch(boxedOrders, "status");
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
		String yearMonth = todayStr.substring(2, 4);
		Integer serialNo = RangedSeq.increaseSequence(domainId, "DPS_UNIQUE_BOX_ID", prefix, "YEAR_MONTH", yearMonth, "MHE_NO", mheNo);
		String serialStr = StringUtils.leftPad(LogisConstants.EMPTY_STRING + serialNo, 6, LogisConstants.ZERO_STRING);
		return prefix + yearMonth + mheNo + serialStr;
	}

}
