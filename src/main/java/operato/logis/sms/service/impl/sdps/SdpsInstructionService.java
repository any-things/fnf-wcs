package operato.logis.sms.service.impl.sdps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WcsMheDasOrder;
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
@Component("sdpsInstructionService")
public class SdpsInstructionService extends AbstractQueryService implements IInstructionService {

	@Autowired
	private SmsQueryStore queryStore;
	
	@Override
	public void targetClassing(JobBatch batch, Object... params) {
		// TODO Auto-generated method stub	
	}
	
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
		int retCnt = newBatch.getBatchOrderQty();
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
		
		//Query query = AnyOrmUtil.newConditionForExecution(mainBatch.getDomainId());
		//query.addFilter("batchGroupId", mainBatch.getId());
		//List<JobBatch> jobBatches = this.queryManager.selectList(JobBatch.class, query);
		//List<String> batchIds = AnyValueUtil.filterValueListBy(jobBatches, "id");
		//Map<String, Object> condition = ValueUtil.newMap("batchIds", batchIds);
		//String sql = "SELECT COALESCE(SUM(TOTAL_PCS), 0) AS PCS_CNT, COUNT(DISTINCT(CELL_ASSGN_CD)) AS ORDER_CNT FROM ORDER_PREPROCESSES WHERE BATCH_ID IN (:batchIds )";
		//Map<String, Object> totalResult = this.queryManager.selectBySql(sql, condition, Map.class);
		//mainBatch.setParentOrderQty(ValueUtil.toInteger(totalResult.get("order_cnt")));
		//mainBatch.setParentPcs(ValueUtil.toInteger(totalResult.get("pcs_cnt")));
		
		//this.queryManager.update(mainBatch, "parentOrderQty", "parentPcs");
		
		this.doInstructBatch(newBatch, null, true);
		
		
		return retCnt;
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
			batch.setEquipGroupCd(batch.getEquipCd());
			this.queryManager.update(batch, "status", "instructedAt", "equipGroupCd");
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
		Map<String, Object> drParams = ValueUtil.newMap("batchId", batch.getId());
		List<Map> mheDrList = this.queryManager.selectListBySql(queryStore.getSdpsPasOrder(), drParams, Map.class, 0, 0);
		
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
			wcsMhePasOrder.setOrderQty(ValueUtil.toInteger(detail.get("pick_qty")));
			wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
			wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
			wcsMhePasOrder.setStrrId(ValueUtil.toString(detail.get("strr_id")));
			pasOrderList.add(wcsMhePasOrder);
		}
		
		if(ValueUtil.isNotEmpty(pasOrderList)) {
			AnyOrmUtil.insertBatch(pasOrderList, 100);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void interfaceRack(JobBatch batch) {
		Map<String, Object> drParams = ValueUtil.newMap("batchId", batch.getId());
		List<Map> mheDrList = this.queryManager.selectListBySql(queryStore.getSdpsDasOrder(), drParams, Map.class, 0, 0);
		
		Query mainConds = new Query();
		mainConds.addFilter("id", batch.getBatchGroupId());
		JobBatch mainBatch = this.queryManager.select(JobBatch.class, mainConds);
		
		List<WcsMheDasOrder> dasOrderList = new ArrayList<WcsMheDasOrder>(mheDrList.size());
		for (Map detail : mheDrList) {
			WcsMheDasOrder wcsMheDasOrder = new WcsMheDasOrder();
			wcsMheDasOrder.setId(UUID.randomUUID().toString());
			wcsMheDasOrder.setBatchNo(batch.getId());
			wcsMheDasOrder.setMheNo(batch.getEquipCd());
			wcsMheDasOrder.setJobDate(mainBatch.getJobDate().replaceAll("-", ""));
			wcsMheDasOrder.setJobType(WcsMheDasOrder.JOB_TYPE_DPS);
			wcsMheDasOrder.setCellNo(ValueUtil.toString(detail.get("class_cd")));
			wcsMheDasOrder.setChuteNo(ValueUtil.toString(detail.get("sub_equip_cd")));
			wcsMheDasOrder.setShopCd(ValueUtil.toString(detail.get("shipto_id")));
			wcsMheDasOrder.setShopNm(ValueUtil.toString(detail.get("shipto_nm")));
			wcsMheDasOrder.setItemCd(ValueUtil.toString(detail.get("item_cd")));
			wcsMheDasOrder.setBarcode(ValueUtil.toString(detail.get("barcode")));
			wcsMheDasOrder.setBarcode2(ValueUtil.toString(detail.get("barcode2")));
			wcsMheDasOrder.setStrrId(ValueUtil.toString(detail.get("strr_id")));
			wcsMheDasOrder.setItemSeason(ValueUtil.toString(detail.get("item_season")));
			wcsMheDasOrder.setItemStyle(ValueUtil.toString(detail.get("item_style")));
			wcsMheDasOrder.setItemColor(ValueUtil.toString(detail.get("item_color")));
			wcsMheDasOrder.setItemSize(ValueUtil.toString(detail.get("item_size")));
			wcsMheDasOrder.setOrderQty(ValueUtil.toInteger(detail.get("pick_qty")));
			wcsMheDasOrder.setIfYn(LogisConstants.N_CAP_STRING);
			wcsMheDasOrder.setInsDatetime(DateUtil.getDate());
			
			dasOrderList.add(wcsMheDasOrder);
		}
		
		if(ValueUtil.isNotEmpty(dasOrderList)) {
			AnyOrmUtil.insertBatch(dasOrderList, 100);
		}
	}
	
}
