package operato.fnf.wcs.service.stock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.SkuSearchService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class SkuForStockSearch extends StockInSearch {
	// client에서 현재랙에 batch_id가 있는지 여부를 알아야 함.
	
	public ResponseObj searchSkuForStock(Map<String, ?> params) throws Exception {
		
		String rackCd = String.valueOf(params.get("rackCd"));
		String skuCd = String.valueOf(params.get("skuCd"));
		
		if (ValueUtil.isEmpty(rackCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog("랙을 선택해 주세요");
		}
		if (ValueUtil.isEmpty(skuCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog("상품 바코드를 입력해 주세요");
		}
		
		JobBatch rackBatch = super.getRackBatch(rackCd);
		boolean hasRackBatch = false;
		if (ValueUtil.isNotEmpty(rackBatch)) {
			hasRackBatch = true;
		}
		
		ResponseObj resp = new ResponseObj();
		Map<String, Object> values = new HashMap<>();
		values.put("hasRackBatch", hasRackBatch);
		
		Long curDomainId = Domain.currentDomainId();
		if (hasRackBatch) {
			Rack rack = AnyEntityUtil.findEntityBy(curDomainId, true, Rack.class, null, "rackCd", rackCd);
			if (ValueUtil.isEmpty(rack.getId())) {
				throw ThrowUtil.newValidationErrorWithNoLog("랙에 할당된 배치가 없습니다.");
			}
			if (!rack.getBatchId().equals(rackBatch.getId())) {
				throw ThrowUtil.newValidationErrorWithNoLog("랙에 할당된 배치와 배치의 랙정보가 일치하지 않습니다.");
			}
			
			List<SKU> skuList = BeanUtil.get(SkuSearchService.class).searchList(rackBatch, skuCd);
			resp.setItems(skuList);
		} else {
			String itemMasterTb = SettingUtil.getValue(curDomainId, "fnf.item_barcode.table.name", "mhe_item_barcode");
			String qry = "select brand as brand_cd, item_color as color_cd, 'FnF' as com_cd, item_size as size_cd, barcode as sku_barcd, item_cd as sku_cd, item_gcd_nm as sku_nm, item_style as style_cd from " + itemMasterTb + " where (item_cd = :skuCd or barcode = :skuCd or barcode2 = :skuCd)";
			List<SKU> skuList = this.queryManager.selectListBySql(qry, ValueUtil.newMap("skuCd", skuCd), SKU.class, 0, 0);
		
			if(ValueUtil.isEmpty(skuList)) {
				List<String> terms = ValueUtil.toList(MessageUtil.getTerm("terms.label.sku", "SKU"), skuCd);
				throw ThrowUtil.newValidationErrorWithNoLog(true, SysMessageConstants.NOT_FOUND, terms);
			}
			
			resp.setItems(skuList);
		}
		
		return resp;
	}
}
