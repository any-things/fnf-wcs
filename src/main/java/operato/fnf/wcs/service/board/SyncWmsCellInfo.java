package operato.fnf.wcs.service.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WcsCell;
import operato.fnf.wcs.entity.WmsCellList;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.util.BeanUtil;

@Component
public class SyncWmsCellInfo extends AbstractLogisService {
	public ResponseObj syncWmsCellInfo(Map<String, Object> params) throws Exception {
		
		IQueryManager wmsQueryMgr = BeanUtil.get(DataSourceManager.class).getQueryManager("WMS");
		Query conds = new Query();
		conds.addFilter("buildingTcd", "A");
		List<WmsCellList> cellList = wmsQueryMgr.selectList(WmsCellList.class, conds);
		
		List<WcsCell> list = new ArrayList<>();
		int fromIndex = 0;
		int page = 5000;
		for (int i = 0; i < cellList.size(); i++) {
			int toIndex = Math.min(fromIndex + page, cellList.size());
			List<WmsCellList> subList = cellList.subList(fromIndex, toIndex);
			for (WmsCellList obj: subList) {
				WcsCell wcsCell = FnfUtils.populate(obj, new WcsCell(), false);
				list.add(wcsCell);
			}
			queryManager.insertBatch(list);
			fromIndex += toIndex;
			list = new ArrayList<>();
		}
		
		conds.setFilter("buildingTcd", "B");
		cellList = new ArrayList<>();
		cellList = wmsQueryMgr.selectList(WmsCellList.class, conds);
		
		list = new ArrayList<>();
		fromIndex = 0;
		for (int i = 0; i < cellList.size(); i++) {
			int toIndex = Math.min(fromIndex + page, cellList.size());
			List<WmsCellList> subList = cellList.subList(fromIndex, toIndex);
			for (WmsCellList obj: subList) {
				WcsCell wcsCell = FnfUtils.populate(obj, new WcsCell(), false);
				list.add(wcsCell);
			}
			queryManager.insertBatch(list);
			fromIndex += toIndex;
			list = new ArrayList<>();
		}
		
		return new ResponseObj();
	}
}
