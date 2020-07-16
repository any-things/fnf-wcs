package operato.fnf.wcs.service.rfid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.RfidBoxResult;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.ValueUtil;

@Component
public class ShowIfPasdeliverySend extends AbstractQueryService {

	@Autowired
	private DataSourceManager dataSourceMgr;
	public ResponseObj showIfPasdeliverySend(Map<String, Object> params) throws Exception {
		
		IQueryManager wmsQueryMgr = dataSourceMgr.getQueryManager(RfidBoxResult.class);
		
		List<RfidBoxResult> list = new ArrayList<>();
		String serviceSql = FnfUtils.queryCustService("das_if_pasdelivery_send");
		if (ValueUtil.isEmpty(serviceSql)) {
			Query conds = new Query(0, 0);
			conds.addFilter("dtDelivery", String.valueOf(params.get("date")));
			conds.addFilter("tpMachine", 2);
			conds.addOrder("noBox", true);
			list = wmsQueryMgr.selectList(RfidBoxResult.class, conds);
		}
		
		String date = String.valueOf(params.get("date"));
		String sql = "select count(distinct no_box) from rfid_if.if_pasdelivery_send where dt_delivery = :date and tp_machine = '2'";
		Integer boxNoCount = wmsQueryMgr.selectBySql(sql, ValueUtil.newMap("date", date), Integer.class);
		
		//List<RfidBoxItem> list = wmsQueryMgr.selectListBySqlPath("operato/fnf/wcs/service/rfid/send_test.sql", ValueUtil.newMap("date", date), RfidBoxItem.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		resp.setValues(ValueUtil.newMap("boxNoCount", boxNoCount));
		return resp;
	}
}
