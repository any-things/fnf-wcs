package operato.fnf.wcs.service.summary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.model.RecommandSku;
import operato.logis.wcs.entity.TopSkuSetting;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.DateUtil;

@Component
public class DpsLastdayPopularProduct extends AbstractQueryService {
	public ResponseObj calcPopularProduct(Map<String, Object> params) throws Exception {
		
		String date = DateUtil.currentDate();

		Query conds = new Query(0, 1);
		conds.addOrder("updatedAt", false);
		TopSkuSetting setting = queryManager.selectByCondition(true, TopSkuSetting.class, conds);
		
		String dateSql = FnfUtils.queryCustServiceWithCheck("dps_sku_out_last_date");
		Map<String, Object> dateParamMap = new HashMap<>();
		dateParamMap.put("date", date);
		dateParamMap.put("limit", setting.getScopeDays());
		@SuppressWarnings("unchecked")
		Map<String, String> values = queryManager.selectBySql(dateSql, dateParamMap, HashMap.class);

		Map<String, Object> paramMap = new HashMap<>();
		String fromDate = values.get("fromdate");
		paramMap.put("fromDate", fromDate);
		paramMap.put("toDate", date);
		String sql = FnfUtils.queryCustServiceWithCheck("dps_outb_lastday_sku_sum");
		List<RecommandSku> skuSums = queryManager.selectListBySql(sql, paramMap, RecommandSku.class, 0, setting.getTopCount());
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(skuSums);
		return resp;
	}
}
