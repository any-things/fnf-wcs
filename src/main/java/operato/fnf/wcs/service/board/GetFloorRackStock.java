package operato.fnf.wcs.service.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.model.BoardCellSum;
import operato.fnf.wcs.service.model.BoardRackStock;
import operato.fnf.wcs.service.model.FloorTotalSum;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class GetFloorRackStock extends AbstractLogisService {
	public ResponseObj getFloorRackStock(Map<String, Object> params) throws Exception {
		String floorTcd = String.valueOf(params.get("floorTcd"));	// "3F%"
		String wcellNo = String.valueOf(params.get("wcellNo"));	// "3F%"
		String buildingTcd = String.valueOf(params.get("buildingTcd"));
		String date = String.valueOf(params.get("date"));
		
		if (ValueUtil.isEmpty(date)) {
			params.put("date", DateUtil.getCurrentDay());
		}
		
		String sql = FnfUtils.queryCustServiceWithCheck("board_floor_rack_stock");
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		Map<String, Object> wmsParams = ValueUtil.newMap("floorTcd,wcellNo,buildingTcd,date", floorTcd,wcellNo,buildingTcd,date);
		List<BoardRackStock> list = wmsQueryMgr.selectListBySql(sql, wmsParams, BoardRackStock.class, 0, 0);
		
		Map<String, BoardCellSum> cellMap = new HashMap<>();
		for (BoardRackStock obj: list) {
			BoardCellSum cell = cellMap.get(obj.getLocation());
			if (ValueUtil.isEmpty(cell)) {
				cell = new BoardCellSum();
				cellMap.put(obj.getLocation(), cell);
				cell.setCapacity((float)obj.getSpaceCbm());
				cell.setUsed(obj.getUsedRate());	// 셀 사용율을 함수내에서 계산해줌.
			} else {
				cell.setCapacity(cell.getCapacity() + obj.getSpaceCbm());
				cell.setUsed(cell.getUsed() + obj.getUsedRate());	// 셀 사용율을 함수내에서 계산해줌.
			}
			if (ValueUtil.isNotEmpty(obj.getSkuCd()) && !cell.getSkuCds().contains(obj.getSkuCd())) {
				cell.addSkuCd(obj.getSkuCd());
			}
		}
		
		String skuCntSql = FnfUtils.queryCustServiceWithCheck("board_floor_rack_sku_cnt");
		Float totalSkuCnt = wmsQueryMgr.selectBySql(skuCntSql, wmsParams, Float.class);
		
		String usedRateSql = FnfUtils.queryCustServiceWithCheck("board_floor_rack_used_rate");
		FloorTotalSum floorTotalSum = wmsQueryMgr.selectBySql(usedRateSql, wmsParams, FloorTotalSum.class);
		floorTotalSum.setSkuCount(Long.parseLong(String.valueOf(totalSkuCnt)));
		
		Map<String, Object> values = new HashMap<>();
		values.put("cells", cellMap);
		values.put("floorSum", floorTotalSum);
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		resp.setValues(values);
		return resp;
	}
}
