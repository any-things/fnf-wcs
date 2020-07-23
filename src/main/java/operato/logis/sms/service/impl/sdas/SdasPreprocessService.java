package operato.logis.sms.service.impl.sdas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.sms.SmsConstants;
import operato.logis.sms.entity.Chute;
import operato.logis.sms.query.SmsQueryStore;
import operato.logis.sms.service.model.ChuteStatus;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.BatchPreprocessEvent;
import xyz.anythings.base.service.api.IPreprocessService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@Component("sdasPreprocessService")
public class SdasPreprocessService extends AbstractExecutionService implements IPreprocessService {
	
	/**
	 * Sms 쿼리 스토어
	 */
	@Autowired
	private SmsQueryStore queryStore;
	
	@Override
	public List<OrderPreprocess> searchPreprocessList(JobBatch batch) {
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		
		condition.addSelect("id", "batchId", "jobType", "comCd", "cellAssgnCd", "cellAssgnNm", "equipCd", "equipNm", "subEquipCd", "skuQty", "totalPcs");
		condition.addFilter("batchId", batch.getId());
		condition.addOrder("totalPcs", false);
		return this.queryManager.selectList(OrderPreprocess.class, condition);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, ?> buildPreprocessSet(JobBatch batch, Query query) {
		List<String> smsJobType = new ArrayList<>(3);
		smsJobType.add(SmsConstants.JOB_TYPE_SDAS);
		smsJobType.add(SmsConstants.JOB_TYPE_SDPS);
		smsJobType.add(SmsConstants.JOB_TYPE_SRTN);
		smsJobType.remove(batch.getJobType());
		
		String sql = "SELECT * FROM JOB_BATCHES WHERE JOB_TYPE <> :jobType AND JOB_TYPE IN ( :smsJobType ) and status = :status";
		Map<String, Object> params = ValueUtil.newMap("jobType,smsJobType,status", batch.getJobType(), smsJobType, JobBatch.STATUS_RUNNING);
		int cnt = this.queryManager.selectSizeBySql(sql, params);
		
		String errorType = "";
		for (int i = 0 ; i < smsJobType.size() ; i++) {
			errorType += smsJobType.get(i);
			if(i +1 < smsJobType.size()) errorType += ", ";
		}
		
		if(cnt > 0) {
			throw ThrowUtil.newAlreadyBstatus(errorType, JobBatch.STATUS_RUNNING);
		}
		
		
		List<Map> batchInfo = this.preprocessSummaryByBatch(batch);
		
		List<Map> summaryByChutes = this.preprocessSummaryByChutes(batch);
		// 7. 리턴 데이터 셋
		
		return ValueUtil.newMap("batch-info,chute-info", batchInfo, summaryByChutes);
	}

	@SuppressWarnings({ "unchecked", "null" })
	@Override
	public int generatePreprocess(JobBatch batch, Object... params) {
		/**
		 * 1. 가공 버튼 클릭시 preprocess에 들어간다.
		 * 2. 주문 가공 데이터를 생성하기 위해 주문 데이터를 조회
		 */
		
		// 자동으로 생성할때 소터를 선택 해야 하는건지? 상위 시스템에서 소터 코드를 지정해서 내려 주는 것인지?
		// TODO 현재는 고정
		String sorterCd = "";
		
		Map<String, Object> chuteStatus = null;
		
		if(ValueUtil.isNotEmpty(params)) {
			chuteStatus = (Map<String, Object>) params[0];
			sorterCd = ValueUtil.toString(params[1]);
		} else {
			Map<String, Object> sqlParams = ValueUtil.newMap("status,active_flag", Order.STATUS_WAIT, true);
			List<Chute> chuteList = this.queryManager.selectList(Chute.class, sqlParams);
			
			for (Chute chute : chuteList) {
				chuteStatus.put("chute-" + chute.getChuteNo(), SysConstants.CAP_Y_STRING);
			}
		}
		
		// 2. 주문 가공 데이터를 생성하기 위해 주문 데이터를 조회
		String sql = queryStore.getSdasGeneratePreprocessQuery();
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,jobType,equipCd", batch.getDomainId(), batch.getId(), batch.getJobType(), sorterCd);
		List<OrderPreprocess> preprocessList = this.queryManager.selectListBySql(sql, condition, OrderPreprocess.class, 0, 0);

		// 3. 주문 가공 데이터를 추가
		int generatedCount = ValueUtil.isNotEmpty(preprocessList) ? preprocessList.size() : 0;
		if(generatedCount > 0) {
			this.assignChuteByAuto(batch, sorterCd, preprocessList, false, chuteStatus);
		}

		// 4. 결과 리턴
		return generatedCount;
	}

	@Override
	public int deletePreprocess(JobBatch batch) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobBatch> completePreprocess(JobBatch batch, Object... params) {
		// 1. 주문 가공 후 처리 이벤트 전송
		BatchPreprocessEvent afterEvent = new BatchPreprocessEvent(batch, SysEvent.EVENT_STEP_AFTER, EventConstants.EVENT_PREPROCESS_COMPLETE);
		afterEvent = (BatchPreprocessEvent)this.eventPublisher.publishEvent(afterEvent);
		
		// 2. 다음 단계 취소라면 ...
		if(afterEvent.isAfterEventCancel()) {
			Object result = afterEvent.getEventResultSet() != null && afterEvent.getEventResultSet().getResult() != null ? afterEvent.getEventResultSet().getResult() : null;
			if(result instanceof List<?>) {
				return (List<JobBatch>)result;
			}
		}
		
		// 3. 주문 가공 정보가 존재하는지 체크
		this.beforeCompletePreprocess(batch, true);
	
		// 4. 주문 가공 완료 처리
		this.completePreprocessing(batch);
	
		// 5. 주문 가공 완료 처리한 배치 리스트 리턴
		return ValueUtil.toList(batch);
	}

	@Override
	public void resetPreprocess(JobBatch batch, boolean isRackReset, List<String> equipCdList) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("null")
	@Override
	public int assignEquipLevel(JobBatch batch, String equipCds, List<OrderPreprocess> items, boolean automatically) {
		// 1. 상품 정보가 존재하는지 체크
		if(ValueUtil.isEmpty(items)) {
			throw new ElidomRuntimeException("There is no OrderPreprocess!");
		}
		
		Map<String, Object> sqlParams = ValueUtil.newMap("status,active_flag", Order.STATUS_WAIT, true);
		List<Chute> chuteList = this.queryManager.selectList(Chute.class, sqlParams);
		
		Map<String, Object> chuteStatus = null;
		for (Chute chute : chuteList) {
			chuteStatus.put("chute-" + chute.getChuteNo(), SysConstants.CAP_Y_STRING);
		}
		
		// 2. 슈트 지정
		if(automatically) {
			assignChuteByAuto(batch, equipCds, items, true, chuteStatus);
		} else {
			assignChuteByManual(batch, equipCds, items);
		}
		
		return items.size(); 
	}

	@Override
	public int assignSubEquipLevel(JobBatch batch, String equipType, String equipCd, List<OrderPreprocess> items) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@SuppressWarnings("rawtypes")
	public void assignChuteByAuto(JobBatch batch, String equipCd, List<OrderPreprocess> items, boolean isUpdate, Map<String, Object> chuteStatus) {
		// 1. 작업지시 대기 인 배치를 Wait 상태로 변경한다.
		this.resetOrderList(batch);
		
		// 2. 사용자가 선택한 슈트번호로 데이터 가공
		List<String> enableChute = new ArrayList<String>();
		for(Entry<String, Object> entry : chuteStatus.entrySet()) {
			if(ValueUtil.isEqual(entry.getValue(), SysConstants.CAP_Y_STRING)) {
				enableChute.add(entry.getKey().replaceAll("chute-check-", ""));
			}
		}
		
		// 2-1. 현재 셀에 매핑되어 있는 shop를 조회하여 할당할 batch에서 미리 셀을 할당한다.
		this.alreadyAssignShop(items);
		
		
		// 3. 사용가능한 Cell을 사용 순서순으로 조회한다.
		String sql = queryStore.getSdasCellStatusQuery();
		Map<String, Object> paramMap = ValueUtil.newMap("chuteNo,activeFlag", enableChute, true);
		List<Map> cellList = this.queryManager.selectListBySql(sql, paramMap, Map.class, 0, 0);
		
		if(items.size() > cellList.size()) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getTerm("terms.text.custs_must_less_than_cells", "AvailableChute Count : " + cellList.size() + " SHOP COUNT : " + items.size()));
		}
		
		// 4. 사용가능한 Cell에 주문정보를 매핑한다.
		int cellIdx;
		for(cellIdx = 0 ; cellIdx < cellList.size() ; cellIdx++) {
			if(ValueUtil.isEmpty(items.get(cellIdx).getSubEquipCd()) && ValueUtil.isEmpty(items.get(cellIdx).getClassCd())) {
				items.get(cellIdx).setSubEquipCd(ValueUtil.toString(cellList.get(cellIdx).get("chute_no")));
				items.get(cellIdx).setClassCd(ValueUtil.toString(cellList.get(cellIdx).get("cell_cd")));
			}
			if(cellIdx >= items.size() - 1) break;
		}
		
		if(isUpdate) {
			this.queryManager.updateBatch(items);
		} else {
			this.queryManager.insertBatch(items);
		}
	}
	
	public void selectChuteGeneratePreprocess(JobBatch batch, Map<String, Object> chuteStatus) {
		
	}
	
	public void assignChuteByManual(JobBatch batch, String equipCds, List<OrderPreprocess> items) {
		// 1. 오더타입 조회 (반품, 출고)
		// 2. 화면에서 지정한 매장 or 상품을 슈트에 지정한다.
		// 3. 주문 가공 정보 업데이트
	}
	
	/**
	 * 작업 배치 별 주문 가공 정보에서 호기별로 SHOP 할당 상태를 조회하여 리턴
	 *
	 * @param batch
	 * @return
	 */
	public List<ChuteStatus> chuteAssignmentStatus(JobBatch batch) {
		String sql = queryStore.getSrtnChuteStatusQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		return this.queryManager.selectListBySql(sql, params, ChuteStatus.class, 0, 0); 
	}
	
	/**
	 * 작업 배치 별 슈트별 물량 할당 요약 정보를 조회하여 리턴
	 *
	 * @param batch 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> preprocessSummaryByChutes(JobBatch batch) {
		String sql = queryStore.getSdasChuteInfo();
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		return this.queryManager.selectListBySql(sql, params,Map.class, 0, 0);
	}
	
	/**
	 * 작업 배치 요약 정보를 조회하여 리턴
	 *
	 * @param batch 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> preprocessSummaryByBatch(JobBatch batch) {
		String sql = queryStore.getSdasBatchInfo();
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		return this.queryManager.selectListBySql(sql, params,Map.class, 0, 0);
	}
	
	/**
	 * 주문 가공 완료가 가능한 지 체크
	 *
	 * @param batch
	 * @param checkRackAssigned
	 */
	private void beforeCompletePreprocess(JobBatch batch, boolean checkRackAssigned) {
		// 1. 상태 확인
		if(!ValueUtil.isEqualIgnoreCase(batch.getStatus(), JobBatch.STATUS_WAIT) && !ValueUtil.isEqualIgnoreCase(batch.getStatus(), JobBatch.STATUS_READY)) {
			// 상태가 유효하지 않습니다.
			throw ThrowUtil.newValidationErrorWithNoLog(true, "INVALID_STATUS");
		}
		
		// 2. 주문 가공 정보가 존재하는지 체크
		int preprocessCount = this.preprocessCount(batch, null, null, null);
		
		if(preprocessCount == 0) {
			throw new ElidomRuntimeException("No preprocess data.");
		}
		
		// 4. 슈트 지정이 안 된 shop_cd 가 존재하는지 체크
		if(checkRackAssigned) {
			int notAssignedCount = this.preprocessCount(batch, "sub_equip_cd", "is_blank", OrmConstants.EMPTY_STRING);
			
			if(notAssignedCount > 0) {
				// 랙 지정이 안된 상품이 (notAssignedCount)개 있습니다.
				throw ThrowUtil.newValidationErrorWithNoLog(true, "CHUTE_EXIST_NOT_ASSIGNED_SHOP", ValueUtil.toList("" + notAssignedCount));
			}
		}
	}
	
	/**
	 * 조건에 따른 주문 가공 데이터 건수를 조회하여 리턴
	 *
	 * @param batch
	 * @param filterNames
	 * @param filterOpers
	 * @param filterValues
	 * @return
	 */
	private int preprocessCount(JobBatch batch, String filterNames, String filterOpers, String filterValues) {
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());

		if(ValueUtil.isNotEmpty(filterNames)) {
			String[] names = filterNames.split(SysConstants.COMMA);
			String[] opers = ValueUtil.isNotEmpty(filterOpers) ? filterOpers.split(SysConstants.COMMA) : SysConstants.EMPTY_STRING.split(SysConstants.COMMA);
			String[] values = ValueUtil.isNotEmpty(filterValues) ? filterValues.split(SysConstants.COMMA) : SysConstants.EMPTY_STRING.split(SysConstants.COMMA);

			for(int i = 0 ; i < names.length ; i++) {
				condition.addFilter(new Filter(names[i], opers[i], values[i]));
			}
		}

		return this.queryManager.selectSize(OrderPreprocess.class, condition);
	}
	
	/**
	 * 주문 가공 완료 처리
	 *
	 * @param batch
	 */
	private void completePreprocessing(JobBatch batch) {
		batch.setStatus(JobBatch.STATUS_READY);
		this.queryManager.update(batch, "status");
	}
	
	@SuppressWarnings("rawtypes")
	private void resetOrderList(JobBatch batch) {
		String selectSql = "SELECT OP.BATCH_ID, JB.STATUS FROM ORDER_PREPROCESSES OP LEFT OUTER JOIN JOB_BATCHES JB ON OP.BATCH_ID = JB.ID GROUP BY BATCH_ID, JB.STATUS";
		List<Map> batchIdList = this.queryManager.selectListBySql(selectSql, new HashMap<String, Object>(), Map.class, 0, 0);
		
		if(ValueUtil.isNotEmpty(batchIdList)) {
			List<String> batchIds = new ArrayList<String>();
			for (Map batchId : batchIdList) {
				if(!(ValueUtil.isEqual(ValueUtil.toString(batchId.get("status")), JobBatch.STATUS_RUNNING) || ValueUtil.isEqual(ValueUtil.toString(batchId.get("status")), JobBatch.STATUS_MERGED))) {
					batchIds.add(ValueUtil.toString(batchId.get("batch_id")));
				}
			}
			if(ValueUtil.isNotEmpty(batchIds)) {
				List<String> enableStatus = new ArrayList<String>();
				enableStatus.add(JobBatch.STATUS_READY);
				enableStatus.add(JobBatch.STATUS_RECEIVE);
				String updateSql = "UPDATE JOB_BATCHES SET STATUS = :status, UPDATED_AT = NOW() WHERE ID IN ( :id ) AND STATUS IN ( :enableStatus )";
				Map<String, Object> updateParamMap = ValueUtil.newMap("status,id,enableStatus", JobBatch.STATUS_WAIT, batchIds, enableStatus);
				this.queryManager.executeBySql(updateSql, updateParamMap);
				
				String deleteSql = "DELETE FROM ORDER_PREPROCESSES WHERE BATCH_ID IN ( :batchId )";
				Map<String, Object> deleteParamMap = ValueUtil.newMap("batchId", batchIds);
				this.queryManager.executeBySql(deleteSql, deleteParamMap);
			}
			
		}
	}
	
	private void alreadyAssignShop(List<OrderPreprocess> items) {
		Query query = new Query();
		query.addFilter("equipType", "Sorter");
		query.addFilter("categoryFlag", false);
		query.addFilter("batchId", LogisConstants.IS_NOT_NULL, LogisConstants.EMPTY_STRING);
		List<Cell> cellList = this.queryManager.selectList(Cell.class, query);
		for (Cell cell : cellList) {
			for (OrderPreprocess item : items) {
				if(ValueUtil.isEqual(cell.getClassCd(), item.getCellAssgnCd())) {
					String chuteNo = cell.getEquipCd().split("-")[1];
					int chuteCd = Integer.parseInt(chuteNo);
					item.setSubEquipCd(String.format("%03d", chuteCd));
					item.setClassCd(cell.getCellCd());
				}
			}
		}
	}
}
