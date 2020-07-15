package operato.fnf.wcs.service.board;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.service.model.BoardRackStock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class GetRackStock extends AbstractLogisService {
	public ResponseObj getRackStock(Map<String, Object> params) throws Exception {
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		String.valueOf(params.get("floorCd"));
		String.valueOf(params.get("wcellNo"));
		String.valueOf(params.get("buildingTcd"));
		
		String date = String.valueOf(params.get("date"));
//		if (ValueUtil.isEmpty(date)) {
//			params.put("date", DateUtil.getCurrentDay());
//		}
		
		List<BoardRackStock> list = wmsQueryMgr.selectListBySqlPath("operato/fnf/wcs/service/wms/rack_stock.sql", ValueUtil.newMap("date", date), BoardRackStock.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		return resp;
	}
}
