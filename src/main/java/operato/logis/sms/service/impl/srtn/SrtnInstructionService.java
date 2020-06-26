package operato.logis.sms.service.impl.srtn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WcsMheDasOrder;
import operato.fnf.wcs.entity.WcsMhePasOrder;
import operato.fnf.wcs.entity.WmsWmtUifImpInbRtnTrg;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@Component("srtnInstructionService")
public class SrtnInstructionService extends AbstractQueryService implements IInstructionService {
	@Autowired
	protected SmsQueryStore queryStore;
	
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
			// TODO 쿼리 수정 해야함 
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
		List<String> cellNoList = AnyValueUtil.filterValueListBy(preprocesses, "classCd");
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("id", "cellCd", "classCd");
		condition.addFilter("cellCd", SysConstants.IN, cellNoList);
		condition.addFilter("activeFlag", true);
		condition.addOrder("cellCd", false);
		List<Cell> cellList = this.queryManager.selectList(Cell.class, condition);
		
		// 3. 슈트 중에 현재 작업 중이거나 사용 불가한 슈트가 있는지 체크
		for(Cell cell : cellList) {
			if(ValueUtil.isNotEmpty(cell.getClassCd())) {
				// 호기에 다른 작업 배치가 할당되어 있습니다
				throw ThrowUtil.newValidationErrorWithNoLog(true, "ASSIGNED_ANOTHER_BATCH", ValueUtil.toList(cell.getClassCd()));
			}
		}
		
		int cellCount = cellList.size();
		for(int i = 0 ; i < cellCount ; i++) {
			Cell cell = cellList.get(i);
			List<OrderPreprocess> cellPreprocesses = AnyValueUtil.filterListBy(preprocesses, "classCd", cell.getCellCd());
			this.generateJobInstances(batch, cell, cellPreprocesses);
		}
		this.interfaceSorter(batch);
		this.interfaceRack(batch);
				
		
//		AnyOrmUtil.updateBatch(cellList, 100, "classCd");
//		batch.setStatus(JobBatch.STATUS_RUNNING);
//		this.queryManager.update(batch, "status");
		// TODO agent에 정보 생성후 전달 해야한다.
		
		return preprocesses.size();
	}
	
	private void generateJobInstances(JobBatch batch, Cell cell, List<OrderPreprocess> preprocesses) {
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
//		String insertQuery = queryStore.getSrtnGenerateJobInstancesQuery();
		
		for (OrderPreprocess preprocess : preprocesses) {
			params.put("equipCd", preprocess.getEquipCd());
			params.put("equipNm", preprocess.getEquipNm());
			params.put("subEquipCd", preprocess.getSubEquipCd());
			params.put("shopNm", preprocess.getCellAssgnNm());
			params.put("shopCd", preprocess.getCellAssgnCd());
//			this.queryManager.executeBySql(insertQuery, params);
			
			cell.setClassCd(preprocess.getCellAssgnCd());
		}
	}
	
	private void interfaceSorter(JobBatch batch) {
		// TODO 연구소 테스트 DB로 검수결과 확정 테이블이 없어 임시로 매장예정정보로 테스트
		Query wmsCondition = new Query();
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length == 4) {
			wmsCondition.addFilter("STRR_ID", batchInfo[0]);
			wmsCondition.addFilter("REF_SEASON", batchInfo[1]);
			wmsCondition.addFilter("SHOP_RTN_TYPE", batchInfo[2]);
			wmsCondition.addFilter("SHOP_RTN_SEQ", batchInfo[3]);
		}
		
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifImpInbRtnTrg.class);
		List<WmsWmtUifImpInbRtnTrg> rtnTrgList = dsQueryManager.selectList(WmsWmtUifImpInbRtnTrg.class, wmsCondition);
		
		List<String> skuCdList = AnyValueUtil.filterValueListBy(rtnTrgList, "refDetlNo");
		
		String skuInfoQuery = queryStore.getSrtnCnfmQuery();
		Map<String,Object> sqlParams = ValueUtil.newMap("batchId,skuCd", batch.getId(), skuCdList);
		List<Map> skuInfoList = this.queryManager.selectListBySql(skuInfoQuery, sqlParams, Map.class, 0, 0);
		
		
		List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(rtnTrgList.size());
		
		for (WmsWmtUifImpInbRtnTrg rtnTrg : rtnTrgList) {
			WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
			wcsMhePasOrder.setId(UUID.randomUUID().toString());
			wcsMhePasOrder.setBatchNo(batch.getId());
			wcsMhePasOrder.setJobDate(rtnTrg.getInbEctDate());
			wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
			wcsMhePasOrder.setBoxId(rtnTrg.getRefNo());
			wcsMhePasOrder.setSkuCd(rtnTrg.getRefDetlNo());
			wcsMhePasOrder.setOrderQty(rtnTrg.getInbEctQty());
			wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
			
			for (Map skuInfo : skuInfoList) {
				if(ValueUtil.isEqual(skuInfo.get("sku_cd"), rtnTrg.getRefDetlNo())) {
					wcsMhePasOrder.setSkuBcd(ValueUtil.toString(skuInfo.get("sku_barcd")));
					wcsMhePasOrder.setChuteNo(ValueUtil.toString(skuInfo.get("sub_equip_cd")));	
				}
			}
			pasOrderList.add(wcsMhePasOrder);
		}
		
		if(ValueUtil.isNotEmpty(pasOrderList)) {
			AnyOrmUtil.insertBatch(pasOrderList, 100);
		}
	}
	
	private void interfaceRack(JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		
		Long domainId = batch.getDomainId();
		Query query = AnyOrmUtil.newConditionForExecution(domainId);
		query.addFilter("batchId", batch.getId());  
		List<OrderPreprocess> preprocesses = this.queryManager.selectList(OrderPreprocess.class, query);
		
		List<String> skuCdList = AnyValueUtil.filterValueListBy(preprocesses, "cellAssgnCd");
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("skuCd", "skuBarcd");
		condition.addFilter("skuCd", SysConstants.IN, skuCdList);
		condition.addFilter("batchId", batch.getId());
		List<Order> skuInfoList = this.queryManager.selectList(Order.class, condition);

		List<WcsMheDasOrder> dasOrderList = new ArrayList<WcsMheDasOrder>(preprocesses.size());
		
		for (OrderPreprocess preProcess : preprocesses) {
			WcsMheDasOrder wcsMheDasOrder = new WcsMheDasOrder();
			wcsMheDasOrder.setId(UUID.randomUUID().toString());
			wcsMheDasOrder.setBatchNo(batch.getId());
			wcsMheDasOrder.setJobDate(batch.getJobDate().replaceAll("-", ""));
			wcsMheDasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
			wcsMheDasOrder.setItemCd(preProcess.getCellAssgnCd());
			wcsMheDasOrder.setStrrId(batchInfo[0]);
			wcsMheDasOrder.setItemSeason(batchInfo[1]);
			wcsMheDasOrder.setOrderQty(preProcess.getTotalPcs());
			wcsMheDasOrder.setInsDatetime(DateUtil.getDate());
			
			for (Order skuInfo : skuInfoList) {
				if(ValueUtil.isEqual(skuInfo.getSkuCd(), preProcess.getCellAssgnCd())) {
					wcsMheDasOrder.setCellNo(preProcess.getClassCd());
					wcsMheDasOrder.setChuteNo(preProcess.getSubEquipCd());
					wcsMheDasOrder.setBarcode(skuInfo.getSkuBarcd());
				}
			}
			dasOrderList.add(wcsMheDasOrder);
		}
		
		if(ValueUtil.isNotEmpty(dasOrderList)) {
			AnyOrmUtil.insertBatch(dasOrderList, 100);
		}
	}
}
