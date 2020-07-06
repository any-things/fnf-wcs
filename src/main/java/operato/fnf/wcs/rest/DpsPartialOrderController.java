package operato.fnf.wcs.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WmsDpsPartialOrder;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dbist.util.StringJoiner;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/dps_partial_order")
@ServiceDesc(description = "DpsPartialOrder Service API")
public class DpsPartialOrderController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return WmsDpsPartialOrder.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		// 1. query 변환 
		Map<String,Object> params = AnyValueUtil.queryToParamMap(query);

		
		// 2. 러닝 배치 리스트 
		List<String> runningBatchs = this.getRunningBatchList();
		params.put("whCd", "ICF");
		
		// 2.1. 러닝 배치가 있으면 기존 배치에 강제 할당 주문 ID 구하기 
		if(AnyValueUtil.isNotEmpty(runningBatchs)) {
			
			params.put("batchIds", runningBatchs);
			
			// 2.2.기존에 강제 할당 설정한 주문 정보 조회 
			String mheDrAssignYSql = "select distinct ref_no from mhe_dr where wh_cd = :whCd and work_unit in (:batchIds) and dps_partial_assign_yn='Y' #if($ref_no) and ref_no =:ref_no #end ";
			List<String> assignPartialOrderList = this.queryManager.selectListBySql(mheDrAssignYSql, params, String.class, 0, 0);
			if(AnyValueUtil.isNotEmpty(assignPartialOrderList)) {
				// 3.1 기존에 강제할당된 주문이 있을 경우에만 parameter 추가 
				params.put("assignPartialOrderList", assignPartialOrderList);
			}
		}
		
		
		// 3. WMS 부분 할당 테이블에서 데이터 조회 
		StringJoiner dpsPartialSql = new StringJoiner("\n");
		dpsPartialSql.add("")
		 .add("SELECT WH_CD, REF_NO, SHIPTO_NM, OUTB_ECT_QTY, TO_PICK_QTY, 'false' AS ASSIGN_YN")
		 .add("  FROM DPS_PARTIAL_ORDERS")
		 .add(" WHERE 1 = 1")
		 .add("   AND WH_CD = :whCd ")
		 .add(" #if($ref_no)")
		 .add("   AND REF_NO = :ref_no ")
		 .add(" #end")
		 .add(" #if($assignPartialOrderList)")
		 .add("   AND REF_NO not in (:assignPartialOrderList) ")
		 .add(" #end");
		 
		// 4. 조회 
		return this.queryManager.selectPageBySql(dpsPartialSql.toString(), params, this.entityClass(), page, limit);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<WmsDpsPartialOrder> list) {
		
		// 1. 러닝 배치 리스트 
		List<String> runningBatchs = this.getRunningBatchList();

		Query condition = new Query();
		condition.addSelect("id","outbNo","shiptoId","locationCd","itemCd");
		condition.addFilter("work_unit","in",runningBatchs);
		
		// 2. update 대상 list 생성 
		List<WcsMheDr> updateList = new ArrayList<WcsMheDr>();
		for(WmsDpsPartialOrder partialOrder : list) {
			if(ValueUtil.isEqualIgnoreCase(partialOrder.getCudFlag_(), OrmConstants.CUD_FLAG_UPDATE)) {
				// 2.1 mheDr 에서 refNo 검색 
				condition.removeFilter("ref_no");
				condition.addFilter("ref_no", partialOrder.getRefNo());
				
				List<WcsMheDr> orderList = this.queryManager.selectList(WcsMheDr.class, condition);
				orderList.forEach(item -> item.setDpsPartialAssignYn("Y"));
				updateList.addAll(orderList);
			}
		}
		
		// 3. update배치 
		AnyOrmUtil.updateBatch(updateList, 100, "dpsPartialAssignYn");
		
		return true;
	}
	
	/**
	 * 작업 중인 배치 리스트 가져오기 
	 * @return
	 */
	private List<String> getRunningBatchList(){
		Map<String,Object> params = ValueUtil.newMap("domainId,status,jobType", Domain.currentDomainId(), JobBatch.STATUS_RUNNING, LogisConstants.JOB_TYPE_DPS);
		String qry = "select id from job_batches where domain_id =:domainId and status = :status and job_type = :jobType ";
		return this.queryManager.selectListBySql(qry, params, String.class, 0, 0);
	}
}