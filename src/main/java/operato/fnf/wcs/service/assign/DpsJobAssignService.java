package operato.fnf.wcs.service.assign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.query.store.FnFDpsQueryStore;
import operato.fnf.wcs.service.model.DpsJobAssign;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 할당 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsJobAssignService extends AbstractQueryService {

	/**
	 * Event Publisher
	 */
	@Autowired
	protected ApplicationEventPublisher eventPublisher;
	/**
	 * DPS Query Store
	 */
	@Autowired
	private FnFDpsQueryStore dpsQueryStore;
	
	/**
	 * 작업 배치 별 작업 할당 처리
	 * 
	 * @param domain
	 * @param batch
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void assignBatchJobs(Domain domain, JobBatch batch) {
		// 1. 작업 배치 내 모든 재고 중에 가장 많은 재고 순으로 상품별 재고 수량 조회
		List<Stock> stockList = this.searchStocksForAssign(batch);

		// 2. 재고가 없다면 스킵
		if(ValueUtil.isEmpty(stockList)) {
			return;
		}
		
		// 3. 트랜잭션 분리를 위해 자신을 레퍼런스, 트랜잭션 분리는 각 주문 단위 ...
		DpsJobAssignService dpsJobAssignSvc = BeanUtil.get(DpsJobAssignService.class);
		List<String> skipOrderList = this.searchSkipOrders(batch);
		
		// 4. 배치 내 SKU가 적치된 재고 수량을 기준으로 많은 재고 조회
		for(Stock stock : stockList) {
			// 4.1 현재 시점에 특정 상품의 할당 가능한 재고 총 수량 계산
			Integer stockQty = this.calcTotalStockQty(batch, stock);
			
			// 4.2 재고 총 수량 체크 
			if(stockQty == null || stockQty < 1) {
				continue;
			}
			
			// 4.3 재고의 상품이 필요한 주문번호 검색
			List<Order> orders = this.searchOrdersForStock(batch, stock, stockQty, skipOrderList);
			
			// 4.4 할당이 필요한 주문번호가 없다면 스킵
			if(ValueUtil.isEmpty(orders)) {
				continue;
			}
			
			// 4.5 할당이 필요한 주문 번호별로 ...
			for(Order order : orders) {
				// 4.5.1 남은 재고 수량이 주문 수량보다 적으면 해당 상품에 대한 주문 할당 처리는 종료하면서 처리 못하는 주문 번호 리스트에 추가
				if(stockQty < order.getOrderQty()) {
					skipOrderList.add(order.getOrderNo());
					break;
				}
								
				// 4.5.2 해당 주문 별로 주문별 상품별 가용 재고 조회
				List<DpsJobAssign> candidates = this.searchAssignableCandidates(batch, order.getOrderNo());
				
				// 4.5.3 가용 재고가 없으면 스킵 
				if(ValueUtil.isEmpty(candidates)) {
					continue;
				}
				
				// 4.5.4 주문별 상품별 가용 재고 조회 할당 여부 판별 후 할당
				try {
					stockQty = dpsJobAssignSvc.assignJobs(stock, order, stockQty, candidates, skipOrderList);
				} catch(Exception e) {
					ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_ASSIGN_ERROR", e, null, true, true);
					this.eventPublisher.publishEvent(errorEvent);
				}
			}
		}
	}

	/**
	 * 현재 시점에 특정 상품의 할당 가능한 재고 총 수량 계산
	 * 
	 * @param batch
	 * @param stock
	 * @return
	 */
	private Integer calcTotalStockQty(JobBatch batch, Stock stock) {
		String sql = "SELECT SUM(S.LOAD_QTY) FROM STOCKS S WHERE S.DOMAIN_ID = :domainId AND S.EQUIP_CD = :equipCd AND S.EQUIP_TYPE = :equipType AND S.SKU_CD = :skuCd AND S.ACTIVE_FLAG = :activeFlag AND (S.LOAD_QTY IS NOT NULL AND S.LOAD_QTY > 0)";
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,skuCd,activeFlag", batch.getDomainId(), batch.getEquipType(), batch.getEquipCd(), stock.getSkuCd(), true);
		return this.queryManager.selectBySql(sql, params, Integer.class);
	}
	
	/**
	 * 작업 배치 내 모든 재고 중에 가장 많은 재고 순으로 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<Stock> searchStocksForAssign(JobBatch batch) {
		String sql = this.dpsQueryStore.getStocksForJobAssign();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,equipCd,activeFlag", batch.getDomainId(), batch.getId(), batch.getEquipType(), batch.getEquipCd(), true);
		return this.queryManager.selectListBySql(sql, params, Stock.class, 0, 0);
	}
	
	/**
	 * 해당 재고가 필요한 주문 조회
	 * 
	 * @param batch
	 * @param stock
	 * @param stockQty
	 * @param skipOrderList
	 * @return
	 */
	private List<Order> searchOrdersForStock(JobBatch batch, Stock stock, int stockQty, List<String> skipOrderList) {
		String sql = this.dpsQueryStore.getSearchOrderForStock();
		Map<String, Object> params = ValueUtil.newMap("batchId,skuCd,stockQty,skipOrderIdList", batch.getId(), stock.getSkuCd(), stockQty, skipOrderList.isEmpty() ? null : skipOrderList);
		return this.queryManager.selectListBySql(sql, params, Order.class, 0, 0);
	}
	
	/**
	 * 브랜드 믹스 주문 조회 
	 * 
	 * @param batch
	 * @return
	 */
	private List<String> searchSkipOrders(JobBatch batch) {
		// 1. 브랜드 합포 주문 번호 조회
		String sql = "SELECT REF_NO FROM MPS_BRAND_MIX_REF_NO WHERE WH_CD = :whCd";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		Map<String, Object> params = ValueUtil.newMap("whCd,batchId", "ICF", batch.getId());
		List<String> orderNoList = wmsQueryMgr.selectListBySql(sql, params, String.class, 0, 0);

		// 2. 브랜드 합포 주문 중에 MLB, MLB Kids 두 개의 주문이 병합이 되었는지 체크
		if(ValueUtil.isNotEmpty(orderNoList)) {
			// 브랜드 합포(MLB, MLB Kids 두 개의 브랜드)이면서 현재 두 브랜드의 배치 정보가 모두 진행 중인지 체크하여 둘 중 하나라도 없으면 스킵
			sql = "select order_no from (select ref_no as order_no, count(distinct(strr_id)) as cnt from mhe_dr where wh_cd = :whCd and work_unit = :batchId and dps_assign_yn = 'N' and ref_no in (:orderNoList) group by ref_no) a where a.cnt = 1";
			return this.queryManager.selectListBySql(sql, params, String.class, 0, 0);
			
		} else {
			return new ArrayList<String>();
		}
	}
		
	/**
	 * 작업 할당에 필요한 주문 및 재고 조합 정보 조회
	 *  
	 * @param batch
	 * @param orderNo
	 * @return
	 */
	private List<DpsJobAssign> searchAssignableCandidates(JobBatch batch, String orderNo) {
		String sql = this.dpsQueryStore.getSearchAssignCandidates();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipGroupCd,equipType,equipCd,orderNo", batch.getDomainId(), batch.getId(), batch.getEquipGroupCd(), batch.getEquipType(), batch.getEquipCd(), orderNo);
		return this.queryManager.selectListBySql(sql, params, DpsJobAssign.class, 0, 0);
	}
	
	/**
	 * 주문별 작업 할당 처리
	 * 
	 * @param stock
	 * @param order
	 * @param stockQty
	 * @param skipOrderList
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private int assignJobs(Stock stock, Order order, int stockQty, List<DpsJobAssign> candidates, List<String> skipOrderList) {
		// 1. 주문별 주문 수량 초기화
		int orderQty = 0;
		
		// 2. 주문 할당 대상별로 ...
		for(DpsJobAssign candidate : candidates) {
			// 2.1 할당할 수 있는 수량이 아니면 스킵
			if(candidate.getCheckAssignable() != 0) {
				skipOrderList.add(candidate.getOrderNo());
				break;
			}
			
			// 2.2 주문 라인 내 첫 번째 순위인 경우에 
			if(candidate.getRanking() == 1) {
				orderQty = order.getOrderQty();
				
				// 주문 내 상품 정보가 stock의 상품 정보와 같은 경우는 stockQty를 업데이트
				if(ValueUtil.isEqual(candidate.getSkuCd(), stock.getSkuCd())) {
					stockQty = stockQty - orderQty;
				}
			}

			// 2.3 최종 작업 할당 ...
			this.assignJob(candidate, stockQty, orderQty, skipOrderList);
		}
		
		// 3. 작업 할당 후 남은 재고 수량
		return stockQty;
	}
	
	/**
	 * 주문별 주문 라인별 작업 할당 처리 
	 * 
	 * @param candidate
	 * @param stockQty
	 * @param orderQty
	 * @param skipOrderList
	 * @return
	 */
	public void assignJob(DpsJobAssign candidate, int stockQty, int orderQty, List<String> skipOrderList) {
		// 1. 할당 수량 초기화 
		int assignQty = (orderQty > candidate.getLoadQty()) ? candidate.getLoadQty() : orderQty;
		
		// 2. MHE_DR 데이터에 작업 할당 처리
		String sql = "UPDATE MHE_DR SET STATUS = 'A', DPS_ASSIGN_YN = 'Y', DPS_ASSIGN_AT = now(), CELL_CD = :cellCd WHERE WORK_UNIT = :batchId AND REF_NO = :orderNo AND ITEM_CD = :skuCd AND (DPS_ASSIGN_YN IS NULL OR DPS_ASSIGN_YN = 'N') AND (STATUS IS NULL OR STATUS = '')";
		Map<String, Object> params = ValueUtil.newMap("batchId,orderNo,skuCd,cellCd", candidate.getBatchId(), candidate.getOrderNo(), candidate.getSkuCd(), candidate.getCellCd());
		int exeCnt = this.queryManager.executeBySql(sql, params);
		
		// 3. 재고 업데이트
		if(exeCnt > 0) {
			Stock s = AnyEntityUtil.findEntityByIdWithLock(false, Stock.class, candidate.getStockId(), "id", "equip_type", "equip_cd", "cell_cd", "com_cd", "sku_cd", "alloc_qty", "load_qty");
			if(s != null) {
				s.setLastTranCd(Stock.TRX_ASSIGN);
				s.setAllocQty(ValueUtil.toInteger(s.getAllocQty()) + assignQty);
				s.setLoadQty(ValueUtil.toInteger(s.getLoadQty()) - assignQty);
				if(s.getAllocQty() < 0) s.setAllocQty(0);
				if(s.getLoadQty() < 0) s.setLoadQty(0);
				this.queryManager.update(s, "lastTranCd", "allocQty", "loadQty", "updatedAt");
			}
		}
	}

}
