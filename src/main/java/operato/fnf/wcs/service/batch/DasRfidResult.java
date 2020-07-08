package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.RfidBoxResult;
import operato.fnf.wcs.service.model.RfidResult;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@Component
public class DasRfidResult extends AbstractRestService {
	@Autowired
	private DataSourceManager dataSourceMgr;
	
	public ResponseObj dasRfidResult(Map<String, Object> params) throws Exception {
		
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
				if ("dt_delivery".equals(filter.getName())) {					
					queryParams.put(filter.getName(), String.valueOf(filter.getValue()).replaceAll("-", ""));
				} else if ("no_box".equals(filter.getName()) || "no_waybill".equals(filter.getName())) {					
					queryParams.put(filter.getName(), "%"+filter.getValue()+"%");
				} else {
					queryParams.put(filter.getName(), filter.getValue());
				}
			}
		}
		
		StringJoiner sqlJoiner = new StringJoiner(SysConstants.LINE_SEPARATOR);
		sqlJoiner.add("SELECT * FROM (");
		sqlJoiner.add("SELECT");
		sqlJoiner.add("  pr.*,");
		sqlJoiner.add("  ps.no_waybill,");
		sqlJoiner.add("  ps.result_st,");
		sqlJoiner.add("  ps.tp_status,");
		sqlJoiner.add("  ps.dm_bf_recv,");
		sqlJoiner.add("  ps.dm_af_recv,");
		sqlJoiner.add("  ds_errmsg");
		sqlJoiner.add("FROM");
		sqlJoiner.add("  (");
		sqlJoiner.add("    SELECT DISTINCT");
		sqlJoiner.add("      cd_warehouse,");
		sqlJoiner.add("      dt_delivery,");
		sqlJoiner.add("      ds_batch_no,");
		sqlJoiner.add("      tp_machine,");
		sqlJoiner.add("      cd_brand,");
		sqlJoiner.add("      no_box");
		sqlJoiner.add("    FROM");
		sqlJoiner.add("      rfid_if.if_pasdelivery_recv");
		sqlJoiner.add("    WHERE");
		sqlJoiner.add("      1 = 1"); 
		if (ValueUtil.isNotEmpty(queryParams.get("dt_delivery"))) {
			sqlJoiner.add("      AND dt_delivery =:dt_delivery"); 
		}
		if (ValueUtil.isNotEmpty(queryParams.get("cd_brand"))) {
			sqlJoiner.add("      and cd_brand = :cd_brand");
		}
		if (ValueUtil.isNotEmpty(queryParams.get("tp_machine"))) {
			sqlJoiner.add("      and tp_machine = :tp_machine");
		}
		if (ValueUtil.isNotEmpty(queryParams.get("no_box"))) {
			sqlJoiner.add("      and no_box like :no_box");
		}
		sqlJoiner.add("  ) pr");
		sqlJoiner.add("    LEFT JOIN rfid_if.if_pasdelivery_send ps");
		sqlJoiner.add("    ON pr.cd_warehouse = ps.cd_warehouse");
		sqlJoiner.add("     AND pr.ds_batch_no = ps.ds_batch_no");
		sqlJoiner.add("     AND pr.no_box = ps.no_box");
		sqlJoiner.add("     ) WHERE 1 = 1");
		
		if (ValueUtil.isNotEmpty(queryParams.get("no_waybill"))) {
			sqlJoiner.add("     AND no_waybill like :no_waybill");
		}
		if (ValueUtil.isNotEmpty(queryParams.get("result_st"))) {
			sqlJoiner.add("     AND result_st = :result_st");
		}
		
		sqlJoiner.add(" ORDER BY dt_delivery desc");
		
		IQueryManager wmsQueryMgr = dataSourceMgr.getQueryManager(RfidBoxResult.class);
		Page<RfidResult> resultPage = wmsQueryMgr.selectPageBySql(sqlJoiner.toString(), queryParams, RfidResult.class, page, limit);
		
		List<String> batchNos = new ArrayList<>();
		for (RfidResult obj: resultPage.getList()) {
			if (!batchNos.contains(obj.getDsBatchNo())) {
				batchNos.add(obj.getDsBatchNo());
			}
		}
		
		Map<String, JobBatch> jobBatchMap = new HashMap<>();
		if (ValueUtil.isNotEmpty(batchNos)) {
			StringJoiner sqlBatchJoiner = new StringJoiner(SysConstants.LINE_SEPARATOR);
			sqlBatchJoiner.add("SELECT");
			sqlBatchJoiner.add("	id,");
			sqlBatchJoiner.add("	wms_batch_no,");
			sqlBatchJoiner.add("	job_type,");
			sqlBatchJoiner.add("	stage_cd,");
			sqlBatchJoiner.add("	equip_type,");
			sqlBatchJoiner.add("	equip_group_cd,");
			sqlBatchJoiner.add("	equip_cd,");
			sqlBatchJoiner.add("	equip_nm");
			sqlBatchJoiner.add("FROM");
			sqlBatchJoiner.add("	job_batches");
			sqlBatchJoiner.add("WHERE");
			sqlBatchJoiner.add("	id IN (:batchNos)");
			
			List<JobBatch> jobBatches = queryManager.selectListBySql(sqlBatchJoiner.toString(), ValueUtil.newMap("batchNos", batchNos), JobBatch.class, page, limit);
			
			for (JobBatch jobBatch: jobBatches) {
				jobBatchMap.put(jobBatch.getWmsBatchNo(), jobBatch);
			}
			
			for (RfidResult obj: resultPage.getList()) {
				JobBatch jobBatch = jobBatchMap.get(obj.getDsBatchNo());
				if (ValueUtil.isNotEmpty(jobBatch)) {
					obj.setEquipCd(jobBatch.getEquipCd());
				}
				if (ValueUtil.isEmpty(obj.getResultSt())) {
					obj.setResultSt("W");
				}
			}
		}
		
		ResponseObj resp = new ResponseObj();
		resp.setItems(resultPage.getList());
		resp.setTotal(resultPage.getTotalSize());
		return resp;
	}

	@Override
	protected Class<?> entityClass() {
		return RfidBoxResult.class;
	}
}
