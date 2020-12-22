package xyz.anythings.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
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

import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.classfy.ClassifyInEvent;
import xyz.anythings.base.event.classfy.ClassifyOutEvent;
import xyz.anythings.base.event.classfy.ClassifyRunEvent;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.model.Category;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.api.IIndicationService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.rest.DynamicControllerSupport;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 작업자 디바이스와의 인터페이스 API
 * 
 * @author shortstop
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/device_process")
@ServiceDesc(description = "Device Process Controller API")
public class DeviceProcessController extends DynamicControllerSupport {
	
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;
	/**
	 * 서비스 디스패처
	 */
	@Autowired
	private LogisServiceDispatcher serviceDispatcher;
	
	/**********************************************************************
	 * 								공통 API 
	 **********************************************************************/
	/**
	 * 장비 업데이트 하라는 메시지를 장비 타입별로 publish
	 * 
	 * @param deviceType
	 * @return
	 */
	@RequestMapping(value = "/publish/device_update/{device_type}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Publish device update message")
	public BaseResponse publishDeviceUpdate(@PathVariable("device_type") String deviceType) {
		
		this.serviceDispatcher.getDeviceService().sendDeviceUpdateMessage(Domain.currentDomainId(), deviceType);
		return new BaseResponse(true, LogisConstants.OK_STRING);
	}
	
	/**
	 * 디바이스 업데이트 릴리즈 노트를 조회
	 * 
	 * @param deviceType
	 * @return
	 */
	@RequestMapping(value = "/release_notes/{device_type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Release notes of device type")
	public List<Map<String, Object>> releaseNotesOfDevice(@PathVariable("device_type") String deviceType) {
		
		//List<String> releaseNotes = this.serviceDispatcher.getDeviceService().searchUpdateItems(Domain.currentDomainId(), deviceType);
		
		// TODO
		List<Map<String, Object>> releaseNotes = new ArrayList<Map<String, Object>>(5);
		releaseNotes.add(ValueUtil.newMap("seq,content", 1, "디자인 테마 변경"));
		releaseNotes.add(ValueUtil.newMap("seq,content", 2, "메뉴 아이콘 변경"));
		releaseNotes.add(ValueUtil.newMap("seq,content", 3, "모든 엔티티에 대해서 단 건 조회, 리스트 조회, 페이지네이션, 마스터 디테일 구조의 디테일 리스트 조회 공통 유틸리티 추가"));
		releaseNotes.add(ValueUtil.newMap("seq,content", 4, "Fixed : 디바이스 업데이트 시 오류 제거"));
		return releaseNotes;
	}

	/**
	 * 장비 타입별로 전달할 메시지를 publish
	 * 
	 * @param deviceType
	 * @param message
	 * @return
	 */
	@RequestMapping(value = "/publish/message/{device_type}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Publish device message")
	public BaseResponse publishDeviceMessage(@PathVariable("device_type") String deviceType, @RequestBody String message) {
		
		this.serviceDispatcher.getDeviceService().sendMessageToDevice(Domain.currentDomainId(), deviceType, message);
		return new BaseResponse(true, LogisConstants.OK_STRING);
	}
	
	/**
	 * 작업 배치의 작업 진행 요약 정보 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/batch_progress_rate/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Batch Progress Rate")
	public BatchProgressRate batchProgressRate(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).getBatchProgressSummary(batch);
	}
	
	/**
	 * 고객사 코드 및 상품 코드로 상품 조회
	 * 
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/find/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find sku for cliassification")
	public SKU findSkuForClassify(@PathVariable("com_cd") String comCd, @PathVariable("sku_cd") String skuCd) {
		
		long domainId = Domain.currentDomainId();
		return this.serviceDispatcher.getSkuSearchService().findSku(domainId, comCd, skuCd, true);
	}

	/**
	 * 분류 처리를 위한 설비 유형, 설비 코드 및 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_like/{equip_type}/{equip_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search sku for cliassification")
	public List<SKU> searchSkuCandidates(@PathVariable("equip_type") String equipType
										, @PathVariable("equip_cd") String equipCd
										, @PathVariable("sku_cd") String skuCd) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		return this.serviceDispatcher.getSkuSearchService().searchListInBatch(equipBatchSet.getBatch(), skuCd, true, true);
	}
	
	/**
	 * 배치 그룹 ID 내에서 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param batchId
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_batch/{batch_id}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search SKU List For Middle Classing")
	public List<SKU> searchSkuListByBatch(@PathVariable("batch_id") String batchId, @PathVariable("sku_cd") String skuCd) {
		
		JobBatch batch = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), batchId);
		return this.serviceDispatcher.getSkuSearchService().searchListInBatchGroup(batch, skuCd, true, true);
	}
	
	/**
	 * 배치 그룹 ID 내에서 상품 코드로 like 검색해서 상품 리스트 조회
	 * 
	 * @param batchGroupId
	 * @param skuCd
	 * @return
	 */
	@RequestMapping(value = "/sku/search_by_batch_group/{batch_group_id}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search SKU List For Middle Classing")
	public List<SKU> searchSkuListByBatchGroup(@PathVariable("batch_group_id") String batchGroupId, @PathVariable("sku_cd") String skuCd) {
		
		JobBatch batch = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), batchGroupId);
		return this.serviceDispatcher.getSkuSearchService().searchListInBatchGroup(batch, skuCd, true, true);
	}
	
	/**********************************************************************
	 * 								표시기 점/소등 API
	 **********************************************************************/
	
	/**
	 * 설비 소속 모든 표시기 OFF
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/indicators/off/all/{equip_type}/{equip_cd}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicators off all of equipment")
	public BaseResponse indicatorsOffAll(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		// 1. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 작업 배치 전체 표시기 소등
		this.serviceDispatcher.getIndicationService(batch).indicatorOffAll(batch);
		return new BaseResponse(true);
	}
	
	/**
	 * 장비 존 소속 모든 표시기 OFF
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @return
	 */
	@RequestMapping(value = "/indicators/off/{equip_type}/{equip_cd}/{station_cd}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicators off all of zone")
	public BaseResponse indicatorsOff(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("station_cd") String stationCd) {
		
		// 1. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 작업 배치 내 작업 스테이션 영역의 표시기 소등
		this.serviceDispatcher.getIndicationService(batch).indicatorListOff(batch.getDomainId(), batch.getStageCd(), equipType, equipCd, stationCd);
		return new BaseResponse(true);
	}

	/**
	 * 설비 별로 이전 작업 상태로 표시기 점등 상태 복원
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/indicators/restore/{equip_type}/{equip_cd}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Restore indicators of running job instances")
	public BaseResponse restoreIndicators(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		// 1. 설비 정보로 부터 작업 배치 조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 작업 배치 내 표시기 진행 중인 작업에 대한 재점등
		this.serviceDispatcher.getIndicationService(batch).restoreIndicatorsOn(batch);
		return new BaseResponse(true);
	}

	/**
	 * 투입 시퀀스, 장비 존 별 처리할 작업 / 처리한 작업 표시기 점등
	 * 
	 * @param jobInputId
	 * @param stationCd
	 * @param todoOrDone
	 * @return
	 */
	@RequestMapping(value = "/indicators/restore/{job_input_id}/{station_cd}/{mode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicators off all of station area")
	public BaseResponse restoreIndicators(
			@PathVariable("job_input_id") String jobInputId,
			@PathVariable("station_cd") String stationCd,
			@PathVariable("mode") String todoOrDone) {
		
		// 1. JobInput 조회
		JobInput input = AnyEntityUtil.findEntityById(true, JobInput.class, jobInputId);
		
		// 2. 설비 정보로 부터 작업 배치 조회
		JobBatch batch = LogisServiceUtil.checkRunningBatch(input.getDomainId(), input.getBatchId());
		
		// 3. 작업 배치 내 표시기 진행 중인 작업에 대한 재점등
		this.serviceDispatcher.getIndicationService(batch).restoreIndicatorsOn(batch, input.getInputSeq(), stationCd, todoOrDone);
		return new BaseResponse(true);
	}
	
	/**********************************************************************
	 * 								중분류 API  
	 **********************************************************************/
	
	/**
	 * 중분류 화면에서 배치 그룹 ID 하나를 선택하기 위해 스테이지 내 진행 중인 WMS 배치 ID 리스트를 조회
	 * 스테이지 내에 진행 중인 작업 배치 리스트를 스테이지 / 설비 그룹 / 작업 유형 / WMS 배치 ID로 그루핑하여 조회 
	 * 
	 * @param stageCd
	 * @param jobType
	 * @param jobDate
	 * @return
	 */
	@RequestMapping(value = "/running_batches/{stage_cd}/{job_type}/{job_date}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search running batch list of stage")
	public List<JobBatch> searchRunningBatchGroups(
			@PathVariable("stage_cd") String stageCd, 
			@PathVariable("job_type") String jobType, 
			@PathVariable("job_date") String jobDate) {
		
		// TODO 
		return null;
	}

	/**
	 * 중분류 처리
	 * 
	 * @param batchGroupId
	 * @param comCd
	 * @param skuCd
	 * @param weightFlag
	 * @param varQtyFlag
	 * @return
	 */
	@RequestMapping(value = "/categorize/{batch_group_id}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Categorize by batchGroupId, comCd, skuCd")
	public Category categorize(
			@PathVariable("batch_group_id") String batchGroupId,
			@PathVariable("com_cd") String comCd, 
			@PathVariable("sku_cd") String skuCd, 
			@RequestParam(name = "weight", required = false) Boolean weightFlag,
			@RequestParam(name = "var_qty_flag", required = false) Boolean varQtyFlag) {
		
		// TODO 
		return null;
	}
	
	/**
	 * 중분류 처리 - 중량 업데이트 
	 * 
	 * @param batchGroupId
	 * @param skuInfo
	 * @return
	 */
	@RequestMapping(value = "/categorize/{batch_group_id}/apply_weight", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Categorize - apply SKU Weight!")
	public Object updateSkuWeight(@PathVariable("batch_group_id") String batchGroupId, @RequestBody SKU skuInfo) {
		
		// TODO 
		return null;
	}
	
	/**********************************************************************
	 * 								박스 매핑 API 
	 **********************************************************************/
	/**
	 * 셀과 박스 ID 매핑 현황 조회
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/search/cell_mappings/{equip_type}/{equip_cd}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Cell & Box Mapping")
	public List<WorkCell> searchCellMappings(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd) {
		
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batch.getId());
		return this.queryManager.selectList(WorkCell.class, condition);
	}
	
	/**
	 * 셀과 박스 ID 매핑 (셀 코드 사용)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param subEquipCd
	 * @param boxId
	 * @param isSkipEquipCheck 셀이 equipCd에 소속되었는지 체크할 지 여부
	 * @param isSkipBoxMapping 박스 매핑을 스킵할 지 여부
	 * @return
	 */
	@RequestMapping(value = "/assign/cell_box/{equip_type}/{equip_cd}/{sub_equip_cd}/{box_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cell & Box Mapping")
	public Object boxMappingByCellCd(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("sub_equip_cd") String subEquipCd,
			@PathVariable("box_id") String boxId,
			@RequestParam(name="skip_equip_check", required = false) boolean isSkipEquipCheck,
			@RequestParam(name="skip_box_mapping", required = false) boolean isSkipBoxMapping) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getClassificationService(batch).boxCellMapping(batch, subEquipCd, boxId);
	}
	
	/**
	 * 셀과 박스 ID 매핑 (표시기 코드 사용)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param indCd
	 * @param boxId
	 * @param isSkipEquipCheck 셀이 equipCd에 소속되었는지 체크할 지 여부
	 * @param isSkipBoxMapping 박스 매핑을 스킵할 지 여부
	 * @return
	 */
	@RequestMapping(value = "/assign/ind_box/{equip_type}/{equip_cd}/{ind_cd}/{box_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cell & Box Mapping")
	public Object boxMappingByIndCd(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("ind_cd") String indCd, 
			@PathVariable("box_id") String boxId,
			@RequestParam(name="skip_equip_check", required = false) boolean isSkipEquipCheck,
			@RequestParam(name="skip_box_mapping", required = false) boolean isSkipBoxMapping) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		String sql = "select cell_cd from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd and indCd = :indCd";
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd,indCd", batch.getDomainId(), equipType, equipCd, indCd);
		String subEquipCd = this.queryManager.selectBySql(sql, params, String.class);
		return this.serviceDispatcher.getClassificationService(batch).boxCellMapping(batch, subEquipCd, boxId);
	}
	
	/**********************************************************************
	 * 								소분류 처리 API  
	 **********************************************************************/
	
	/**
	 * 작업 처리 ID (jobInstanceId)로 소분류 작업 처리
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @return
	 */
	@RequestMapping(value = "/classify/confirm/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Confirm classification")
	public BaseResponse confirmClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId) {
		
		// 1. 설비 정보로 부터 Batch조회 
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성 
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase()
				, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM, job, job.getPickQty(), job.getPickQty());
		
		// 4. 이벤트 발생 
		this.eventPublisher.publishEvent(event);
		
		// 5. 이벤트 처리 결과 리턴 
		if(!event.isExecuted()) {
			throw new ElidomServiceException();
		} else {
			return new BaseResponse(true,null, event.getResult());
		}
	}

	/**
	 * 작업 ID (jobInstanceId)로 소분류 작업 분할 처리
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @param reqQty
	 * @param resQty
	 * @return
	 */
	@RequestMapping(value = "/classify/split/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Confirm classification")
	public BaseResponse splitClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId, 
			@RequestParam(name = "req_qty", required = true) Integer reqQty,
			@RequestParam(name = "res_qty", required = true) Integer resQty) {

		// 1. Equip 으로 Batch조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회 
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(domainId, jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성 
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase()
				, LogisCodeConstants.CLASSIFICATION_ACTION_MODIFY, job
				, ValueUtil.isEmpty(reqQty) ? job.getPickQty() : reqQty
				, ValueUtil.isEmpty(resQty) ? 1 : resQty);
		
		// 4. 이벤트 발생
		this.eventPublisher.publishEvent(event);
		
		// 5. 이벤트 처리 결과 리턴
		if(!event.isExecuted()) {
			throw new ElidomServiceException();
		} else {
			return new BaseResponse(true, null, event.getResult());
		}
	}
	
	/**
	 * 작업 ID (jobInstanceId)로 소분류 작업 취소 처리
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @return
	 */
	@RequestMapping(value = "/classify/cancel/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Cancel classification")
	public BaseResponse cancelClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId) {

		// 1. Equip 으로 Batch조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase(), LogisCodeConstants.CLASSIFICATION_ACTION_CANCEL, job);
		
		// 4. 이벤트 발생
		this.eventPublisher.publishEvent(event);
		
		// 5. 이벤트 처리 결과 리턴
		if(!event.isExecuted()) {
			throw new ElidomServiceException();
		} else {
			return new BaseResponse(true,null, event.getResult());
		}
	}
	
	/**
	 * 소분류 확정 취소
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @return
	 */
	@RequestMapping(value = "/classify/undo/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Undo classification")
	public BaseResponse undoClassification(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId) {
		
		// 1. 설비 정보로 Batch조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성
		ClassifyRunEvent event = new ClassifyRunEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase(), LogisCodeConstants.CLASSIFICATION_ACTION_UNDO_PICK, job);
		
		// 4. 액션 실행
		this.serviceDispatcher.getClassificationService(batch).classify(event);
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**
	 * 풀 박스
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param jobInstanceId
	 * @param reqQty
	 * @param boxId
	 * @return
	 */
	@RequestMapping(value = "/fullbox/{device_type}/{equip_type}/{equip_cd}/{job_instance_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Fullbox")
	public BaseResponse fullboxing(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("job_instance_id") String jobInstanceId,
			@RequestParam(name = "req_qty", required = false) Integer reqQty,
			@RequestParam(name = "box_id", required = false) String boxId) {
		
		// 1. 설비 정보로 Batch조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInstance 조회
		JobInstance job = this.serviceDispatcher.getJobStatusService(batch).findPickingJob(batch.getDomainId(), jobInstanceId);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.label.job", jobInstanceId);
		}
		
		// 3. 소분류 이벤트 생성
		ClassifyOutEvent event = new ClassifyOutEvent(batch, SysEvent.EVENT_STEP_ALONE
				, deviceType.toLowerCase()
				, LogisCodeConstants.CLASSIFICATION_ACTION_FULL, job
				, ValueUtil.isEmpty(reqQty) ? 0 : reqQty
				, 1);
		
		event.getWorkCell().setBoxId(boxId);
		event.setBoxId(boxId);
		
		// 4. 액션 실행
		this.serviceDispatcher.getClassificationService(batch).classify(event);
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**
	 * 일괄 풀 박스
	 * 
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	@RequestMapping(value = "/fullbox_all/{equip_type}/{equip_cd}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Batch fullbox")
	public BaseResponse batchFullbox(@PathVariable("equip_type") String equipType,  @PathVariable("equip_cd") String equipCd) {
		
		// 1. 설비 정보로 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 배치 풀 박스 처리
		this.serviceDispatcher.getAssortService(batch).getBoxingService().batchBoxing(batch);
		
		// 3. 응답
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**
	 * 풀 박스 취소
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param cellCd
	 * @param boxId
	 * @return
	 */
	@RequestMapping(value = "/fullbox/undo/{device_type}/{equip_type}/{equip_cd}/{cell_cd}/{box_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Undo fullbox")
	public BaseResponse undoFullboxing(
			@PathVariable("device_type") String deviceType,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("cell_cd") String cellCd, 
			@PathVariable("box_id") String boxId) {
		
		// 1. 설비 정보로 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 박스 조회
		BoxPack box = AnyEntityUtil.findEntityById(false, BoxPack.class, boxId);
		
		if(box == null) {
			box = AnyEntityUtil.findEntityBy(batch.getDomainId(), true, true, BoxPack.class, null, "domainId,batchId,boxId", batch.getDomainId(), batch.getId(), boxId);
			this.serviceDispatcher.getAssortService(batch).cancelBoxing(batch.getDomainId(), box);
		}
		
		// 3. 액션 실행
		return new BaseResponse(true, SysConstants.OK_STRING, null);
	}
	
	/**********************************************************************
	 * 								작업 데이터 조회 API  
	 **********************************************************************/
	
	/**
	 * 호기 범위 내 혹은 작업 존 범위 내 상태별 투입 리스트를 조회 (페이지네이션)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param status 상태 - 빈 값: 전체 보기, U: 미완료인 것만 보기
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/search/input_pages/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input list")
	public Page<JobInput> searchInputPages(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).paginateInputList(batch, equipCd, stationCd, status, page, limit);
	}

	
	/**
	 * 태블릿 피킹 화면 하단 작업 스테이션 별 투입 리스트 조회 (리스트)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param selectedInputId
	 * @return
	 */
	@RequestMapping(value = "/search/input_list/{equip_type}/{equip_cd}/{station_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input list")
	public List<JobInput> searchInputList(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("station_cd") String stationCd,
			@RequestParam(name = "selected_input_id", required = false) String selectedInputId) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		return this.serviceDispatcher.getJobStatusService(batch).searchInputList(batch, equipCd, stationCd, selectedInputId);
	}
	
	/**
	 * 작업 배치내 작업 중인 투입 정보의 작업 리스트를 조회
	 * 
	 * @param jobInputId
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param indOnYn
	 * @return
	 */
	@RequestMapping(value = "/search/input_jobs/{job_input_id}/{equip_type}/{equip_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Input Job list")
	public List<JobInstance> searchInputJobs(
			@PathVariable("job_input_id") String jobInputId,
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@RequestParam(name = "station_cd", required = false) String stationCd,
			@RequestParam(name = "ind_on_yn", required = false) String indOnYn) {
		
		// 1. 작업 배치 체크 및 조회
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. JobInput 조회
		JobInput input = AnyEntityUtil.findEntityBy(domainId, true, JobInput.class, null, "id", jobInputId);
		
		// 3. 서비스 호출 
		List<JobInstance> jobList = this.serviceDispatcher.getJobStatusService(batch).searchInputJobList(batch, input, stationCd);
		boolean indOnFlag = ValueUtil.isEqualIgnoreCase(indOnYn, LogisConstants.Y_CAP_STRING);
		
		// 4. 표시기 점등
		if(indOnFlag) {
			// 해당 스테이션에 존재하는 모든 피킹 중인 작업 리스트를 조회하여 소등
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			indSvc.indicatorListOff(batch, stationCd);
			
			// 작업 리스트 중에 피킹 상태인 작업만 점등 
			List<JobInstance> jobsToIndOn = jobList.stream().filter(job -> ValueUtil.isEqualIgnoreCase(job.getStatus(), LogisConstants.JOB_STATUS_PICKING)).collect(Collectors.toList());
			indSvc.indicatorsOn(batch, false, jobsToIndOn);
		}
		
		// 5. 작업 리스트 리턴
		return jobList;
	}
	
	/**
	 * 상품 코드 스캔으로 상품 투입
	 * 
	 * @param equipType 설비 유형
	 * @param equipCd 설비 코드
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드
	 * @param page 현재 페이지
	 * @param limit 페이지 당 레코드 수
	 * @param status
	 * @return
	 */
	@RequestMapping(value = "/input/sku/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Input SKU")
	public Object inputSKU(
			@PathVariable("equip_type") String equipType, 
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "status", required = false) String status) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		IClassifyInEvent inputEvent = new ClassifyInEvent(equipBatchSet.getBatch(), 
				SysEvent.EVENT_STEP_ALONE, false, LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU, skuCd, 1);
		inputEvent.setComCd(comCd);
		return this.serviceDispatcher.getClassificationService(equipBatchSet.getBatch()).input(inputEvent);
	}
	
	/**
	 * 박스 코드 스캔으로 박스 투입
	 * 
	 * @param equipType 설비 유형
	 * @param equipCd 설비 코드
	 * @param boxId 박스 ID
	 * @return
	 */
	@RequestMapping(value = "/input/box/{equip_type}/{equip_cd}/{box_id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Input Box")
	public Object inputBox(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("box_id") String boxId) {
		
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		IClassifyInEvent inputEvent = new ClassifyInEvent(equipBatchSet.getBatch(),
				SysEvent.EVENT_STEP_ALONE, false, LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_BOX, boxId, 1);
		return this.serviceDispatcher.getClassificationService(equipBatchSet.getBatch()).input(inputEvent);
	}
	
	/**********************************************************************
	 * 								박스 관련 API  
	 **********************************************************************/
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (페이지네이션)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param equipZone
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/paginate/box_list/{equip_type}/{equip_cd}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Paginate box list")
	public Page<BoxPack> paginateBoxList(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd, 
			@PathVariable("equip_zone") String equipZone,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, page, limit);
		condition.addFilter("batchId", equipBatchSet.getBatch().getId());
		return this.queryManager.selectPage(BoxPack.class, condition);
	}
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (페이지네이션)
	 * 
	 * @param batchId
	 * @param equipZone
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/paginate/box_list/{batch_id}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Paginate box list")
	public Page<BoxPack> paginateBoxList(
			@PathVariable("batch_id") String batchId,
			@PathVariable("equip_zone") String equipZone,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit) {
		
		Long domainId = Domain.currentDomainId();
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, page, limit);
		condition.addFilter("batchId", batchId);
		return this.queryManager.selectPage(BoxPack.class, condition);
	}
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (리스트)
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param equipZone
	 * @return
	 */
	@RequestMapping(value = "/search/box_list/{equip_type}/{equip_cd}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search box list")
	public List<BoxPack> searchBoxList(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("equip_zone") String equipZone) {
		
		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(Domain.currentDomainId(), equipType, equipCd);
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", equipBatchSet.getBatch().getId());
		return this.queryManager.selectList(BoxPack.class, condition);
	}
	
	/**
	 * 설비에서 분류 처리된 박스 리스트 조회 (페이지네이션)
	 * 
	 * @param batchId
	 * @param equipZone
	 * @return
	 */
	@RequestMapping(value = "/search/box_list/{batch_id}/{equip_zone}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search box list")
	public List<BoxPack> searchBoxList(
			@PathVariable("batch_id") String batchId, 
			@PathVariable("equip_zone") String equipZone) {
		
		Long domainId = Domain.currentDomainId();
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batchId);
		return this.queryManager.selectList(BoxPack.class, condition);
	}
	
	/**
	 * 박스 처리 ID로 박스 내품 내역 리스트 조회
	 * 
	 * @param boxPackId
	 * @return
	 */
	@RequestMapping(value = "/search/box_items/{box_pack_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search box items")
	public List<BoxItem> searchBoxItems(@PathVariable("box_pack_id") String boxPackId) {
		
		BoxPack boxPack = this.queryManager.select(BoxPack.class, boxPackId);
		if(boxPack != null) {
			boxPack.searchBoxItems();
		}
		
		return boxPack == null ? null : boxPack.getItems();
	}

	/**
	 * 박스 라벨 재발행
	 * 
	 * @param equipType
	 * @param equipCd
	 * @param boxPackId
	 * @param printerId
	 * @return
	 */
	@RequestMapping(value = "/reprint/box_label/{equip_type}/{equip_cd}/{box_pack_id}/{printer_id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Reprint Box Label")
	public BaseResponse reprintBoxLabel(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("box_pack_id") String boxPackId,
			@PathVariable("printer_id") String printerId) {
		
		BoxPack boxPack = this.queryManager.select(BoxPack.class, boxPackId);
		if(boxPack != null) {
			PrintEvent printEvent = new PrintEvent(boxPack.getDomainId(), boxPack.getJobType(), printerId, null, ValueUtil.newMap("box", boxPack));
			this.eventPublisher.publishEvent(printEvent);
			return new BaseResponse(true, ValueUtil.toString(printEvent.getResult()));
		} else {
			return new BaseResponse(false, "Not Found Box By Id [" + boxPackId + "]");
		}
	}

	/**********************************************************************
	 * 								Dynamic API
	 **********************************************************************/

	/**
	 * 디바이스 관련 각 모듈에 특화된 REST GET 서비스
	 * DeviceProcessRestEvent 이벤트를 발생시켜 각 모듈에서 해당 로직 처리
	 */
	@RequestMapping(value = "/dynamic/{job_type}/**", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Device Process Rest GET API")
	public BaseResponse deviceProcessRestGetApi(
			final HttpServletRequest request
			, @PathVariable("job_type") String jobType
			, @RequestParam Map<String,Object> paramMap) {
		
		String finalPath = this.getRequestFinalPath(request);
		DeviceProcessRestEvent event = new DeviceProcessRestEvent(Domain.currentDomainId(), jobType, finalPath, RequestMethod.GET, paramMap);
		return this.restEventPublisher(event);
	}

	/**
	 * 디바이스 관련 각 모듈에 특화된 REST PUT 서비스
	 * DeviceProcessRestEvent 이벤트를 발생시켜 각 모듈에서 해당 로직 처리
	 */
	@RequestMapping(value = "/dynamic/{job_type}/**", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Device Process Rest PUT API")
	public BaseResponse deviceProcessRestPutApi(
			final HttpServletRequest request
			, @PathVariable("job_type") String jobType
			, @RequestParam Map<String,Object> paramMap
			, @RequestBody(required=false) Map<String,Object> requestBody) {
		
		String finalPath = this.getRequestFinalPath(request);
		DeviceProcessRestEvent event = new DeviceProcessRestEvent(Domain.currentDomainId(), jobType, finalPath, RequestMethod.PUT, paramMap);
		event.setRequestPutBody(requestBody);
		return this.restEventPublisher(event);
	}

	/**
	 * 디바이스 관련 각 모듈에 특화된 REST POST 서비스
	 * DeviceProcessRestEvent 이벤트를 발생시켜 각 모듈에서 해당 로직 처리
	 */
	@RequestMapping(value = "/dynamic/{job_type}/**", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Device Process Rest POST API")
	public BaseResponse deviceProcessRestPostApi(
			final HttpServletRequest request
			, @PathVariable("job_type") String jobType
			, @RequestParam Map<String,Object> paramMap
			, @RequestBody(required=false) List<Map<String,Object>> requestBody) {
		
		String finalPath = this.getRequestFinalPath(request);
		DeviceProcessRestEvent event = new DeviceProcessRestEvent(Domain.currentDomainId(), jobType, finalPath, RequestMethod.POST, paramMap);
		event.setRequestPostBody(requestBody);
		return this.restEventPublisher(event);
	}

}
