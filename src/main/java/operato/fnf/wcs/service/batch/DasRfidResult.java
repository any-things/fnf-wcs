package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.RfidBoxResult;
import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.service.model.RfidResult;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@Component
public class DasRfidResult extends AbstractRestService {
	@Autowired
	private DataSourceManager dataSourceMgr;
	private String RESULT_STATUS_WAIT = "W";
	
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
		
		String workDate = String.valueOf(queryParams.get("dt_delivery"));
		if (ValueUtil.isEmpty(workDate)) {
			throw new ElidomInputException("검색조건 [작업일자]를 선택해 주세요.");
		}
		
 		Map<String, JobBatch> jobBatchMap = new HashMap<>();
 		String stageCd = String.valueOf(queryParams.get("stage_cd"));
		if (ValueUtil.isNotEmpty(stageCd)) {
			Query conds = new Query();
			conds.addFilter("jobDate", queryParams.get("dt_delivery"));
			conds.addFilter("stageCd", queryParams.get("stage_cd"));
			
			List<JobBatch> jobBatches = queryManager.selectList(JobBatch.class, conds);

			for (JobBatch obj: jobBatches) {
				jobBatchMap.put(obj.getWmsBatchNo(), obj);
			}
		}
		
		StringJoiner sqlJoiner = new StringJoiner(SysConstants.LINE_SEPARATOR);
		sqlJoiner.add("SELECT * FROM (");
		sqlJoiner.add("SELECT DISTINCT");
		sqlJoiner.add("  pr.cd_warehouse,");
		sqlJoiner.add("  pr.ds_batch_no,");
		sqlJoiner.add("  pr.dt_delivery,");
		sqlJoiner.add("  pr.tp_machine,");
		sqlJoiner.add("  pr.cd_brand,");
		sqlJoiner.add("  pr.no_box,");
		sqlJoiner.add("  ps.no_waybill,");
		sqlJoiner.add("  ps.result_st,");
		sqlJoiner.add("  ps.tp_status,");
//		sqlJoiner.add("  ps.dm_bf_recv,");
//		sqlJoiner.add("  ps.dm_af_recv,");
		sqlJoiner.add("  pr.dm_bf_recv,");
		sqlJoiner.add("  ps.dm_bf_recv   AS dm_bf_send,");
		sqlJoiner.add("  ps.dm_af_recv   AS dm_af_send,");
		sqlJoiner.add("  ds_errmsg");
		sqlJoiner.add("FROM");
		sqlJoiner.add("  (SELECT DISTINCT");
		sqlJoiner.add("      recva.cd_warehouse,");
		sqlJoiner.add("      recva.dt_delivery,");
		sqlJoiner.add("      recvb.ds_batch_no,");
		sqlJoiner.add("      recva.tp_machine,");
		sqlJoiner.add("      recva.cd_brand,");
		sqlJoiner.add("      recva.no_box,");
		sqlJoiner.add("      recva.dm_bf_recv");
		sqlJoiner.add("    FROM");
		sqlJoiner.add("      rfid_if.if_pasdelivery_recv recva,");
		sqlJoiner.add("		 (SELECT");
		sqlJoiner.add("		   no_box,");
		sqlJoiner.add("		   MIN(ds_batch_no) AS ds_batch_no");
		sqlJoiner.add("		 FROM");
		sqlJoiner.add("		   rfid_if.if_pasdelivery_recv");
		sqlJoiner.add("		 WHERE");
		sqlJoiner.add("		   dt_delivery = :dt_delivery");
		sqlJoiner.add("		 GROUP BY");
		sqlJoiner.add("		   no_box) recvb");
		sqlJoiner.add("    WHERE");
		sqlJoiner.add("      1 = 1"); 
		if (ValueUtil.isNotEmpty(queryParams.get("dt_delivery"))) {
			sqlJoiner.add("      AND recva.dt_delivery =:dt_delivery"); 
		}
		if (ValueUtil.isNotEmpty(queryParams.get("cd_brand"))) {
			sqlJoiner.add("      AND recva.cd_brand = :cd_brand");
		}
		if (ValueUtil.isNotEmpty(queryParams.get("tp_machine"))) {
			sqlJoiner.add("      AND recva.tp_machine = :tp_machine");
		}
		if (ValueUtil.isNotEmpty(queryParams.get("no_box"))) {
			sqlJoiner.add("      AND recva.no_box like :no_box");
		}
		sqlJoiner.add("      AND recva.ds_batch_no = recvb.ds_batch_no");
		sqlJoiner.add("      AND recva.no_box = recvb.no_box");
		sqlJoiner.add("  ) pr");
		
		sqlJoiner.add("    LEFT JOIN rfid_if.if_pasdelivery_send ps");
		sqlJoiner.add("    ON pr.cd_warehouse = ps.cd_warehouse");
		sqlJoiner.add("     AND pr.ds_batch_no = ps.ds_batch_no");
		sqlJoiner.add("     AND pr.no_box = ps.no_box");
		sqlJoiner.add("     AND pr.cd_brand = ps.cd_brand");
		sqlJoiner.add("     ) WHERE 1 = 1");
		
		if (ValueUtil.isNotEmpty(queryParams.get("no_waybill"))) {
			sqlJoiner.add("     AND no_waybill like :no_waybill");
		}
		
		String resultStatus = String.valueOf(queryParams.get("result_st"));
		if (ValueUtil.isNotEmpty(resultStatus)) {
			if (RESULT_STATUS_WAIT.equalsIgnoreCase(resultStatus)) {
				sqlJoiner.add("     AND result_st is null");
			} else {				
				sqlJoiner.add("     AND result_st = :result_st");
			}
		}
		
		sqlJoiner.add(" ORDER BY dt_delivery desc");
		
		IQueryManager wmsQueryMgr = dataSourceMgr.getQueryManager(RfidBoxResult.class);
		Page<RfidResult> resultPage = wmsQueryMgr.selectPageBySql(sqlJoiner.toString(), queryParams, RfidResult.class, page, limit);
		List<RfidResult> rfidResults = resultPage.getList();
		
		List<String> batchNos = new ArrayList<>();
		if (ValueUtil.isEmpty(jobBatchMap.keySet())) {
			for (RfidResult obj: rfidResults) {
				if (!batchNos.contains(obj.getDsBatchNo())) {
					batchNos.add(obj.getDsBatchNo());
				}
			}
			
			if (ValueUtil.isNotEmpty(batchNos)) {				
				Query conds = new Query();
				conds.addFilter("jobDate", queryParams.get("dt_delivery"));
				conds.addFilter("wmsBatchNo", "in", batchNos);
				List<JobBatch> jobBatches = queryManager.selectList(JobBatch.class, conds);
				
				for (JobBatch obj: jobBatches) {
					jobBatchMap.put(obj.getWmsBatchNo(), obj);
				}
			}
		}
		
		
		List<String> boxNos = new ArrayList<>();
		for (RfidResult obj: rfidResults) {
			JobBatch jobBatch = jobBatchMap.get(obj.getDsBatchNo());
			if (ValueUtil.isNotEmpty(jobBatch)) {
				obj.setEquipCd(jobBatch.getEquipCd());
				obj.setStageCd(jobBatch.getStageCd());
			}
			if (ValueUtil.isEmpty(obj.getResultSt())) {
				obj.setResultSt("W");
			}
			
			boxNos.add(obj.getNoBox());
		}
		
		StringJoiner wcsBoxQuery = new StringJoiner(SysConstants.LINE_SEPARATOR);
		wcsBoxQuery.add("SELECT");
		wcsBoxQuery.add("	DISTINCT box_no,");
		wcsBoxQuery.add("	mhe_datetime,");
		wcsBoxQuery.add("	if_datetime,");
		wcsBoxQuery.add("	del_yn,");
		wcsBoxQuery.add("	del_datetime");
		wcsBoxQuery.add("FROM");
		wcsBoxQuery.add("	mhe_box");
		wcsBoxQuery.add("WHERE");
		wcsBoxQuery.add("	work_date = :workDate");
		if (ValueUtil.isNotEmpty(batchNos)) {
			wcsBoxQuery.add("	AND work_unit IN (:batchNos)");
		}
		if (ValueUtil.isNotEmpty(boxNos)) {
			wcsBoxQuery.add("	AND box_no IN (:boxNos)");
		}
		
		List<WcsMheBox> boxes = queryManager.selectListBySql(wcsBoxQuery.toString(), ValueUtil.newMap("workDate,batchNos,boxNos", workDate, batchNos, boxNos), WcsMheBox.class, 0, 0);
		
		Map<String, WcsMheBox> boxMap = new HashMap<>();
		for (WcsMheBox box: boxes) {
			boxMap.put(box.getBoxNo(), box);
		}
		
		for (RfidResult obj: rfidResults) {
			WcsMheBox box = boxMap.get(obj.getNoBox());
			if (ValueUtil.isNotEmpty(box)) {
				obj.setMheDateTime(box.getMheDatetime());
				obj.setIfDateTime(box.getIfDatetime());
				obj.setDelYn(box.getDelYn());
				obj.setDelDateTime(box.getMheDatetime());
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
