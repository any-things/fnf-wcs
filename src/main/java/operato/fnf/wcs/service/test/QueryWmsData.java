package operato.fnf.wcs.service.test;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ValidationException;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WmsMheDr;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.ValueUtil;

@Component
public class QueryWmsData extends AbstractQueryService {
	public ResponseObj queryWmsData(Map<String, Object> params) throws Exception {
		
		String sql = FnfUtils.queryCustService("query_wms_data");
		if (ValueUtil.isEmpty(sql)) {
			throw new ValidationException("커스텀 서비스 [query_wms_data]가 존재하지 않습니다.");
		}
		
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheDr.class);
		
		Page<HashMap> page = wmsQueryMgr.selectPageBySql(sql, new HashMap<>(), HashMap.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(page.getList());
		return resp;
	}
}
