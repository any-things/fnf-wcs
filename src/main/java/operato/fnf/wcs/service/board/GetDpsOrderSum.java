package operato.fnf.wcs.service.board;

import java.util.ArrayList;
import java.util.Arrays;
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
			date = DateUtil.getCurrentDay();
			params.put("date", date);
		}
		
		List<String> brands = null;
		if (ValueUtil.isNotEmpty(params.get("brand"))) {
			brands = Arrays.asList(String.valueOf(params.get("brand")).split(","));
			params.put("brand", brands);
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
		
//		String orderSumParams = FnfUtils.queryCustServiceWithCheck("dps_today_order_summary");
//		List<DpsOrderDetail> orderSums = queryManager.selectListBySql(orderSumParams, params, DpsOrderDetail.class, 0, 0);
		
		String itemGroupSql = FnfUtils.queryCustServiceWithCheck("dps_order_item_group");
//		Map<String, Object> itemGroupParams = new HashMap<>();
//		itemGroupParams.put("skuCds", skuCds);
		params.put("skuCds", skuCds);
		List<DpsOrderDetail> dpsItemGroups = queryManager.selectListBySql(itemGroupSql, params, DpsOrderDetail.class, 0, 0);
		
		Map<String, DpsOrderDetail> itemGroupMap = new HashMap<>();
		for (DpsOrderDetail obj: dpsItemGroups) {
			DpsOrderDetail itemGroup = itemGroupMap.get(obj.getItemCd());
			if (ValueUtil.isEmpty(itemGroup)) {
				itemGroupMap.put(obj.getItemCd(), obj);
			}
		}
		
		for (DpsTodayPerformance obj: dpsOrders) {
			DpsOrderDetail detail = itemGroupMap.get(obj.getItemCd());
			if (ValueUtil.isNotEmpty(detail)) {
				obj.setItemSeason(detail.getItemGroup());
			}
		}
		
		String orderCntSql = FnfUtils.queryCustServiceWithCheck("dps_today_order_cnt_by_pack_type");
		List<DpsOrderDetail> orderCntByPackTypes = wmsQueryMgr.selectListBySql(orderCntSql, params, DpsOrderDetail.class, 0, 0);
		
		orderCntSql = FnfUtils.queryCustServiceWithCheck("dps_today_order_cnt_by_brand");
		List<DpsOrderDetail> orderCntByBrands = wmsQueryMgr.selectListBySql(orderCntSql, params, DpsOrderDetail.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		Map<String, List<DpsOrderDetail>> values = new HashMap<>();
//		values.put("orderSums", orderSums);
		values.put("orderCntByPackTypes", orderCntByPackTypes);
		values.put("orderCntByBrands", orderCntByBrands);
		resp.setValues(values);
		resp.setItems(dpsOrders);
		return resp;
	}
	
	
}
