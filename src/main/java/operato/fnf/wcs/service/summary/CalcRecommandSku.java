package operato.fnf.wcs.service.summary;

import java.util.Map;

import org.springframework.stereotype.Component;

import operato.logis.wcs.entity.TopSkuSetting;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class CalcRecommandSku extends AbstractQueryService {
	public ResponseObj calcRecommandSku(Map<String, Object> params) throws Exception {
		String date = String.valueOf(params.get("date"));
		if (ValueUtil.isEmpty(date)) {
			date = DateUtil.getCurrentDay();
		}
		
		Query conds = new Query(0, 1);
		conds.addOrder("updatedAt", false);
		TopSkuSetting setting = queryManager.selectByCondition(true, TopSkuSetting.class, conds);
		
		
		
		
		
		return new ResponseObj();
	}
}
