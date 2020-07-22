package operato.logis.sms.service.impl.sdas;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WcsMhePasOrder;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.DateUtil;
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
			instructCount += this.doInstructBatch(batch, equipCdList, false);
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
	protected int doInstructBatch(JobBatch batch, List<String> regionList, boolean isMerged) {
		// 1. 배치의 주문 가공 정보 조회
		Long domainId = batch.getDomainId();
		Query query = AnyOrmUtil.newConditionForExecution(domainId);
		query.addFilter("batchId", batch.getId());  
		List<OrderPreprocess> preprocesses = this.queryManager.selectList(OrderPreprocess.class, query);
		
		// 2. 주문 가공 정보로 부터 슈트 리스트 조회
		List<String> cellNoList = AnyValueUtil.filterValueListBy(preprocesses, "classCd");
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("id", "cellCd", "classCd");
		condition.addFilter("cellCd", SysConstants.IN, cellNoList);
		condition.addFilter("activeFlag", true);
		condition.addOrder("cellCd", false);
		List<Cell> cellList = this.queryManager.selectList(Cell.class, condition);
		
		int cellCount = cellList.size();
		for(int i = 0 ; i < cellCount ; i++) {
			Cell cell = cellList.get(i);
			List<OrderPreprocess> cellPreprocesses = AnyValueUtil.filterListBy(preprocesses, "classCd", cell.getCellCd());
			this.generateJobInstances(batch, cell, cellPreprocesses);
		}
		this.interfaceSorter(batch);
		this.interfaceRack(batch);
		
		this.updateRackStatus(batch, preprocesses);
		
		AnyOrmUtil.updateBatch(cellList, 100, "classCd", "batchId");
		if(!isMerged) {
			batch.setStatus(JobBatch.STATUS_RUNNING);
			batch.setInstructedAt(new Date());
			this.queryManager.update(batch, "status", "instructedAt");
		}
		
		return preprocesses.size();
	}
	
	private void generateJobInstances(JobBatch batch, Cell cell, List<OrderPreprocess> preprocesses) {
		for (OrderPreprocess preprocess : preprocesses) {
			cell.setClassCd(preprocess.getCellAssgnCd());
			cell.setBatchId(batch.getBatchGroupId());
		}
	}
	
	private void updateRackStatus(JobBatch batch, List<OrderPreprocess> preprocesses) {
		List<String> chuteList = AnyValueUtil.filterValueListBy(preprocesses, "subEquipCd");
		
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("chuteNo", SysConstants.IN, chuteList);
		List<Rack> rackList = this.queryManager.selectList(Rack.class, condition);
		for (Rack rack : rackList) {
			rack.setBatchId(batch.getBatchGroupId());
			rack.setStatus(JobBatch.STATUS_RUNNING);
		}
		
		AnyOrmUtil.updateBatch(rackList, 100, "batchId", "status");
	}
	
	@SuppressWarnings("rawtypes")
	private void interfaceSorter(JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length < 4) {
			String msg = MessageUtil.getMessage("no_batch_id", "설비에서 운영중인 BatchId가 아닙니다.");
			throw ThrowUtil.newValidationErrorWithNoLog(msg);
		}
		
		Map<String, Object> drParams = ValueUtil.newMap("batchId", batch.getId());
		List<Map> mheDrList = this.queryManager.selectListBySql(queryStore.getSdasPasOrder(), drParams, Map.class, 0, 0);
		
		Query condition = new Query();
		condition.addFilter("id", batch.getBatchGroupId());
		JobBatch mainBatch = this.queryManager.select(JobBatch.class, condition);
		
		
		List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(mheDrList.size());
		String srtDate = DateUtil.dateStr(new Date(), "yyyyMMdd");
		
		for (Map detail : mheDrList) {
			WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
			wcsMhePasOrder.setId(UUID.randomUUID().toString());
			wcsMhePasOrder.setBatchNo(batch.getBatchGroupId());
			wcsMhePasOrder.setJobDate(mainBatch.getJobDate().replaceAll("-", ""));
			wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_DAS);
			wcsMhePasOrder.setBoxId(WcsMhePasOrder.DAS_BOX_ID);
			wcsMhePasOrder.setChuteNo(ValueUtil.toString(detail.get("sub_equip_cd")));	
			wcsMhePasOrder.setSkuCd(ValueUtil.toString(detail.get("item_cd")));
			wcsMhePasOrder.setInputDate(srtDate);
			wcsMhePasOrder.setSkuBcd(ValueUtil.toString(detail.get("barcode2")));
			wcsMhePasOrder.setMheNo(batch.getEquipCd());
			wcsMhePasOrder.setShopCd(ValueUtil.toString(detail.get("shipto_id")));
			wcsMhePasOrder.setShopNm(ValueUtil.toString(detail.get("shipto_nm")));
			wcsMhePasOrder.setOrderQty(ValueUtil.toInteger(detail.get("pick_qty")));
			wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
			wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
			wcsMhePasOrder.setStrrId(ValueUtil.toString(detail.get("strr_id")));
			pasOrderList.add(wcsMhePasOrder);
		}
		
		if(ValueUtil.isNotEmpty(pasOrderList)) {
			AnyOrmUtil.insertBatch(pasOrderList, 100);
		}
		
		//우리쪽 MHE_HR / DR에 업데이트를 해줘야 한다???
	}
	
	private void interfaceRack(JobBatch batch) {
		
	}
	
}
