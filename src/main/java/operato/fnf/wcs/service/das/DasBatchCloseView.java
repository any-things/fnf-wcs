package operato.fnf.wcs.service.das;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@Component
public class DasBatchCloseView extends AbstractRestService {
	@SuppressWarnings("unchecked")
	public ResponseObj dasBatchCloseView(Map<String, Object> params) throws Exception {
		Integer page = Integer.valueOf(String.valueOf(params.get("page")));
		Integer limit = Integer.valueOf(String.valueOf(params.get("limit")));
		String sort = String.valueOf(params.get("sort"));
		String select = String.valueOf(params.get("select"));
		String query = String.valueOf(params.get("query"));
		
		Page<JobBatch> jobBatchPage = null;
		String sql = FnfUtils.queryCustService("das_job_batch_close_view");
		if (ValueUtil.isNotEmpty(sql)) {
			Map<String, Object> kvParams = FnfUtils.parseQueryParamsToMap(JobBatch.class, params);
			Map<String, Object> sqlParams = new HashMap<>();
			for (String key: kvParams.keySet()) {
				if (key.equals("progress_rate")) {	
					sqlParams.put(FnfUtils.snakeToCamel(key), Float.parseFloat(String.valueOf(kvParams.get(key))));
				} else {
					sqlParams.put(FnfUtils.snakeToCamel(key), kvParams.get(key));
				}
			}
			jobBatchPage = queryManager.selectPageBySql(sql, sqlParams, JobBatch.class, page, limit);
		} else {
			jobBatchPage = (Page<JobBatch>)this.search(JobBatch.class, page, limit, select, sort, query);
		}
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(jobBatchPage.getList());
		resp.setTotal(jobBatchPage.getTotalSize());
		return resp;
	}

	@Override
	protected Class<?> entityClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
