package operato.fnf.wcs.service.stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.api.IStockService;
import xyz.elidom.sys.entity.Domain;
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
		Stock stock = null;
		if (hasRackBatch) {
			stocks = this.stockService.searchRecommendCells(domainId, equipType, null, comCd, skuCd, null);
			
			stock = this.stockService.calculateSkuOrderStock(domainId, rackBatch.getId(), equipType, null, comCd, skuCd);
			if (ValueUtil.isNotEmpty(stock)) {
				values.put("orderQty", stock.getOrderQty());
				
				if(stock.getOrderQty() > 0) {
					if (stock.getInputQty() == 0) {
						throw ThrowUtil.newValidationErrorWithNoLog("해당 제품은 이미 주문의 필요수량만큼 보충되었습니다.");
					}
					
					stock.setEquipType(equipType);
					stock.setEquipCd(equipCd);
					stock.setOrderQty(stock.getOrderQty() - stock.getAllocQty() - stock.getStockQty());
					
					values.put("orderQty", stock.getOrderQty() - stock.getAllocQty() - stock.getStockQty());
					values.put("stockQty", stock.getStockQty());
					values.put("inputQty", stock.getInputQty());
					values.put("allocQty", stock.getAllocQty());
				}
			}
		} else {
			stocks = this.stockService.searchRecommendCells(Domain.currentDomainId(), equipType, null, comCd, skuCd, true);
//			if (ValueUtil.isEmpty(stocks)) {
//				throw ThrowUtil.newValidationErrorWithNoLog("고정셀이 없습니다.");
//			}
			
			stock = this.stockService.calculateSkuOrderStock(domainId, null, equipType, null, comCd, skuCd);
			
			values.put("stockQty", stock.getStockQty());
		}
		
		resp.setItems(stocks);
		resp.setValues(values);
		
		return resp;
	}
	
	
}
