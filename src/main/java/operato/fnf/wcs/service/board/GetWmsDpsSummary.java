package operato.fnf.wcs.service.board;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class GetWmsDpsSummary extends AbstractLogisService {
	public ResponseObj getWmsDpsSummary(Map<String, Object> params) throws Exception {
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			params.put("date", DateUtil.getCurrentDay());
		}
		
		String wmsSql = FnfUtils.queryCustServiceWithCheck("board_wms_dps_summary");
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		@SuppressWarnings("unchecked")
		Map<String, Integer> wmsDpsSum = (Map<String, Integer>) wmsQueryMgr.selectListBySql(wmsSql, params, Map.class, 0, 0);
		
		Integer donePcsQty = wmsDpsSum.get("done_pcs_qty");
		Integer orderPcsQty = wmsDpsSum.get("order_pcs_qty");
		Integer doneOrderCnt = wmsDpsSum.get("done_order_cnt");
		Integer totalOrderCnt = wmsDpsSum.get("total_order_cnt");
		
		// WCS 합포수량 조회
		String wcsSql = FnfUtils.queryCustServiceWithCheck("board_wcs_dps_summary");
		@SuppressWarnings("unchecked")
		Map<String, Integer> wcsDpsSum = (Map<String, Integer>) wmsQueryMgr.selectListBySql(wcsSql, params, Map.class, 0, 0);
//		Integer hDonePcsQty = wcsDpsSum.get("h_done_pcs_qty");
//		Integer hOrderPcsQty = wcsDpsSum.get("h_order_pcs_qty");
		Integer hDoneOrderCnt = wcsDpsSum.get("h_done_order_cnt");
		Integer hTotalOrderCnt = wcsDpsSum.get("h_total_order_cnt");
		
		// WMS 전체수량 - WCS 합포수량 = 단포수량 
//		Integer dDonePcsQty = donePcsQty - hDonePcsQty;
//		Integer dOrderPcsQty = doneOrderCnt - hOrderPcsQty;
		Integer dDoneOrderCnt = doneOrderCnt - hDoneOrderCnt;
		Integer dTotalOrderCnt = totalOrderCnt - hTotalOrderCnt;
		
		Map<String, Object> result = new HashMap<>();
		result.put("done_pcs_qty", donePcsQty);
		result.put("total_pcs_qty", orderPcsQty);
		result.put("done_order_qty", doneOrderCnt);
		result.put("total_order_qty", totalOrderCnt);
		result.put("multi_done_order_qty", hDoneOrderCnt);
		result.put("multi_total_order_qty", hTotalOrderCnt);
		result.put("single_done_order_qty", dDoneOrderCnt);
		result.put("single_total_order_qty", dTotalOrderCnt);
		
		result.put("done_order_rate", doneOrderCnt/totalOrderCnt * 100);
		result.put("done_pcs_rate", donePcsQty/orderPcsQty * 100);
		result.put("multi_done_rate", hDoneOrderCnt/hTotalOrderCnt * 100);
		result.put("single_done_rate", dDoneOrderCnt/dTotalOrderCnt * 100);
		result.put("bar_single_done_rate", dDoneOrderCnt/dTotalOrderCnt * 100);
		
		ResponseObj resp = new ResponseObj();
		resp.setValues(result);
		return resp;
	}
}
