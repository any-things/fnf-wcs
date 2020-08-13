package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.rfid.DasResultIfYnReset;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.BeanUtil;

@Component
public class DasResultIfYnResetJob extends AbstractFnFJob {
	
	@Transactional
	@Scheduled(initialDelay=31000, fixedDelay=120000)
	public void resetJob() {
		// 스케줄링 활성화 여부 체크
		if(!this.isJobEnabeld()) {
			return;
		}
		
		logger.error("DasResultIfYnResetJob is RUNNING~~");
		
		// 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			DomainContext.setCurrentDomain(domain);
			
			try {
				BeanUtil.get(DasResultIfYnReset.class).dasResultIfYnReset(new HashMap<>());
			} catch (Exception e) {
				logger.error("DasResultIfYnResetJob error~~", e);
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "DasResultIfYnResetJob_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
			} finally {
				// 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
	}
}
