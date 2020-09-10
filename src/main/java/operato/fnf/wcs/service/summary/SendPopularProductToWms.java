package operato.fnf.wcs.service.summary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.service.model.DpsPopularSku;
import operato.logis.wcs.entity.TopSkuSetting;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class SendPopularProductToWms extends AbstractQueryService {

	public ResponseObj sendPopularProductToWms(Map<String, Object> params) throws Exception {
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			date = DateUtil.getCurrentDay();
		}
		
		ResponseObj resp = BeanUtil.get(CalcPopularProduct.class).calcPopularProduct(params);
		@SuppressWarnings("unchecked")
		List<DpsPopularSku> wcsList = (List<DpsPopularSku>)resp.getItems();
		
		StringJoiner sql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		sql.add("INSERT INTO wcs_imp_dps_repl_buffer_rcmd (");
		sql.add("    interface_crt_dt,");
		sql.add("    interface_no,");
		sql.add("    wh_cd,");
		sql.add("    strr_id,");
		sql.add("    item_cd,");
		sql.add("    safety_day,");
		sql.add("    avg_ship_qty,");
		sql.add("    safety_qty,");
		sql.add("    wcs_need_qty,");
		sql.add("    item_prty,");
		sql.add("    if_crt_id,");
		sql.add("    if_crt_dtm");
		sql.add("  )");
		sql.add("VALUES (");
		sql.add("	:date,");
		sql.add("	SEQ_IMP_DPS_REPL_BUFFER_RCMD.NEXTVAL,");
		sql.add("	:whCd,");
		sql.add("	:strrId,");
		sql.add("	:itemCd,");
		sql.add("	:safetyDay,");
		sql.add("	:avgShipQty,");
		sql.add("	:safetyQty,");
		sql.add("	:wcsNeedQty,");
		sql.add("	:itemPrty,");
		sql.add("	'WMS',");
		sql.add("	:ifCrtDtm");
		sql.add(")");
		
		Query conds = new Query(0, 1);
		conds.addOrder("updatedAt", false);
		TopSkuSetting setting = queryManager.selectByCondition(true, TopSkuSetting.class, conds);
		
		int i = 0;
		for (DpsPopularSku obj: wcsList) {
			if (i >= setting.getTopCount()) {	// 50개만 넘겨줌
				break;
			}
			
			IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("date", DateUtil.getCurrentDay());
			paramMap.put("whCd", "ICF");
			paramMap.put("strrId", " ");
			paramMap.put("itemCd", obj.getSkuCd());
			paramMap.put("safetyDay", obj.getDurationDays());
			paramMap.put("avgShipQty", obj.getScopeAvgPcsQty());
			paramMap.put("safetyQty", obj.getDurationPcs());
			paramMap.put("wcsNeedQty", obj.getNeedPcs());
			paramMap.put("itemPrty", obj.getPopularIndex());
			paramMap.put("ifCrtDtm", DateUtil.getCurrentSecond());
			
			wmsQueryMgr.executeBySql(sql.toString(), paramMap);
			i++;
		}
		
		return new ResponseObj();
	}
}
