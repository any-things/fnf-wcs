package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.summary.SendPopularProductToWms;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;

public class SendWcsPopularProductJob extends AbstractFnFJob {
	
	@Autowired
	private SendPopularProductToWms sendPopularProductToWms;
	
	@Transactional
	@Scheduled(cron="0 0 23 * * 1-5")
	public void sendJob() {
		// 스케줄링 활성화 여부 체크
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			// 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				sendPopularProductToWms.sendPopularProductToWms(new HashMap<>());
			} catch (Exception e) {
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "SendWcsPopularProduct_JOB_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
			} finally {
				// 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
	}
}
