package operato.logis.dps.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.EquipGroup;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
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
			
			Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
			condition.addFilter("equipGroupCd", batch.getEquipGroupCd());
			EquipGroup eg = this.queryManager.selectByCondition(EquipGroup.class, condition);
			
			if(eg != null) {
				batch.setInputWorkers(eg.getInputWorkers());
				batch.setTotalWorkers(eg.getTotalWorkers());
			}

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
		
		// 5. 병합 배치 정보 업데이트
		newBatch.setBatchGroupId(mainBatch.getId());
		newBatch.setStageCd(mainBatch.getStageCd());
		newBatch.setAreaCd(mainBatch.getAreaCd());
		newBatch.setEquipGroupCd(mainBatch.getEquipGroupCd());
		newBatch.setEquipType(mainBatch.getEquipType());
		newBatch.setEquipCd(mainBatch.getEquipCd());
		newBatch.setEquipNm(mainBatch.getEquipNm());
		newBatch.setInputWorkers(mainBatch.getInputWorkers());
		newBatch.setStatus(JobBatch.STATUS_MERGED);
		newBatch.setInstructedAt(new Date());
		this.queryManager.update(newBatch);
		
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
		
		// 4. DPS 설비에 상태 전달
		sql = "update mhe_hr set mhe_no = :equipGroupCd, status = 'A' where wh_cd = 'ICF' and work_unit = :batchId";
		Map<String, Object> ifParams = ValueUtil.newMap("equipGroupCd,batchId", batch.getEquipGroupCd(), batch.getId());
		this.queryManager.executeBySql(sql, ifParams);

		// 5. WMS Wave에 상태 전달
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		sql = "update mhe_hr set mhe_no = :equipGroupCd, status = 'B', rcv_datetime = now() where wh_cd = 'ICF' and work_unit = :batchId";
		wmsQueryMgr.executeBySql(sql, ifParams);
		
		// 6. 후 처리 이벤트 
		this.publishInstructionEvent(SysEvent.EVENT_STEP_AFTER, batch, equipList, params);
		
		// 7. 총 주문 건수 리턴
		return batch.getBatchOrderQty();
	}
		
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
