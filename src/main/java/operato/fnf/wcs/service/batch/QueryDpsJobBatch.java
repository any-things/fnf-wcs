package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

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
public class QueryDpsJobBatch extends AbstractRestService {
	public ResponseObj queryDpsJobBatch(Map<String, Object> params) throws Exception {
		int page = Integer.valueOf(String.valueOf(params.get("page")));
		int limit = Integer.valueOf(String.valueOf(params.get("limit")));
		String sort = String.valueOf(params.get("sort"));
		String select = String.valueOf(params.get("select"));
		String query = String.valueOf(params.get("query"));
		Query queryObj = this.parseQuery(RfidBoxResult.class, page, limit, select, sort, query);
		
		List<Filter> filters = queryObj.getFilter();
		Map<String, Object> queryParams = new HashMap<>();
		if (ValueUtil.isNotEmpty(filters)) {
			for (Filter filter: filters) {
				queryParams.put(filter.getName(), filter.getValue());
			}
		}
		
		String sql = FnfUtils.queryCustService("dps-job-batches");
		if (ValueUtil.isEmpty(sql)) {
			throw new ValidationException("커스텀 서비스 [dps-job-batches]가 존재하지 않습니다.");
		}
		
		Page<JobBatch> jbPage = queryManager.selectPageBySql(sql, queryParams, JobBatch.class, page, limit);
		List<JobBatch> jbList = jbPage.getList();
		JobBatch motherJb = null;
		List<JobBatch> totalJb = new ArrayList<>();
		for (int i = jbList.size() - 1; i > 0; i--) {
			JobBatch obj = jbList.get(i);
			if (obj.getWmsBatchNo().equals(obj.getBatchGroupId())) {
				motherJb = obj;
				
				JobBatch sumJb = new JobBatch();
				sumJb.setId("SUM");
				sumJb.setWmsBatchNo("합계: " + obj.getBatchGroupId());
				sumJb.setParentOrderQty(obj.getParentOrderQty());
				sumJb.setBatchOrderQty(obj.getBatchOrderQty());
				sumJb.setResultOrderQty(obj.getResultOrderQty());
				sumJb.setParentPcs(obj.getParentPcs());
				sumJb.setBatchPcs(obj.getBatchPcs());
				sumJb.setResultPcs(obj.getResultPcs());
				sumJb.setResultBoxQty(obj.getResultBoxQty());
				
				totalJb.add(sumJb);
				totalJb.add(obj);
				continue;
			}
			
			motherJb.setParentOrderQty(motherJb.getParentOrderQty() - obj.getParentOrderQty());
			motherJb.setBatchOrderQty(motherJb.getBatchOrderQty() - obj.getBatchOrderQty());
			motherJb.setResultOrderQty(motherJb.getResultOrderQty() - obj.getResultOrderQty());
			motherJb.setParentPcs(motherJb.getParentPcs() - obj.getParentPcs());
			motherJb.setBatchPcs(motherJb.getBatchPcs() - obj.getBatchPcs());
			motherJb.setResultPcs(motherJb.getResultPcs() - obj.getResultPcs());
			motherJb.setResultBoxQty(motherJb.getResultBoxQty() - obj.getResultBoxQty());
			totalJb.add(obj);
		}
		
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(totalJb);
		return resp;
	}

	@Override
	protected Class<?> entityClass() {
		// TODO Auto-generated method stub
		return null;
	}
}
