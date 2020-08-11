package operato.fnf.wcs.service.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WmsOdpsZoneInv;
import operato.fnf.wcs.service.model.RecommandSku;
import operato.logis.wcs.entity.TopSkuSetting;
import operato.logis.wcs.entity.TopSkuTrace;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

//http://localhost:9500/rest/wcs/calc_recommand_sku?query=%5B%7B%22name%22%3A%22sum_date%22%2C%22operator%22%3A%22eq%22%2C%22value%22%3A%2220200810%22%7D%5D&page=1&limit=50
@Component
public class CalcRecommandSku extends AbstractQueryService {
	public ResponseObj calcRecommandSku(Map<String, Object> params) throws Exception {
		Map<String, Object> kvParams = FnfUtils.parseQueryParamsToMap(TopSkuTrace.class, params);
		
		String date = String.valueOf(kvParams.get("sum_date"));
		if (ValueUtil.isEmpty(date)) {
			date = DateUtil.getCurrentDay();
		}
		
		Query conds = new Query(0, 1);
		conds.addOrder("updatedAt", false);
		TopSkuSetting setting = queryManager.selectByCondition(true, TopSkuSetting.class, conds);
		
		Query traceConds = new Query(0, setting.getTopCount());
		traceConds.addFilter("sumDate", date);
		traceConds.addOrder("popularIndex", false);
		List<TopSkuTrace> topSkuTraces = queryManager.selectList(TopSkuTrace.class, traceConds);
		List<String> skuCds = new ArrayList<>();
		for (TopSkuTrace obj: topSkuTraces) {
			skuCds.add(obj.getSkuCd());
		}
		
		if (ValueUtil.isEmpty(skuCds) || skuCds.size() == 0) {
			return new ResponseObj();
		}
		
		Map<String, Integer> wcsStockMap = this.getSkuWcsStocks(skuCds);
		//Map<String, Integer> wmsStockMap = this.getSkuWmsStocks(skuCds);
		List<RecommandSku> recommandSkus = new ArrayList<>();
		for (TopSkuTrace obj: topSkuTraces) {
			//obj.getDurationPcs() < 현재재고수량
			Integer wcsSkuStockQty = wcsStockMap.get(obj.getSkuCd());
			if (ValueUtil.isEmpty(wcsSkuStockQty)) {
				wcsSkuStockQty = 0;
			}
//			Integer wmsSkuStockQty = wmsStockMap.get(obj.getSkuCd());
//			if (ValueUtil.isEmpty(wmsSkuStockQty)) {
//				wmsSkuStockQty = 0;
//			}
//			
//			Integer needQty = 0;
//			if (obj.getDurationPcs() < wcsSkuStockQty && obj.getScopeAvgPcsQty() > wcsSkuStockQty) {
//				needQty = Math.round(obj.getScopeAvgPcsQty()) - wcsSkuStockQty;
//				
//				if (wmsSkuStockQty < needQty ) {
//					needQty = wmsSkuStockQty;
//				}
//			}
			Integer needQty = Math.round(obj.getScopeAvgPcsQty() * obj.getDurationDays() - wcsSkuStockQty);
			RecommandSku rsku = FnfUtils.populate(obj, new RecommandSku(), true);
			rsku.setNeedPcsQty(needQty);
			recommandSkus.add(rsku);
		}
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(recommandSkus);
		return resp;
	}
	
	@SuppressWarnings("unused")
	private Map<String, Integer> getSkuWmsStocks(List<String> skuCds) throws Exception {
		Query wmsConds = new Query(0, 1);
		wmsConds.addFilter("whCd", FnFConstants.WH_CD_ICF);
		wmsConds.addFilter("itemCd", "in", skuCds);
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsOdpsZoneInv.class);
		List<WmsOdpsZoneInv> zoneInvs = wmsQueryMgr.selectList(WmsOdpsZoneInv.class, wmsConds);
		
		Map<String, Integer> map = new HashMap<>();
		for (WmsOdpsZoneInv obj: zoneInvs) {
			map.put(obj.getItemCd(), obj.getInvnQty());
		}
		
		return map;
	}
	
	private Map<String, Integer> getSkuWcsStocks(List<String> skuCds) throws Exception {
		//Query conds = AnyOrmUtil.newConditionForExecution(Domain.currentDomainId());
		//condition.addFilter("equipType", equipType);
		//condition.addFilter("comCd", comCd);
		//conds.addFilter("skuCd", "in", skuCds);
		//condition.addFilter("fixedFlag", true);
		//condition.addOrder("loadQty", true);
		//List<Stock> stocks = queryManager.selectList(Stock.class, conds);
		
		
		String sql = FnfUtils.queryCustServiceWithCheck("dps_wcs_stock_sum");
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("skuCds", skuCds);
		List<Stock> stocks = queryManager.selectListBySql(sql, paramMap, Stock.class, 0, 0);
		
		Map<String, Integer> map = new HashMap<>();
		for (Stock obj: stocks) {
			map.put(obj.getSkuCd(), obj.getStockQty());
		}
		
		return map;
	}
}
