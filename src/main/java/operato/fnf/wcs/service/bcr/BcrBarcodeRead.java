package operato.fnf.wcs.service.bcr;

import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.DpsJobInstance;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.util.ValueUtil;

@Component
public class BcrBarcodeRead extends AbstractLogisService {
	private final String STATUS_FINISH = "F";
	public ResponseObj bcrBarcodeRead(Map<String, Object> params) throws Exception {
		//String workUnit = String.valueOf(params.get("workUnit"));
		String boxId = String.valueOf(params.get("boxId"));
		
		FnfUtils.checkValueEmpty("boxId", boxId);
		//FnfUtils.checkValueEmpty("workUnit", workUnit, "boxId", boxId);
		
		Query conds = new Query(0, 1);
		//conds.addFilter("workUnit", workUnit);
		conds.addFilter("boxId", boxId);
		conds.addOrder("dpsAssignAt", false);
		DpsJobInstance dpsJobInstance = queryManager.selectByCondition(DpsJobInstance.class, conds);
		
		if (ValueUtil.isEmpty(dpsJobInstance)) {
			throw new ElidomValidationException("BcrBarcodeRead: boxId["+ boxId +"] not found~~");
		}
		
		dpsJobInstance.setBcrStatus(STATUS_FINISH);
		queryManager.update(dpsJobInstance, "brcStatus");
		
		ResponseObj resp = new ResponseObj();
		return resp;
	}
}
