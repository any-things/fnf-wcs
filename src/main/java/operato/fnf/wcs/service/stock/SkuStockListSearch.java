package operato.fnf.wcs.service.stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WmsOdpsZoneInv;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.api.IStockService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class SkuStockListSearch extends StockInSearch {
	@Autowired
	private IStockService stockService;
	
	public ResponseObj skuStockListSearch(Map<String, ?> params) throws Exception {
		String equipType = String.valueOf(params.get("equipType"));
		String equipCd = String.valueOf(params.get("equipCd"));
		String comCd = String.valueOf(params.get("comCd"));
		String skuCd = String.valueOf(params.get("skuCd"));
		
		if (ValueUtil.isEmpty(equipCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog("랙을 선택해 주세요");
		}
		if (ValueUtil.isEmpty(skuCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog("상품 바코드를 입력해 주세요");
		}
		
		JobBatch rackBatch = super.getRackBatch(equipCd);
		boolean hasRackBatch = false;
		if (ValueUtil.isNotEmpty(rackBatch)) {
			hasRackBatch = true;
		}
		
		ResponseObj resp = new ResponseObj();
		Map<String, Object> values = new HashMap<>();
		values.put("hasRackBatch", hasRackBatch);
		values.put("orderQty", 0);
		values.put("stockQty", 0);
		values.put("inputQty", 0);
		values.put("allocQty", 0);
		
		Long domainId = Domain.currentDomainId();
		List<Stock> stocks = new ArrayList<>();
		
		Query conds = new Query(0, 1);
		conds.addFilter("name", "dps.stock.odps.check");
		Setting setting = queryManager.selectByCondition(Setting.class, conds);
		
		boolean wmsStockCheck = false;
		if (ValueUtil.isNotEmpty(setting) && "Y".equalsIgnoreCase(setting.getValue())) {
			wmsStockCheck = true;
		}
		
		if (hasRackBatch) {
			stocks = this.stockService.searchRecommendCells(domainId, equipType, null, comCd, skuCd, false);
			
			Stock stock = null;
//			if (wmsStockCheck) {
//				stock = this.calculateSkuOrderStock(skuCd, stocks, true);
//			} else {
				stock = this.stockService.calculateSkuOrderStock(domainId, rackBatch.getId(), equipType, null, comCd, skuCd);
//			}
			if (ValueUtil.isNotEmpty(stock)) {
				if (stock.getInputQty() == 0) {
					throw ThrowUtil.newValidationErrorWithNoLog("이제품은 이미 필요수량만큼 보충되었습니다.");
				}
				
				values.put("orderQty", stock.getOrderQty());
				if(stock.getOrderQty() > 0) {
					stock.setEquipType(equipType);
					stock.setEquipCd(equipCd);
					//stock.setOrderQty(stock.getOrderQty() - stock.getAllocQty() - stock.getStockQty());
					
					values.put("orderQty", stock.getOrderQty());
					values.put("stockQty", stock.getStockQty());
					values.put("inputQty", stock.getInputQty());
					values.put("allocQty", stock.getAllocQty());
				}
			}
		} else {
			Query condition = AnyOrmUtil.newConditionForExecution(domainId);
			condition.addFilter("equipType", equipType);
			condition.addFilter("comCd", comCd);
			condition.addFilter("skuCd", skuCd);
			condition.addFilter("fixedFlag", true);
			condition.addOrder("loadQty", true);
			
			stocks = queryManager.selectList(Stock.class, condition);
//			stocks = this.stockService.searchRecommendCells(Domain.currentDomainId(), equipType, null, comCd, skuCd, true);
//			if (ValueUtil.isEmpty(stocks)) {
//				throw ThrowUtil.newValidationErrorWithNoLog("고정셀이 없습니다.");
//			}
			
			//stock = this.stockService.calculateSkuOrderStock(domainId, null, equipType, null, comCd, skuCd);
			Stock stock = this.calculateSkuOrderStock(skuCd, stocks, wmsStockCheck);
			if (stock.getInputQty() == 0 && wmsStockCheck) {
				throw ThrowUtil.newValidationErrorWithNoLog("이제품은 이미 필요수량만큼 보충되었습니다.");
			}
			
			if (ValueUtil.isNotEmpty(stock)) {
				values.put("orderQty", stock.getOrderQty());
				values.put("stockQty", stock.getStockQty());
				values.put("inputQty", stock.getInputQty());
			}
		}
		
		resp.setItems(stocks);
		resp.setValues(values);
		
		return resp;
	}
	
	private Stock calculateSkuOrderStock(String skuCd, List<Stock> stocks, boolean wmsStockCheck) throws Exception {
		// query wms odps inventory
//		Query conds = new Query(0, 1);
//		conds.addFilter("whCd", FnFConstants.WH_CD_ICF);
//		conds.addFilter("itemCd", skuCd);
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsOdpsZoneInv.class);
		//WmsOdpsZoneInv zoneInv = wmsQueryMgr.selectByCondition(WmsOdpsZoneInv.class, conds);
		String sql = "select * from dps_inventory where wh_cd = :whCd and item_cd = :itemCd";
		WmsOdpsZoneInv zoneInv = wmsQueryMgr.selectBySql(sql, ValueUtil.newMap("whCd,itemCd", FnFConstants.WH_CD_ICF, skuCd), WmsOdpsZoneInv.class);
		if (wmsStockCheck && ValueUtil.isEmpty(zoneInv) 
				|| ValueUtil.isEmpty(zoneInv.getInvnQty()) || zoneInv.getInvnQty() == 0) {
			throw ThrowUtil.newValidationErrorWithNoLog("이제품은 재고가 없습니다.");
		}
		
		Integer allowQty = 0;
		if (ValueUtil.isNotEmpty(zoneInv)) {
			allowQty = zoneInv.getInvnQty();
		}
		Integer sumLoadQty = 0;
		for (Stock obj: stocks) {
			// 작업 완료후 보충하므로, 실제 재고 수량은 적치수량이랑 같음.
			if (ValueUtil.isNotEmpty(obj.getLoadQty())) {
//				if (wmsStockCheck) {
//					allowQty -= obj.getLoadQty();
//				}
				sumLoadQty += obj.getLoadQty();
			}			
		}
		
		Stock stock = new Stock();
		stock.setOrderQty(allowQty);
		stock.setStockQty(sumLoadQty);
		stock.setInputQty(allowQty - sumLoadQty);
		
		return stock;
	}
}
