package operato.fnf.wcs.service.test;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ValidationException;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WmsMheDr;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.ValueUtil;

@Component
public class WmsExcuteSql extends AbstractQueryService {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResponseObj queryWmsData(Map<String, Object> params) throws Exception {
		
		String sql = FnfUtils.queryCustService("wms_excute_sql");
		if (ValueUtil.isEmpty(sql)) {
			throw new ValidationException("커스텀 서비스 [wms_excute_sql]가 존재하지 않습니다.");
		}
		
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheDr.class);
		wmsQueryMgr.executeBySql(sql, new HashMap());
		
		ResponseObj resp = new ResponseObj();
		return resp;
	}
}
