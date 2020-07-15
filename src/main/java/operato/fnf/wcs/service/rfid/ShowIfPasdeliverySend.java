package operato.fnf.wcs.service.rfid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	public ResponseObj showIfPasdeliverySend(Map<String, Object> params) {
		
		IQueryManager wmsQueryMgr = dataSourceMgr.getQueryManager(RfidBoxResult.class);
		
		Query conds = new Query(0, 1000);
		conds.addFilter("dtDelivery", String.valueOf(params.get("date")));
		conds.addFilter("tpMachine", 2);
		conds.addOrder("noBox", true);
		List<RfidBoxResult> list = wmsQueryMgr.selectList(RfidBoxResult.class, conds);
		
		String date = String.valueOf(params.get("date"));
		String sql = "select count(distinct no_box) from rfid_if.if_pasdelivery_send where dt_delivery = :date and tp_machine = '2'";
		Integer boxNoCount = wmsQueryMgr.selectBySql(sql, ValueUtil.newMap("date", date), Integer.class);
		
		//List<RfidBoxItem> list = wmsQueryMgr.selectListBySqlPath("operato/fnf/wcs/service/wms/send_test.sql", ValueUtil.newMap("date", date), RfidBoxItem.class, 0, 0);
		
		int i = wmsQueryMgr.selectSizeBySql("SELECT\r\n" + 
				"              ds_batch_no,\r\n" + 
				"              cd_brand,\r\n" + 
				"              no_box,\r\n" + 
				"              MAX(result_st) AS result_st\r\n" + 
				"            FROM\r\n" + 
				"              rfid_if.if_pasdelivery_send\r\n" + 
				"            WHERE\r\n" + 
				"              dt_delivery = '20200713'\r\n" + 
				"            GROUP BY\r\n" + 
				"              ds_batch_no,\r\n" + 
				"              cd_brand,\r\n" + 
				"              no_box", null);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		resp.setValues(ValueUtil.newMap("boxNoCount, maxcount", boxNoCount, i));
		return resp;
	}
}
