package operato.fnf.wcs.service.board;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;

@Component
public class GetDasBuildingB {
	public ResponseObj getDasBuildingB(Map<String, Object> params) throws Exception {
		String sumSql = FnfUtils.queryCustServiceWithCheck("board_das_building_b");
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		@SuppressWarnings("unchecked")
		Map<String, Object> rtnSum = (Map<String, Object>) wmsQueryMgr.selectBySql(sumSql, params, HashMap.class);
	
		
		ResponseObj resp = new ResponseObj();
		resp.setValues(rtnSum);
		return resp;
	}
}
