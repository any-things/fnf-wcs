package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.assign.DpsJobAssignService;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 할당 잡
 * 	- 매 30초 마다 실행
 * 
 * @author shortstop
 */
@Component
public class DpsAssignJob extends AbstractFnFJob {

	/**
	 * 작업 할당 서비스
	 */
	@Autowired
	private DpsJobAssignService dpsJobAssignService;
	/**
	 * 현재 할당 작업이 진행 중인지 여부
	 */
	private boolean assignJobRunning = false;
	
	/**
	 * 매 1분 마다 실행되어 DPS 재고 기반으로 주문 할당을 처리
	 */
	@Transactional
	@Scheduled(cron="0 0/1 * * * *")
	public void monitorWave() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld() || this.assignJobRunning) {
			return;
		}
		
		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		// 3. 모든 도메인에 대해서 ...
		for(Domain domain : domainList) {
			// 3.1 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 3.2 작업 중 플래그 리셋
				this.assignJobRunning = true;
				
				// 3.3 작업 할당 처리 
				this.assignDomainJobs(domain);
			
			} catch(Exception e) {
				// 3.4. 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_DPS_ASSIGN_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 3.4. 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
				
				// 3.5 작업 중 플래그 리셋
				this.assignJobRunning = false;
			}
		}
	}
	
	/**
	 * 도메인 별 모든 진행 중인 작업 배치에 대한 작업 할당 처리
	 * 
	 * @param domain
	 */
	private void assignDomainJobs(Domain domain) {
		// 1. 현재 진행 중인 배치 조회
		Query condition = AnyOrmUtil.newConditionForExecution(domain.getId());
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.addOrder("jobDate", false);
		List<JobBatch> batchList = this.queryManager.selectList(JobBatch.class, condition);
		
		if(ValueUtil.isNotEmpty(batchList)) {
			// 2. 현재 진행 중인 작업 배치 별 작업 할당 처리
			for(JobBatch batch : batchList) {
				this.dpsJobAssignService.assignBatchJobs(domain, batch);
			}
		}
	}

}
