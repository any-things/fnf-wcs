package operato.fnf.wcs.service.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.DpsJobInstance;
import operato.fnf.wcs.service.model.DpsOutbWaybill;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class DpsOutbTcdSum extends AbstractQueryService {
	public ResponseObj dpsOutbTcdSum(Map<String, Object> params) throws Exception {
		// FNF_IF.MPS_EXPRESS_WAYBILL_PRINT.OUTB_TCD, 

		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			params.put("date", DateUtil.getCurrentDay());
		}
		
		// outb_tcd 업데이트
		String outbEmptySql = FnfUtils.queryCustServiceWithCheck("board_dps_outb_empty_list");
		List<DpsJobInstance> dpsEmptyOutbs = queryManager.selectListBySql(outbEmptySql, params, DpsJobInstance.class, 0, 1000);
		List<String> waybillNos = new ArrayList<>();
		for (DpsJobInstance obj: dpsEmptyOutbs) {
			waybillNos.add(obj.getWaybillNo());
		}
		
		if (dpsEmptyOutbs.size() > 0) {
			IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
			String byOutbTcdSql = FnfUtils.queryCustServiceWithCheck("board_dps_outb_tcd_summary");
			params.put("waybillNos", waybillNos);
			List<DpsOutbWaybill> sumDpsOutbTcds = wmsQueryMgr.selectListBySql(byOutbTcdSql, params, DpsOutbWaybill.class, 0, 1000);
			
			Map<String, DpsJobInstance> outbTcds = new HashMap<>();
			for (DpsJobInstance obj: dpsEmptyOutbs) {
				outbTcds.put(obj.getWaybillNo(), obj);
			}
			for (DpsOutbWaybill obj: sumDpsOutbTcds) {
				DpsJobInstance jobInstance = outbTcds.get(obj.getWaybillNo());
				jobInstance.setOutbTcd(obj.getOutbTcd());
			}
			
			queryManager.updateBatch(dpsEmptyOutbs, "outbTcd");
			
		}
		
		String byBrandSql = FnfUtils.queryCustServiceWithCheck("board_dps_outb_brand_summary");
		List<DpsOutbWaybill> dpsOutbs = queryManager.selectListBySql(byBrandSql, params, DpsOutbWaybill.class, 0, 10000);
		
		for (DpsOutbWaybill obj: dpsOutbs) {
			String brand = obj.getStrrId();
			obj.setStrrId(brand);
			if ("M".equals(obj.getStrrId())) {
				obj.setStrrNm("MLB");
			} else if ("I".equals(obj.getStrrId())) {
				obj.setStrrNm("MLB Kids");
			} else if ("X".equals(obj.getStrrId())) {
				obj.setStrrNm("Discovery Expedition");
			} else if ("A".equals(obj.getStrrId())) {
				obj.setStrrNm("Stretch Angels");
			} else if ("V".equals(obj.getStrrId())) {
				obj.setStrrNm("Duvetica");
			}
		}
		
		//Map<String, Object> result = new HashMap<>();
		ResponseObj resp = new ResponseObj();
		resp.setItems(dpsOutbs);
		resp.setTotal(dpsOutbs.size());
		return resp;
	}
}
