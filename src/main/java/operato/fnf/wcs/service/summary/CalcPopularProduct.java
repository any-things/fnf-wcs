package operato.fnf.wcs.service.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.model.OnlineOutSkuSum;
import operato.logis.wcs.entity.TopSkuSetting;
import operato.logis.wcs.entity.TopSkuTrace;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class CalcPopularProduct extends AbstractQueryService {
	public ResponseObj calcPopularProduct(Map<String, Object> params) throws Exception {
		
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			date = DateUtil.getCurrentDay();
		}
		
		Query conds = new Query(0, 1);
		conds.addOrder("updatedAt", false);
		TopSkuSetting setting = queryManager.selectByCondition(true, TopSkuSetting.class, conds);
		
		String dateSql = FnfUtils.queryCustServiceWithError("sku_out_from_date");
		Map<String, Object> dateParamMap = new HashMap<>();
		dateParamMap.put("date", date);
		dateParamMap.put("limit", setting.getScopeDays());
		@SuppressWarnings("unchecked")
		Map<String, String> values = queryManager.selectBySql(dateSql, dateParamMap, HashMap.class);

		Map<String, Object> paramMap = new HashMap<>();
		String fromDate = values.get("fromdate");
		paramMap.put("fromDate", fromDate);
		paramMap.put("toDate", date);
		String sql = FnfUtils.queryCustServiceWithError("online_out_sku_sum");
		List<OnlineOutSkuSum> skuSums = queryManager.selectListBySql(sql, paramMap, OnlineOutSkuSum.class, 0, 0);
		
		Map<String, TopSkuTrace> skuSumMap = new HashMap<>();
		for(OnlineOutSkuSum obj: skuSums) {
			TopSkuTrace trace = skuSumMap.get(obj.getItemCd());
			if (ValueUtil.isEmpty(trace)) {
				trace = new TopSkuTrace();
				skuSumMap.put(obj.getItemCd(), trace);
			}
			
			FnfUtils.populate(setting, trace, false);
			trace.setId(null);
			trace.setSkuCd(obj.getItemCd());
			trace.setSumDate(date);
			
			Integer pcsQty = trace.getScopeDaysPcsQty();
			Integer skuCnt = trace.getScopeDaysSkuCnt();
			Integer ordCnt = trace.getScopeDaysOrdCnt();
			if (ValueUtil.isEmpty(pcsQty)) {
				pcsQty = 0;
			}
			pcsQty += obj.getOutPcsQty();
			
			if (ValueUtil.isEmpty(skuCnt)) {
				skuCnt = 0;
			}
			skuCnt += obj.getOutSkuTimes();
			
			if (ValueUtil.isEmpty(ordCnt)) {
				ordCnt = 0;
			}
			ordCnt += obj.getOutSkuTimes();
			
			trace.setScopeDaysPcsQty(pcsQty);
			trace.setScopeDaysSkuCnt(skuCnt);
			trace.setScopeDaysOrdCnt(ordCnt);
		}
		
		List<TopSkuTrace> traces = new ArrayList<>();
		for (String skuCd: skuSumMap.keySet()) {
			TopSkuTrace obj = skuSumMap.get(skuCd);
			obj.setScopeAvgPcsQty(obj.getScopeDaysPcsQty()/setting.getScopeDays());
			
			float index = obj.getScopeDaysPcsQty() * setting.getOutbQtyRate()/100 + obj.getScopeDaysSkuCnt() * setting.getOutbDaysRate()/100;
			obj.setPopularIndex(index);
			traces.add(obj);
		}
		
		queryManager.insertBatch(traces);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(traces);
		return resp;
	}
}
