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
import operato.fnf.wcs.entity.WcsMhePasOrder;
import operato.fnf.wcs.entity.WcsMhePasRlst;
import operato.fnf.wcs.entity.WmsWmtUifImpInbRtnTrg;
import operato.fnf.wcs.entity.WmsWmtUifImpMheRtnScan;
import operato.fnf.wcs.entity.WmsWmtUifWcsInbRtnCnfm;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
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
//		String srtDate = DateUtil.dateStr(new Date(), "yyyyMMdd");
		
		for (WmsWmtUifImpInbRtnTrg rtnTrg : rtnTrgList) {
			WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
			wcsMhePasOrder.setId(UUID.randomUUID().toString());
			wcsMhePasOrder.setBatchNo(batch.getBatchGroupId());
			wcsMhePasOrder.setMheNo(batch.getEquipCd());
			wcsMhePasOrder.setJobDate(mainBatch.getJobDate());
			wcsMhePasOrder.setInputDate(rtnTrg.getInbEctDate());
			wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
			wcsMhePasOrder.setBoxId(rtnTrg.getRefNo());
			wcsMhePasOrder.setSkuCd(rtnTrg.getRefDetlNo());
			wcsMhePasOrder.setShopCd(rtnTrg.getSupprId());
			wcsMhePasOrder.setShopNm(rtnTrg.getSupprNm());
			wcsMhePasOrder.setOrderQty(rtnTrg.getInbEctQty());
			wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
			wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
			
			for (Map skuInfo : skuInfoList) {
				if(ValueUtil.isEqual(skuInfo.get("sku_cd"), rtnTrg.getRefDetlNo())) {
					wcsMhePasOrder.setSkuBcd(ValueUtil.toString(skuInfo.get("sku_barcd2")));
					wcsMhePasOrder.setChuteNo(ValueUtil.toString(skuInfo.get("sub_equip_cd")));	
				}
			}
			pasOrderList.add(wcsMhePasOrder);
		}
		
		if(ValueUtil.isNotEmpty(pasOrderList)) {
			AnyOrmUtil.insertBatch(pasOrderList, 100);
		}
		dsQueryManager.executeBySql(queryStore.getSrtnInspBoxTrgUpdate(), inspParams);
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

		Query conds = new Query();
		conds.addFilter("whCd", FnFConstants.WH_CD_ICF);
		conds.addFilter("strrId", batchInfo[0]);
		conds.addFilter("refSeason", batchInfo[1]);
		conds.addFilter("shopRtnType", batchInfo[2]);
		conds.addFilter("shopRtnSeq", batchInfo[3]);
		conds.addFilter("wcsIfChk", LogisConstants.N_CAP_STRING);
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifWcsInbRtnCnfm.class);
		List<WmsWmtUifWcsInbRtnCnfm> rtnCnfmList = dsQueryManager.selectList(WmsWmtUifWcsInbRtnCnfm.class, conds);
		
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
		String sql = "SELECT 'W' || LPAD(FNF_IF.WMS_UIF_IMP_MHE_RTN_SCAN.NEXTVAL,14,'0') AS seq FROM DUAL";
		Map<String, Object> maxSeq = wmsQueryManager.selectBySql(sql, new HashMap<String, Object>(), Map.class);
		int interfaceNo = ValueUtil.toInteger(maxSeq.get("seq"));
		List<WmsWmtUifImpMheRtnScan> resultValue = new ArrayList<WmsWmtUifImpMheRtnScan>(tempResults.size());
		for (WcsMhePasRlst result : tempResults) {
			WmsWmtUifImpMheRtnScan scan = new WmsWmtUifImpMheRtnScan();
			scan.setInterfaceCrtDt(srtDate);
			scan.setInterfaceNo(ValueUtil.toString(interfaceNo));
			scan.setWhCd(FnFConstants.WH_CD_ICF);
			scan.setStrrId(batchInfo[0]);
			scan.setInbNo(result.getBoxId());
			scan.setInbDetlNo(result.getSkuCd());
			scan.setItemCd(result.getSkuCd());
			scan.setQty(result.getQty());
			scan.setDmgQty(result.getDmgQty());
			scan.setNewYn(result.getNewYn());
			scan.setInsPersonId(result.getMheNo());
			scan.setInsDatetime(result.getInsDatetime());
			
			resultValue.add(scan);
			interfaceNo++;
		}
		
		this.queryManager.updateBatch(pasResults);
		dsQueryManager.updateBatch(rtnCnfmList);
		wmsQueryManager.insert(resultValue);
	}
}
