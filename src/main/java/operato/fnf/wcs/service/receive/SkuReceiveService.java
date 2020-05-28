package operato.fnf.wcs.service.receive;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.SkuSyncHist;
import operato.fnf.wcs.entity.WmsMheItemBarcode;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.event.master.SkuReceiptEvent;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.DateUtil;

/**
 * 상품 수신 서비스
 * 
 * @author shortstop
 */
@Component
public class SkuReceiveService extends AbstractLogisService {

	/**
	 * 상품 수신 - 예정 수량 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = SkuReceiptEvent.class, condition = "#event.receiveType == 'sku' and #event.eventStep == 1")
	public void readyToReceive(SkuReceiptEvent event) {
		
		Long domainId = event.getDomainId();
		
		// WMS의 mhe_item_barcode의 업데이트 시간을 기준으로 주문 수신
		String skuLatestReceivedAt = this.findLatestReceivedAt(domainId);
		event.setLastReceivedAt(skuLatestReceivedAt);
		
		// 주문 수신 카운트를 조회
		int planCount = this.findSkuReceivePlanCount(domainId, skuLatestReceivedAt);
		event.setPlanCount(planCount);
		
		// 이벤트 실행 여부 설정
		event.setExecuted(true);
	}
	
	/**
	 * 최종 수신 기준 시간을 조회한다.
	 * 
	 * @param domainId
	 * @return
	 */
	private String findLatestReceivedAt(Long domainId) {
		
		String sql = "select sync_time from sku_sync_hists where domain_id = :domainId order by sync_time desc limit 1";
		String latestReceivedAt = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId", domainId), String.class);
		
		if(ValueUtil.isEmpty(latestReceivedAt)) {
			sql = "select upd_dt from (select to_char(upd_datetime, 'YYYY-MM-DD HH24:MI:SS') as upd_dt from mhe_item_barcode order by upd_datetime asc) where rownum <= 1";
			IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheItemBarcode.class);
			latestReceivedAt = wmsQueryMgr.selectBySql(sql, new HashMap<String, Object>(1), String.class);
		}
		
		return latestReceivedAt;
	}
	
	/**
	 * 상품 정보 수신 예정 건수를 조회 1000건 보다 크다면 1000건만 수신 
	 * 
	 * @param domainId
	 * @param latestReceivedAt
	 * @return
	 */
	private int findSkuReceivePlanCount(Long domainId, String latestReceivedAt) {
		
		String sql = "select count(*) from mhe_item_barcode where upd_datetime > to_date(:latestReceivedAt, 'YYYY-MM-DD HH24:MI:SS')";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheItemBarcode.class);
		int planCount = wmsQueryMgr.selectBySql(sql, ValueUtil.newMap("latestReceivedAt", latestReceivedAt), Integer.class);
		return (planCount > 1000) ? 1000 : planCount;
	}
	
	/**
	 * 상품 수신
	 *  
	 * @param event
	 */
	@EventListener(classes = SkuReceiptEvent.class, condition = "#event.receiveType == 'sku' and #event.eventStep == 2")
	public void startToReceive(SkuReceiptEvent event) {

		// 상품 정보 수신
		this.receiveSku(event);
		
		// 이벤트 실행 여부 설정
		event.setExecuted(true);
	}

	/**
	 * 상품 수신
	 * 
	 * @param event
	 */
	private void receiveSku(SkuReceiptEvent event) {
		// 수신 예정 건수가 없다면 수신 기준 시간만 업데이트 ...
		if(event.getPlanCount() == 0) {
			this.recordHistory(event.getDomainId(), event.getLastReceivedAt(), 0, 0);
		// 있다면 상품 수신
		} else {
			this.receive(event.getDomainId(), event.getLastReceivedAt(), event.getPlanCount());
		}
	}
	
	/**
	 * 수신 
	 * 
	 * @param domainId
	 * @param latestReceivedAt
	 * @param planCount
	 */
	private void receive(Long domainId, String latestReceivedAt, int planCount) {
		SkuSyncHist history = new SkuSyncHist();
		history.setCreatorId("job");
		history.setUpdaterId("job");
		history.setCreatedAt(new Date());
		
		String sql = "select * from (select * from mhe_item_barcode where upd_datetime > to_date(:latestReceivedAt, 'YYYY-MM-DD HH24:MI:SS') order by upd_datetime asc) where rownum <= :planCount";
		Map<String, Object> params = ValueUtil.newMap("latestReceivedAt,planCount", latestReceivedAt, planCount);
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheItemBarcode.class);
		List<WmsMheItemBarcode> fromSkuList = wmsQueryMgr.selectListBySql(sql, params, WmsMheItemBarcode.class, 0, 0);
		Map<String, Object> condition = ValueUtil.newMap("domainId,comCd", domainId, "FnF");
		int receivedCount = 0;
		Date latestTime = null;
		
		for(WmsMheItemBarcode fromSku : fromSkuList) {
			condition.put("skuCd", fromSku.getItemCd());
			SKU toSku = this.queryManager.selectByCondition(SKU.class, condition);
			
			if(toSku == null) {
				toSku = new SKU();
				toSku.setDomainId(domainId);
				toSku.setSkuNm(LogisConstants.SPACE);
			}
			
			toSku.setComCd("FnF");
			toSku.setSkuCd(fromSku.getItemCd());
			toSku.setSkuBarcd(fromSku.getBarcode());
			toSku.setBrandCd(fromSku.getBrand());
			toSku.setSeasonCd(fromSku.getItemSeason());
			toSku.setStyleCd(fromSku.getItemStyle());
			toSku.setColorCd(fromSku.getItemColor());
			toSku.setSizeCd(fromSku.getItemSize());
			toSku.setSkuClass(fromSku.getFloorCd());
			toSku.setSkuType(fromSku.getItemGcd());
			toSku.setSkuDesc(fromSku.getItemGcdNm());
			this.queryManager.upsert(toSku);
			
			if(fromSku.getUpdDatetime() != null) {
				latestTime = fromSku.getUpdDatetime();
			}
			
			receivedCount++;
		}
		
		String latestSyncAt = receivedCount > 0 ? DateUtil.dateTimeStr(latestTime) : latestReceivedAt;
		history.setSyncTime(latestSyncAt);
		history.setSyncCnt(receivedCount);
		history.setUpdatedAt(new Date());
		this.queryManager.insert(history);
	}

	/**
	 * 수신 결과 이력 정보 저장
	 * 
	 * @param domainId
	 * @param lastReceivedAt
	 * @param planCount
	 * @param receivedCount
	 */
	private void recordHistory(Long domainId, String lastReceivedAt, int planCount, int receivedCount) {
		
		SkuSyncHist history = null;
		
		if(receivedCount == 0) {
			Query condition = AnyOrmUtil.newConditionForExecution(domainId);
			history = this.queryManager.selectByCondition(SkuSyncHist.class, condition);
		}
		
		if(history == null) {
			history = new SkuSyncHist(lastReceivedAt, receivedCount);
		}
		
		this.queryManager.upsert(history);
	}

}
