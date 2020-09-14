package operato.fnf.wcs.service.rfid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WcsMheBox;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class DasResultIfYnReset extends AbstractQueryService {
	public ResponseObj dasResultIfYnReset(Map<String, Object> params) throws Exception {
		
		String ifDoThis = SettingUtil.getValue("rfid.ifyn.reset");
		
		if (ValueUtil.isEmpty(ifDoThis) || "N".equals(ifDoThis)) {
			return new ResponseObj();
		}
		
		if ("D".equals(ifDoThis)) {
			String sql = FnfUtils.queryCustServiceWithCheck("das_cancel_box_info");
			List<WcsMheBox> wcsMheBoxes = queryManager.selectListBySql(sql, new HashMap<String, Object>(), WcsMheBox.class, 0, 50);
			if (wcsMheBoxes.size() == 0) {
				return new ResponseObj();
			}
			
			List<String> ids = new ArrayList<>();
			for (WcsMheBox obj: wcsMheBoxes) {
				ids.add(obj.getId());
			}
			
			this.dataReset(ids, null);
		} else {
			Query batchConds = new Query();
			batchConds.addFilter("status", "RUN");
			List<JobBatch> jobBatches = queryManager.selectList(JobBatch.class, batchConds);
			if (jobBatches.size() == 0) {
				return new ResponseObj();
			}
			
			List<String> workUnits = new ArrayList<>();
			for (JobBatch obj: jobBatches) {
				workUnits.add(obj.getWmsBatchNo());
			}
			
			this.dataReset(null, workUnits);
		}
		
		return new ResponseObj();
	}
	
	private void dataReset(List<String> ids, List<String> workUnits) {
		Query conds = new Query();
		conds.addFilter("ifYn", "E");
		//conds.addFilter("delYn", "N");
		if (ValueUtil.isNotEmpty(ids)) {
			conds.addFilter("id", "in", ids);
		}
		if (ValueUtil.isNotEmpty(workUnits)) {
			conds.addFilter("workUnit", "in", workUnits);			
		}
		List<WcsMheBox> wcsMheBoxList = queryManager.selectList(WcsMheBox.class, conds);
		for (WcsMheBox obj: wcsMheBoxList) {
			obj.setIfYn("N");
		}
		
		queryManager.updateBatch(wcsMheBoxList, "ifYn");
	}
}
