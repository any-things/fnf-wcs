package operato.fnf.wcs.service.batch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.entity.BatchReceipt;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.event.EventPublisher;
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
		
		Integer autoReceiveDays = 15;
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
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public BatchReceipt prepare(String areaCd, String stageCd, String comCd, String workDate, String jobType) {
		BatchReceipt receipt = this.serviceDispatcher.getReceiveBatchService()
				.readyToReceive(Domain.currentDomainId(), areaCd, stageCd, comCd, workDate, jobType);
		return receipt;
	}
}
