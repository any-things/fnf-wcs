package operato.fnf.wcs.service.batch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import operato.fnf.wcs.FnfUtils;
import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.event.EventPublisher;
import xyz.elidom.sys.entity.Domain;
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

	public ResponseObj dasAutoReceiveBatchService(Map<String, String> params) {
		String comCd = "_na_";
		String areaCd = "_na_";
		String stageCd = "_na_";
		String jobType = "DAS,RTN";
		
		String workDate = String.valueOf(params.get("workDate"));
		if (ValueUtil.isEmpty(workDate)) {
			workDate = FnfUtils.today();
		}
		
		BatchReceipt receipt = this.serviceDispatcher.getReceiveBatchService()
				.readyToReceive(Domain.currentDomainId(), areaCd, stageCd, comCd, workDate, jobType);
		
		if (AnyConstants.COMMON_STATUS_FINISHED.equalsIgnoreCase(receipt.getStatus())) {
			ResponseObj resp = new ResponseObj();
			resp.setMsg("Receive finished~~~");
			return resp;
		}
		
		this.serviceDispatcher.getReceiveBatchService().startToReceive(receipt);
		
		return new ResponseObj();
	}
	
//	public ResponseObj dasAutoReceiveBatchService(Map<String, String> map) {
//		String comCd = "_na_";
//		String areaCd = "_na_";
//		String stageCd = "_na_";
//		
//		String jobDate = DateUtil.currentDate();
//		
//		int jobSeq = BatchReceipt.newBatchReceiptJobSeq(Domain.currentDomainId(), comCd, areaCd, stageCd, jobDate);
//		BatchReceipt batchReceipt = new BatchReceipt();
//		batchReceipt.setComCd(comCd);
//		batchReceipt.setAreaCd(areaCd);
//		batchReceipt.setStageCd(stageCd);
//		batchReceipt.setJobDate(jobDate);
//		batchReceipt.setJobSeq(ValueUtil.toString(jobSeq));
//		batchReceipt.setStatus(LogisConstants.COMMON_STATUS_WAIT);
//		this.queryManager.insert(batchReceipt);
//		
//		Map<String, Object> params = ValueUtil.newMap("whCd,jobType,jobDate,status", this.whCd, this.DAS_JOB_TYPE, jobDate, "A");
//		EventResultSet resultSet = this.publishBatchReceiveEvent((short)10, SysEvent.EVENT_STEP_BEFORE, Domain.currentDomainId(), 
//				"DAS", comCd, areaCd, stageCd, jobDate, batchReceipt, null, params);
//	
//		if (!resultSet.isExecuted()) {
//			ResponseObj resp = new ResponseObj();
//			resp.setMsg("EventResultSet-10-SysEvent.EVENT_STEP_BEFORE error~");
//			return resp;
//		}
//
//		this.createReadyToReceiveData(batchReceipt);
//		List<BatchReceiptItem> items = batchReceipt.getItems();
//		if (items.size() > 0) {
//			resultSet = this.publishBatchReceiveEvent((short)20, SysEvent.EVENT_STEP_BEFORE, Domain.currentDomainId(), 
//					"DAS", comCd, areaCd, stageCd, jobDate, batchReceipt, null, params);
//		}
//		
//		return new ResponseObj();
//	}
//	
//	private EventResultSet publishBatchReceiveEvent(
//			short eventType, 
//			short eventStep, 
//			Long domainId, 
//			String jobType, 
//			String areaCd, 
//			String stageCd, 
//			String comCd, 
//			String jobDate, 
//			BatchReceipt receiptData, 
//			JobBatch batch, 
//			Object ... params) {
//		
//		// 1. 이벤트 생성 
//		BatchReceiveEvent receiptEvent = new BatchReceiveEvent(domainId, eventType, eventStep);
//		receiptEvent.setJobType(jobType);
//		receiptEvent.setComCd(comCd);
//		receiptEvent.setAreaCd(areaCd);
//		receiptEvent.setStageCd(stageCd);
//		receiptEvent.setJobDate(jobDate);
//		receiptEvent.setJobBatch(batch);
//		receiptEvent.setReceiptData(receiptData);
//		receiptEvent.setPayload(params);
//		
//		// 2. Event Publish
//		receiptEvent = (BatchReceiveEvent)this.eventPublisher.publishEvent(receiptEvent);
//		return receiptEvent.getEventResultSet();
//	}
}
