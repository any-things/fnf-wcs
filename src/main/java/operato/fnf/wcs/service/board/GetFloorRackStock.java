package operato.fnf.wcs.service.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WmsCellUseRate;
import operato.fnf.wcs.service.model.BoardCellSum;
import operato.fnf.wcs.service.model.BoardRackStock;
import operato.fnf.wcs.service.model.FloorTotalSum;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class GetFloorRackStock extends AbstractLogisService {
	public ResponseObj getFloorRackStock(Map<String, Object> params) throws Exception {
		String buildingTcd = String.valueOf(params.get("buildingTcd"));
		String floorTcd = String.valueOf(params.get("floorTcd"));	// "3F%"
		String brand = String.valueOf(params.get("brand"));
		String itemCd = String.valueOf(params.get("itemCd"));
		String itemGcd = String.valueOf(params.get("itemGcd"));
		String itemNm = String.valueOf(params.get("itemNm"));
		String season = String.valueOf(params.get("season"));
		String color = String.valueOf(params.get("color"));
		String style = String.valueOf(params.get("style"));
		String size = String.valueOf(params.get("size"));
		String assortYn = String.valueOf(params.get("assortYn"));
		String assortCd = String.valueOf(params.get("assortCd"));
		
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		Map<String, Object> wmsParams = ValueUtil.newMap("building_tcd,floor_tcd", buildingTcd,floorTcd);
		if (ValueUtil.isNotEmpty(brand) && !"undefined".equals(brand)) {
			wmsParams.put("brand", brand);
		}
		if (ValueUtil.isNotEmpty(itemCd) && !"undefined".equals(itemCd)) {
			wmsParams.put("item_cd", itemCd);
		}
		if (ValueUtil.isNotEmpty(itemGcd) && !"undefined".equals(itemGcd)) {
			wmsParams.put("item_gcd", itemGcd);
		}
		if (ValueUtil.isNotEmpty(itemNm) && !"undefined".equals(itemNm)) {
			wmsParams.put("item_nm", itemNm);
		}
		if (ValueUtil.isNotEmpty(season) && !"undefined".equals(season)) {
			wmsParams.put("season", season);
		}
		if (ValueUtil.isNotEmpty(color) && !"undefined".equals(color)) {
			wmsParams.put("color", color);
		}
		if (ValueUtil.isNotEmpty(style) && !"undefined".equals(style)) {
			wmsParams.put("style", style);
		}
		if (ValueUtil.isNotEmpty(size) && !"undefined".equals(size)) {
			wmsParams.put("size", size);
		}
		if (ValueUtil.isNotEmpty(assortYn) && !"undefined".equals(assortYn)) {
			wmsParams.put("assort_yn", assortYn);
		}
		if (ValueUtil.isNotEmpty(assortCd) && !"undefined".equals(assortCd)) {
			wmsParams.put("assort_cd", assortCd);
		}
		
		String stockDetailSql = FnfUtils.queryCustServiceWithCheck("board_floor_rack_stock");	// detail
		List<BoardRackStock> stockDetailList = wmsQueryMgr.selectListBySql(stockDetailSql, wmsParams, BoardRackStock.class, 0, 0);
		
		Map<String, BoardCellSum> cellMap = new HashMap<>();
		
		for (BoardRackStock obj: stockDetailList) {
			BoardCellSum cell = cellMap.get(obj.getLocation());
			if (ValueUtil.isEmpty(cell)) {
				cell = new BoardCellSum();
				cellMap.put(obj.getLocation(), cell);
				cell.setLocation(obj.getLocation());
				cell.setCapacity((float)obj.getSpaceCbm());
				cell.setUsed((float)obj.getUsedCbm());
				cell.setUsedRate(cell.getUsed()/cell.getCapacity() * 100);
				
				cell.setErpSaleRate(Math.max(cell.getErpSaleRate(), obj.getErpSaleRate()));
				cell.setCellPcsQty(obj.getPcsQty());
				String velocity = cell.getVelocity().compareTo(obj.getVelocity()) <= 0 ? obj.getVelocity() : cell.getVelocity();	// 출고빈도
				cell.setVelocity(velocity);
			} else {
				cell.setCellPcsQty(cell.getCellPcsQty() + obj.getPcsQty());
				cell.setUsed(cell.getUsed() + (float)obj.getUsedCbm());
				cell.setUsedRate(cell.getUsed()/cell.getCapacity() * 100);
			}
			
			if (ValueUtil.isNotEmpty(obj.getItemCd()) && !cell.getItems().contains(obj)) {
				cell.addItems(obj);
			} else {
				obj.setBrand(" ");
				obj.setSeason(" ");
				obj.setStyle(" ");
				obj.setColor(" ");
				obj.setSize(" ");
				obj.setPcsQty(0f);
				obj.setBoxQty(0f);
				
				cell.addItems(obj);
			}
		}
		
		String skuCntSql = FnfUtils.queryCustServiceWithCheck("board_floor_rack_sum");	// 층 summary
		FloorTotalSum floorTotalSum = wmsQueryMgr.selectBySql(skuCntSql, wmsParams, FloorTotalSum.class);
		
		Query conds = new Query();
		conds.addFilter("buildingTcd", buildingTcd);
		conds.addFilter("floorTcd", floorTcd);
		List<WmsCellUseRate> cellUseRates = wmsQueryMgr.selectList(WmsCellUseRate.class, conds);
		for (WmsCellUseRate obj: cellUseRates) {
			if ("A".equals(obj.getRackType())) {
				floorTotalSum.setArackUsedRate(obj.getRtUseLoc());
			} else if ("P".equals(obj.getRackType())) {
				floorTotalSum.setPrackUsedRate(obj.getRtUseLoc());
			}
		}
		
//		String ifAddData = SettingUtil.getValue("board.rack.add.data");	// 가상데이터 스위치
//		if ("Y".equalsIgnoreCase(ifAddData)) {			
//			List<String> emptyCells = new ArrayList<>();
//			
//			this.getVirtualData(buildingTcd, floorTcd, emptyCells);
//			// 빈셀
//			
//			// 가상데이터랑 실제 데이터를 merge
//			for (String cellNo: emptyCells) {
//				BoardCellSum cellSum = cellMap.get(cellNo);
//				cellSum.setLocation(cellNo);
////				cellSum.setUsed(used);
////				cellSum.setCellItemCount();
////				cellSum.setUsedRate(usedRate);
//			}
//		}
		
		Map<String, Object> values = new HashMap<>();
		values.put("cellData", cellMap.values());
		values.put("floorSum", floorTotalSum);
		
		ResponseObj resp = new ResponseObj();
		resp.setValues(values);
		return resp;
	}
	
//	private void getVirtualData(String buildingTcd, String floorTcd, List<String> emptyCells) {
//		//sumData: usedRate, skuCount, 
//		
//		//List<WcsCell> wcsCell = 
//	}
}
