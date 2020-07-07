package xyz.anythings.base.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.service.impl.PreprocessService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/order_preprocesses")
@ServiceDesc(description = "OrderPreprocess Service API")
public class OrderPreprocessController extends AbstractRestService {
	/**
	 * 주문 가공 서비스
	 */
	@Autowired
	private PreprocessService preprocessService;
	
	@Autowired
	private SmsQueryStore queryStore;

	@Override
	protected Class<?> entityClass() {
		return OrderPreprocess.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public OrderPreprocess findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public OrderPreprocess create(@RequestBody OrderPreprocess input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public OrderPreprocess update(@PathVariable("id") String id, @RequestBody OrderPreprocess input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<OrderPreprocess> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	/**
	 * 주문 가공 데이터 셋 조회
	 * MPS Manager > 주문 및 작업 > 작업 배치 -> 주문 가공 버튼 클릭시 DAS 주문 가공 화면 진입시 호출
	 * 
	 * @param select
	 * @param sort
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "/index_all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search All By Search Conditions")
	public Map<String, ?> indexAll(
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		// 1. 조회 조건 파싱 
		Query queryObj = this.parseQuery(OrderPreprocess.class, 0, 0, select, sort, query);
		// 2. 배치 ID 추출
		String batchId = AnyValueUtil.getFilterValue(queryObj, "id");
		
		if(batchId == null) {
			batchId = AnyValueUtil.getFilterValue(queryObj, "batchId");
		}
		// 3. 배치 ID 체크 
		if(batchId == null) {
			throw ThrowUtil.newNotAllowedEmptyInfo("terms.label.batch_id");
		}
		
		// 4. 검색 조건 설정 TODO 수정
		queryObj.removeFilter("id");
		queryObj.addFilter("batch_id", batchId);
		
		String comCd = AnyValueUtil.getFilterValue(queryObj, "comCd");
		String equipCd = AnyValueUtil.getFilterValue(queryObj, "equipCd");
		
		if(ValueUtil.isNotEmpty(comCd)) {
			queryObj.addFilter("com_cd", comCd);
		}
		
		if(ValueUtil.isNotEmpty(equipCd)) {
			queryObj.addFilter("equip_cd", equipCd);
		}		
		
		// 5. 작업 배치 조회
		JobBatch batch = this.checkBatch(batchId);
		// 6. 배치에 대한 주문 가공 정보 조회
		return this.preprocessService.buildPreprocessSet(batch, queryObj);
	}
	
	/**
	 * 호기 할당 (Anythings Manager > 작업 관리 > 작업 배치 > 호기 할당 버튼)
	 * 
	 * @param batchId
	 * @param items
	 * @param equipCds
	 * @return
	 */
	@RequestMapping(value = "/{batch_id}/assign/equip_level", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Assign first_step by order quantities")
	public List<OrderPreprocess> assignEquipList(
			@PathVariable("batch_id") String batchId, 
			@RequestBody List<OrderPreprocess> items,
			@RequestParam(name = "equip_cds", required = false) String equipCds,
			@RequestParam(name = "auto_assign", required = false) boolean autoAssign) {
		
		// 1. 배치 조회 
		JobBatch batch = this.checkBatch(batchId, items);
		// 2. 배치 상태 체크
		if(!ValueUtil.isEqualIgnoreCase(batch.getStatus(), JobBatch.STATUS_WAIT)) {
			// '주문 가공 대기' 상태가 아닙니다.
			throw new ElidomValidationException(MessageUtil.getTerm("terms.text.is_not_wait_state"));
		}
		// 3. 물량이 많은 거래처 순으로 자동으로 호기 지정을 한다.
		this.preprocessService.assignEquipLevel(batch, equipCds, items, autoAssign);
		// 4. 결과 리턴
		return items;
	}
	
	/**
	 * 셀 할당 (Anything Manager > 주문 및 작업 > 작업 배치 > DAS 주문 가공 화면 -> 주문 가공 완료 버튼)
	 * 
	 * @param batchId
	 * @param items
	 * @param equipType
	 * @param equipCds
	 * @return
	 */
	@RequestMapping(value = "/{batch_id}/assign/sub_equip_level/{equip_type}/{equip_cd}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Assign second step by order quantities")
	public List<OrderPreprocess> assignLocationsByAuto(
			@PathVariable("batch_id") String batchId,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestBody List<OrderPreprocess> items) {
		
		// 1. 배치 조회 
		JobBatch batch = this.checkBatch(batchId, items);
		// 2. 주문과 셀 매핑.
		this.preprocessService.assignSubEquipLevel(batch, equipType, equipCd, items);
		// 3. 결과 리턴
		return items;
	}
	
	/**
	 * 주문 가공 완료
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/complete_preprocess", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Complete preprocess")
	public List<JobBatch> completePreprocess(@PathVariable("id") String batchId) {

		JobBatch batch = this.checkBatch(batchId);
		return this.preprocessService.completePreprocess(batch);
	}
	
	/**
	 * 주문 가공 리셋
	 * 
	 * @param batchId
	 * @param resetAll
	 * @param items
	 * @return
	 */
	@RequestMapping(value = "/{id}/reset_preprocess", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Complete preprocess")
	public Map<String, Object> resetPreprocess(
			@PathVariable("id") String batchId,
			@RequestParam(name = "reset_all", required = false) boolean resetAll,
			@RequestBody List<Rack> items) {
		
		JobBatch batch = this.checkBatch(batchId);
		List<String> equipCdList = AnyValueUtil.filterValueListBy(items, "rackCd");
		this.preprocessService.resetPreprocess(batch, resetAll, equipCdList);
		return ValueUtil.newMap("result", SysConstants.OK_STRING);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{batch_id}/{chute_no}/cell_info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cell Info")
	public List<Map> findOrderStock(@PathVariable("chute_no") String chuteNo, @PathVariable("batch_id") String batchId) {
		String sql = queryStore.getSrtnCellInfo();
		Map<String, Object> params = ValueUtil.newMap("chuteNo,batchId", chuteNo, batchId);
		return this.queryManager.selectListBySql(sql, params,Map.class, 0, 0);
	}
	
	/**
	 * 주문 가공 Cell 바꾸기
	 * 
	 * @param batchId
	 * @param items
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}/swap_cell", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Swap CellCd")
	public Map<String, Object> swapCell(
			@PathVariable("id") String batchId,
			@RequestBody Map<String, Object> items) {
		
		JobBatch batch = this.checkBatchStatus(batchId);
		
		if(ValueUtil.isEmpty(items.get("prev_cell_cd"))) {
			throw ThrowUtil.newNotAllowedEmptyInfo("terms.label.cell_cd");
		} else if(ValueUtil.isEmpty(items.get("current_cell_cd"))) {
			throw ThrowUtil.newNotAllowedEmptyInfo("terms.label.cell_cd");
		}
		
		String prevCellCd = ValueUtil.toString(items.get("prev_cell_cd"));
		String currentCellCd = ValueUtil.toString(items.get("current_cell_cd"));
		
		String filterNames = "batchId,classCd";
		List<Object> prevFilterValues = ValueUtil.newList(batch.getId(), prevCellCd);
		List<Object> currentFilterValues = ValueUtil.newList(batch.getId(), currentCellCd);
		
		OrderPreprocess prevPreProcess = AnyEntityUtil.findEntityBy(batch.getDomainId(), false, OrderPreprocess.class, filterNames, prevFilterValues.toArray());
		OrderPreprocess currentPreProcess = AnyEntityUtil.findEntityBy(batch.getDomainId(), false, OrderPreprocess.class, filterNames, currentFilterValues.toArray());
		
		String prevChuteNo = prevPreProcess.getSubEquipCd();
		String currentChuteNo = currentPreProcess.getSubEquipCd();
		
		prevPreProcess.setSubEquipCd(currentChuteNo);
		prevPreProcess.setClassCd(currentCellCd);
		
		currentPreProcess.setSubEquipCd(prevChuteNo);
		currentPreProcess.setClassCd(prevCellCd);
		
		this.queryManager.update(prevPreProcess);
		this.queryManager.update(currentPreProcess);
		
		String sql = queryStore.getSrtnCellInfo();
		Map<String, Object> fromParams = ValueUtil.newMap("chuteNo,batchId", currentPreProcess.getSubEquipCd(), batchId);
		List<Map> fromCellInfo = this.queryManager.selectListBySql(sql, fromParams, Map.class, 0, 0);
		
		Map<String, Object> toParams = ValueUtil.newMap("chuteNo,batchId", prevPreProcess.getSubEquipCd(), batchId);
		List<Map> toCellInfo = this.queryManager.selectListBySql(sql, toParams, Map.class, 0, 0);
		
		return ValueUtil.newMap("fromChuteCellInfo,toChuteCellInfo", fromCellInfo, toCellInfo);
	}
	
	/**
	 * 주문 자동 가공
	 */
	@RequestMapping(value="/generate_order", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create")
	public int returnRegister(@RequestBody Map<String, Object> input) {
		JobBatch jobBatch = new JobBatch();
		jobBatch.setId(input.get("batchId").toString());
		jobBatch = this.queryManager.select(jobBatch);
		@SuppressWarnings("unchecked")
		Map<String, Object> chuteStatus = (Map<String, Object>) input.get("chuteStatus");
		return this.preprocessService.generatePreprocess(jobBatch, chuteStatus);
	}
	
	/**
	 * 작업 배치, 거래처 리스트 체크 
	 * 
	 * @param batchId
	 * @param items
	 * @return
	 */
	private JobBatch checkBatch(String batchId, List<OrderPreprocess> items) {
		// 1. 거래처 리스트 체크 
		if(ValueUtil.isEmpty(items)) {
			throw ThrowUtil.newNotFoundRecord("terms.label.assigned_cust");
		}
		
		// 2. 배치 조회 
		return this.checkBatch(batchId);
	}

	/**
	 * 작업 배치 존재 및 작업 유형 체크
	 *  
	 * @param batchId
	 * @return
	 */
	private JobBatch checkBatch(String batchId) {
//		JobBatch batch = AnyEntityUtil.findEntityByIdWithLock(true, JobBatch.class, batchId);
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, batchId);
		
		if(!LogisConstants.isB2BJobType(batch.getJobType())) {
			throw ThrowUtil.newNotSupportedMethodYet();
		}
		
		return batch;
	}
	
	private JobBatch checkBatchStatus(String batchId) {
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, batchId);
		
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_WAIT) && ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_READY)) {
			throw ThrowUtil.newInvalidStatus(batch.getStatus(), batch.getId(), JobBatch.STATUS_WAIT + ", " + JobBatch.STATUS_READY);
		}
		
		return batch;
	}
}