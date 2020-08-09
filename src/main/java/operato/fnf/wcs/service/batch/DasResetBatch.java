package operato.fnf.wcs.service.batch;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;

@Component
public class DasResetBatch extends AbstractQueryService {
	
	public ResponseObj dasResetBatch(Map<String, Object> params) throws Exception {

		@SuppressWarnings("unchecked")
		List<LinkedHashMap<String, Object>> temp = (List<LinkedHashMap<String, Object>>)params.get("list");
		List<JobBatch> dataList = FnfUtils.parseObjList(JobBatch.class, temp);
		
		for (JobBatch data : dataList) {
			FnfUtils.checkValueEmpty("jobDate", data.getJobDate(), "wmsBatchNo", data.getWmsBatchNo());
			
			Map<String, Object> delParams = new HashMap<String, Object>();
			delParams.put("jobDate", data.getJobDate());
			delParams.put("wmsBatchNo", data.getWmsBatchNo());
			String jobBatchDelSql = "delete from job_batches where job_date = :jobDate and wms_batch_no = :wmsBatchNo";
			queryManager.executeBySql(jobBatchDelSql, delParams);
			
			delParams.put("jobDate", data.getJobDate().replace("-", ""));
			String mheHrDelSql = "delete from mhe_hr where work_date = :jobDate and work_unit = :wmsBatchNo";
			queryManager.executeBySql(mheHrDelSql, delParams);
			
			String mheDrDelSql = "delete from mhe_dr where work_date = :jobDate and work_unit = :wmsBatchNo";
			queryManager.executeBySql(mheDrDelSql, delParams);
		}

		ResponseObj resp = new ResponseObj();
		return resp;
	}
}
