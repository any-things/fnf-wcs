package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.assign.DpsJobAssignService;
import operato.fnf.wcs.service.send.DpsBoxSendService;
import xyz.anythings.base.LogisConstants;
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
	 * 박스 실적 전송 서비스
	 */
	@Autowired
	private DpsBoxSendService dpsBoxSendService;
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
		// 스케줄링 활성화 여부 && 이전 작업이 진행 중인 여부 체크
		if(!this.isJobEnabeld() || this.assignJobRunning) {
			return;
		}
		
		// 1. 작업 중 플래그 Up
		this.assignJobRunning = true;
		
		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		// 3. 모든 도메인에 대해서 ...
		for(Domain domain : domainList) {
			// 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 3.1 진행 중인 배치 리스트 조회
				List<JobBatch> batchList = this.searchRunningBatchList(domain);
				
				if(ValueUtil.isNotEmpty(batchList)) {
					for(JobBatch batch : batchList) {
						// 3.2 현재 진행 중인 작업 배치 별 DPS 완료 박스 실적 전송
						this.sendDpsBoxResults(domain, batch);
						
						// 3.3 현재 진행 중인 작업 배치 별 작업 할당 처리
						this.assignDomainJobs(domain, batch);
					}
				}
			} catch(Exception e) {
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_DPS_ASSIGN_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
		
		// 4. 작업 중 플래그 리셋
		this.assignJobRunning = false;
	}
	
	/**
	 * 현재 진행 중인 DPS 배치 리스트 조회
	 * 
	 * @param domain
	 * @return
	 */
	private List<JobBatch> searchRunningBatchList(Domain domain) {
		Query condition = AnyOrmUtil.newConditionForExecution(domain.getId());
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.addFilter("jobType", LogisConstants.JOB_TYPE_DPS);
		condition.addOrder("jobDate", false);
		return this.queryManager.selectList(JobBatch.class, condition);
	}
	
	/**
	 * 도메인 별 모든 진행 중인 작업 배치에 대한 박스 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendDpsBoxResults(Domain domain, JobBatch batch) {
		this.dpsBoxSendService.sendBoxResults(domain, batch);
	}
	
	/**
	 * 도메인 별 모든 진행 중인 작업 배치에 대한 작업 할당 처리
	 * 
	 * @param domain
	 * @param batch
	 */
	private void assignDomainJobs(Domain domain, JobBatch batch) {
		this.dpsJobAssignService.assignBatchJobs(domain, batch);
	}

}
