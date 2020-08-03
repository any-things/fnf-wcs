package operato.fnf.wcs.service.summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.RfidBoxResult;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.service.model.DpsProductivity;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.util.ResourceUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.ValueUtil;
import xyz.elidom.util.converter.msg.IJsonParser;

@Component
public class CalcDpsProductivityByDay extends AbstractQueryService {
	private final String WORK_TYPE_TOTAL_PICKING = "TPCK";
	private final String WORK_TYPE_LOAD_STOCK = "LOAD";
	private final String WORK_TYPE_PICKING = "PICK";
	private final String WORK_TYPE_INSPECTION = "INSP";
	
	@Autowired
	@Qualifier("under_to_camel")
	protected IJsonParser jsonParser;
	
	public ResponseObj calcDpsProductivity(Map<String, Object> params) throws Exception {
		int page = Integer.valueOf(String.valueOf(params.get("page")));
		int limit = Integer.valueOf(String.valueOf(params.get("limit")));
		String sort = String.valueOf(params.get("sort"));
		String select = String.valueOf(params.get("select"));
		String query = String.valueOf(params.get("query"));
		Query queryObj = this.parseQuery(RfidBoxResult.class, page, limit, select, sort, query);
		
		List<Filter> filters = queryObj.getFilter();
		String fromDate = null;
		String toDate = null;
		if (ValueUtil.isNotEmpty(filters)) {
			for (Filter filter: filters) {
				if ("work_date".equals(filter.getName())) {
					String[] dates = String.valueOf(filter.getValue()).split(",");
					if (dates.length < 2) {
						throw new ElidomInputException("값[date]이(가) 빈 값입니다.");
					}
					
					fromDate = String.valueOf(filter.getValue()).split(",")[0];
					if (ValueUtil.isNotEmpty(fromDate)) {
						fromDate = fromDate.replace("-", "").replace(" ", "").replace(":", "");
					}
					toDate = String.valueOf(filter.getValue()).split(",")[1];
					if (ValueUtil.isNotEmpty(toDate)) {
						toDate = toDate.replace("-", "").replace(" ", "").replace(":", "");
					}
				}
			}
		}
		
		FnfUtils.checkValueEmpty("fromDate", fromDate, "toDate", toDate);
		
		List<DpsProductivity> dpsProductivities = new ArrayList<>();
		List<DpsProductivity> tpSum = this.getDpsWcsSum(WORK_TYPE_TOTAL_PICKING, fromDate, toDate);
		if (ValueUtil.isNotEmpty(tpSum)) {
			this.calcByDay(tpSum);
			dpsProductivities.addAll(tpSum);
		}
		List<DpsProductivity> loadSum = this.getDpsWcsSum(WORK_TYPE_LOAD_STOCK, fromDate, toDate);
		if (ValueUtil.isNotEmpty(loadSum)) {
			this.calcByDay(loadSum);
			dpsProductivities.addAll(loadSum);
		}
		List<DpsProductivity> pickingSum = this.getDpsWcsSum(WORK_TYPE_PICKING, fromDate, toDate);
		if (ValueUtil.isNotEmpty(pickingSum)) {
			this.calcByDay(pickingSum);
			dpsProductivities.addAll(pickingSum);
		}
		List<DpsProductivity> inspSum = this.getDpsWcsSum(WORK_TYPE_INSPECTION, fromDate, toDate);
		if (ValueUtil.isNotEmpty(inspSum)) {
			this.calcByDay(inspSum);
			dpsProductivities.addAll(inspSum);
		}
		
		Collections.sort(dpsProductivities);
		ResponseObj resp = new ResponseObj();
		resp.setItems(dpsProductivities);
		return resp;
	}
	
	private List<DpsProductivity> getDpsWcsSum(String type, String fromDate, String toDate) throws Exception {
		String dateSql = "";
		IQueryManager queryManager = this.queryManager;
		if (WORK_TYPE_TOTAL_PICKING.equals(type)) {
			dateSql = FnfUtils.queryCustServiceWithError("dps_total_picking_ps_by_day");
			queryManager = this.getDataSourceQueryManager(WmsMheHr.class);
		} else if (WORK_TYPE_LOAD_STOCK.equals(type)) {
			dateSql = FnfUtils.queryCustServiceWithError("dps_load_stock_ps_by_day");
		} else if (WORK_TYPE_PICKING.equals(type)) {
			dateSql = FnfUtils.queryCustServiceWithError("dps_picking_ps_by_day");
		} else if (WORK_TYPE_INSPECTION.equals(type)) {
			dateSql = FnfUtils.queryCustServiceWithError("dps_inspection_ps_by_day");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		List<DpsProductivity> dpsPrdSum = queryManager.selectListBySql(dateSql, params, DpsProductivity.class, 0, 50000);
		if (ValueUtil.isEmpty(dpsPrdSum)) {
			return null;
		}
		
		return dpsPrdSum;
	}

	private void calcByDay(List<DpsProductivity> dpsPrdSum) throws Exception {
		for (DpsProductivity obj: dpsPrdSum) {
			obj.setPh(obj.getDoneQty()/obj.getWorkHours());
			obj.setPhp(obj.getPh() / (obj.getWorkers()/obj.getWorkHours()));
		}
	}
	
	protected Query parseQuery(Class<?> entityClass, Integer page, Integer limit, String select, String sort, String query) {
		Query queryObj = new Query();
		queryObj.setPageIndex(page == null ? 1 : page.intValue());
		limit = (limit == null) ? ValueUtil.toInteger(SettingUtil.getValue(SysConfigConstants.SCREEN_PAGE_LIMIT, "50")) : limit.intValue();
		queryObj.setPageSize(limit);

		if (ValueUtil.isNotEmpty(select)) {
			List<String> selectList = new ArrayList<String>(Arrays.asList(select.split(SysConstants.COMMA)));
			Resource extResource = ResourceUtil.findExtResource(entityClass.getSimpleName());
			// 확정 컬럼 정보가 존재하지 않을 경우, 기본 검색 항목에 추가 
			if (ValueUtil.isEmpty(extResource) || ValueUtil.isEmpty(extResource.getId())) {
				queryObj.setSelect(selectList);
				
			} else {
				List<String> masterColumnList = new ArrayList<String>();
				List<String> extColumnList = new ArrayList<String>();
				List<String> extColumns = ResourceUtil.resourceColumnNames(extResource.getName());

				for (String column : selectList) {
					if (extColumns.contains(column)) {
						extColumnList.add(column);
					} else {
						masterColumnList.add(column);
					}
				}

				queryObj.setSelect(masterColumnList);
				queryObj.setExtselect(extColumnList);
			}
		}

		if (ValueUtil.isNotEmpty(sort)) {
			queryObj.addOrder(this.jsonParser.parse(sort, Order[].class));
		}

		if (limit >= 0 && ValueUtil.isNotEmpty(query)) {
			queryObj.addFilter(this.jsonParser.parse(query, Filter[].class));
		}
		
		return queryObj;
	}
}
