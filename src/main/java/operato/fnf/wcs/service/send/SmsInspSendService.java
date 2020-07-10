package operato.fnf.wcs.service.send;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.entity.WcsMhePasOrder;
import operato.fnf.wcs.entity.WmsWmtUifImpInbRtnTrg;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
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
		
		
		List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(rtnTrgList.size());
		String srtDate = DateUtil.dateStr(new Date(), "yyyyMMdd");
		
		for (WmsWmtUifImpInbRtnTrg rtnTrg : rtnTrgList) {
			WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
			wcsMhePasOrder.setId(UUID.randomUUID().toString());
			wcsMhePasOrder.setBatchNo(batch.getBatchGroupId());
			wcsMhePasOrder.setMheNo(batch.getEquipCd());
			wcsMhePasOrder.setJobDate(srtDate);
			wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
			wcsMhePasOrder.setBoxId(rtnTrg.getRefNo());
			wcsMhePasOrder.setSkuCd(rtnTrg.getRefDetlNo());
			wcsMhePasOrder.setOrderQty(rtnTrg.getInbEctQty());
			wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
			wcsMhePasOrder.setIfYn(LogisConstants.N_CAP_STRING);
			
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
		dsQueryManager.executeBySql(queryStore.getSrtnInspBoxTrgUpdate(), inspParams);
	}
}
