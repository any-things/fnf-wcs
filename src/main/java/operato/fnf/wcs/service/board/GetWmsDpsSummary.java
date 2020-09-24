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
		Map<String, Object> wmsDpsSum = (Map<String, Object>) wmsQueryMgr.selectBySql(wmsSql, params, HashMap.class);
		
		Integer donePcsQty = Integer.parseInt(String.valueOf(wmsDpsSum.get("done_pcs_qty")));
		Integer orderPcsQty = Integer.parseInt(String.valueOf(wmsDpsSum.get("order_pcs_qty")));
		Integer doneOrderCnt = Integer.parseInt(String.valueOf(wmsDpsSum.get("done_order_cnt")));
		Integer totalOrderCnt = Integer.parseInt(String.valueOf(wmsDpsSum.get("total_order_cnt")));
		
		// WCS 합포수량 조회
		String wcsSql = FnfUtils.queryCustServiceWithCheck("board_wcs_dps_summary");
		@SuppressWarnings("unchecked")
		Map<String, Object> wcsDpsSum = (Map<String, Object>) wmsQueryMgr.selectBySql(wcsSql, params, HashMap.class);
//		Integer hDonePcsQty = wcsDpsSum.get("h_done_pcs_qty");
//		Integer hOrderPcsQty = wcsDpsSum.get("h_order_pcs_qty");
		Integer hDoneOrderCnt = Integer.parseInt(String.valueOf(wcsDpsSum.get("h_done_order_cnt")));
		Integer hTotalOrderCnt = Integer.parseInt(String.valueOf(wcsDpsSum.get("h_total_order_cnt")));
		
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
