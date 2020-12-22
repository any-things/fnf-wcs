package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.RfidBoxResult;
import operato.fnf.wcs.entity.WmsMheDr;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.BeanUtil;
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
			for (Filter filter : filters) {
				queryParams.put(filter.getName(), filter.getValue());
			}
		}
		
		String sql = FnfUtils.queryCustServiceWithCheck("dps-job-batches");
		
		Page<JobBatch> jbPage = queryManager.selectPageBySql(sql, queryParams, JobBatch.class, page, limit);
		List<JobBatch> jbList = jbPage.getList();
		JobBatch mainJob = null;
		List<JobBatch> totalJb = new ArrayList<>();
		for (int i = jbList.size() - 1; i >= 0; i--) {
			JobBatch obj = jbList.get(i);
			if (obj.getWmsBatchNo().equals(obj.getBatchGroupId())) {
				mainJob = obj;
				
//				String orderQtySql = FnfUtils.queryCustServiceWithError("dps-main-job-wms-order-qty");
//				Map<String, Object> wmsOrdCntParams = new HashMap<>();
//				wmsOrdCntParams.put("workUnit", obj.getWmsBatchNo());
//				IQueryManager wmsQueryManager = BeanUtil.get(DataSourceManager.class).getQueryManager(WmsMheDr.class);
//				Integer wmsOrderCnt = wmsQueryManager.selectBySql(orderQtySql, wmsOrdCntParams, Integer.class);
				
				String orderQtySql = FnfUtils.queryCustServiceWithCheck("dps-main-job-order-qty");
				Map<String, Object> wmsOrdCntParams = new HashMap<>();
				wmsOrdCntParams.put("waveNo", obj.getWcsBatchNo());
				wmsOrdCntParams.put("workseqNo", obj.getJobSeq());
				IQueryManager wmsQueryManager = BeanUtil.get(DataSourceManager.class).getQueryManager(WmsMheDr.class);
				Integer orderCnt = wmsQueryManager.selectBySql(orderQtySql, wmsOrdCntParams, Integer.class);
				
				JobBatch sumJb = new JobBatch();
				sumJb.setId("SUM");
				sumJb.setWmsBatchNo(obj.getBatchGroupId());
				sumJb.setParentOrderQty(obj.getParentOrderQty());
				sumJb.setBatchOrderQty(obj.getBatchOrderQty());
				sumJb.setResultOrderQty(obj.getResultOrderQty());
				sumJb.setParentPcs(obj.getParentPcs());
				sumJb.setBatchPcs(obj.getBatchPcs());
				sumJb.setResultPcs(obj.getResultPcs());
				sumJb.setResultBoxQty(obj.getResultBoxQty());
				
				mainJob.setParentOrderQty(orderCnt);
				mainJob.setBatchOrderQty(orderCnt);
				
				totalJb.add(sumJb);
				totalJb.add(obj);
				continue;
			}
			
			if (ValueUtil.isNotEmpty(mainJob)) {
				//motherJb.setParentOrderQty();
				//motherJb.setBatchOrderQty();
				//motherJb.setResultOrderQty();
				mainJob.setParentPcs(mainJob.getParentPcs() - obj.getParentPcs());
				mainJob.setBatchPcs(mainJob.getBatchPcs() - obj.getBatchPcs());
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
