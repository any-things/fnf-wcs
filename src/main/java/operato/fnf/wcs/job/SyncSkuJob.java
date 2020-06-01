package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.event.master.SkuReceiptEvent;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.ThreadUtil;

/**
 * 상품 수신 잡 
 * 
 * @author shortstop
 */
@Component
public class SyncSkuJob extends AbstractFnFJob {

	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;
	/**
	 * 현재 할당 작업이 진행 중인지 여부
	 */
	private boolean syncJobRunning = false;
	
	@Transactional
	@Scheduled(cron="0 0 0/1 * * *")
	public void syncJob() {
		// 스케줄링 활성화 여부 && 이전 작업이 진행 중인 여부 체크
		if(!this.isJobEnabeld() || this.syncJobRunning) {
			return;
		}

		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		// 3. 모든 도메인에 대해서 ...
		for(Domain domain : domainList) {
			// 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				SkuReceiptEvent event1 = new SkuReceiptEvent(domain.getId(), SkuReceiptEvent.RECEIVE_TYPE_SKU, "FnF", SysEvent.EVENT_STEP_BEFORE);
				event1 = (SkuReceiptEvent)this.eventPublisher.publishEvent(event1);
				
				ThreadUtil.sleep(1000);
				
				SkuReceiptEvent event2 = new SkuReceiptEvent(domain.getId(), SkuReceiptEvent.RECEIVE_TYPE_SKU, "FnF", SysEvent.EVENT_STEP_AFTER);
				event2.setPlanCount(event1.getPlanCount());
				event2.setLastReceivedAt(event1.getLastReceivedAt());
				event2 = (SkuReceiptEvent)this.eventPublisher.publishEvent(event2);
				
			} catch(Exception e) {
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_SKY_SYNC_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
		
		// 4. 작업 중 플래그 리셋
		this.syncJobRunning = false;
	}

}
