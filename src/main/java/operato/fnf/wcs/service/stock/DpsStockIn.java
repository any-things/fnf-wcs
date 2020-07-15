package operato.fnf.wcs.service.stock;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.api.ISkuSearchService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class DpsStockIn extends StockInSearch {
	
	@Autowired
	private ISkuSearchService skuSearchService;
	
	public ResponseObj dpsStockIn(Map<String, ?> params) throws Exception {
		String rackCd = String.valueOf(params.get("rackCd"));
		String cellCd = String.valueOf(params.get("cellCd"));
		String comCd = String.valueOf(params.get("comCd"));
		String skuCd = String.valueOf(params.get("skuCd"));
		Integer inputQty = Integer.valueOf(String.valueOf(params.get("inputQty")));
		
		FnfUtils.checkValueEmpty("랙번호", rackCd, "셀번호", cellCd, "회사코드", comCd, "제품바코드", skuCd, "입력수량", inputQty);
		
		Long curDomainId = Domain.currentDomainId();
		JobBatch rackBatch = super.getRackBatch(rackCd);
		boolean hasRackBatch = false;
		if (ValueUtil.isNotEmpty(rackBatch)) {
			hasRackBatch = true;
		}
		
		ResponseObj resp = new ResponseObj();
				
		Query conds = new Query();
		conds.addFilter("domainId", curDomainId);
		conds.addFilter("cellCd", cellCd);
		Stock stock = queryManager.selectByCondition(Stock.class, conds);
		
		SKU sku = this.skuSearchService.findSku(curDomainId, comCd, skuCd, true);	// TODO check
		
		if (hasRackBatch) {	// 작업중: free랙에만 재고보충할수 있음.
			
			if (ValueUtil.isEmpty(stock)) {
				stock = new Stock();
				stock.setDomainId(curDomainId);
				stock.setComCd(sku.getComCd());
				stock.setCellCd(cellCd);
			}
			
			if (ValueUtil.isNotEmpty(stock.getFixedFlag()) && stock.getFixedFlag()) {
				throw ThrowUtil.newValidationErrorWithNoLog("고정셀은 작업도중에 보충할수 없습니다.");
			}
			
			if (ValueUtil.isEmpty(stock.getSkuCd())) {
				stock.setComCd(sku.getComCd());
				stock.setSkuCd(sku.getSkuCd());
				stock.setSkuBarcd(sku.getSkuBarcd());
				stock.setSkuNm(sku.getSkuNm());
				
			} else if(ValueUtil.isNotEqual(stock.getSkuCd(), sku.getSkuCd())) {
				// 재고의 상품 정보와 sku의 상품 정보가 다른 경우 재고 수량이 존재하지 않으면 sku 정보로 재고 설정 
				if(ValueUtil.toInteger(stock.getAllocQty(), 0) == 0 && ValueUtil.toInteger(stock.getLoadQty(), 0) == 0) {
					stock.setComCd(sku.getComCd());
					stock.setSkuCd(sku.getSkuCd());
					stock.setSkuBarcd(sku.getSkuBarcd());
					stock.setSkuNm(sku.getSkuNm());
				} else {
					throw ThrowUtil.newValidationErrorWithNoLog("해당 재고에 다른 상품 재고가 존재합니다.");
				}
			}
		} else {	// 작업종료상태: 고정랙에만 재고보충할수 있음.
			if(ValueUtil.isEmpty(stock) || stock.getFixedFlag() == null || !stock.getFixedFlag()) {
				throw ThrowUtil.newValidationErrorWithNoLog("고정셀 보충만 가능합니다. 고정셀을 입력하세요.");
			}
			
			if(ValueUtil.isNotEmpty(stock.getSkuCd()) && ValueUtil.isNotEqual(stock.getSkuCd(), sku.getSkuCd())) {
				throw ThrowUtil.newValidationErrorWithNoLog("해당 셀에 다른 상품 재고가 존재합니다.");
			}
			
			if(ValueUtil.isNotEmpty(stock.getMaxStockQty()) && stock.getMaxStockQty() > 0 && (ValueUtil.toInteger(stock.getLoadQty(), 0) + inputQty) > stock.getMaxStockQty()) {
				throw ThrowUtil.newValidationErrorWithNoLog("최대 적치 수량을 초과해서 적치 할 수 없습니다.");
			}
			
			stock.setComCd(sku.getComCd());
			stock.setSkuCd(sku.getSkuCd());
			stock.setSkuBarcd(sku.getSkuBarcd());
			stock.setSkuNm(sku.getSkuNm());
		}
		
		stock.addStock(inputQty);	// "comCd", "skuCd", "skuBarcd", "skuNm", "lastTranCd", "loadQty", "stockQty"
		
		Map<String, Object> values = new HashMap<>();
		values = FnfUtils.objectToMap(stock);
		values.put("hasRackBatch", hasRackBatch);
		resp.setValues(values);
		
		return resp;
	}
}
