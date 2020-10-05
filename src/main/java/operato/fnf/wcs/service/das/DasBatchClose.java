package operato.fnf.wcs.service.das;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.service.batch.DasCloseBatchService;
import operato.logis.sms.SmsConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.ValueUtil;

@Component
public class DasBatchClose extends AbstractLogisService {
	
	@Autowired
	private DasCloseBatchService closeBatchSvc;
	
	public ResponseObj dasBatchClose(Map<String, Object> params) throws Exception {
		String workUnit = String.valueOf(params.get("id"));
		FnfUtils.checkValueEmpty("WmsBatchId", workUnit);
		
		Query mheHrConds = new Query();
		//mheHrConds.addFilter("domainId", Domain.currentDomainId());
		mheHrConds.addFilter("workUnit", workUnit);
		WcsMheHr wcsMheHr = this.queryManager.selectByCondition(true, WcsMheHr.class, mheHrConds);
		if (!"C".equals(wcsMheHr.getStatus())) {
			throw new ElidomValidationException("작업배치[" + workUnit + "]는(은) DAS에서 작업완료가 되지 않았습니다.");
		}
		
		// 1. WCS MHE_HR 정보로 부터 작업 배치를 조회 
		Query batchConds = new Query();
		batchConds.addFilter("domainId", Domain.currentDomainId());
		batchConds.addFilter("id", workUnit);
		JobBatch jobBatch = this.queryManager.selectByCondition(true, JobBatch.class, batchConds);

		// 2. WMS에 박스 실적 한 번에 전송
		closeBatchSvc.sendAllBoxToWms(jobBatch);
		
		// 3. WMS에 최종 피킹 실적을 한 번에 전송
		closeBatchSvc.sendAllPickingToWms(jobBatch);
		
		// 4. 10분 생산성 최종 마감
		closeBatchSvc.closeProductivity(jobBatch);
		
		// 5. 배치에 반영 
		closeBatchSvc.setBatchInfoOnClosing(jobBatch);
		
		// 5-1. SDAS 인경우 Rack, Cell 초기화
		if(ValueUtil.isEqual(jobBatch.getJobType(), SmsConstants.JOB_TYPE_SDAS)) {
			closeBatchSvc.resetRacksAndCells(jobBatch);
			closeBatchSvc.deleteOrderPreprocesses(jobBatch);
		}
		
		// 6. WMS MHE_HR 테이블에 반영
		closeBatchSvc.closeWmsWave(jobBatch, wcsMheHr);
		
		// 7. WCS MHE_HR 테이블에 반영
		closeBatchSvc.closeWcsWave(jobBatch, wcsMheHr);
		
		
		return new ResponseObj();
	}	
}
