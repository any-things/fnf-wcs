package operato.fnf.wcs.service.sms;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.service.batch.SmsCloseBatchService;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.elidom.exception.server.ElidomValidationException;
import xyz.elidom.util.ValueUtil;

@Component
public class SmsSrtnBatchClose extends AbstractLogisService {

	@Autowired
	private SmsCloseBatchService smsCloseBatchSvc;
	
	public ResponseObj smsSrtnBatchClose(Map<String, Object> params) throws Exception {
		String batchId = String.valueOf(params.get("id"));
		FnfUtils.checkValueEmpty("BatchId", batchId);
		
		JobBatch batch = this.queryManager.select(JobBatch.class, batchId);
		
		if(ValueUtil.isEmpty(batch)) {
			throw new ElidomValidationException("작업배치[" + batchId + "]는(은) 작업 내역이 없습니다.");
		}
		
		this.smsCloseBatchSvc.sendInspBoxScanResultToWms(batch);
//		this.smsCloseBatchSvc.sendRtnBoxResultToWms(batch);
		
		return new ResponseObj();
	}
}
