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
import operato.fnf.wcs.service.model.DpsProductivity;
import operato.logis.sms.SmsConstants;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.util.ResourceUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.ValueUtil;
import xyz.elidom.util.converter.msg.IJsonParser;

@Component
public class CalcSmsProductivity extends AbstractQueryService {

	@Autowired
	@Qualifier("under_to_camel")
	protected IJsonParser jsonParser;
	
	public ResponseObj calcSmsProductivity(Map<String, Object> params) throws Exception {
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
					toDate = String.valueOf(filter.getValue()).split(",")[1];
				}
			}
		}
		
		FnfUtils.checkValueEmpty("fromDate", fromDate, "toDate", toDate);
		
		
		
		String workTimeSql = FnfUtils.queryCustServiceWithCheck("sms_work_time");//min
		String totalWorkersSql = FnfUtils.queryCustServiceWithCheck("sms_total_workers");
		String itemCdCntSql = FnfUtils.queryCustServiceWithCheck("sms_total_items");
		String resultQtySql = FnfUtils.queryCustServiceWithCheck("sms_total_qty");
		
		Map<String, Object> sqlParams = new HashMap<>();
		sqlParams.put("fromDate", fromDate);
		sqlParams.put("toDate", toDate);
		sqlParams.put("jobType", ValueUtil.toList(SmsConstants.JOB_TYPE_SRTN, SmsConstants.JOB_TYPE_SDAS, SmsConstants.JOB_TYPE_SDPS));
		
		List<DpsProductivity> smsProductivities = this.queryManager.selectListBySql(workTimeSql, sqlParams, DpsProductivity.class, 0, 50000);
		
		if (ValueUtil.isEmpty(smsProductivities)) {
			return null;
		}
		
		List<DpsProductivity> smsTotalWorkers = this.queryManager.selectListBySql(totalWorkersSql, sqlParams, DpsProductivity.class, 0, 50000);
		List<DpsProductivity> smsItemCnt = this.queryManager.selectListBySql(itemCdCntSql, sqlParams, DpsProductivity.class, 0, 50000);
		List<DpsProductivity> smsResultQty = this.queryManager.selectListBySql(resultQtySql, sqlParams, DpsProductivity.class, 0, 50000);
		
		for (DpsProductivity obj: smsProductivities) {
			for (DpsProductivity smsWorker : smsTotalWorkers) {
				if(ValueUtil.isEqualIgnoreCase(obj.getWorkDate(), smsWorker.getWorkDate())) {
					obj.setWorkers((float)Math.round(smsWorker.getWorkers()*100)/100);
					break;
				}
			}
			for (DpsProductivity smsItem : smsItemCnt) {
				if(ValueUtil.isEqualIgnoreCase(obj.getWorkDate(), smsItem.getWorkDate())) {
					obj.setItemCdCnt(smsItem.getItemCdCnt());
					break;
				}
			}
			for (DpsProductivity smsQty : smsResultQty) {
				if(ValueUtil.isEqualIgnoreCase(obj.getWorkDate(), smsQty.getWorkDate())) {
					obj.setDoneQty(smsQty.getDoneQty());
					break;
				}
			}
		}
		
		for (DpsProductivity obj: smsProductivities) {
			obj.setWorkMinutes((float)Math.round(obj.getWorkMinutes()*100)/100);
			obj.setWorkHours((float)Math.round(obj.getWorkHours()*100)/100);
			obj.setPh((float)Math.round(obj.getDoneQty() / obj.getWorkHours()*100)/100);
			if(obj.getWorkers() > 0) {
				obj.setPhp((float)Math.round(obj.getPh() / obj.getWorkers() *100)/100);
			} else {
				obj.setPhp(0);
			}
		}
		
		Collections.sort(smsProductivities);
		ResponseObj resp = new ResponseObj();
		resp.setItems(smsProductivities);
		return resp;
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
