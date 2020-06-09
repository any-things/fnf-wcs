package operato.fnf.wcs.service.batch;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.EquipGroup;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 배치 시작 서비스
 * 
 * @author shortstop
 */
@Component
public class DasStartBatchService extends AbstractQueryService {

	/**
	 * MheHr 정보로 부터 JobBatch에 배치 시작 정보를 반영한다.
	 * 
	 * @param domainId
	 * @param wcsMheHr
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void startBatch(Long domainId, WcsMheHr wcsMheHr) {
		// 1. WCS MHE_HR 정보로 부터 작업 배치를 조회 
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("id", wcsMheHr.getWorkUnit());
		JobBatch batch = this.queryManager.selectByCondition(JobBatch.class, condition);
		
		if(batch == null || ValueUtil.isEqualIgnoreCase(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
			return;
		}
		
		// 2. 배치에 반영 
		this.setBatchInfoOnStarting(batch, wcsMheHr);
		
		// 3. WcsMheHr 엔티티에 반영
		wcsMheHr.setStatus("B");
		wcsMheHr.setPrcsYn(LogisConstants.Y_CAP_STRING);
		this.queryManager.update(wcsMheHr, "status", "prcsYn");
		
		// 4. WMS MHE_HR 테이블에 반영
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		condition = new Query();
		condition.addFilter("whCd", wcsMheHr.getWhCd());
		condition.addFilter("workUnit", wcsMheHr.getWorkUnit());
		WmsMheHr wmsWave = wmsQueryMgr.selectByCondition(WmsMheHr.class, condition);
		
		if(wmsWave != null) {
			wmsWave.setStatus("B");
			wmsWave.setMheNo(wcsMheHr.getMheNo());
			wmsWave.setCnfDatetime(new Date());
			wmsQueryMgr.update(wmsWave, "status", "mheNo", "cnfDatetime");
		}
	}
	
	/**
	 * 배치 종료시에 설정할 정보 설정
	 * 
	 * @param batch
	 * @param wcsMheHr
	 */
	private void setBatchInfoOnStarting(JobBatch batch, WcsMheHr wcsMheHr) {
		batch.setStatus(JobBatch.STATUS_RUNNING);
		batch.setInstructedAt(new Date());
		batch.setEquipGroupCd(wcsMheHr.getMheNo());
		batch.setInputWorkers(0);
		batch.setProgressRate(0.0f);
		batch.setResultBoxQty(0);
		batch.setResultOrderQty(0);
		batch.setResultPcs(0);
		batch.setEquipRuntime(0.0f);
		batch.setUph(0.0f);
		
		if(ValueUtil.isNotEmpty(wcsMheHr.getMheNo())) {
			Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
			condition.addFilter("equipGroupCd", wcsMheHr.getMheNo());
			EquipGroup eg = this.queryManager.selectByCondition(EquipGroup.class, condition);
			
			if(eg != null) {
				batch.setAreaCd(eg.getAreaCd());
				batch.setStageCd(eg.getStageCd());
				batch.setInputWorkers(eg.getInputWorkers());
				batch.setTotalWorkers(eg.getTotalWorkers());
			}
		}
		
		this.queryManager.update(batch, "areaCd", "stageCd", "equipGroupCd", "status", "inputWorkers", "totalWorkers", "resultBoxQty", "resultOrderQty", "resultPcs", "progressRate", "equipRuntime", "uph", "instructedAt", "updatedAt");
	}

}
