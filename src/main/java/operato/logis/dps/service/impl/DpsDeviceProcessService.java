package operato.logis.dps.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.RfidResult;
import operato.logis.dps.DpsCodeConstants;
import operato.logis.dps.DpsConstants;
import operato.logis.dps.model.DpsBatchInputableBox;
import operato.logis.dps.model.DpsBatchSummary;
import operato.logis.dps.model.DpsInspection;
import operato.logis.dps.model.DpsSinglePackJobInform;
import operato.logis.dps.model.DpsSinglePackSummary;
import operato.logis.dps.query.store.DpsBatchQueryStore;
import operato.logis.dps.service.api.IDpsJobStatusService;
import operato.logis.dps.service.api.IDpsPickingService;
import operato.logis.dps.service.util.DpsBatchJobConfigUtil;
import operato.logis.dps.service.util.DpsServiceUtil;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 모바일 장비에서 요청하는 트랜잭션 이벤트 처리 서비스 
 * 
 * @author yang
 */
@Component
public class DpsDeviceProcessService extends AbstractLogisService {
	/**
	 * 배치 쿼리 스토어
	 */
	@Autowired
	private DpsBatchQueryStore dpsBatchQueryStore;
	/**
	 * DPS 피킹 서비스
	 */
	@Autowired
	private IDpsPickingService dpsPickingService;
	/**
	 * DPS 작업 현황 조회 서비스
	 */
	@Autowired
	private IDpsJobStatusService dpsJobStatusService;
	/**
	 * DPS 출고 검수 서비스
	 */
	@Autowired
	private DpsInspectionService dpsInspectionService;

	/*****************************************************************************************************
	 * 										작 업 진 행 율 A P I
	 *****************************************************************************************************
	/**
	 * DPS 배치 작업 진행율 조회 : 진행율 + 투입 순서 리스트
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/batch_summary', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void batchSummaryEventProcess(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		int limit = ValueUtil.toInteger(params.get("limit"));
		int page = ValueUtil.toInteger(params.get("page"));
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 배치 서머리 조회 
		DpsBatchSummary summary = this.getBatchSummary(batch, equipType, equipCd, limit, page);

		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, summary));
		event.setExecuted(true);
	}
	
	/**
	 * B2C 배치에 대한 진행율 조회 
	 * 
	 * @param batch
	 * @param equipType
	 * @param equipCd
	 * @param limit
	 * @param page
	 * @return
	 */
	private DpsBatchSummary getBatchSummary(JobBatch batch, String equipType, String equipCd, int limit, int page) {
		
		// 1. 작업 진행율 조회
		BatchProgressRate rate = this.dpsJobStatusService.getBatchProgressSummary(batch);
		
		// 2. 투입 정보 리스트 조회 
		Page<JobInput> inputItems = this.dpsJobStatusService.paginateInputList(batch, equipCd, null, page, limit);
		
		// 3. 파라미터
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType", domainId, batch.getId(), equipType);
		
		if(ValueUtil.isNotEmpty(batch.getEquipCd())) {
			params.put("equipCd", equipCd);
		}
		
		// 4. 투입 가능 박스 수량 조회 
		Integer inputableBox = AnyEntityUtil.findItem(domainId, false, Integer.class, this.dpsBatchQueryStore.getBatchInputableBoxQuery(), params);
		
		// 5. 결과 리턴
		return new DpsBatchSummary(rate, inputItems, inputableBox);
	}
	
	/**
	 * 주문 상세 정보 조회 
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/order_items', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchOrderItems(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String orderNo = params.get("orderNo").toString();
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 투입 상세 리스트 조회
		List<JobInstance> jobList = this.dpsJobStatusService.searchInputJobList(batch, ValueUtil.newMap("orderNo", orderNo));

		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, jobList));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 * 									 박 스 투 입 A P I
	 *****************************************************************************************************
	
	/**
	 * DPS 박스 투입 (BOX or Tray)
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/box_requirement', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getBoxRequirementList(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 진행 중인 배치가 있을때만 조회 
		if(!ValueUtil.isEmpty(batch)) {
			// 3.1. 호기별 배치 분리 여부
			// 투입 대상 박스 리스트 조회시 별도의 로직 처리 필요 
			boolean useSeparatedBatch = DpsBatchJobConfigUtil.isSeparatedBatchByRack(batch);
			
			String query = this.dpsBatchQueryStore.getBatchInputableBoxByTypeQuery();
			Map<String,Object> queryParams = ValueUtil.newMap("domainId,batchId,equipType",event.getDomainId(),batch.getId(),equipType);
			
			// 3.2. 호기가 분리된 배치의 경우 
			if(useSeparatedBatch) {
				queryParams.put("equipCd",equipCd);
			}
			
			List<DpsBatchInputableBox> inputableBoxs = AnyEntityUtil.searchItems(event.getDomainId(), true, DpsBatchInputableBox.class, query, queryParams);
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inputableBoxs));
		} else {
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, null));
		}

		// 5. 이벤트 처리 결과 셋팅 
		event.setExecuted(true);
	}
	
	/**
	 * DPS 트레이 박스 투입
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/input_bucket', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inputBucket(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String boxId = params.get("bucketCd").toString();
		int limit = ValueUtil.toInteger(params.get("limit"));
		int page = ValueUtil.toInteger(params.get("page"));
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 트레이 박스 투입
		this.dpsPickingService.inputEmptyBucket(batch, false, boxId);
		
		// 4. 배치 서머리 조회 
		DpsBatchSummary summary = this.getBatchSummary(batch, equipType, equipCd, limit, page);

		// 5. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, summary));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 작업 존 박스 도착
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/bucket_arrive', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void bucketArrive(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String boxId = params.get("bucketCd").toString();
				
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatch = DpsServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatch.getBatch();
		
		// 3.1 대기 상태인 투입 정보 조회 
		JobInput input = AnyEntityUtil.findEntityBy(domainId, false, JobInput.class, null, "batchId,equipType,equipCd,stationCd,boxId,status", batch.getId(), equipType, equipCd, stationCd, boxId, DpsCodeConstants.JOB_INPUT_STATUS_WAIT);
		
		// 3.2 없으면 진행 중인 투입 정보 조회 
		if(input == null) {
			input = AnyEntityUtil.findEntityBy(domainId, true, JobInput.class, null, "batchId,equipType,equipCd,stationCd,boxId,status", batch.getId(), equipType, equipCd, stationCd, boxId, DpsCodeConstants.JOB_INPUT_STATUS_RUN);
		}
		
		// 3.3 투입 정보 상태 업데이트 (WAIT => RUNNING)
		if(ValueUtil.isEqualIgnoreCase(input.getStatus(), DpsCodeConstants.JOB_INPUT_STATUS_WAIT)) {
			input.setStatus(DpsCodeConstants.JOB_INPUT_STATUS_RUN);
			this.queryManager.update(input, DpsConstants.ENTITY_FIELD_STATUS, DpsConstants.ENTITY_FIELD_UPDATER_ID, DpsConstants.ENTITY_FIELD_UPDATED_AT);
		}
		
		// 4. 표시기 점등을 위한 작업 데이터 조회
		List<JobInstance> jobList = this.dpsJobStatusService.searchPickingJobList(batch, stationCd, input.getOrderNo());
				
		// 5. 작업 데이터로 표시기 점등 & 작업 데이터 상태 및 피킹 시작 시간 등 업데이트 
		if(ValueUtil.isNotEmpty(jobList)) {
			this.serviceDispatcher.getIndicationService(batch).indicatorsOn(batch, false, jobList);
		}
		
		// 6. 도착한 박스 기준으로 태블릿에 표시할 투입 박스 리스트 조회  
		List<JobInput> inputList = this.dpsJobStatusService.searchInputList(batch, equipCd, stationCd, input.getId());
		
		// 7. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inputList));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 * 									 단 포 처 리 A P I
	 *****************************************************************************************************

	/**
	 * DPS 단포 피킹 처리
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/single_pack/pick', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void singlePackPick(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		String jobId = event.getRequestParams().get("jobId").toString();

		// 2. 작업 데이터 조회 
		JobInstance job = AnyEntityUtil.findEntityById(true, JobInstance.class, jobId);
		
		// 3. 작업 배치 조회 
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, job.getBatchId());
		
		// 4. 피킹 검수 설정 확인
		int resQty = job.getPickQty();
		if(DpsBatchJobConfigUtil.isPickingWithInspectionEnabled(batch)) {
			resQty = 1;
		}
		
		// 5. 확정 처리 
		this.dpsPickingService.confirmPick(batch, job, resQty);
		
		// 6. 작업 완료가 되었다면 단포 작업 현황 조회
		if(job.getPickedQty() >= job.getPickQty()) {
			// 상품에 대한 단포 작업 정보 조회 
			List<DpsSinglePackSummary> singlePackInfo = this.dpsJobStatusService.searchSinglePackSummary(batch, job.getSkuCd(), job.getBoxTypeCd(), job.getPickQty());
			// 처리 결과 설정
			DpsSinglePackJobInform result = new DpsSinglePackJobInform(singlePackInfo, null);
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
			
		} else {
			// 처리 결과 설정 
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		}

		event.setExecuted(true);
	}
	
	/**
	 * DPS 단포 박스 투입
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/single_pack/box_input', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void singlePackBoxInput(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String skuCd = params.get("skuCd").toString();
		String boxId = params.get("bucketCd").toString();

		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 사용 박스 타입 작업 설정에서 조회
		String boxType = DpsBatchJobConfigUtil.getInputBoxType(batch);
		
		// 4. 단포 박스 투입 서비스 호출
		boolean isBox = ValueUtil.isEqualIgnoreCase(boxType, DpsCodeConstants.BOX_TYPE_BOX);
		JobInstance job = (JobInstance)this.dpsPickingService.inputSinglePackEmptyBucket(batch, isBox, skuCd, boxId);
		
		// 5. 상품에 대한 단포 작업 정보 조회 
		//List<DpsSinglePackSummary> singlePackInfo = this.dpsJobStatusService.searchSinglePackSummary(batch, skuCd, job.getBoxTypeCd(), job.getPickQty());
		//DpsSinglePackJobInform result = new DpsSinglePackJobInform(singlePackInfo, job);
		
		// 6. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 단포 상품 변경
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/single_pack/sku_change', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void singlePackSkuChange(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String skuCd = params.get("skuCd").toString();

		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 상품에 대한 단포 작업 정보 조회 
		List<DpsSinglePackSummary> singlePackInfo = this.dpsJobStatusService.searchSinglePackSummary(batch, skuCd, null, null);
		
		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, singlePackInfo));
		event.setExecuted(true);
	}

	/*****************************************************************************************************
	 * 										출 고 검 수 A P I
	 *****************************************************************************************************
	
	/**
	 * DPS 출고 검수를 위한 검수 정보 조회 - 박스 ID
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_box', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String boxType = params.get("boxType").toString();
		String boxId = params.get("boxId").toString();
		boolean reprintMode = params.containsKey("reprintMode") ? ValueUtil.toBoolean(params.get("reprintMode")) : false;
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();

		// 3. 검수 정보 조회
		DpsInspection inspection = null;
		
		if(ValueUtil.isEqualIgnoreCase(boxType, LogisCodeConstants.BOX_TYPE_TRAY)) {
			inspection = this.dpsInspectionService.findInspectionByTray(batch, boxId, reprintMode, true);
		} else {
			inspection = this.dpsInspectionService.findInspectionByBox(batch, boxId, reprintMode, true);
		}

		// 3. 이벤트 처리 결과 셋팅  
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 출고 검수를 위해 송장 번호로 검수 정보 조회
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_invoice', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByInvoice(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String invoiceId = params.get("invoiceId").toString();
		boolean reprintMode = params.containsKey("reprintMode") ? ValueUtil.toBoolean(params.get("reprintMode")) : false;
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 검수 정보 조회
		DpsInspection inspection = this.dpsInspectionService.findInspectionByInvoice(batch, invoiceId, reprintMode, true);
		
		// 4. 이벤트 처리 결과 셋팅  
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 출고 검수를 위해 주문 번호로 검수 정보 조회
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_order', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String orderNo = params.get("orderNo").toString();
		boolean reprintMode = params.containsKey("reprintMode") ? ValueUtil.toBoolean(params.get("reprintMode")) : false;
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 검수 정보 조회
		DpsInspection inspection = this.dpsInspectionService.findInspectionByOrder(batch, orderNo, reprintMode, true);

		// 4. 이벤트 처리 결과 셋팅  
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 내품 검수 (RFID 검수)
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/by_item', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void doInspectBoxItem(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String rfidId = params.get("rfidId").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
				
		// 3. RFID 상태값 체크 프로시져 호출
		Map<String, Object> procParams = ValueUtil.newMap("IN_CD_RFIDUID", rfidId);
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidBoxItem.class);
		Map<?, ?> result = rfidQueryMgr.callReturnProcedure("PRO_RFIDUID_STATUS_CHECK", procParams, Map.class);
		// 리턴 파라미터 : OUT_CONFIRM, OUT_MSG, OUT_DEPART, OUT_ITEM_CD, OUT_STYLE, OUT_GOODS, OUT_COLOR, OUT_SIZE, OUT_BARCOD
		String successYn = ValueUtil.toString(result.get("OUT_CONFIRM"));
		
		// 5. RFID가 문제가 없는 경우
		if(ValueUtil.isEqualIgnoreCase(successYn, LogisConstants.Y_CAP_STRING)) {
			RfidResult rfid = new RfidResult();
			rfid.setRfidId(rfidId);
			rfid.setBrandCd(ValueUtil.toString(result.get("OUT_DEPART")));
			rfid.setSkuCd(ValueUtil.toString(result.get("OUT_ITEM_CD")));
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, rfid));
			
		// 6. RFID가 문제가 있는 경우 
		} else {
			String errorMsg = ValueUtil.isNotEmpty(result.get("OUT_MSG")) ? result.get("OUT_MSG").toString() : "유효하지 않은 RFID!";
			event.setReturnResult(new BaseResponse(false, errorMsg, null));
		}
		
		event.setExecuted(true);
	}
	
	/**
	 * DPS 출고 검수 완료
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/finish', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void finishInspection(DeviceProcessRestEvent event) {
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String orderNo = params.get("orderNo").toString();
		String trayCd = params.get("trayCd").toString();
		String boxId = params.get("boxId").toString();
		String printerId = params.get("printerId").toString();
		List<Map<String, Object>> itemObjs = event.getRequestPostBody();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 검수 완료 처리
		BoxPack boxPack = new BoxPack();
		boxPack.setDomainId(batch.getDomainId());
		boxPack.setBatchId(batch.getId());
		boxPack.setOrderNo(orderNo);
		boxPack.setBoxTypeCd(trayCd);
		boxPack.setBoxId(boxId);		
		
		if(ValueUtil.isEmpty(itemObjs)) {
			this.dpsInspectionService.finishInspection(batch, boxPack, null, printerId);
		} else {
			this.dpsInspectionService.finishInspection(batch, boxPack, null, printerId, itemObjs);
		}

		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, boxPack));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 송장 출력 
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/print_invoice', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void printInvoiceLabel(DeviceProcessRestEvent event) {
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String printerId = params.get("printerId").toString();
		String orderNo = params.get("orderNo").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 검수 정보 모두 조회 - 송장 분할인 경우 한 주문에 송장이 여러 장일 수 있음 
		List<DpsInspection> inspectionList = this.dpsInspectionService.searchInspectionList(batch, orderNo, true, true);
		Integer printedCount = 0;
		
		if(ValueUtil.isNotEmpty(inspectionList)) {
			// 4. 송장 발행
			for(DpsInspection inspection : inspectionList) {
				printedCount += this.dpsInspectionService.printInvoiceLabel(batch, inspection, printerId);
			}
		}
				
		// 5. 이벤트 처리 결과 셋팅
		String message = printedCount == 1 ? LogisConstants.OK_STRING : "송장 분할되어 [" + printedCount + "] 장 출력되었습니다.";
		event.setReturnResult(new BaseResponse(true, message, printedCount));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 거래명세서 출력 
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/print_trade_statement', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void printTradeStatement(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String printerId = params.get("printerId").toString();
		String boxId = params.get("boxId").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 박스 조회
		BoxPack boxPack = AnyEntityUtil.findEntityBy(event.getDomainId(), false, BoxPack.class, null, "batchId,boxId", batch.getId(), boxId);
		if(boxPack == null) {
			boxPack = AnyEntityUtil.findEntityBy(event.getDomainId(), false, BoxPack.class, null, "boxId", boxId);
		}
		
		// 4. 거래명세서 발행
		Integer printedCount = this.dpsInspectionService.printTradeStatement(batch, boxPack, printerId);
		
		// 5. 이벤트 처리 결과 셋팅  
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, printedCount));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 송장 (박스) 분할
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/split_box', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void splitBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		String orderNo = params.get("orderNo").toString();
		String trayCd = params.get("trayCd").toString();
		String boxId = params.get("boxId").toString();
		String printerId = params.get("printerId").toString();
		List<Map<String, Object>> itemObjs = event.getRequestPostBody();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 검수 완료 처리
		BoxPack boxPack = new BoxPack();
		boxPack.setDomainId(batch.getDomainId());
		boxPack.setBatchId(batch.getId());
		boxPack.setOrderNo(orderNo);
		boxPack.setBoxTypeCd(trayCd);
		boxPack.setBoxId(boxId);		
		
		if(ValueUtil.isEmpty(itemObjs)) {
			this.dpsInspectionService.finishInspection(batch, boxPack, null, printerId);
		} else {
			this.dpsInspectionService.finishInspection(batch, boxPack, null, printerId, itemObjs);
		}

		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, boxPack));
		event.setExecuted(true);
		
//		// 1. 파라미터 
//		Map<String, Object> params = event.getRequestParams();
//		String equipCd = params.get("equipCd").toString();
//		String equipType = params.get("equipType").toString();
//		String trayCd = params.get("trayCd").toString();		
//		String orderNo = params.get("orderNo").toString();
//		String boxId = params.get("boxId").toString();
//		String printerId = params.get("printerId").toString();
//		List<Map<String, Object>> inspItems = event.getRequestPostBody();
//		
//		if(ValueUtil.isEmpty(inspItems)) {
//			event.setReturnResult(new BaseResponse(true, LogisConstants.NG_STRING, null));
//			event.setExecuted(true);
//		} 
//		
//		// 2. 송장 분할 아이템 대상
//		List<DpsInspItem> splitInspItems = new ArrayList<DpsInspItem>(inspItems.size());
//		for(Map<String, Object> item : inspItems) {
//			DpsInspItem inspItem = new DpsInspItem();
//			inspItem.setSkuCd(ValueUtil.toString(item.get("sku_cd")));
//			inspItem.setPickedQty(ValueUtil.toInteger(item.get("picked_qty")));
//			inspItem.setConfirmQty(ValueUtil.toInteger(item.get("confirm_qty")));
//			splitInspItems.add(inspItem);
//		}
//		
//		// 3. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
//		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
//		JobBatch batch = equipBatchSet.getBatch();
//		
//		// 4. 검수 정보 조회 - 주문 번호가 orderNo이고 박스 ID가 boxId이고 송장 번호가 없는 작업 정보로 검수 정보 조회 
//		BoxPack boxPack = new BoxPack();
//		boxPack.setDomainId(batch.getDomainId());
//		boxPack.setBatchId(batch.getId());
//		boxPack.setBoxTypeCd(trayCd);
//		boxPack.setOrderNo(orderNo);
//		boxPack.setBoxId(boxId);
//		
//		// 5. 송장 분할
//		BoxPack splitBox = this.dpsInspectionService.splitBox(batch, boxPack, splitInspItems, printerId);
//
//		// 6. 이벤트 처리 결과 셋팅
//		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, splitBox));
//		event.setExecuted(true);
	}
	
//	/**
//	 * DPS RFID에 의한 송장 (박스) 분할
//	 * 
//	 * @param event
//	 */
//	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/split_box_rfid', 'dps')")
//	@Order(Ordered.LOWEST_PRECEDENCE)
//	public void splitBoxByRfid(DeviceProcessRestEvent event) {
//		
//		// 1. 파라미터 
//		Map<String, Object> params = event.getRequestParams();
//		String equipCd = params.get("equipCd").toString();
//		String equipType = params.get("equipType").toString();
//		String trayCd = params.get("trayCd").toString();		
//		String orderNo = params.get("orderNo").toString();
//		String boxId = params.get("boxId").toString();
//		String printerId = params.get("printerId").toString();
//		List<Map<String, Object>> inspItems = event.getRequestPostBody();
//		
//		if(ValueUtil.isEmpty(inspItems)) {
//			event.setReturnResult(new BaseResponse(true, LogisConstants.NG_STRING, null));
//			event.setExecuted(true);
//		}
//		
//		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
//		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
//		JobBatch batch = equipBatchSet.getBatch();
//		
//		// 3. 송장 분할 RFID 아이템 대상 실적 저장 
//		List<RfidResult> splitInspItems = new ArrayList<RfidResult>(inspItems.size());
//		for(Map<String, Object> item : inspItems) {
//			RfidResult rfidItem = new RfidResult();
//			rfidItem.setRfidId(ValueUtil.toString(item.get("rfid_id")));
//			rfidItem.setSkuCd(ValueUtil.toString(item.get("sku_cd")));
//			rfidItem.setOrderNo(orderNo);
//			rfidItem.setBoxId(boxId);
//			rfidItem.setBatchId(batch.getId());
//			rfidItem.setBrandCd(ValueUtil.toString(item.get("brand_cd")));
//			rfidItem.setJobDate(batch.getJobDate());
//			rfidItem.setShopCd(ValueUtil.toString(item.get("shop_cd")));
//			rfidItem.setOrderQty(ValueUtil.toInteger(item.get("order_qty")));
//			splitInspItems.add(rfidItem);
//		}
//		
//		this.queryManager.insertBatch(splitInspItems);
//		
//		// 4. 검수 정보 조회 - 주문 번호가 orderNo이고 박스 ID가 boxId이고 송장 번호가 없는 작업 정보로 검수 정보 조회 
//		BoxPack boxPack = new BoxPack();
//		boxPack.setDomainId(batch.getDomainId());
//		boxPack.setBatchId(batch.getId());
//		boxPack.setBoxTypeCd(trayCd);
//		boxPack.setOrderNo(orderNo);
//		boxPack.setBoxId(boxId);
//		
//		// 5. 송장 분할
//		BoxPack splitBox = this.dpsInspectionService.splitBoxByRfid(batch, boxPack, splitInspItems, printerId);
//
//		// 6. 이벤트 처리 결과 셋팅
//		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, splitBox));
//		event.setExecuted(true);
//	}
//	
//	/**
//	 * DPS RFID 출고 검수 완료
//	 * 
//	 * @param event
//	 */
//	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/finish_by_rfid', 'dps')")
//	@Order(Ordered.LOWEST_PRECEDENCE)
//	public void finishInspectionByRFID(DeviceProcessRestEvent event) {
//		// 1. 파라미터 
//		Map<String, Object> params = event.getRequestParams();
//		String equipCd = params.get("equipCd").toString();
//		String equipType = params.get("equipType").toString();
//		String trayCd = params.get("trayCd").toString();
//		String orderNo = params.get("orderNo").toString();
//		String boxId = params.get("boxId").toString();
//		String printerId = params.get("printerId").toString();
//		
//		// 2. DPS RFID 출고 검수 완료
//		List<Map<String, Object>> rfidList = event.getRequestPostBody();
//		
//		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
//		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
//		JobBatch batch = equipBatchSet.getBatch();
//		
//		// 3. 검수 완료
//		this.finishInspectionByRfid(batch, orderNo, trayCd, boxId, rfidList, printerId);
//
//		// 4. 이벤트 처리 결과 셋팅
//		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, null));
//		event.setExecuted(true);
//	}
//
//	/**
//	 * RFID 검수 처리 ...
//	 * 
//	 * @param batch
//	 * @param orderNo
//	 * @param trayCd
//	 * @param boxId
//	 * @param rfidIdList
//	 * @param printerId
//	 */
//	private void finishInspectionByRfid(JobBatch batch, String orderNo, String trayCd, String boxId, List<Map<String, Object>> rfidIdList, String printerId) {
//		// 1. WMS로 박스 실적 전송
//		this.dpsBoxSendSvc.sendPackingToWms(batch, orderNo, boxId);
//		
//		// 2. 송장 발행 요청
//		String invoiceId = this.dpsBoxSendSvc.requestInvoiceToWms(batch, boxId, boxId);
//		
//		// 3. RFID 검수 실적 저장
//		List<RfidResult> rfidResultList = new ArrayList<RfidResult>(rfidIdList.size());
//		for(Map<String, Object> rfidInfo : rfidIdList) {
//			RfidResult result = new RfidResult();
//			result.setBatchId(batch.getId());
//			result.setBoxId(boxId);
//			result.setJobDate(batch.getJobDate().replace(LogisConstants.DASH, LogisConstants.EMPTY_STRING));
//			result.setRfidId(ValueUtil.toString(rfidInfo.get("rfid_id")));
//			result.setShopCd(ValueUtil.toString(rfidInfo.get("shop_cd")));
//			result.setBrandCd(ValueUtil.toString(rfidInfo.get("brand_cd")));
//			result.setSkuCd(ValueUtil.toString(rfidInfo.get("sku_cd")));
//			result.setOrderQty(ValueUtil.toInteger(rfidInfo.get("order_qty")));
//			result.setInvoiceId(invoiceId);
//			result.setOrderNo(orderNo);
//			rfidResultList.add(result);
//		}
//		this.queryManager.insertBatch(rfidResultList);
//		
//		// 4. RFID 검수 실적 전송
//		this.dpsBoxSendSvc.sendPackingToRfid(batch, invoiceId);
//		
//		// 5. 박스 내품 검수 항목 완료 처리
//		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId,status", batch.getDomainId(), batch.getId(), invoiceId, BoxPack.BOX_STATUS_EXAMED);
//		String sql = "update mhe_dr set status = :status where wh_cd = 'ICF' and work_unit = :batchId and waybill_no = :invoiceId";
//		this.queryManager.executeBySql(sql, params);
//		
//		// 6. Tray 박스 상태 리셋
//		TrayBox condition = new TrayBox();
//		condition.setTrayCd(trayCd);
//		TrayBox tray = this.queryManager.selectByCondition(TrayBox.class, condition);
//		tray.setStatus(BoxPack.BOX_STATUS_WAIT);
//		this.queryManager.update(tray, "status", "updaterId", "updatedAt");
//		
//		// 7. 검수 정보 조회
//		BoxPack box = new BoxPack();
//		box.setDomainId(batch.getDomainId());
//		box.setBatchId(batch.getId());
//		box.setBoxTypeCd(trayCd);
//		box.setBoxId(boxId);
//		box.setOrderNo(orderNo);
//		DpsInspection inspection = this.dpsInspectionService.findInspectionByBoxPack(box, false);
//		
//		// 8. 송장 발행 - 별도 트랜잭션
//		this.dpsInspectionService.printInvoiceLabel(batch, inspection, printerId);
//	}
	
}
