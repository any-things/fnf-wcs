package xyz.anythings.base.rest;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

import operato.logis.sms.SmsConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.BatchReceiptItem;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.service.impl.BatchService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/job_batches")
@ServiceDesc(description = "JobBatch Service API")
public class JobBatchController extends AbstractRestService {

	/**
	 * 서비스 디스패처
	 */
	@Autowired
	private LogisServiceDispatcher serviceDispatcher;
	/**
	 * 배치 서비스
	 */
	@Autowired
	private BatchService batchService;
	
	@Override
	protected Class<?> entityClass() {
		return JobBatch.class;
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
	public JobBatch findOne(@PathVariable("id") String id) {
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
	public JobBatch create(@RequestBody JobBatch input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public JobBatch update(@PathVariable("id") String id, @RequestBody JobBatch input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<JobBatch> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	/**
	 * 스테이지 별 작업 일자 별 작업 전체 진행 상황 
	 * 
	 * @param stageCd
	 * @param jobDate
	 * @return
	 */
	@RequestMapping(value = "/daily_progress_rate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Daily progress rate")
	public BatchProgressRate dailyProgressRate(
			@RequestParam(name = "stage_cd", required = true) String stageCd, 
			@RequestParam(name = "job_date", required = true) String jobDate) {
		
		return this.batchService.dailyProgressRate(Domain.currentDomainId(), stageCd, jobDate);
	}
	
	/**
	 * 작업 배치 별 진행 상황
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/progress_rate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Progress rate of job batch")
	public BatchProgressRate batchProgressRate(@RequestParam(name = "id", required = true) String id) {
		
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, id);
		return this.serviceDispatcher.getJobStatusService(batch).getBatchProgressSummary(batch);
	}
	
	/**
	 * 설비에서 진행 중인 작업 배치 조회
	 * 
	 * @param stageCd
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/running_batch/{stage_cd}/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find running batch of stage")
	public JobBatch findRunningBatch(
			@PathVariable(name = "stage_cd") String stageCd, 
			@PathVariable(name = "equip_type") String equipType,
			@PathVariable(name = "equip_cd") String equipCd) {
		
		return this.batchService.findRunningBatch(Domain.currentDomainId(), stageCd, equipType, equipCd);
	}
	
	/**
	 * 설비에서 진행 중인 메인 작업 배치 리스트 조회
	 * 
	 * @param stageCd
	 * @param jobType
	 * @param jobDate
	 * @return
	 */
	@RequestMapping(value = "/running_main_batch/{stage_cd}/{job_type}/{job_date}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find running main batch of stage")
	public List<JobBatch> findRunningMainBatches(
			@PathVariable(name = "stage_cd") String stageCd, 
			@PathVariable(name = "job_type") String jobType,
			@PathVariable(name = "job_date") String jobDate) {
		
		return this.batchService.searchRunningMainBatchList(Domain.currentDomainId(), stageCd, jobType, jobDate);
	}
	
	/**
	 * 설비에서 진행 중인 작업 배치 조회
	 * 
	 * @param stageCd
	 * @param jobType
	 * @param jobDate
	 * @return
	 */
	@RequestMapping(value = "/running_batches/{stage_cd}/{job_type}/{job_date}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search running batches of stage")
	public List<JobBatch> searchRunningBatchList(
			@PathVariable(name = "stage_cd") String stageCd, 
			@PathVariable(name = "job_type") String jobType,
			@PathVariable(name = "job_date") String jobDate) {
		
		return this.batchService.searchRunningBatchList(Domain.currentDomainId(), stageCd, jobType, jobDate);
	}
	
	/**
	 * 주문 수신 준비
	 * 
	 * @param areaCd
	 * @param stageCd
	 * @param comCd
	 * @param jobDate
	 * @return
	 */
	@RequestMapping(value = "/receive_batches/ready/{area_cd}/{stage_cd}/{com_cd}/{job_date}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Ready to receive batch orders")
	public BatchReceipt readyToReceiveOrders(
			@PathVariable("area_cd") String areaCd,
			@PathVariable("stage_cd") String stageCd,
			@PathVariable("com_cd") String comCd, 
			@PathVariable("job_date") String jobDate,
			@RequestParam(name = "job_type", required = false) String jobType) {

		return this.serviceDispatcher.getReceiveBatchService().readyToReceive(Domain.currentDomainId(), areaCd, stageCd, comCd, jobDate, jobType);
	}
	
	/**
	 * 배치 수신 시작
	 * 
	 * @param summary
	 * @return
	 */
	@RequestMapping(value = "/receive_batches/start", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Start to receive batch orders")
	public BatchReceipt startReceivingOrders(@RequestBody BatchReceipt summary) {

		return this.serviceDispatcher.getReceiveBatchService().startToReceive(summary);
	}
	
	/**
	 * 배치 수신 상태 조회 
	 * @param batchSummaryId
	 * @return
	 */
	@RequestMapping(value = "/receive_batches/rate/{batch_summary_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Receive batch orders Status")
	public BatchReceipt startReceivingOrders(@PathVariable("batch_summary_id") String batchSummaryId) {
		BatchReceipt summary = AnyEntityUtil.findEntityById(false, BatchReceipt.class, batchSummaryId);
		summary.setItems(AnyEntityUtil.searchDetails(Domain.currentDomainId(), BatchReceiptItem.class, "batchReceiptId", batchSummaryId));
		return summary;
	}
	
	/**
	 * 대상 분류 처리
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/target_class", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Target classing")
	public BaseResponse targetClass(@PathVariable("id") String batchId) {
		
		// 1. 작업 배치 조회 
		JobBatch batch = this.findWithLock(true, batchId, false);
		// 2. 대상 분류 작업 호출
		this.serviceDispatcher.getInstructionService(batch).targetClassing(batch);
		// 3. 대상 분류 결과 리턴 
		return new BaseResponse(true);
	}
	
	/**
	 * 작업 지시 팝업 시 팝업 화면에 표시를 위해 호출하는 데이터
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/instruction_data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Select batch instruction data")
	public Map<String, Object> searchInstructionData(@PathVariable("id") String batchId) {
		
		// 1. 작업 배치 조회 
		JobBatch batch = this.findWithLock(true, batchId, false);
		// 2. 작업 지시 데이터 조회
		return this.serviceDispatcher.getInstructionService(batch).searchInstructionData(batch);
	}
	
	/**
	 * 작업 지시 처리
	 * 
	 * @param batchId
	 * @param equipList
	 * @return
	 */
	@RequestMapping(value = "/{id}/instruct/batch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> instructBatch(
			@PathVariable("id") String batchId, 
			@RequestBody(required = false) List<String> equipList) {
		
		// 1. 작업 배치 조회
		JobBatch batch = this.findWithLock(true, batchId, true);
		// 2. 작업 지시  
		int createdCount = this.serviceDispatcher.getInstructionService(batch).instructBatch(batch, equipList);
		// 3. 작업 지시 결과 리턴
		return ValueUtil.newMap("result,count", SysConstants.OK_STRING, createdCount);
	}
	
	/**
	 * 작업 지시 여러 건 처리 
	 * 
	 * @param batchList
	 * @return
	 */
	@RequestMapping(value = "/instruct/batches", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> instructBatches(@RequestBody(required = true) List<JobBatch> batchList) {
		// 1. 작업 지시 처리
		for(JobBatch batch : batchList) {
			this.instructBatch(batch.getId(), null);
		}
		
		// 2. 작업 지시 결과 리턴
		return ValueUtil.newMap("result,count", SysConstants.OK_STRING, batchList.size());
	}
	
	/**
	 * 토탈 피킹 지시 처리
	 * 
	 * @param batchId
	 * @param equipIdList
	 * @return
	 */
	@RequestMapping(value = "/{id}/instruct/total_picking", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> instructTotalPicking(
			@PathVariable("id") String batchId, 
			@RequestBody(required = false) List<String> equipIdList) {
		
		// 1. 작업 배치 조회
		JobBatch batch = this.findWithLock(true, batchId, true);
		// 2. 작업 지시 
		int count = this.serviceDispatcher.getInstructionService(batch).instructTotalpicking(batch, equipIdList);
		// 3. 작업 지시 결과 리턴
		return ValueUtil.newMap("result,count", SysConstants.OK_STRING, count);
	}
	
	/**
	 * 작업 지시 여러 건 처리 
	 * 
	 * @param batchList
	 * @return
	 */
	@RequestMapping(value = "/instruct/total_pickings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> instructTotalPickings(@RequestBody(required = true) List<JobBatch> batchList) {
		// 1. 작업 지시 처리
		for(JobBatch batch : batchList) {
			this.instructTotalPicking(batch.getId(), null);
		}
		
		// 2. 작업 지시 결과 리턴
		return ValueUtil.newMap("result,count", SysConstants.OK_STRING, batchList.size());
	}
	
	/**
	 * 작업 병합
	 * 
	 * @param sourceBatchId
	 * @param mainBatchId
	 * @return
	 */
	@RequestMapping(value = "/{source_batch_id}/instruct/merge_batch/{main_batch_id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Merge batch")
	public Map<String, Object> mergeBatch(
			@PathVariable("source_batch_id") String sourceBatchId,
			@PathVariable("main_batch_id") String mainBatchId) {
		
		// 1. 병합할 메인 배치 정보 조회 
		JobBatch mainBatch = this.findWithLock(true, mainBatchId, true);
		// 2. 병합될 배치 정보 조회 
		JobBatch sourceBatch = this.findWithLock(true, sourceBatchId, true);
		// 3. 작업 배치 병합
		int mergedCnt = this.serviceDispatcher.getInstructionService(mainBatch).mergeBatch(mainBatch, sourceBatch);
		// 4. 결과 리턴
		return ValueUtil.newMap("result,count", SysConstants.OK_STRING, mergedCnt);
	}
	
	/**
	 * 작업 지시 취소 
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/instruct/cancel", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cancel batch instruction")
	public Map<String, Object> cancelInstructionBatch(@PathVariable("id") String batchId) {
		
		// 1. 작업 배치 조회 
		JobBatch batch = this.findWithLock(true, batchId, true);
		// 2. 작업 지시 취소
		int count = this.serviceDispatcher.getInstructionService(batch).cancelInstructionBatch(batch);
		// 3. 작업 지시 결과 리턴
		return ValueUtil.newMap("result,count", SysConstants.OK_STRING, count);		
	}
	
	/**
	 * 배치 마감
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/close_batch", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Close batch")
	public Map<String, Object> closeBatch(@RequestParam(name = "id", required = true) String batchId) {

		// 1. JobBatch 조회 
		JobBatch batch = this.findWithLock(true, batchId, true);
		
		// 2. 작업 배치 마감
		try {
			this.batchService.closeBatch(batch, false);
		} catch (ElidomValidationException eve) {
			return ValueUtil.newMap("result,msg", LogisConstants.NG_STRING, eve.getMessage()); 
		}
		// 3. 결과 리턴
		return ValueUtil.newMap("result,msg", LogisConstants.OK_STRING, LogisConstants.OK_STRING);
	}
	
	/**
	 * 강제 작업 배치 마감 
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}/close_batch_forcibly", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Close batch forcibly")
	public Map<String, Object> closeBatchForcibly(@PathVariable("id") String batchId) {
		
		// 1. JobBatch 조회 
		JobBatch batch = this.findWithLock(true, batchId, true);
		// 2. 작업 배치 마감
		this.batchService.closeBatch(batch, true);
		// 3. 결과 리턴
		return ValueUtil.newMap("result", SysConstants.OK_STRING);
	}
	
	/**
	 * 작업 배치 수신 취소 
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/cancel_batch", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cancel received batch")
	public Map<String, Object> cancelBatch(@PathVariable("id") String batchId) {
		
		// 1. JobBatch 조회 
		JobBatch batch = this.findWithLock(true, batchId, true);
		// 2. 작업 배치 마감
		int count = this.serviceDispatcher.getReceiveBatchService().cancelBatch(batch);
		// 3. 작업 배치 수신 취소
		return ValueUtil.newMap("result,cancel_count", SysConstants.OK_STRING, count);
	}
	
	/**
	 * 배치 그룹 ID로 배치 작업을 모두 찾아 배치 마감
	 * 
	 * @param batchGroupId
	 * @param forcibly
	 * @return
	 */
	@RequestMapping(value = "/{batch_group_id}/close_batches/by_group", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Complete batch by batch group id")
	public Map<String, Object> closeBatchesByGroup(
			@PathVariable("batch_group_id") String batchGroupId, 
			@RequestParam(name = "forcibly", required = false) boolean forcibly) {
		
		int count = this.batchService.closeBatchGroup(Domain.currentDomainId(), batchGroupId, forcibly);
		return ValueUtil.newMap("result,msg,count", SysConstants.OK_STRING, SysConstants.OK_STRING, count);
	}

	/**
	 * 작업 배치를 락을 걸면서 조회
	 * 
	 * @param exceptionWhenEmpty
	 * @param batchId
	 * @param findConfigSet
	 * @return
	 */
	private JobBatch findWithLock(boolean exceptionWhenEmpty, String batchId, boolean findConfigSet) {
		JobBatch batch = AnyEntityUtil.findEntityByIdByUnselectedWithLock(exceptionWhenEmpty, JobBatch.class, batchId, "jobConfigSet", "indConfigSet");
		
		if(batch == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.JobBatch", batchId);
		} else {
			if(findConfigSet) {
				if(ValueUtil.isNotEmpty(batch.getIndConfigSetId())) {
					batch.setIndConfigSet(AnyEntityUtil.findEntityById(false, IndConfigSet.class, batch.getIndConfigSetId()));
				}
				
				if(ValueUtil.isNotEmpty(batch.getJobConfigSetId())) {
					batch.setJobConfigSet(AnyEntityUtil.findEntityById(false, JobConfigSet.class, batch.getJobConfigSetId()));
				}
			}
		}
		
		return batch;
	}
	
	/**
	 * 배치 일시정지
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/pause_batch", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Pause batch")
	public Map<String, Object> pauseBatch(@RequestParam(name = "id", required = true) String batchId) {

		// 1. JobBatch 조회 
		JobBatch batch = this.findWithLock(true, batchId, true);
		batch.setStatus(JobBatch.STATUS_PAUSE);
		this.queryManager.update(batch);
		// 2. Rack, Cell 초기화
		try {
			Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getBatchGroupId());
		  	this.queryManager.executeBySql("UPDATE RACKS SET STATUS = null, BATCH_ID = null WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", params);
		  	this.queryManager.executeBySql("UPDATE CELLS SET CLASS_CD = null, BATCH_ID = null, BRAND_CD = null WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", params);
		} catch (ElidomValidationException eve) {
			return ValueUtil.newMap("result,msg", LogisConstants.NG_STRING, eve.getMessage()); 
		}
		// 3. 결과 리턴
		return ValueUtil.newMap("result,msg", LogisConstants.OK_STRING, LogisConstants.OK_STRING);
	}
	
	/**
	 * 배치 일시정지
	 * 
	 * @param batchId
	 * @return
	 */
	@RequestMapping(value = "/{id}/restart_batch", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Restart batch")
	public Map<String, Object> restartBatch(@RequestParam(name = "id", required = true) String batchId) {

		// 1. JobBatch 조회 
		JobBatch batch = this.findWithLock(true, batchId, true);
		batch.setStatus(JobBatch.STATUS_RUNNING);
		this.queryManager.update(batch);
		
		String jobBatchSql = "select * from job_batches where batch_group_id = :batchGroupId";
		Map<String, Object> query = ValueUtil.newMap("batchGroupId", batch.getBatchGroupId());
		List<JobBatch> jobBatches = this.queryManager.selectListBySql(jobBatchSql, query, JobBatch.class, 0, 0);
		List<String> batchList = AnyValueUtil.filterValueListBy(jobBatches, "id");
		
		StringJoiner updateSql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		
		if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SRTN)) {
			updateSql.add("update");
			updateSql.add("	cells");
			updateSql.add("set batch_id = a.batch_no, class_cd = a.item_cd, brand_cd = a.strr_id");
			updateSql.add("from");
			updateSql.add("	(select batch_no, cell_no, item_cd, strr_id from mhe_das_order where batch_no in ( :batchList ) group by batch_no, cell_no, strr_id, item_cd) as a");
			updateSql.add("where");
			updateSql.add("cells.cell_cd = a.cell_no");
		} else {
			updateSql.add("update");
			updateSql.add("	cells");
			updateSql.add("set batch_id = a.batch_no, class_cd = a.shop_cd, brand_cd = a.strr_id");
			updateSql.add("from");
			updateSql.add("	(select batch_no, cell_no, shop_cd, strr_id from mhe_das_order where batch_no in ( :batchList ) group by batch_no, cell_no, strr_id, shop_cd) as a");
			updateSql.add("where");
			updateSql.add("cells.cell_cd = a.cell_no");
		}
		Map<String, Object> updateParams = ValueUtil.newMap("batchList,status", batchList, JobBatch.STATUS_RUNNING);

		StringJoiner updateRackSql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		updateRackSql.add("update");
		updateRackSql.add("	racks");
		updateRackSql.add("set status = :status, batch_id = a.batch_id ");
		updateRackSql.add("from");
		updateRackSql.add("	(select * from cells where batch_id in ( :batchList )) as a");
		updateRackSql.add("where");
		updateRackSql.add("	racks.rack_cd = a.equip_cd");
		
		// 2. 작업 배치 마감
		try {
			this.queryManager.executeBySql(updateSql.toString(), updateParams);
			this.queryManager.executeBySql(updateRackSql.toString(), updateParams);
		} catch (ElidomValidationException eve) {
			return ValueUtil.newMap("result,msg", LogisConstants.NG_STRING, eve.getMessage()); 
		}
		// 3. 결과 리턴
		return ValueUtil.newMap("result,msg", LogisConstants.OK_STRING, LogisConstants.OK_STRING);
	}
}