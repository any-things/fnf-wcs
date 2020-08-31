package operato.fnf.wcs.service.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.sf.common.util.ValueUtils;
import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.DpsTodayPerformance;
import operato.fnf.wcs.service.model.DpsOrderDetail;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class GetDpsOrderSum extends AbstractLogisService {
	public ResponseObj getDpsOrderSum(Map<String, Object> params) throws Exception {
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			params.put("date", DateUtil.getCurrentDay());
		}
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		
		String sql = FnfUtils.queryCustServiceWithCheck("dps_total_order_detail");
		List<DpsTodayPerformance> dpsOrders = wmsQueryMgr.selectListBySql(sql, params, DpsTodayPerformance.class, 0, 0);
		List<String> skuCds = new ArrayList<>();
		for (DpsTodayPerformance obj: dpsOrders) {
			skuCds.add(obj.getItemCd());
		}
		
		if (ValueUtils.isEmpty(skuCds) || skuCds.size() == 0) {
			return new ResponseObj();
		}
		
		String itemGroupSql = FnfUtils.queryCustServiceWithCheck("dps_order_item_group");
		Map<String, Object> itemGroupParams = new HashMap<>();
		itemGroupParams.put("skuCds", skuCds);
		List<DpsOrderDetail> dpsItemGroups = queryManager.selectListBySql(itemGroupSql, itemGroupParams, DpsOrderDetail.class, 0, 0);
		
		Map<String, DpsOrderDetail> itemGroupMap = new HashMap<>();
		for (DpsOrderDetail obj: dpsItemGroups) {
			DpsOrderDetail itemGroup = itemGroupMap.get(obj.getItemCd());
			if (ValueUtil.isEmpty(itemGroup)) {
				itemGroupMap.put(obj.getItemCd(), obj);
			}
		}
		
		for (DpsTodayPerformance obj: dpsOrders) {
			DpsOrderDetail itemGroup = itemGroupMap.get(obj.getItemCd());
			FnfUtils.populate(itemGroup, obj, false);
		}
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(dpsOrders);
		return resp;
	}
}
