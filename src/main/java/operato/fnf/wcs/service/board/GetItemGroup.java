package operato.fnf.wcs.service.board;

import java.util.List;
import java.util.Map;

import operato.fnf.wcs.entity.WmsItemGroup;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;

public class GetItemGroup extends AbstractLogisService {
	public ResponseObj getItemGroup(Map<String, Object> params) throws Exception {
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		List<WmsItemGroup> list = wmsQueryMgr.selectList(WmsItemGroup.class, new Query());
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(list);
		return resp;
	}
}
