package operato.fnf.wcs.service.summary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.model.VolumnByDay;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class CalcVolumnByDay extends AbstractQueryService {

	public ResponseObj calcVolumnByDay(Map<String, Object> params) throws Exception {
		
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			date = DateUtil.getCurrentDay();
		}
		
		Integer limit = 15;
		try {			
			limit = Integer.parseInt(SettingUtil.getValue("dps.volumn.days.limit"));
		} catch(Exception e) {
			logger.error("CalcVolumnByDay error~~", e);
		}
		
		String dateSql = FnfUtils.queryCustServiceWithCheck("dps_volumn_from_date");
		Map<String, Object> dateParamMap = new HashMap<>();
		dateParamMap.put("date", date);
		dateParamMap.put("limit", limit);
		@SuppressWarnings("unchecked")
		Map<String, String> values = queryManager.selectBySql(dateSql, dateParamMap, HashMap.class);
		String fromDate = values.get("fromdate");
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("fromDate", fromDate);
		paramMap.put("toDate", date);
		
		String volumnSql = FnfUtils.queryCustServiceWithCheck("dps_volumn_by_day");
		List<VolumnByDay> volumns = queryManager.selectListBySql(volumnSql, paramMap, VolumnByDay.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(volumns);
		
		return resp;
	}
}
