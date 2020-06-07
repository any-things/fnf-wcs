package xyz.anythings.base.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.entity.Stocktaking;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.query.store.StockQueryStore;
import xyz.anythings.base.service.api.IStockService;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 재고 서비스 기본 구현
 * 
 * @author shortstop
 */
@Component
public class StockService extends AbstractLogisService implements IStockService {

	/**
	 * 재고 쿼리 스토어
	 */
	@Autowired
	private StockQueryStore stockQueryStore;
	
	@Override
	public Stock findStock(Long domainId, String cellCd, boolean exceptionWhenEmpty) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.findItem(domainId, exceptionWhenEmpty, Stock.class, sql, "domainId,cellCd", domainId, cellCd);
	}

	@Override
	public Stock findStock(Long domainId, String cellCd, String comCd, String skuCd, boolean exceptionWhenEmpty) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.findItem(domainId, exceptionWhenEmpty, Stock.class, sql, "domainId,cellCd,comCd,skuCd", domainId, cellCd, comCd, skuCd);
	}

	@Override
	public Stock findOrCreateStock(Long domainId, String cellCd) {
		Stock stock = this.findStock(domainId, cellCd, false);
		
		if(stock == null) {
			stock = this.createStock(domainId, cellCd, null, null, null);
		}
		
		return stock;
	}
	
	@Override
	public Stock findOrCreateStock(Long domainId, String cellCd, String comCd, String skuCd) {
		SKU sku = null;
		
		if(ValueUtil.isNotEmpty(skuCd)) {
			sku = AnyEntityUtil.findEntityBy(domainId, true, SKU.class, "id,com_cd,sku_cd,sku_barcd,sku_nm", "comCd,skuCd", comCd, skuCd);
		}
		
		if(sku != null) {
			return this.findOrCreateStock(domainId, cellCd, sku);
			
		} else {
			Stock stock = this.findStock(domainId, cellCd, false);
			
			if(stock == null) {
				stock = this.createStock(domainId, cellCd, comCd, skuCd, null);
			}
			
			if(ValueUtil.isNotEmpty(skuCd)) {
				stock.setComCd(comCd);
				stock.setSkuCd(skuCd);
			}
			
			return stock;			
		}
	}
	
	@Override
	public Stock findOrCreateStock(Long domainId, String cellCd, SKU sku) {
		Stock stock = this.findStock(domainId, cellCd, false);
		
		if(stock == null) {
			stock = this.createStock(domainId, cellCd, sku.getComCd(), sku.getSkuCd(), sku.getSkuNm());
			
		} else {
			if(ValueUtil.isEmpty(stock.getSkuCd())) {
				stock.setComCd(sku.getComCd());
				stock.setSkuCd(sku.getSkuCd());
				stock.setSkuBarcd(sku.getSkuBarcd());
				stock.setSkuNm(sku.getSkuNm());
			}
		}
		
		return stock;
	}
	
	@Override
	public Stock createStock(Long domainId, String cellCd, SKU sku) {
		Cell cell = AnyEntityUtil.findEntityBy(domainId, true, Cell.class, null, "domainId,cellCd", domainId, cellCd);
		Stock stock = new Stock();
		stock.setComCd(sku.getComCd());
		stock.setSkuCd(sku.getSkuCd());
		stock.setSkuNm(sku.getSkuNm());
		stock.setSkuBarcd(sku.getSkuBarcd());
		stock.setCellCd(cellCd);
		stock.setEquipType(cell.getEquipType());
		stock.setEquipCd(cell.getEquipCd());
		stock.setActiveFlag(cell.getActiveFlag());
		stock.setLoadQty(0);
		stock.setAllocQty(0);
		stock.setStockQty(0);
		stock.setPickedQty(0);
		stock.setMinStockQty(0);
		stock.setMaxStockQty(0);
		stock.setLastTranCd(Stock.TRX_CREATE);
		this.queryManager.insert(stock);
		
		return stock;
	}

	@Override
	public Stock createStock(Long domainId, String cellCd, String comCd, String skuCd, String skuNm) {
		SKU sku = null;
		
		if(ValueUtil.isEmpty(skuNm)) {
			sku = AnyEntityUtil.findEntityBy(domainId, true, SKU.class, "id,com_cd,sku_cd,sku_barcd,sku_nm", "comCd,skuCd", comCd, skuCd);
			
		} else {
			sku = new SKU();
			sku.setComCd(comCd);
			sku.setSkuCd(skuCd);
		}
		
		return this.createStock(domainId, cellCd, sku);
	}

	@Override
	public Stock addStock(Stock stock, String tranCd, int addQty) {
		tranCd = ValueUtil.isEmpty(tranCd) ? Stock.TRX_IN : tranCd;
		stock.setLastTranCd(tranCd);
		stock.setLoadQty(ValueUtil.toInteger(stock.getLoadQty()) + addQty);
		stock.setStockQty(stock.getLoadQty() + stock.getAllocQty());
		this.queryManager.upsert(stock, "comCd", "skuCd", "skuBarcd", "skuNm", "lastTranCd", "loadQty", "stockQty", "updaterId", "updatedAt");
		return stock;
	}

	@Override
	public Stock removeStock(Stock stock, String tranCd, int removeQty) {
		tranCd = ValueUtil.isEmpty(tranCd) ? Stock.TRX_OUT : tranCd;
		stock.setLastTranCd(tranCd);
		stock.setLoadQty(ValueUtil.toInteger(stock.getLoadQty()) - removeQty);
		stock.setStockQty(stock.getLoadQty() + stock.getAllocQty());
		this.queryManager.upsert(stock, "lastTranCd", "loadQty", "stockQty", "updaterId", "updatedAt");
		return stock;
	}
	
	@Override
	public Stock adjustStock(Long domainId, String tranCd, String cellCd, String comCd, String skuCd, int adjustQty) {
		Stock stock = this.findStock(domainId, cellCd, comCd, skuCd, false);
		if(stock == null) {
			stock = this.findStock(domainId, cellCd, true);
		}
		
		tranCd = ValueUtil.isEmpty(tranCd) ? Stock.TRX_ADJUST : tranCd;
		stock.setLastTranCd(tranCd);
		stock.setLoadQty(ValueUtil.toInteger(stock.getLoadQty()) + adjustQty);
		stock.setStockQty(stock.getLoadQty() + stock.getAllocQty());
		this.queryManager.update(stock, "comCd", "skuCd", "skuBarcd", "skuNm", "lastTranCd", "loadQty", "stockQty", "updaterId", "updatedAt");
		return stock;
	}
	
	@Override
	public Stock supplyStock(Long domainId, String cellCd, SKU sku, int loadQty) {
		Stock stock = this.findOrCreateStock(domainId, cellCd);
		
		if(ValueUtil.isEmpty(stock.getSkuCd()) || ValueUtil.isEqualIgnoreCase(stock.getSkuCd(), sku.getSkuCd())) {
			stock.setComCd(sku.getComCd());
			stock.setSkuCd(sku.getSkuCd());
			stock.setSkuNm(sku.getSkuNm());
		}
		
		stock.setLoadQty(ValueUtil.toInteger(stock.getLoadQty()) + loadQty);
		this.queryManager.update(stock, "comCd", "skuCd", "skuBarcd", "skuNm", "loadQty", "updaterId", "updatedAt");
		return stock;
	}
	
	@Override
	public List<Stock> searchStocksBySku(Long domainId, String equipType, String equipCd, String comCd, String skuCd) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, "domainId,equipType,equipCd,comCd,skuCd", domainId, equipType, equipCd, comCd, skuCd);
	}
	
	@Override
	public List<Stock> searchStocksBySku(Long domainId, String equipType, String equipCd, Boolean fixedFlag, String comCd, String skuCd) {
		String sql = this.stockQueryStore.getSearchStocksQuery();
		return AnyEntityUtil.searchItems(domainId, false, Stock.class, sql, "domainId,equipType,equipCd,comCd,skuCd", domainId, equipType, equipCd, comCd, skuCd);
	}

	@Override
	public List<Stock> searchRecommendCells(Long domainId, String equipType, String equipCd, String comCd, String skuCd, Boolean fixedFlag) {
		// 1. 조회 조건
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("cellCd", "fixedFlag");
		condition.addFilter("equipType", equipType);
		condition.addFilter("comCd", comCd);
		condition.addFilter("skuCd", skuCd);
		
		if(equipCd != null) {
			condition.addFilter("equipCd", equipCd);
		}
		
		if(fixedFlag != null) {
			condition.addFilter("fixedFlag", fixedFlag);
		}
		
		return this.queryManager.selectList(Stock.class, condition);
	}
	
	@Override
	public Stock calcuateOrderStock(Stock stock) {
		// 1. 고정식인 경우 
		if(stock.getFixedFlag() != null && stock.getFixedFlag()) {
			stock.setStockQty(stock.getMaxStockQty() - stock.getLoadQty() - stock.getAllocQty());
			return stock;
			
		// 2. 자유식인 경우 
		} else {
			EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(stock.getDomainId(), stock.getEquipType(), stock.getEquipCd());
			JobBatch batch = equipBatchSet.getBatch();
			return this.calculateSkuOrderStock(stock.getDomainId(), batch.getId(), stock.getEquipType(), null, stock.getComCd(), stock.getSkuCd());
		}
	}
	
	@Override
	public int calcSkuInputQty(String batchId, Stock stock) {
		String sql = this.stockQueryStore.getCalcSkuSupplementQtyQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,equipCd,cellCd,comCd,skuCd", stock.getDomainId(), batchId, stock.getEquipType(), stock.getEquipCd(), stock.getCellCd(), stock.getComCd(), stock.getSkuCd());
		Stock stockStatus = this.queryManager.selectBySql(sql, params, Stock.class);
		return stockStatus.getInputQty();
	}
	
	@Override
	public Stock calculateSkuOrderStock(Long domainId, String batchId, String equipType, String equipCd, String comCd, String skuCd) {
		String sql = this.stockQueryStore.getCalcSkuSupplementQtyQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,equipCd,comCd,skuCd", domainId, batchId, equipType, equipCd, comCd, skuCd);
		return this.queryManager.selectBySql(sql, params, Stock.class);
	}

	@Override
	public boolean toggleLedSettingForStock(Long domainId, boolean on, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int ledOnShortageStocks(Long domainId, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ledOffByEquip(Long domainId, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Stock adjustStock(Long domainId, String stocktakingId, String indCd, int fromQty, int toQty) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void startStocktaking(Long domainId, String equipType, List<String> equipCdList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startStocktaking(Long domainId, String today, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishStocktaking(Long domainId, String stocktakingId) {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public Stocktaking findLatestStocktaking(Long domainId, String date, String equipType, String equipCd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeStockForPicking(Long domainId, String equipType, String equipCd, String cellCd, String comCd, String skuCd, int pickQty) {
		// 1. Lock 을 걸고 재고 조회 
		Stock stock = AnyEntityUtil.findEntityBy(domainId, true, true, Stock.class, null, "equipType,equipCd,cellCd,comCd,skuCd", equipType, equipCd, cellCd, comCd, skuCd);
		stock.setAllocQty(stock.getAllocQty() - pickQty);
		stock.setPickedQty(stock.getPickedQty() + pickQty);
		
		if(stock.getAllocQty() < 0) {
			stock.setAllocQty(0);
		}
		
		// 2. 재고 업데이트 
		this.queryManager.update(stock, "allocQty", "pickedQty", LogisConstants.ENTITY_FIELD_UPDATED_AT);		
	}

}
