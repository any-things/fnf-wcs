package operato.logis.dps.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 용 작업 지시 서비스
 * 
 * @author yang
 */
@Component("dpsInstructionService")
public class DpsInstructionService extends AbstractInstructionService implements IInstructionService {

	@Override
	public Map<String, Object> searchInstructionData(JobBatch batch, Object... params) {
		// DPS에서는 구현이 필요없음
		return null;
	}	
	
	@Override
	public int instructBatch(JobBatch batch, List<String> equipIdList, Object... params) {
		// 배치 호기 
		Rack rack = null;
		
		if(ValueUtil.isNotEmpty(equipIdList) && equipIdList.size() == 1) {
			// 선택한 랙 정보를 작업 배치에 설정 ...
			rack = AnyEntityUtil.findEntityById(true, Rack.class, equipIdList.get(0));
			batch.setAreaCd(rack.getAreaCd());
			batch.setStageCd(rack.getStageCd());
			batch.setEquipType(LogisConstants.EQUIP_TYPE_RACK);
			batch.setEquipCd(rack.getRackCd());
			batch.setEquipNm(rack.getRackNm());
			batch.setEquipGroupCd(rack.getEquipGroupCd());

		} else {
			if(ValueUtil.isEmpty(batch.getEquipCd())) {
				throw ThrowUtil.newValidationErrorWithNoLog("하나의 설비를 선택하세요.");
			} else {
				rack = AnyEntityUtil.findEntityByCode(batch.getDomainId(), true, Rack.class, "rackCd", batch.getEquipCd());
			}
		}
		
		// 호기 설정
		rack.setBatchId(batch.getId());
		rack.setStatus(JobBatch.STATUS_RUNNING);
		this.queryManager.update(rack, "batchId", "status", "updatedAt");
		
		// 작업 지시 처리
		List<Rack> rackList = ValueUtil.toList(rack);
		return this.doInstructBatch(batch, rackList);
	}

	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		// 1. 토털 피킹 전 처리 이벤트 
		EventResultSet befResult = this.publishTotalPickingEvent(SysEvent.EVENT_STEP_BEFORE, batch, equipIdList, params);
		
		// 2. 다음 처리 취소일 경우 결과 리턴 
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 토털 피킹 후 처리 이벤트
		EventResultSet aftResult = this.publishTotalPickingEvent(SysEvent.EVENT_STEP_AFTER, batch, equipIdList, params);
		
		// 4. 후처리 이벤트 실행 후 리턴 결과가 있으면 해당 결과 리턴 
		if(aftResult.isExecuted()) {
			if(aftResult.getResult() != null ) { 
				return ValueUtil.toInteger(aftResult.getResult());
			}
		}
		
		return 0;
	}

	@Override
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
//		// 1. 작업 배치 정보로 설비 리스트 조회
//		List<?> equipList = this.searchEquipListByBatch(mainBatch, null);
//		
//		// 2. 병합의 경우에는 메인 배치의 설정 셋을 가져온다 .
//		newBatch.setJobConfigSetId(mainBatch.getJobConfigSetId());
//		newBatch.setIndConfigSetId(mainBatch.getIndConfigSetId());
//		this.queryManager.update(newBatch , "jobConfigSetId","indConfigSetId");
//		
//		// 2. 소분류 코드, 방면 분류 코드 값을 설정에 따라서 주문 정보에 추가한다.
//		this.doUpdateClassificationCodes(newBatch, params);
//
//		// 3. 대상 분류 
//		this.doClassifyOrders(newBatch, equipList, params);
//		
//		// 4. 추천 로케이션 정보 생성
//		this.doRecommendCells(newBatch, equipList, params);
//		
//		// 5. 작업 병합 처리
//		int retCnt = this.doMergeBatch(mainBatch, newBatch, equipList, params);
//		
//		// 6. 작업 병합 후 박스 요청 
//		this.doRequestBox(mainBatch, equipList, params);
//		
//		// 7. 병합 건수 리턴
//		return retCnt;
		
		// 1. 예정 주문 정보의 배치 ID 업데이트
		Map<String, Object> condition = ValueUtil.newMap("mainBatchId,newBatchId,whCd", mainBatch.getId(), newBatch.getId(), "ICF");
		String sql = "update mhe_dr set work_unit = :mainBatchId where wh_cd = :whCd and work_unit = :newBatchId";
		int retCnt = this.queryManager.executeBySql(sql, condition);
		
		// 2. 메인 작업 배치 주문 수 업데이트
		sql = "select count(distinct(ref_no)) as result from mhe_dr where wh_cd = :whCd and work_unit = :mainBatchId";
		int orderCnt = this.queryManager.selectBySql(sql, condition, Integer.class);
		mainBatch.setParentOrderQty(orderCnt);
		mainBatch.setBatchOrderQty(orderCnt);
		
		// 3. 메인 작업 배치 주문 수량 업데이트
		sql = "select sum(pick_qty) as result from mhe_dr where wh_cd = :whCd and work_unit = :mainBatchId";
		int pickQty = this.queryManager.selectBySql(sql, condition, Integer.class);
		mainBatch.setParentPcs(pickQty);
		mainBatch.setBatchPcs(pickQty);
		
		// 4. 메인 작업 배치 정보 업데이트
		this.queryManager.update(mainBatch, "parentOrderQty", "batchOrderQty", "parentPcs", "batchPcs");
		
		// 5. 병합 건수 리턴
		return retCnt;
	}

	@Override
	public int cancelInstructionBatch(JobBatch batch) {
		// 1. 작업 지시 취소 전 처리 이벤트 
		EventResultSet befResult = this.publishInstructionCancelEvent(SysEvent.EVENT_STEP_BEFORE, batch, null);
		
		// 2. 다음 처리 취소일 경우 결과 리턴 
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		batch.setBatchOrderQty(0);
		batch.setBatchPcs(0);
		batch.setStatus(JobBatch.STATUS_READY);
		batch.setInstructedAt(null);
		this.queryManager.update(batch);
		
		// 3. 호기 설정
		String sql = "UPDATE RACKS SET BATCH_ID = null, STATUS = NULL, UPDATED_AT = now() WHERE DOMAIN_ID = :domainId AND RACK_CD = :rackCd";
		this.queryManager.executeBySql(sql, ValueUtil.newMap("domainId,rackCd", batch.getDomainId(), batch.getEquipCd()));
		
		// 4. 작업 지시 취소 후 처리 이벤트
		EventResultSet aftResult = this.publishInstructionCancelEvent(SysEvent.EVENT_STEP_AFTER, batch, null);
		
		// 5. 후 처리 이벤트 실행 후 리턴 결과가 있으면 해당 결과 리턴 
		if(aftResult.isExecuted()) {
			if(aftResult.getResult() != null ) { 
				return ValueUtil.toInteger(aftResult.getResult());
			}
		}
		
		return 0;
	}
	
	/**
	 * 작업 배치 소속 주문 데이터의 소분류, 방면 분류 코드를 업데이트 ...
	 * 
	 * @param batch
	 * @param params
	 */
	/*private void doUpdateClassificationCodes(JobBatch batch, Object ... params) {
		// 1. 소분류 매핑 필드 - class_cd 매핑 
		String classTargetField = DpsBatchJobConfigUtil.getBoxMappingTargetField(batch);
		
		if(ValueUtil.isNotEmpty(classTargetField)) {
			String sql = "UPDATE ORDERS SET CLASS_CD = %s WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
			sql = String.format(sql, classTargetField);
			Map<String, Object> updateParams = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
			this.queryManager.executeBySql(sql, updateParams);
		}

		// 2. 방면분류 매핑 필드 - box_class_cd 매핑
		String boxClassTargetField = DpsBatchJobConfigUtil.getBoxOutClassTargetField(batch , false);
		
		if(ValueUtil.isNotEmpty(boxClassTargetField)) {
			String sql = "UPDATE ORDERS SET BOX_CLASS_CD = %s WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
			sql = String.format(sql, boxClassTargetField);
			Map<String, Object> updateParams = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
			this.queryManager.executeBySql(sql, updateParams);
		}
	}*/
	
	/**
	 * 작업 대상 분류
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 */
	/*private void doClassifyOrders(JobBatch batch, List<?> equipList, Object... params) {
		// 1. 전처리 이벤트   
		EventResultSet befResult = this.publishClassificationEvent(SysEvent.EVENT_STEP_BEFORE, batch, equipList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴 
		if(!befResult.isAfterEventCancel()) {
			
			// 3. 대상 분류 프로세싱 
			this.processClassifyOrders(batch, equipList, params);
			
			// 4. 후처리 이벤트 
			this.publishClassificationEvent(SysEvent.EVENT_STEP_AFTER, batch, equipList, params);
		}
	}*/
	
	/**
	 * 대상 분류 프로세싱 
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	/*private int processClassifyOrders(JobBatch batch, List<?> equipList, Object... params) {
		// 1. 단포 작업 활성화 여부 
		boolean useSinglePack = DpsBatchJobConfigUtil.isSingleSkuNpcsClassEnabled(batch);
		// 2. 파라미터 생성
		Map<String, Object> inputParams = ValueUtil.newMap("P_IN_DOMAIN_ID,P_IN_BATCH_ID,P_IN_SINGLE_PACK", batch.getDomainId(), batch.getId(),useSinglePack);
		// 3. 프로시져 콜 
		Map<?, ?> result = this.queryManager.callReturnProcedure("OP_DPS_BATCH_SET_ORDER_TYPE", inputParams, Map.class);
		// 4. 처리 건수 취합
		int resultCnt = ValueUtil.toInteger(result.get("P_OUT_MT_COUNT"));
		resultCnt += ValueUtil.toInteger(result.get("P_OUT_OT_COUNT"));
		// 5. 처리 건수 리턴 
		return resultCnt;
	}*/
	
	/**
	 * 추천 로케이션 처리
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 */
	/*private void doRecommendCells(JobBatch batch, List<?> equipList, Object ... params) {
		// 1. 전 처리 이벤트
		EventResultSet befResult = this.publishRecommendCellsEvent(SysEvent.EVENT_STEP_BEFORE, batch, equipList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴 
		if(!befResult.isAfterEventCancel()) {
			
			// 3. 작업 지시 실행
			this.processRecommendCells(batch, equipList, params);
			
			// 4. 후 처리 이벤트 
			this.publishRecommendCellsEvent(SysEvent.EVENT_STEP_AFTER, batch, equipList, params);
		}		
	}*/
	
	/**
	 * 추천 로케이션 실행
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 */
	/*private void processRecommendCells(JobBatch batch, List<?> equipList, Object ... params) {
		// 재고 적치 추천 셀 사용 유무 
		boolean useRecommendCell = DpsBatchJobConfigUtil.isRecommendCellEnabled(batch);
		
		if(useRecommendCell) {
			// 1. 파라미터 생성
			Map<String, Object> inputParams = ValueUtil.newMap("P_IN_DOMAIN_ID,P_IN_BATCH_ID", batch.getDomainId(), batch.getId());
			// 2. 프로시져 콜 
			this.queryManager.callReturnProcedure("OP_DPS_BATCH_RECOMM_CELL", inputParams, Map.class);
		}
	}*/
	
	/**
	 * 작업 지시 처리
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	private int doInstructBatch(JobBatch batch, List<?> equipList, Object ... params) {
		// 1. 전 처리 이벤트
		EventResultSet befResult = this.publishInstructionEvent(SysEvent.EVENT_STEP_BEFORE, batch, equipList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴 
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 작업 지시 실행
		String sql = "SELECT COUNT(DISTINCT REF_NO) as batch_order_qty, SUM(PICK_QTY) as batch_pcs FROM MHE_DR WHERE WH_CD = 'ICF' AND WORK_UNIT = :batchId";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		JobBatch qtys = this.queryManager.selectBySql(sql, condition, JobBatch.class);
		batch.setBatchOrderQty(qtys.getBatchOrderQty());
		batch.setBatchPcs(qtys.getBatchPcs());
		batch.setStatus(JobBatch.STATUS_RUNNING);
		batch.setInstructedAt(new Date());
		this.queryManager.update(batch);
		
		// 4. 후 처리 이벤트 
		this.publishInstructionEvent(SysEvent.EVENT_STEP_AFTER, batch, equipList, params);
		
		// 5. 총 주문 건수 리턴
		return batch.getBatchOrderQty();
	}

	/**
	 * 박스 요청 처리
	 *  
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	/*private int doRequestBox(JobBatch batch, List<?> equipList, Object... params) {
		// 1. 단독 처리 이벤트   
		EventResultSet eventResult = this.publishRequestBoxEvent(batch, equipList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴 
		if(eventResult.isExecuted()) {
			return ValueUtil.toInteger(eventResult.getResult());
		}
		
		return 0;
	}*/
	
	/**
	 * 작업 병합 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param equipList
	 * @param params
	 * @return
	 */
	/*private int doMergeBatch(JobBatch mainBatch, JobBatch newBatch, List<?> equipList, Object... params) {
		// 1. 전처리 이벤트   
		EventResultSet befResult = this.publishMergingEvent(SysEvent.EVENT_STEP_BEFORE, mainBatch, newBatch, equipList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴 
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 배치 병합 처리 
		int resultCnt = this.processMerging(mainBatch, newBatch, params);
		
		// 4. 후처리 이벤트 
		EventResultSet aftResult = this.publishMergingEvent(SysEvent.EVENT_STEP_AFTER, mainBatch, newBatch, equipList, params);
		
		// 5. 후처리 이벤트가 실행 되고 리턴 결과가 있으면 해당 결과 리턴 
		if(aftResult.isExecuted()) {
			if(aftResult.getResult() != null) { 
				resultCnt += ValueUtil.toInteger(aftResult.getResult());
			}
		}

		return resultCnt;
	}*/
	
	/**
	 * 작업 병합 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 * @return
	 */
	/*private int processMerging(JobBatch mainBatch, JobBatch newBatch, Object ... params) {
		// 1. 단포 작업 활성화 여부 
		boolean useSinglePack = DpsBatchJobConfigUtil.isSingleSkuNpcsClassEnabled(mainBatch);
		// 2. 호기별 배치 분리 여부
		boolean useSeparatedBatch = DpsBatchJobConfigUtil.isSeparatedBatchByRack(mainBatch);

		// 3. 인풋 파라미터 설정
		Map<String, Object> inputParams = ValueUtil.newMap("P_IN_DOMAIN_ID,P_IN_BATCH_ID,P_IN_MAIN_BATCH_ID,P_IN_SINGLE_PACK,P_IN_SEPARATED_BATCH"
				, mainBatch.getDomainId(), newBatch.getId(), mainBatch.getId(), useSinglePack, useSeparatedBatch);
		// 4. 프로시져 콜 
		this.queryManager.callReturnProcedure("OP_DPS_BATCH_MERGE", inputParams, Map.class);
		
		return 1;
	}*/
		
	/******************************************************************
	 * 							이벤트 전송
	/******************************************************************/
	
	/**
	 * 대상 분류 이벤트 전송
	 * 
	 * @param eventStep
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	/*private EventResultSet publishClassificationEvent(short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_CLASSIFICATION, eventStep, batch, equipList, params);
	}*/
	
	/**
	 * 박스 요청 이벤트 전송
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	/*private EventResultSet publishRequestBoxEvent(JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_BOX_REQ, SysEvent.EVENT_STEP_ALONE, batch, equipList, params);
	}*/
	
	/**
	 * 추천 로케이션 이벤트 전송
	 * 
	 * @param eventStep
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	/*private EventResultSet publishRecommendCellsEvent(short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_RECOMMEND_CELLS, eventStep, batch, equipList, params);
	}*/
	
	/**
	 * 토털 피킹 이벤트 전송
	 * 
	 * @param eventStep
	 * @param mainBatch
	 * @param equipList
	 * @param params
	 * @return
	 */
	private EventResultSet publishTotalPickingEvent(short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_TOTAL_PICKING, eventStep, batch, equipList, params);
	}

}
