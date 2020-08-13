package operato.fnf.wcs.service.das;

import java.util.Map;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.batch.DasCloseBatchService;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;

public class DasBatchCloseView extends DasCloseBatchService {
	public ResponseObj dasBatchCloseView(Map<String, Object> params) throws Exception {
		
		Map<String, Object> kvParams = FnfUtils.parseQueryParamsToMap(JobBatch.class, params);
		
		ResponseObj resp = new ResponseObj();
		return resp;
	}
}
