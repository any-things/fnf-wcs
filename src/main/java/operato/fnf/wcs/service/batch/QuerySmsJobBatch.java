package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.RfidBoxResult;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@Component
public class QuerySmsJobBatch extends AbstractRestService {
	@SuppressWarnings("unchecked")
	public ResponseObj querySmsJobBatch(Map<String, Object> params) throws Exception {
		int page = Integer.valueOf(String.valueOf(params.get("page")));
		int limit = Integer.valueOf(String.valueOf(params.get("limit")));
		String sort = String.valueOf(params.get("sort"));
		String select = String.valueOf(params.get("select"));
		String query = String.valueOf(params.get("query"));
		Query queryObj = this.parseQuery(RfidBoxResult.class, page, limit, select, sort, query);
		
		List<Filter> filters = queryObj.getFilter();
		Map<String, Object> queryParams = new HashMap<>();
		if (ValueUtil.isNotEmpty(filters)) {
			for (Filter filter : filters) {
				queryParams.put(filter.getName(), filter.getValue());
			}
		}
		
		
		
		String sql = FnfUtils.queryCustServiceWithCheck("sms-job-batches");
		
		Page<JobBatch> jbPage = queryManager.selectPageBySql(sql, queryParams, JobBatch.class, page, limit);
		List<JobBatch> jbList = jbPage.getList();
		JobBatch mainJob = null;
		List<JobBatch> totalJb = new ArrayList<>();
		for (int i = jbList.size() - 1; i >= 0; i--) {
			JobBatch obj = jbList.get(i);
			if (obj.getWmsBatchNo().equals(obj.getBatchGroupId())) {
				Map<String, Object> condition = ValueUtil.newMap("batchGroupId", obj.getBatchGroupId());
				
				String batchQtySql = FnfUtils.queryCustServiceWithCheck("sms-main-job-qty");
				Map<String, Object> batchSummary = this.queryManager.selectBySql(batchQtySql, condition, Map.class);
				
				mainJob = obj;
				
				JobBatch sumJb = new JobBatch();
				sumJb.setId("SUM");
				sumJb.setWmsBatchNo(obj.getBatchGroupId());
				sumJb.setParentOrderQty(obj.getParentOrderQty());
				sumJb.setParentPcs(obj.getParentPcs());
				sumJb.setResultOrderQty(obj.getResultOrderQty());
				sumJb.setResultPcs(obj.getResultPcs());
				sumJb.setResultBoxQty(obj.getResultBoxQty());
				
				
				
				sumJb.setBatchOrderQty(obj.getParentOrderQty());
				sumJb.setBatchPcs(ValueUtil.toInteger(batchSummary.get("batch_pcs")));//배치총pcs
				
				mainJob.setParentOrderQty(mainJob.getParentOrderQty());
				mainJob.setBatchOrderQty(mainJob.getBatchOrderQty());
				
				totalJb.add(sumJb);
				totalJb.add(obj);
				continue;
			}
			
			if (ValueUtil.isNotEmpty(mainJob)) {
				mainJob.setResultPcs(mainJob.getResultPcs() - obj.getResultPcs());
				mainJob.setResultBoxQty(mainJob.getResultBoxQty() - obj.getResultBoxQty());
			}
			
			totalJb.add(obj);
		}
		
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(totalJb);
		resp.setTotal(jbPage.getTotalSize());
		return resp;
	}

	@Override
	protected Class<?> entityClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
