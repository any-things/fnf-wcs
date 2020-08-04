package operato.fnf.wcs.service.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.service.model.BoardCellSum;
import operato.fnf.wcs.service.model.BoardRackStock;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class GetFloorRackStock extends AbstractLogisService {
	public ResponseObj getRackStock(Map<String, Object> params) throws Exception {
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		String floorCd = String.valueOf(params.get("floorCd"));
		String wcellNo = String.valueOf(params.get("wcellNo"));
		String buildingTcd = String.valueOf(params.get("buildingTcd"));
		String date = String.valueOf(params.get("date"));
		
//		if (ValueUtil.isEmpty(date)) {
//			params.put("date", DateUtil.getCurrentDay());
//		}
		
		List<BoardRackStock> list = wmsQueryMgr.selectListBySqlPath("operato/fnf/wcs/service/wms/rack_stock.sql", 
				ValueUtil.newMap("floorCd,wcellNo,buildingTcd,date", floorCd,wcellNo,buildingTcd,date), BoardRackStock.class, 0, 0);
		
		Map<String, BoardCellSum> cellMap = new HashMap<>();
		for (BoardRackStock obj: list) {
			BoardCellSum cell = cellMap.get(obj.getLocation());
			if (ValueUtil.isEmpty(cell)) {
				cell = new BoardCellSum();
				cellMap.put(obj.getLocation(), cell);
			}
			if (ValueUtil.isNotEmpty(obj.getSkuCd()) && !cell.getSkuCds().contains(obj.getSkuCd())) {
				cell.addSkuCd(obj.getSkuCd());
			}
			cell.setCapacity(cell.getCapacity() + obj.getSpaceCbm());
			cell.setUsed(cell.getUsed() + obj.getUsedRate());	// 셀 사용율을 계산해줌.
		}
		
		Map<String, Object> values = new HashMap<>();
		values.put("cells", cellMap.values());
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		resp.setValues(values);
		return resp;
	}
}
