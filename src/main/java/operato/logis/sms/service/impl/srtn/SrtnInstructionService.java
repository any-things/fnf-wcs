package operato.logis.sms.service.impl.srtn;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WcsMheDasOrder;
import operato.fnf.wcs.entity.WcsMhePasOrder;
import operato.fnf.wcs.entity.WmsWmtUifImpInbRtnTrg;
import operato.fnf.wcs.entity.WmsWmtUifWcsInbRtnCnfm;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.entity.SKU;
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
			// TODO 쿼리 수정 해야함 
			instructCount += this.doInstructBatch(batch, equipCdList, false);
		}
		
		return instructCount;
	}

	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
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
		
		Query query = AnyOrmUtil.newConditionForExecution(mainBatch.getDomainId());
		query.addFilter("batchGroupId", mainBatch.getId());
		List<JobBatch> jobBatches = this.queryManager.selectList(JobBatch.class, query);
		List<String> batchIds = AnyValueUtil.filterValueListBy(jobBatches, "id");
		Map<String, Object> condition = ValueUtil.newMap("batchIds", batchIds);
		String sql = "SELECT COALESCE(SUM(TOTAL_PCS), 0) AS PCS_CNT, COUNT(DISTINCT(CELL_ASSGN_CD)) AS ORDER_CNT FROM ORDER_PREPROCESSES WHERE BATCH_ID IN (:batchIds )";
		Map<String, Object> totalResult = this.queryManager.selectBySql(sql, condition, Map.class);
		mainBatch.setParentOrderQty(ValueUtil.toInteger(totalResult.get("order_cnt")));
		
		this.queryManager.update(mainBatch, "parentOrderQty");
		
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
		condition.addSelect("id", "cellCd", "classCd", "categoryFlag");
		condition.addFilter("cellCd", SysConstants.IN, cellNoList);
		condition.addFilter("activeFlag", true);
		condition.addOrder("cellCd", false);
		List<Cell> cellList = this.queryManager.selectList(Cell.class, condition);
		
		int cellCount = cellList.size();
		for(int i = 0 ; i < cellCount ; i++) {
			Cell cell = cellList.get(i);
			this.generateJobInstances(batch, cell, preprocesses);
		}
		if(ValueUtil.isEqual(batch.getBatchType(), FnFConstants.ORDER_RECEIVE_WMS)) {
			this.interfaceSorter(batch);
		} else {
			this.interfaceSorterUpload(batch);
		}
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
			if(ValueUtil.isEqual(cell.getCellCd(), preprocess.getClassCd())) {
				if(cell.getCategoryFlag()) {
					String value = "";
					if(ValueUtil.isNotEmpty(cell.getClassCd()) && ValueUtil.isNotEqual(cell.getClassCd(), preprocess.getCellAssgnNm())) {
						value = cell.getClassCd() + ", " + preprocess.getCellAssgnNm();
						cell.setClassCd(value);
					} else if(ValueUtil.isEmpty(cell.getClassCd())) {
						value = preprocess.getCellAssgnNm();
						cell.setClassCd(value);
					}
				} else {
					cell.setClassCd(preprocess.getCellAssgnCd());
				}
				cell.setBatchId(batch.getBatchGroupId());
				
				break;
			}
		}
	}
	
	private void interfaceSorterUpload(JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length < 4) {
			String msg = MessageUtil.getMessage("no_batch_id", "설비에서 운영중인 BatchId가 아닙니다.");
			throw ThrowUtil.newValidationErrorWithNoLog(msg);
		}
		
		Query condition = new Query();
		condition.addFilter("id", batch.getBatchGroupId());
		JobBatch mainBatch = this.queryManager.select(JobBatch.class, condition);

		Map<String, Object> inspParams = ValueUtil.newMap("batchId,jobDate", batch.getId(), mainBatch.getJobDate().replaceAll("-", ""));
		
		List<WcsMhePasOrder> pasOrderList = this.queryManager.selectListBySql(queryStore.getSrtnUploadBatchSendPasOrder(), inspParams, WcsMhePasOrder.class, 0, 0);
		if(ValueUtil.isNotEmpty(pasOrderList)) {
			AnyOrmUtil.insertBatch(pasOrderList, 100);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void interfaceSorter(JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length < 4) {
			String msg = MessageUtil.getMessage("no_batch_id", "설비에서 운영중인 BatchId가 아닙니다.");
			throw ThrowUtil.newValidationErrorWithNoLog(msg);
		}
		
		if(ValueUtil.isEqual(batch.getRfidYn(), LogisConstants.Y_CAP_STRING)) {
			Map<String, Object> inspParams = ValueUtil.newMap(
					"strrId,season,rtnType,jobSeq,wcsIfChk", batchInfo[0], batchInfo[1],
					batchInfo[2], batchInfo[3], LogisConstants.N_CAP_STRING);
			
			IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifWcsInbRtnCnfm.class);
			List<WmsWmtUifWcsInbRtnCnfm> rtnCnfmList = dsQueryManager.selectListBySql(queryStore.getSrtnInspCnfmBox(), inspParams, WmsWmtUifWcsInbRtnCnfm.class, 0, 0);
			
			List<String> skuCdList = AnyValueUtil.filterValueListBy(rtnCnfmList, "itemCd");
			
			if(ValueUtil.isEmpty(skuCdList)) {
				skuCdList.add("1");
			}
			
			String skuInfoQuery = queryStore.getSrtnCnfmQuery();
			Map<String,Object> sqlParams = ValueUtil.newMap("batchId,skuCd", batch.getId(), skuCdList);
			List<Map> skuInfoList = this.queryManager.selectListBySql(skuInfoQuery, sqlParams, Map.class, 0, 0);
			
			Query condition = new Query();
			condition.addFilter("id", batch.getBatchGroupId());
			JobBatch mainBatch = this.queryManager.select(JobBatch.class, condition);
			
			
			List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(rtnCnfmList.size());
			String srtDate = DateUtil.dateStr(new Date(), "yyyyMMddHHmmss");
			
			for (WmsWmtUifWcsInbRtnCnfm rtnCnfm : rtnCnfmList) {
				WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
				wcsMhePasOrder.setId(UUID.randomUUID().toString());
				wcsMhePasOrder.setBatchNo(batch.getBatchGroupId());
				wcsMhePasOrder.setMheNo(batch.getEquipCd());
				wcsMhePasOrder.setJobDate(mainBatch.getJobDate().replaceAll("-", ""));
				wcsMhePasOrder.setInputDate(rtnCnfm.getInbDate());
				wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
				wcsMhePasOrder.setBoxId(rtnCnfm.getRefNo());
				wcsMhePasOrder.setSkuCd(rtnCnfm.getItemCd());
				wcsMhePasOrder.setShopCd(rtnCnfm.getSupprId());
				wcsMhePasOrder.setShopNm(rtnCnfm.getSupprNm());
				wcsMhePasOrder.setOrderQty(rtnCnfm.getInbCmptQty());
				wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
				wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
				wcsMhePasOrder.setStrrId(rtnCnfm.getStrrId());
				
				for (Map skuInfo : skuInfoList) {
					if(ValueUtil.isEqual(skuInfo.get("sku_cd"), rtnCnfm.getItemCd())) {
						wcsMhePasOrder.setSkuBcd(ValueUtil.toString(skuInfo.get("sku_barcd2")));
						wcsMhePasOrder.setChuteNo(ValueUtil.toString(skuInfo.get("sub_equip_cd")));	
					}
				}
				if(ValueUtil.isNotEmpty(wcsMhePasOrder.getChuteNo())) {
					pasOrderList.add(wcsMhePasOrder);
					
					rtnCnfm.setWcsIfChk(LogisConstants.Y_CAP_STRING);
					rtnCnfm.setWcsIfChkDtm(srtDate);
				}
			}
			
			if(ValueUtil.isNotEmpty(pasOrderList)) {
				AnyOrmUtil.insertBatch(pasOrderList, 100);
			}
			dsQueryManager.updateBatch(rtnCnfmList);
			
//			String wmsUpdateSql = "update WMT_UIF_WCS_INB_RTN_CNFM set wcs_if_chk = :updateWcsIfChk, wcs_if_chk_dtm = :wcsIfChkDtm where STRR_ID = :strrId and REF_SEASON = :season and SHOP_RTN_TYPE = :rtnType and SHOP_RTN_SEQ = :jobSeq and WCS_IF_CHK = :wcsIfChk";
//			Map<String, Object> updateParams = ValueUtil.newMap("updateWcsIfChk,wcsIfChkDtm,strrId,season,rtnType,jobSeq,wcsIfChk", LogisConstants.CAP_Y_STRING, srtDate, batchInfo[0], batchInfo[1], batchInfo[2], batchInfo[3], LogisConstants.N_CAP_STRING);
//			dsQueryManager.executeBySql(wmsUpdateSql, updateParams);
			
		} else {
			Map<String, Object> inspParams = ValueUtil.newMap(
					"whCd,strrId,season,rtnType,jobSeq", FnFConstants.WH_CD_ICF, batchInfo[0], batchInfo[1], batchInfo[2], batchInfo[3]);
			
			IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifImpInbRtnTrg.class);
			List<WmsWmtUifImpInbRtnTrg> rtnTrgList = dsQueryManager.selectListBySql(queryStore.getSrtnInspBoxTrg(), inspParams, WmsWmtUifImpInbRtnTrg.class, 0, 0);
			List<WmsWmtUifImpInbRtnTrg> tempRtnTrgList = new ArrayList<WmsWmtUifImpInbRtnTrg>(rtnTrgList.size());
			List<String> skuCdList = AnyValueUtil.filterValueListBy(rtnTrgList, "itemCd");
			tempRtnTrgList.addAll(rtnTrgList);
			
			if(ValueUtil.isEmpty(skuCdList)) {
				skuCdList.add("1");
			}
			
			Query pasConds = new Query();
			pasConds.addFilter("batchNo", batch.getBatchGroupId());
			List<WcsMhePasOrder> pasList = this.queryManager.selectList(WcsMhePasOrder.class, pasConds);
			
			
			String skuInfoQuery = queryStore.getSrtnCnfmQuery();
			Map<String,Object> sqlParams = ValueUtil.newMap("batchId,skuCd", batch.getId(), skuCdList);
			List<Map> skuInfoList = this.queryManager.selectListBySql(skuInfoQuery, sqlParams, Map.class, 0, 0);
			
			Query condition = new Query();
			condition.addFilter("id", batch.getBatchGroupId());
			JobBatch mainBatch = this.queryManager.select(JobBatch.class, condition);
			
			List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(rtnTrgList.size());
			
			for (WmsWmtUifImpInbRtnTrg trg : rtnTrgList) {
				for (WcsMhePasOrder pas : pasList) {
					if(ValueUtil.isEqual(trg.getRefNo(), pas.getBoxId()) && ValueUtil.isEqual(trg.getItemCd(), pas.getSkuCd())) {
						tempRtnTrgList.remove(trg);
						break;
					}
				}
			}
			
			for (WmsWmtUifImpInbRtnTrg rtnTrg : tempRtnTrgList) {
				WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
				wcsMhePasOrder.setId(UUID.randomUUID().toString());
				wcsMhePasOrder.setBatchNo(batch.getBatchGroupId());
				wcsMhePasOrder.setMheNo(batch.getEquipCd());
				wcsMhePasOrder.setJobDate(mainBatch.getJobDate().replaceAll("-", ""));
				wcsMhePasOrder.setInputDate(rtnTrg.getInbEctDate());
				wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
				wcsMhePasOrder.setBoxId(rtnTrg.getRefNo());
				wcsMhePasOrder.setSkuCd(rtnTrg.getItemCd());
				wcsMhePasOrder.setShopCd(rtnTrg.getSupprId());
				wcsMhePasOrder.setShopNm(rtnTrg.getSupprNm());
				wcsMhePasOrder.setOrderQty(rtnTrg.getInbEctQty());
				wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
				wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
				wcsMhePasOrder.setStrrId(rtnTrg.getStrrId());
				
				for (Map skuInfo : skuInfoList) {
					if(ValueUtil.isEqual(skuInfo.get("sku_cd"), rtnTrg.getItemCd())) {
						wcsMhePasOrder.setSkuBcd(ValueUtil.toString(skuInfo.get("sku_barcd2")));
						wcsMhePasOrder.setChuteNo(ValueUtil.toString(skuInfo.get("sub_equip_cd")));	
						
						pasOrderList.add(wcsMhePasOrder);
						break;
					}
				}
			}
			
			if(ValueUtil.isNotEmpty(pasOrderList)) {
				AnyOrmUtil.insertBatch(pasOrderList, 100);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void interfaceRack(JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		
		Long domainId = batch.getDomainId();
		Query query = AnyOrmUtil.newConditionForExecution(domainId);
		query.addFilter("batchId", batch.getId());  
		List<OrderPreprocess> preprocesses = this.queryManager.selectList(OrderPreprocess.class, query);
		
		List<String> skuCdList = AnyValueUtil.filterValueListBy(preprocesses, "cellAssgnCd");

		//동일한 batchId로 줘야한다. bathGroupId로 줘야함 그리고 동일 셀에 sku_cd인놈은 제외하고 insert 한다
		List<WcsMheDasOrder> dasOrderList = new ArrayList<WcsMheDasOrder>(preprocesses.size());
		
		Query conds = new Query();
		conds.addFilter("batchNo", batch.getBatchGroupId());
		List<WcsMheDasOrder> dasList = this.queryManager.selectList(WcsMheDasOrder.class, conds);
		List<String> dasSkuList = AnyValueUtil.filterValueListBy(dasList, "itemCd");
		
		skuCdList.removeAll(dasSkuList);
		
		Query skuQuery = AnyOrmUtil.newConditionForExecution(domainId);
		skuQuery.addSelect("skuCd", "skuBarcd", "skuBarcd2", "styleCd", "sizeCd", "colorCd");
		skuQuery.addFilter("skuCd", SysConstants.IN, skuCdList.size() == 0 ? '1' : skuCdList);
		List<SKU> skuInfoList = this.queryManager.selectList(SKU.class, skuQuery);
		
		Query mainConds = new Query();
		mainConds.addFilter("id", batch.getBatchGroupId());
		JobBatch mainBatch = this.queryManager.select(JobBatch.class, mainConds);
		
		for (OrderPreprocess preProcess : preprocesses) {
			for (SKU skuInfo : skuInfoList) {
				if(ValueUtil.isEqual(skuInfo.getSkuCd(), preProcess.getCellAssgnCd())) {
					WcsMheDasOrder wcsMheDasOrder = new WcsMheDasOrder();
					wcsMheDasOrder.setId(UUID.randomUUID().toString());
					wcsMheDasOrder.setBatchNo(batch.getBatchGroupId());
					wcsMheDasOrder.setMheNo(batch.getEquipCd());
					wcsMheDasOrder.setJobDate(mainBatch.getJobDate().replaceAll("-", ""));
					wcsMheDasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
					wcsMheDasOrder.setItemCd(preProcess.getCellAssgnCd());
					wcsMheDasOrder.setStrrId(batchInfo[0]);
					wcsMheDasOrder.setItemSeason(batchInfo[1]);
					wcsMheDasOrder.setOrderQty(preProcess.getTotalPcs());
					wcsMheDasOrder.setInsDatetime(DateUtil.getDate());
					wcsMheDasOrder.setIfYn(LogisConstants.N_CAP_STRING);
					
					wcsMheDasOrder.setCellNo(preProcess.getClassCd());
					wcsMheDasOrder.setChuteNo(preProcess.getSubEquipCd());
					wcsMheDasOrder.setBarcode(skuInfo.getSkuBarcd());
					wcsMheDasOrder.setBarcode2(skuInfo.getSkuBarcd2());
					wcsMheDasOrder.setItemStyle(skuInfo.getStyleCd());
					wcsMheDasOrder.setItemColor(skuInfo.getColorCd());
					wcsMheDasOrder.setItemSize(skuInfo.getSizeCd());
					
					dasOrderList.add(wcsMheDasOrder);
					
					break;
				}
			}
		}
		
		if(ValueUtil.isNotEmpty(dasOrderList)) {
			AnyOrmUtil.insertBatch(dasOrderList, 100);
			
			String maxPltNoSql = "select COALESCE(max(cast(plt_no as int)), 0) + 1 as seq from mhe_das_order where batch_no = :batchId";
			Map<String, Object> condition = ValueUtil.newMap("batchId", batch.getBatchGroupId());
			Map<String, Object> maxResult = this.queryManager.selectBySql(maxPltNoSql, condition, Map.class);
			int pltNo = ValueUtil.toInteger(maxResult.get("seq"));
			
			String styleGroupSql = "select item_style, plt_no from mhe_das_order where batch_no = :batchId and plt_no is not null group by item_style, plt_no";
			Map<String,Object> styleGroupParams = ValueUtil.newMap("batchId,skuCd", batch.getBatchGroupId(), skuCdList);
			List<Map> styleGroupList = this.queryManager.selectListBySql(styleGroupSql, styleGroupParams, Map.class, 0, 0);
			
			Map<String, Object> stylePltNo = new HashMap<String, Object>();
			
			for (Map style : styleGroupList) {
				stylePltNo.put(ValueUtil.toString(style.get("item_style")), ValueUtil.toString(style.get("plt_no")));
			}
			
			for (WcsMheDasOrder wcsMheDasOrder : dasOrderList) {
				if(stylePltNo.containsKey(wcsMheDasOrder.getItemStyle())) {
					wcsMheDasOrder.setPltNo(ValueUtil.toString(stylePltNo.get(wcsMheDasOrder.getItemStyle())));
				}else {
					stylePltNo.put(wcsMheDasOrder.getItemStyle(), pltNo);
					wcsMheDasOrder.setPltNo(ValueUtil.toString(stylePltNo.get(wcsMheDasOrder.getItemStyle())));
					pltNo++;
				}
			}
			AnyOrmUtil.updateBatch(dasOrderList, 100, "pltNo");
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
}
