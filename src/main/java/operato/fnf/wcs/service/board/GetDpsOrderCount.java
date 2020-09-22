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
public class GetDpsOrderCount extends AbstractLogisService {
	public ResponseObj getDpsOrderCount(Map<String, Object> params) throws Exception {
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			params.put("date", DateUtil.getCurrentDay());
		}
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		
		Integer orderCount = wmsQueryMgr.selectBySql(this.getOrderCntQuery(), params, Integer.class);
		Integer pcsCount = wmsQueryMgr.selectBySql(this.getOrderPcsQuery(), params, Integer.class);
		
		Map<String, Integer> values = new HashMap<>();
		values.put("order_qty", orderCount);
		values.put("pcs_qty", pcsCount);
		ResponseObj resp = new ResponseObj();
		resp.setValues(values);
		return resp;
	}
	
	private String getOrderCntQuery() throws Exception {
//		StringJoiner query = new StringJoiner(SysConstants.LINE_SEPARATOR);
//		
//		query.add("SELECT");
//		query.add("COUNT(DISTINCT ref_no) AS order_qty");
//		query.add("FROM");
//		query.add("  dps_today_performance");
//		query.add("WHERE");
//		query.add("  outb_ect_ymd = :date");
//		return query.toString();
		
		String sql = FnfUtils.queryCustServiceWithCheck("board_dps_order_count");
		
		return sql;
	}
	
	private String getOrderPcsQuery() throws Exception {
//		StringJoiner query = new StringJoiner(SysConstants.LINE_SEPARATOR);
//		
//		query.add("SELECT");
//		query.add("  SUM(ORDER_QTY) AS pcs_qty");
//		query.add("FROM");
//		query.add("  dps_today_performance");
//		query.add("WHERE");
//		query.add("  outb_ect_ymd = :date");
//		
//		return query.toString();
		
		String sql = FnfUtils.queryCustServiceWithCheck("board_dps_pcs_qty");
		
		return sql;
	}
}
