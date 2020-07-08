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
import operato.fnf.wcs.entity.WmsWmtUifWcsInbRtnCnfm;
import operato.logis.sms.query.SmsQueryStore;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DAS 박스 실적 전송 서비스
 * 
 * @author shortstop
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
		Query wmsCondition = new Query();
		String[] batchInfo = batch.getId().split("-");
		if(batchInfo.length == 4) {
			wmsCondition.addFilter("STRR_ID", batchInfo[0]);
			wmsCondition.addFilter("REF_SEASON", batchInfo[1]);
			wmsCondition.addFilter("SHOP_RTN_TYPE", batchInfo[2]);
			wmsCondition.addFilter("SHOP_RTN_SEQ", batchInfo[3]);
			wmsCondition.addFilter("WCS_IF_CHK", "N");
		}
		
		IQueryManager dsQueryManager = this.getDataSourceQueryManager(WmsWmtUifImpInbRtnTrg.class);
		List<WmsWmtUifWcsInbRtnCnfm> rtnCnfmList = dsQueryManager.selectList(WmsWmtUifWcsInbRtnCnfm.class, wmsCondition);
		
		List<String> skuCdList = AnyValueUtil.filterValueListBy(rtnCnfmList, "refDetlNo");
		
		if(ValueUtil.isEmpty(skuCdList)) {
			skuCdList.add("1");
		}
		
		String skuInfoQuery = queryStore.getSrtnCnfmQuery();
		Map<String,Object> sqlParams = ValueUtil.newMap("batchId,skuCd", batch.getId(), skuCdList);
		List<Map> skuInfoList = this.queryManager.selectListBySql(skuInfoQuery, sqlParams, Map.class, 0, 0);
		
		
		List<WcsMhePasOrder> pasOrderList = new ArrayList<WcsMhePasOrder>(rtnCnfmList.size());
		Date currentTime = new Date();
		String currentTimeStr = DateUtil.dateTimeStr(currentTime, "yyyyMMddHHmmss");
		
		for (WmsWmtUifWcsInbRtnCnfm cnfmTrg : rtnCnfmList) {
			WcsMhePasOrder wcsMhePasOrder = new WcsMhePasOrder();
			wcsMhePasOrder.setId(UUID.randomUUID().toString());
			wcsMhePasOrder.setBatchNo(batch.getId());
			wcsMhePasOrder.setJobDate(cnfmTrg.getInbDate());
			wcsMhePasOrder.setJobType(WcsMhePasOrder.JOB_TYPE_RTN);
			wcsMhePasOrder.setBoxId(cnfmTrg.getRefNo());
			wcsMhePasOrder.setSkuCd(cnfmTrg.getRefDetlNo());
			wcsMhePasOrder.setOrderQty(cnfmTrg.getInbCmptQty());
			wcsMhePasOrder.setInsDatetime(DateUtil.getDate());
			wcsMhePasOrder.setIfYn("N");
			
			for (Map skuInfo : skuInfoList) {
				if(ValueUtil.isEqual(skuInfo.get("sku_cd"), cnfmTrg.getRefDetlNo())) {
					wcsMhePasOrder.setSkuBcd(ValueUtil.toString(skuInfo.get("sku_barcd")));
					wcsMhePasOrder.setChuteNo(ValueUtil.toString(skuInfo.get("sub_equip_cd")));	
				}
			}
			pasOrderList.add(wcsMhePasOrder);
			cnfmTrg.setWcsIfChk(SysConstants.CAP_Y_STRING);
			cnfmTrg.setWcsIfChkDtm(currentTimeStr);
		}
		
		if(ValueUtil.isNotEmpty(pasOrderList)) {
			AnyOrmUtil.insertBatch(pasOrderList, 100);
		}
		dsQueryManager.updateBatch(rtnCnfmList);
	}
}
