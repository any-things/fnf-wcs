package operato.fnf.wcs.service.board;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.model.BoardRackStock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;

@Component
public class GetStockDataByFloor extends AbstractLogisService {
	public ResponseObj getStockDataByFloor(Map<String, Object> params) throws Exception {

		String sql = FnfUtils.queryCustServiceWithCheck("board_floor_stock_sum");
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		List<BoardRackStock> list = wmsQueryMgr.selectListBySql(sql, params, BoardRackStock.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		return resp;
	}
}
