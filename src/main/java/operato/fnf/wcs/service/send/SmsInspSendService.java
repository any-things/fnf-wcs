package operato.fnf.wcs.service.send;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.DpsJobInstance;
import operato.fnf.wcs.entity.WcsMhePasOrder;
import operato.fnf.wcs.entity.WcsMhePasRlst;
import operato.fnf.wcs.entity.WmsWmtUifImpInbRtnTrg;
import operato.fnf.wcs.entity.WmsWmtUifImpMheRtnScan;
import operato.fnf.wcs.entity.WmsWmtUifWcsInbRtnCnfm;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * SMS 박스 실적 전송 서비스
 * 
 * 
 */
@Component
public class SmsInspSendService extends AbstractQueryService {
	
	/**
	 * Event Publisher
	 */
	@Autowired
	protected ApplicationEventPublisher eventPublisher;
	
	@Autowired
	protected SmsQueryStore queryStore;

	/**
	 * 검수 박스 실적 전송(Sorter로 전송)
	 * 
	 * @param domain
	 * @param batch
	 */
	@SuppressWarnings("rawtypes")
	public void sendInspBoxResults(Domain domain, JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length < 4) {
			String msg = MessageUtil.getMessage("no_batch_id", "설비에서 운영중인 BatchId가 아닙니다.");
			throw ThrowUtil.newValidationErrorWithNoLog(msg);
		}
		
		if(ValueUtil.isEqual(batch.getRfidYn(), LogisConstants.N_CAP_STRING)) {
			Map<String, Object> inspParams = ValueUtil.newMap(
					"strrId,season,rtnType,jobSeq,ifAction,wcsIfChk", batchInfo[0], batchInfo[1],
					batchInfo[2], batchInfo[3], LogisConstants.COMMON_STATUS_SKIPPED, LogisConstants.N_CAP_STRING);
			
			IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifImpInbRtnTrg.class);
			List<WmsWmtUifImpInbRtnTrg> rtnTrgList = dsQueryManager.selectListBySql(queryStore.getSrtnInspBoxTrg(), inspParams, WmsWmtUifImpInbRtnTrg.class, 0, 0);
			
			List<String> skuCdList = AnyValueUtil.filterValueListBy(rtnTrgList, "refDetlNo");
			
			if(ValueUtil.isEmpty(skuCdList)) {
				skuCdList.add("1");
			}
			
			String skuInfoQuery = queryStore.getSrtnCnfmQuery();
			Map<String,Object> sqlParams = ValueUtil.newMap("batchId,skuCd", batch.getId(), skuCdList);
			List<Map> skuInfoList = this.queryManager.selectListBySql(skuInfoQuery, sqlParams, Map.class, 0, 0);
			
			Query condition = new Query();
			condition.addFilter("id", batch.getBatchGroupId());
			JobBatch mainBatch = this.queryManager.select(JobBatch.class, condition);
			
			
			List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(rtnTrgList.size());
			String srtDate = DateUtil.dateStr(new Date(), "yyyyMMddHHmmss");
			
			for (WmsWmtUifImpInbRtnTrg rtnTrg : rtnTrgList) {
				WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
				wcsMhePasOrder.setId(UUID.randomUUID().toString());
				wcsMhePasOrder.setBatchNo(batch.getBatchGroupId());
				wcsMhePasOrder.setMheNo(batch.getEquipCd());
				wcsMhePasOrder.setJobDate(mainBatch.getJobDate().replaceAll("-", ""));
				wcsMhePasOrder.setInputDate(rtnTrg.getInbEctDate());
				wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
				wcsMhePasOrder.setBoxId(rtnTrg.getRefNo());
				wcsMhePasOrder.setSkuCd(rtnTrg.getRefDetlNo());
				wcsMhePasOrder.setShopCd(rtnTrg.getSupprId());
				wcsMhePasOrder.setShopNm(rtnTrg.getSupprNm());
				wcsMhePasOrder.setOrderQty(rtnTrg.getInbEctQty());
				wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
				wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
				wcsMhePasOrder.setStrrId(rtnTrg.getStrrId());
				
				for (Map skuInfo : skuInfoList) {
					if(ValueUtil.isEqual(skuInfo.get("sku_cd"), rtnTrg.getRefDetlNo())) {
						wcsMhePasOrder.setSkuBcd(ValueUtil.toString(skuInfo.get("sku_barcd2")));
						wcsMhePasOrder.setChuteNo(ValueUtil.toString(skuInfo.get("sub_equip_cd")));	
					}
				}
				pasOrderList.add(wcsMhePasOrder);
				
				rtnTrg.setWcsIfChk(LogisConstants.Y_CAP_STRING);
				rtnTrg.setWcsIfChkDtm(srtDate);
			}
			
			if(ValueUtil.isNotEmpty(pasOrderList)) {
				AnyOrmUtil.insertBatch(pasOrderList, 100);
			}
			dsQueryManager.updateBatch(rtnTrgList);
		} else {
			Map<String, Object> inspParams = ValueUtil.newMap(
					"strrId,season,rtnType,jobSeq,wcsIfChk", batchInfo[0], batchInfo[1],
					batchInfo[2], batchInfo[3], LogisConstants.N_CAP_STRING);
			
			IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifWcsInbRtnCnfm.class);
			List<WmsWmtUifWcsInbRtnCnfm> rtnCnfmList = dsQueryManager.selectListBySql(queryStore.getSrtnInspCnfmBox(), inspParams, WmsWmtUifWcsInbRtnCnfm.class, 0, 0);
			
			List<String> skuCdList = AnyValueUtil.filterValueListBy(rtnCnfmList, "refDetlNo");
			
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
				pasOrderList.add(wcsMhePasOrder);
				
				rtnCnfm.setWcsIfChk(LogisConstants.Y_CAP_STRING);
				rtnCnfm.setWcsIfChkDtm(srtDate);
			}
			
			if(ValueUtil.isNotEmpty(pasOrderList)) {
				AnyOrmUtil.insertBatch(pasOrderList, 100);
			}
			dsQueryManager.updateBatch(rtnCnfmList);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendInspBoxScanResultToWms(JobBatch batch) {
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length < 4) {
			String msg = MessageUtil.getMessage("no_batch_id", "설비에서 운영중인 BatchId가 아닙니다.");
			throw ThrowUtil.newValidationErrorWithNoLog(msg);
		}
		Date currentTime = new Date();
		String srtDate = DateUtil.dateStr(currentTime, "yyyyMMdd");
		String currentTimeStr = DateUtil.dateTimeStr(currentTime, "yyyyMMddHHmmss");
		
		Query batchConds = new Query();
		batchConds.addFilter("batchGroupId", batch.getBatchGroupId());
		List<JobBatch> batchGroupList = this.queryManager.selectList(JobBatch.class, batchConds);
		
		String rtnCnfmSql = "select * from WMT_UIF_WCS_INB_RTN_CNFM where WH_CD = :whCd and (strr_id = :strrId AND ref_season = :refSeason AND SHOP_RTN_TYPE = :shopRtnType AND SHOP_RTN_SEQ = :shopRtnSeq AND (WCS_IF_CHK = :wcsIfChk OR WCS_IF_CHK IS NULL))";
		for (JobBatch jobBatch : batchGroupList) {
			String[] jobBatchInfo = jobBatch.getId().split("-");
			if(jobBatchInfo.length < 4) {
				String msg = MessageUtil.getMessage("no_batch_id", "설비에서 운영중인 BatchId가 아닙니다.");
				throw ThrowUtil.newValidationErrorWithNoLog(msg);
			}
			
			rtnCnfmSql += " or (strr_id ='" + jobBatchInfo[0] + "' AND ref_season = '" + jobBatchInfo[1] + "' AND SHOP_RTN_TYPE ='" + jobBatchInfo[2] + "' AND SHOP_RTN_SEQ = " + jobBatchInfo[3] + "AND (WCS_IF_CHK = 'N' OR WCS_IF_CHK IS NULL))";
		}
		
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifWcsInbRtnCnfm.class);
		Map<String, Object> conds = ValueUtil.newMap("whCd,strrId,refSeason,shopRtnType,shopRtnSeq,wcsIfChk", FnFConstants.WH_CD_ICF, batchInfo[0], batchInfo[1], batchInfo[2], batchInfo[3], LogisConstants.N_CAP_STRING);
		List<WmsWmtUifWcsInbRtnCnfm> rtnCnfmList = dsQueryManager.selectListBySql(rtnCnfmSql, conds, WmsWmtUifWcsInbRtnCnfm.class, 0, 0);
		
		Query condition = new Query();
		condition.addFilter("batchNo", batch.getBatchGroupId());
		condition.addFilter("ifYn", LogisConstants.N_CAP_STRING);
		List<WcsMhePasRlst> pasResults = this.queryManager.selectList(WcsMhePasRlst.class, condition);
		
		List<WcsMhePasRlst> tempResults = new ArrayList<WcsMhePasRlst>(pasResults.size());
		tempResults.addAll(pasResults);
		
		for (WcsMhePasRlst pasResult : pasResults) {
			for (WmsWmtUifWcsInbRtnCnfm wmsResult : rtnCnfmList) {
				if(ValueUtil.isEqual(pasResult.getBoxId(), wmsResult.getRefNo()) && ValueUtil.isEqual(pasResult.getSkuCd(), wmsResult.getItemCd())) {
					tempResults.remove(pasResult);
					wmsResult.setWcsIfChk(LogisConstants.CAP_Y_STRING);
					wmsResult.setWcsIfChkDtm(currentTimeStr);
				}
			}
			pasResult.setIfYn(LogisConstants.CAP_Y_STRING);
		}
		
		IQueryManager wmsQueryManager = this.getDataSourceQueryManager(WmsWmtUifImpMheRtnScan.class);
		
		List<WmsWmtUifImpMheRtnScan> resultValue = new ArrayList<WmsWmtUifImpMheRtnScan>(tempResults.size());
		for (WcsMhePasRlst result : tempResults) {
			String sql = "SELECT 'W' || LPAD(FNF_IF.WMS_UIF_IMP_MHE_RTN_SCAN.NEXTVAL,14,'0') AS seq FROM DUAL";
			Map<String, Object> maxSeq = wmsQueryManager.selectBySql(sql, new HashMap<String, Object>(), Map.class);
			String interfaceNo = ValueUtil.toString(maxSeq.get("seq"));
			
			WmsWmtUifImpMheRtnScan scan = new WmsWmtUifImpMheRtnScan();
			scan.setInterfaceCrtDt(srtDate);
			scan.setInterfaceNo(ValueUtil.toString(interfaceNo));
			scan.setWhCd(FnFConstants.WH_CD_ICF);
			if(ValueUtil.isEmpty(result.getStrrId())) {
				String skuSql = "select * from sku where domain_id = :domainId and com_cd = :comCd and (sku_cd = :skuCd or sku_barcd = :skuCd or sku_barcd2 = :skuCd)";
				Map<String,Object> skuParams = ValueUtil.newMap("domainId,comCd,skuCd", batch.getDomainId(), batch.getComCd(), result.getSkuCd());
				List<SKU> skuList = this.queryManager.selectListBySql(skuSql, skuParams, SKU.class, 0, 0);
				if(ValueUtil.isEmpty(skuList)) {
					scan.setStrrId("EMPTY");
				} else {
					scan.setStrrId(skuList.get(0).getBrandCd());
					scan.setItemCd(skuList.get(0).getSkuCd());
				}
			} else {
				scan.setStrrId(result.getStrrId());
			}
			scan.setInbNo(result.getBoxId());
			scan.setInbDetlNo(result.getSkuCd());
			scan.setItemCd(result.getSkuCd());
			scan.setQty(result.getQty());
			scan.setDmgQty(result.getDmgQty());
			scan.setNewYn(result.getNewYn());
			scan.setInsPersonId(result.getMheNo());
			scan.setInsDatetime(result.getInsDatetime());
			scan.setIfYn(LogisConstants.N_CAP_STRING);
			
			resultValue.add(scan);
		}
		
		this.queryManager.updateBatch(pasResults);
		dsQueryManager.updateBatch(rtnCnfmList);
		wmsQueryManager.insertBatch(resultValue);
	}
	
	public void sendSdpsBoxResults(Domain domain, JobBatch batch) {
		Map<String, Object> condition = ValueUtil.newMap("domainId,boxStatus,batchId,delYn", batch.getDomainId(), BoxPack.BOX_STATUS_BOXED, batch.getId(), LogisConstants.Y_CAP_STRING);
		List<DpsJobInstance> dpsBoxList = this.queryManager.selectListBySql(queryStore.getSdpsBoxResult(), condition, DpsJobInstance.class, 0, 0);
		
		for (DpsJobInstance dpsJobInstance : dpsBoxList) {
			dpsJobInstance.setId(UUID.randomUUID().toString());
		}
		
		if(ValueUtil.isNotEmpty(dpsBoxList)) {
			AnyOrmUtil.insertBatch(dpsBoxList, 100);
		}

		List<String> refNoList = AnyValueUtil.filterValueListBy(dpsBoxList, "refNo");
		
		for (String ref : refNoList) {
			String sql = "update mhe_box set if_yn = 'Y', if_datetime = now() where work_unit = :batchId and shipto_id = :refNo and del_yn != 'Y'";
			this.queryManager.executeBySql(sql, ValueUtil.newMap("batchId,refNo", batch.getId(), ref));
		}
		
	}
}
