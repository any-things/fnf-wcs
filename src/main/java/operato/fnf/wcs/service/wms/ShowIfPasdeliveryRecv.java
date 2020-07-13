package operato.fnf.wcs.service.wms;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.RfidBoxItem;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;

@Component
public class ShowIfPasdeliveryRecv extends AbstractQueryService {

	@Autowired
	private DataSourceManager dataSourceMgr;
	public ResponseObj showIfPasdeliveryRecv(Map<String, Object> params) {
		
		IQueryManager wmsQueryMgr = dataSourceMgr.getQueryManager(RfidBoxItem.class);
		
		Query conds = new Query(0, 1000);
		conds.addFilter("dtDelivery", String.valueOf(params.get("date")));
		List<RfidBoxItem> list = wmsQueryMgr.selectList(RfidBoxItem.class, conds);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		return resp;
	}
}
