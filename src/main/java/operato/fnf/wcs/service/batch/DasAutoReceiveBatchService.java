package operato.fnf.wcs.service.batch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@Service
public class DasAutoReceiveBatchService extends AbstractLogisService {
	/**
	 * 서비스 디스패처
	 */
	@Autowired
	private LogisServiceDispatcher serviceDispatcher;
	
	@Autowired
	protected EventPublisher eventPublisher;

	private final String DAS_AUTO_RECEIVE_DAYS_KEY = "das.auto.receive.days";
	
	public ResponseObj dasAutoReceiveBatchService(Map<String, String> params) {
		String comCd = "_na_";
		String areaCd = "_na_";
		String stageCd = "_na_";
		String jobType = "DAS";
		
		String workDate = String.valueOf(params.get("workDate"));
		if (ValueUtil.isEmpty(workDate)) {
			workDate = DateUtil.currentDate();
		}
		
		Integer autoReceiveDays = 30;
		try {			
			autoReceiveDays = Integer.parseInt(SettingUtil.getValue(DAS_AUTO_RECEIVE_DAYS_KEY));
		} catch(Exception e) {
			logger.error("DasAutoReceiveBatchService parseInt Error~~", e);
		}
		
		String toDate = DateUtil.addDateToStr(DateUtil.parse(workDate, "yyyy-MM-dd"), autoReceiveDays);
		
		while (workDate.compareTo(toDate) <= 0) {
			BatchReceipt receipt = BeanUtil.get(DasAutoReceiveBatchService.class).prepare(areaCd, stageCd, comCd, workDate, jobType);
			
			workDate = DateUtil.addDateToStr(DateUtil.parse(workDate, "yyyy-MM-dd"), 1);
			if (ValueUtil.isEmpty(receipt.getItems()) || AnyConstants.COMMON_STATUS_FINISHED.equalsIgnoreCase(receipt.getStatus())) {
				continue;
			}
			
			try {
				this.serviceDispatcher.getReceiveBatchService().startToReceive(receipt);
			} catch(Exception e) {
				logger.error("DasAutoReceiveBatchService~~", e);
			}
		}
		
		return new ResponseObj();
	}
	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public BatchReceipt prepare(String areaCd, String stageCd, String comCd, String workDate, String jobType) {
//		BatchReceipt receipt = this.serviceDispatcher.getReceiveBatchService()
//				.readyToReceive(Domain.currentDomainId(), areaCd, stageCd, comCd, workDate, jobType);
//		return receipt;
//	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BatchReceipt prepare(String areaCd, String stageCd, String comCd, String workDate, String jobType) {
		// 1. BatchReceipt 하나 생성
		int jobSeq = BatchReceipt.newBatchReceiptJobSeq(Domain.currentDomainId(), areaCd, stageCd, comCd, workDate);
		BatchReceipt batchReceipt = new BatchReceipt();
		batchReceipt.setComCd(comCd);
		batchReceipt.setAreaCd(areaCd);
		batchReceipt.setStageCd(stageCd);
		batchReceipt.setJobDate(workDate);
		batchReceipt.setJobSeq(ValueUtil.toString(jobSeq));
		batchReceipt.setStatus(LogisConstants.COMMON_STATUS_WAIT);
		this.queryManager.insert(batchReceipt);
		batchReceipt.setStatus("AW");
		
		this.readyToReceiveEvent(SysEvent.EVENT_STEP_BEFORE, Domain.currentDomainId(), jobType, areaCd, stageCd, comCd, workDate, batchReceipt);
		
		// 4. 수신 정보가 있는지 체크 
		if(ValueUtil.isEmpty(batchReceipt.getItems())) {
			batchReceipt.setStatus(AnyConstants.COMMON_STATUS_FINISHED);
		}
		
		// 5. 수신 정보가 있다면 리턴
		return batchReceipt;
	}
	
	private EventResultSet readyToReceiveEvent(
			short eventStep, 
			Long domainId, 
			String jobType, 
			String areaCd, 
			String stageCd, 
			String comCd, 
			String jobDate, 
			BatchReceipt receiptData, 
			Object ... params) {
		
		return this.publishBatchReceiveEvent(EventConstants.EVENT_RECEIVE_TYPE_RECEIPT, eventStep, domainId, jobType, areaCd, stageCd, comCd, jobDate, receiptData, null, params);
	}
	
	private EventResultSet publishBatchReceiveEvent(
			short eventType, 
			short eventStep, 
			Long domainId, 
			String jobType, 
			String areaCd, 
			String stageCd, 
			String comCd, 
			String jobDate, 
			BatchReceipt receiptData, 
			JobBatch batch, 
			Object ... params) {
		
		// 1. 이벤트 생성 
		BatchReceiveEvent receiptEvent = new BatchReceiveEvent(domainId, eventType, eventStep);
		receiptEvent.setJobType(jobType);
		receiptEvent.setComCd(comCd);
		receiptEvent.setAreaCd(areaCd);
		receiptEvent.setStageCd(stageCd);
		receiptEvent.setJobDate(jobDate);
		receiptEvent.setJobBatch(batch);
		receiptEvent.setReceiptData(receiptData);
		receiptEvent.setPayload(params);
		
		// 2. Event Publish
		receiptEvent = (BatchReceiveEvent)this.eventPublisher.publishEvent(receiptEvent);
		return receiptEvent.getEventResultSet();
	}
}
