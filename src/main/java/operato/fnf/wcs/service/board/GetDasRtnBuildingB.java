package operato.fnf.wcs.service.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.model.DasRtnBuildingB;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;

@Component
public class GetDasRtnBuildingB extends AbstractLogisService {
	public ResponseObj getDasRtnBuildingB(Map<String, Object> params) throws Exception {
		String sumSql = FnfUtils.queryCustServiceWithCheck("board_das_rtn_building_b");
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		@SuppressWarnings("unchecked")
		Map<String, Object> rtnSum = (Map<String, Object>) wmsQueryMgr.selectBySql(sumSql, params, HashMap.class);
		
		String dailySql = FnfUtils.queryCustServiceWithCheck("board_das_rtn_daily_building_b");
		List<DasRtnBuildingB> rtnDailies = wmsQueryMgr.selectListBySql(dailySql, params, DasRtnBuildingB.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		resp.setValues(rtnSum);
		resp.setItems(rtnDailies);
		return resp;
	}
}
