package operato.fnf.wcs.service.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
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
		// TODO FNF_IF.MPS_EXPRESS_WAYBILL_PRINT.OUTB_TCD, 

		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			params.put("date", DateUtil.getCurrentDay());
		}
		
		String byBrandSql = FnfUtils.queryCustServiceWithCheck("board_dps_outb_brand_summary");
		List<DpsOutbWaybill> dpsOutbs = queryManager.selectListBySql(byBrandSql, params, DpsOutbWaybill.class, 0, 10000);
		
		List<String> waybillNos = new ArrayList<>();
		List<DpsOutbWaybill> dpsOutbTcds = new ArrayList<>();
		for (int i = 0; i < dpsOutbs.size(); i++) {
			DpsOutbWaybill obj = dpsOutbs.get(i);
			waybillNos.add(obj.getWaybillNo());
			
			if (((float)waybillNos.size()) % 1000 == 0 || i == dpsOutbs.size() - 1) {
				IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
				String byOutbTcdSql = FnfUtils.queryCustServiceWithCheck("board_dps_outb_tcd_summary");
				params.put("waybillNos", waybillNos);
				List<DpsOutbWaybill> sumDpsOutbTcds = wmsQueryMgr.selectListBySql(byOutbTcdSql, params, DpsOutbWaybill.class, 0, 10000);
				
				dpsOutbTcds.addAll(sumDpsOutbTcds);
				waybillNos = new ArrayList<>();
			}			
		}
		
		if (dpsOutbTcds.size() == 0) {
			return new ResponseObj();
		}
		
		Map<String, DpsOutbWaybill> outbTcds = new HashMap<>();
		for (DpsOutbWaybill obj: dpsOutbTcds) {
			outbTcds.put(obj.getWaybillNo(), obj);
		}
		
		for (DpsOutbWaybill obj: dpsOutbs) {
			DpsOutbWaybill dpsOutbTcd = outbTcds.get(obj.getWaybillNo());
			String brand = dpsOutbTcd.getStrrId();
			obj.setStrrId(brand);
			obj.setOutbTcd(dpsOutbTcd.getOutbTcd());
		}
		
		//Map<String, Object> result = new HashMap<>();
		ResponseObj resp = new ResponseObj();
		resp.setItems(dpsOutbs);
		resp.setTotal(dpsOutbs.size());
		return resp;
	}
}
