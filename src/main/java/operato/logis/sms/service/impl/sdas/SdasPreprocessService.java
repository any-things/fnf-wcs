package operato.logis.sms.service.impl.sdas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.sms.entity.Chute;
import operato.logis.sms.query.SmsQueryStore;
import operato.logis.sms.service.model.ChuteStatus;
import operato.logis.sms.service.model.SdasPreprocessSummary;
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

	@Override
	public Map<String, ?> buildPreprocessSet(JobBatch batch, Query query) {
		// 1. 가공할 주문 정보 조회
		List<OrderPreprocess> preprocesses = this.queryManager.selectList(OrderPreprocess.class, query);
		
		// 2. 주문 가공 정보가 존재하지 않는다면 주문 정보로 생성
		if(ValueUtil.isEmpty(preprocesses)) {
			this.generatePreprocess(batch);
			preprocesses = this.queryManager.selectList(OrderPreprocess.class, query);
		}
		// 4. 호기 정보 조회 - 주문 가공 화면의 우측 호기 리스트
//		List<ChuteStatus> shopChutes = this.chuteAssignmentStatus(batch);
		// 5. 호기별 물량 요약 정보 - 주문 가공 화면의 우측 상단 호기별 물량 요약 정보
		List<SdasPreprocessSummary> summaryByChutes = this.preprocessSummaryByChutes(batch);
		// 7. 리턴 데이터 셋
		
		return ValueUtil.newMap("regions,preprocesses,summary", preprocesses, preprocesses, summaryByChutes);
	}

	@SuppressWarnings({ "unchecked", "null" })
	@Override
	public int generatePreprocess(JobBatch batch, Object... params) {
		// 1. 주문 가공 데이터 삭제  
		this.deletePreprocess(batch);
		
		/**
		 * 1. 가공 버튼 클릭시 preprocess에 들어간다.
		 * 2. 주문 가공 데이터를 생성하기 위해 주문 데이터를 조회
		 * 3. 주문 가공 데이터 자동 설정
		 */
		
		// 자동으로 생성할때 소터를 선택 해야 하는건지? 상위 시스템에서 소터 코드를 지정해서 내려 주는 것인지?
		// TODO 현재는 고정
		String sorterCd = "94";
		
		Map<String, Object> chuteStatus = null;
		
		if(ValueUtil.isNotEmpty(params)) {
			chuteStatus = (Map<String, Object>) params[0];
		} else {
			Map<String, Object> sqlParams = ValueUtil.newMap("status,active_flag", Order.STATUS_WAIT, true);
			List<Chute> chuteList = this.queryManager.selectList(Chute.class, sqlParams);
			
			for (Chute chute : chuteList) {
				chuteStatus.put("chute-" + chute.getChuteNo(), SysConstants.CAP_Y_STRING);
			}
		}
		
		// 2. 주문 가공 데이터를 생성하기 위해 주문 데이터를 조회
		String sql = queryStore.getSdasGeneratePreprocessQuery();
		Map<String, Object> condition = ValueUtil.newMap("equipCd,domainId,batchId", sorterCd, batch.getDomainId(), batch.getId());
		List<OrderPreprocess> preprocessList = this.queryManager.selectListBySql(sql, condition, OrderPreprocess.class, 0, 0);
//
//		// 3. 주문 가공 데이터 자동 설정
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
		// TODO 초기화 버튼 클릭시
	}

	@Override
	public int assignEquipLevel(JobBatch batch, String equipCds, List<OrderPreprocess> items, boolean automatically) {
		// 자동할당 버튼 클릭시
		
		// 1. 상품 정보가 존재하는지 체크
		if(ValueUtil.isEmpty(items)) {
			throw new ElidomRuntimeException("There is no OrderPreprocess!");
		}
		
		Map<String, Object> sqlParams = ValueUtil.newMap("status,active_flag", Order.STATUS_WAIT, true);
		List<Chute> chuteList = this.queryManager.selectList(Chute.class, sqlParams);
		
		Map<String, Object> chuteStatus = new HashMap<String, Object>();
		for (Chute chute : chuteList) {
			chuteStatus.put("chute-" + chute.getChuteNo(), SysConstants.CAP_Y_STRING);
		}
		
		// 소터 코드 우선 고정으로...
//		equipCds = "94";
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
	public void assignChuteByAuto(JobBatch batch, String equipCds, List<OrderPreprocess> items, boolean isUpdate, Map<String, Object> chuteStatus) {
		// 1. 오더타입 조회 (반품, 출고)
		// 2. 슈트별 작업자 그룹이 있는지 확인
		// 3. 1번이 있다면 작업자별 생산성으로 우선순위를 조회한다.
		// 4. 작업할 오더의 우선순위를 조회한다.
		// 5. 3번과 2번의 조합으로 슈트별 오더를 지정 (루프)
		// 6. 주문 가공 정보 업데이트
		
		// 작업자별 시작 슈트번호 ex) [002, 011, 021, 031....]
		ArrayList<String> chuteArr = new ArrayList<>();
		// 작업자별 역순 시작 슈트번호 ex) [031, 021, 011, 002....]
		List<String> reverseChuteArr = new ArrayList<>();
		// 작업자별 작업 범위  ex) [9, 10, 10, 11, ....]
		ArrayList<Integer> chuteSizeArr = new ArrayList<>();
		// 작업자별 작업 범위  ex) [11, 10, 10, 9, ....]
		List<Integer> reverseChuteSizeArr = new ArrayList<>();
		
		String sql = queryStore.getSdasStationQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,sorterCd,activeFlag", batch.getDomainId(), equipCds, 1);
		List<Map> stationGroup = this.queryManager.selectListBySql(sql, params, Map.class, 0, 0);
		
		for (Map station : stationGroup) {
			chuteArr.add(ValueUtil.toString(station.get("chute")));
			chuteSizeArr.add(ValueUtil.toInteger(station.get("cnt")));
		}
		
		int groupCnt = 0;
		int maxCnt = 0;
		// 매장 갯수 대비 사용하는 작업자 존 수량을 구한다.
		// 존에 해당하는 슈트 수량이 다를수 있으므로 최대 슈트 갯수를 구한다.
		for(int i = 0 ; i < chuteSizeArr.size() ; i++) {
			groupCnt += chuteSizeArr.get(i);
			if(maxCnt < chuteSizeArr.get(i)) {
				maxCnt = chuteSizeArr.get(i);
			}
			if(items.size() <= groupCnt + chuteSizeArr.get(i)) {
				if(maxCnt < chuteSizeArr.get(i)) {
					maxCnt = chuteSizeArr.get(i);
				}
				groupCnt = i + 1;
				break;
			} 
		}
		
		reverseChuteArr.addAll(chuteArr.subList(0, groupCnt));
		Collections.reverse(reverseChuteArr);
		reverseChuteSizeArr.addAll(chuteSizeArr.subList(0, groupCnt));
		Collections.reverse(reverseChuteSizeArr);
		
		int listIdx = 0;
		// 작업 존별로 수량이 많은 매장부터 할당 
		for(int i = 0 ; i < maxCnt ; i++) {
			for(int j = 0 ; j <groupCnt ; j++) {
//				ArrayList<String> idList = new ArrayList<>();
				// 순차적으로 할당
				if(i % 2 == 0) {
					if(i < chuteSizeArr.get(j) && i < items.size()) {
						int chuteNo = Integer.parseInt(chuteArr.get(j)) + i;
						String chuteName = String.format("%03d", chuteNo);
						items.get(listIdx).setSubEquipCd(chuteName);
						listIdx++;
					}
				// 역순으로 할당
				} else {
					if(i < reverseChuteSizeArr.get(j) && i < items.size()) {
						int chuteNo = Integer.parseInt(reverseChuteArr.get(j)) + i;
						String chuteName = String.format("%03d", chuteNo);
						items.get(listIdx).setSubEquipCd(chuteName);
						listIdx++;
					}
				}
			}
		}
		
		// 매장 수량 대비 작업 존을 계산 했을 떄 나머지 수량에 대한 매장 할당
		for(int i = listIdx ; i < items.size() ; i++) {
			//ArrayList<String> idList = new ArrayList<>();
			int chuteNo = Integer.parseInt(chuteArr.get(groupCnt)) + (i - listIdx);
			String chuteName = String.format("%03d", chuteNo);
			items.get(i).setSubEquipCd(chuteName);
		}
		
		if(isUpdate) {
			this.queryManager.updateBatch(items);
		} else {
			this.queryManager.insertBatch(items);
		}
	}
	
	public void assignChuteByManual(JobBatch batch, String equipCds, List<OrderPreprocess> items) {
		// 1. 오더타입 조회 (반품, 출고)
		// 2. 화면에서 지정한 매장 or 상품을 슈트에 지정한다.
		// 3. 주문 가공 정보 업데이트
	}
	
	/**
	 * 작업 배치 별 주문 가공 정보에서 호기별로 상품 할당 상태를 조회하여 리턴
	 *
	 * @param batch
	 * @return
	 */
	public List<ChuteStatus> chuteAssignmentStatus(JobBatch batch) {
		String sql = queryStore.getSdasChuteStatusQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		return this.queryManager.selectListBySql(sql, params, ChuteStatus.class, 0, 0); 
	}
	
	/**
	 * 작업 배치 별 슈트별 물량 할당 요약 정보를 조회하여 리턴
	 *
	 * @param batch 
	 * @return
	 */
	public List<SdasPreprocessSummary> preprocessSummaryByChutes(JobBatch batch) {
		String sql = queryStore.getSdasPreprocessSummaryQuery();
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		return this.queryManager.selectListBySql(sql, params,SdasPreprocessSummary.class, 0, 0);
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
		
		// 4. 슈트 지정이 안 된 Shop_cd 가 존재하는지 체크
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
}
