package operato.fnf.wcs.service.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.RfidBoxResult;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@Component
public class DasRfidResult extends AbstractRestService {
	@Autowired
	private DataSourceManager dataSourceMgr;
	
	public ResponseObj dasRfidResult(Map<String, Object> params) throws Exception {
		
		int page = Integer.valueOf(String.valueOf(params.get("page")));
		int limit = Integer.valueOf(String.valueOf(params.get("limit")));
		String sort = String.valueOf(params.get("sort"));
		String select = String.valueOf(params.get("select"));
		String query = String.valueOf(params.get("query"));
		Query queryObj = this.parseQuery(RfidBoxResult.class, page, limit, select, sort, query);
		
		StringJoiner sqlJoiner = new StringJoiner(SysConstants.LINE_SEPARATOR);
		sqlJoiner.add("SELECT");
		sqlJoiner.add("	ps.*,");
		sqlJoiner.add("	pr.qt_delivery");
		sqlJoiner.add("FROM");
		sqlJoiner.add("	if_pasdelivery_send ps,");
		sqlJoiner.add("	if_pasdelivery_recv pr");
		sqlJoiner.add("WHERE");
		sqlJoiner.add("	ps.cd_warehouse = pr.cd_warehouse");
		sqlJoiner.add("	AND ps.ds_batch_no = pr.ds_batch_no"); 
		sqlJoiner.add("	AND ps.no_box = pr.no_box");
		
		Map<String, Object> queryParams = new HashMap<>();
		if (ValueUtil.isNotEmpty(queryObj.getFilter())) {			
			for (Filter filter : queryObj.getFilter()) {
				queryParams.put(filter.getName(), filter.getValue());
				
				sqlJoiner.add(" AND ps." + filter.getName() + " = :" + filter.getName());
			}
		}
		
		sqlJoiner.add(" ORDER BY ps.tp_delivery desc");
		
		IQueryManager wmsQueryMgr = dataSourceMgr.getQueryManager(RfidBoxResult.class);
		Page<RfidBoxResult> resultPage = wmsQueryMgr.selectPageBySql(sqlJoiner.toString(), queryParams, RfidBoxResult.class, page, limit);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(resultPage.getList());
		resp.setTotal(resultPage.getTotalSize());
		return resp;
	}

	@Override
	protected Class<?> entityClass() {
		return RfidBoxResult.class;
	}
}
