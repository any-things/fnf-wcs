package operato.fnf.wcs.service.rfid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WcsMheBox;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;

@Component
public class DasResultIfYnReset extends AbstractQueryService {
	public ResponseObj dasResultIfYnReset(Map<String, Object> params) throws Exception {
		
		Query batchConds = new Query();
		batchConds.addFilter("status", "RUN");
		List<JobBatch> jobBatches = queryManager.selectList(JobBatch.class, batchConds);
		
		if (jobBatches.size() == 0) {
			return new ResponseObj();
		}
		
		List<String> ids = new ArrayList<>();
		for (JobBatch obj: jobBatches) {
			ids.add(obj.getWmsBatchNo());
		}
		
		Query conds = new Query();
		conds.addFilter("ifYn", "E");
		conds.addFilter("delYn", "N");
		conds.addFilter("workUnit", "in", ids);
		List<WcsMheBox> wcsMheBoxList = queryManager.selectList(WcsMheBox.class, conds);
		for (WcsMheBox obj: wcsMheBoxList) {
			obj.setIfYn("N");
		}
		
		queryManager.updateBatch(wcsMheBoxList, "ifYn");
		
		return new ResponseObj();
	}
}
