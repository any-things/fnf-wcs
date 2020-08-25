package operato.fnf.wcs.service.bcr;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.DpsBcrIfData;
import operato.fnf.wcs.entity.DpsJobInstance;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class BcrBarcodeProcess extends AbstractLogisService {
	private final String STATUS_FINISH = "F";
	public ResponseObj bcrBarcodeProcess(Map<String, Object> params) throws Exception {
		@SuppressWarnings("unchecked")
		List<LinkedHashMap<String, Object>> temp = (List<LinkedHashMap<String, Object>>)params.get("list");
		List<DpsBcrIfData> list = FnfUtils.parseObjList(DpsBcrIfData.class, temp);
		
		for (DpsBcrIfData obj: list) {
			BeanUtil.get(BcrBarcodeProcess.class).processData(obj);
		}
		
		return new ResponseObj();
	}
	
	public void processData(DpsBcrIfData obj) throws Exception {
		try {
			DpsJobInstance dpsJobInstance = queryManager.selectByCondition(DpsJobInstance.class, obj.getId());
			
			if (ValueUtil.isEmpty(dpsJobInstance)) {
				throw new ElidomValidationException("BcrBarcodeRead: boxId["+ obj.getWaybillNo() +"] not found~~");
			}
			dpsJobInstance.setBcrStatus(STATUS_FINISH);
			queryManager.update(dpsJobInstance, "bcrStatus");
			
			obj.setProcYn("Y");
			queryManager.update(obj, "procYn");
		} catch(Exception e) {
			logger.error("BcrBarcodeProcessJob error~~", e);
			obj.setProcYn("E");
			obj.setErrorMsg(e.getMessage());
			queryManager.update(obj, "procYn");
		}
	}
}
