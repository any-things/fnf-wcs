package operato.logis.sms.service.impl.sdas;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.sms.entity.Chute;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

// 호기복사와 배치 추가가 필요하다.
@Component("sdasInstructionService")
public class SdasInstructionService extends AbstractQueryService implements IInstructionService {

	@Autowired
	private SmsQueryStore queryStore;
	
	@Override
	public Map<String, Object> searchInstructionData(JobBatch batch, Object... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int instructBatch(JobBatch batch, List<String> equipCdList, Object... params) {
		// 1. 배치에 대한 상태 체크
		// 2. 작업지시할 수 있는 상태이면 Status Update
		// 3. OrderPreprocess 데이터 삭제
		int instructCount = 0;
		if(this.beforeInstructBatch(batch, equipCdList)) {
			instructCount += this.doInstructBatch(batch, equipCdList);
		}
		
		return instructCount;
	}

	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		// 1. merge 대상 배치 상태 및 상품 or 거래처를 조회한다.
		// 2. 새로운 배치에 대한 상품 or 거래처를 조회한다.
		// 3. 기존 배치에 동일한 상품 or 거래처가 있으면 합치고(merge) 새로운 상품 or 거래처를 할당할 수 있는 chute가 있는지 조회한다.
		// 4. 여유 chute가 부족하다면 실패 merge가능 하다면 배치 Update
		return 0;
	}

	@Override
	public int cancelInstructionBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * 작업 지시 전 처리 액션
	 *
	 * @param batch
	 * @param rackList
	 * @return
	 */
	protected boolean beforeInstructBatch(JobBatch batch, List<String> equipIdList) {
		// 배치 상태가 작업 지시 상태인지 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_READY)) {
			// '작업 지시 대기' 상태가 아닙니다
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getTerm("terms.text.is_not_wait_state", "JobBatch status is not 'READY'"));
		}

		return true;
	}
	
	/**
	 * 작업 지시 처리 로직
	 *
	 * @param batch
	 * @param regionList
	 * @return
	 */
	protected int doInstructBatch(JobBatch batch, List<String> regionList) {
		// 1. 배치의 주문 가공 정보 조회
		Long domainId = batch.getDomainId();
		Query query = AnyOrmUtil.newConditionForExecution(domainId);
		query.addFilter("batchId", batch.getId());  
		List<OrderPreprocess> preprocesses = this.queryManager.selectList(OrderPreprocess.class, query);
		
		// 2. 주문 가공 정보로 부터 슈트 리스트 조회
		List<String> chuteNoList = AnyValueUtil.filterValueListBy(preprocesses, "subEquipCd");
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("id", "chuteNo", "batchId", "status");
		condition.addFilter("chuteNo", SysConstants.IN, chuteNoList);
		condition.addFilter("activeFlag", 1);
		condition.addOrder("chuteNo", false);
		List<Chute> chuteList = this.queryManager.selectList(Chute.class, condition);
		
		// 3. 슈트 중에 현재 작업 중이거나 사용 불가한 슈트가 있는지 체크
		for(Chute chute : chuteList) {
			if(ValueUtil.isEqualIgnoreCase(chute.getStatus(), JobBatch.STATUS_RUNNING)) {
				// 호기에 다른 작업 배치가 할당되어 있습니다
				throw ThrowUtil.newValidationErrorWithNoLog(true, "ASSIGNED_ANOTHER_BATCH", ValueUtil.toList(chute.getChuteNo()));
			}
		}
		
		int chuteCount = chuteList.size();
		for(int i = 0 ; i < chuteCount ; i++) {
			Chute chute = chuteList.get(i);
			List<OrderPreprocess> chutePreprocesses = AnyValueUtil.filterListBy(preprocesses, "subEquipCd", chute.getChuteNo());
			this.generateJobInstances(batch, chute, chutePreprocesses);
		}
				
		
		AnyOrmUtil.updateBatch(chuteList, 100, "status", "batchId", "jobType");
		batch.setStatus(JobBatch.STATUS_RUNNING);
		this.queryManager.update(batch, "status");
		// TODO agent에 정보 생성후 전달 해야한다.
		
		return preprocesses.size();
	}
	
	private void generateJobInstances(JobBatch batch, Chute chute, List<OrderPreprocess> preprocesses) {
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		String insertQuery = queryStore.getSdasGenerateJobInstancesQuery();
		
		for (OrderPreprocess preprocess : preprocesses) {
			params.put("equipCd", preprocess.getEquipCd());
			params.put("equipNm", preprocess.getEquipNm());
			params.put("subEquipCd", preprocess.getSubEquipCd());
			params.put("shopNm", preprocess.getCellAssgnNm());
			params.put("shopCd", preprocess.getCellAssgnCd());
			this.queryManager.executeBySql(insertQuery, params);
		}
		
		chute.setStatus(JobBatch.STATUS_RUNNING);
		chute.setBatchId(batch.getId());
		chute.setJobType(batch.getJobType());
	}
	
}
