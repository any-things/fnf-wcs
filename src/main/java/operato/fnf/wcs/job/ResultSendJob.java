package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.send.DasBoxSendService;
import operato.fnf.wcs.service.send.PickingResultSendService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DAS / DPS 박스 처리 실적 전송 Job
 * 
 * @author shortstop
 */
@Component
public class ResultSendJob extends AbstractFnFJob {
	
	/**
	 * DAS 박스 전송 서비스
	 */
	@Autowired
	private DasBoxSendService dasBoxSendSvc;
	/**
	 * 피킹 실적 전송 서비스
	 */
	@Autowired
	private PickingResultSendService pickResultSendSvc;
	
	/**
	 * 매 100초 마다  
	 */
	@Transactional
	@Scheduled(initialDelay=100000, fixedDelay=60000)
	public void summaryJob() {
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
				// 진행 중인 배치 리스트 조회
				List<JobBatch> batches = this.searchRunningBatches(domain.getId());
				
				if(ValueUtil.isNotEmpty(batches)) {
					for(JobBatch batch : batches) {
						// 1. DAS에서 올려 준 피킹 실적을 WMS에 피킹 실적 전송 - 별도 트랜잭션
						this.sendPickResults(domain, batch);
						// 2. RFID에 박스 실적 전송 - 박스 실적 전송 & 박스 취소까지 처리 - 별도 트랜잭션
						this.sendBoxResults(domain, batch);
					}
				}
			} catch (Exception e) {
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "RESULT_SEND_JOB_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
	}
	
	/**
	 * 시작된 Wave 리스트를 조회
	 * 
	 * @param domainId
	 * @return
	 */
	private List<JobBatch> searchRunningBatches(Long domainId) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.addFilter("jobType", LogisConstants.IN, ValueUtil.toList(LogisConstants.JOB_TYPE_DAS, LogisConstants.JOB_TYPE_DPS));
		condition.addOrder("jobType", false);
		condition.addOrder("instructedAt", true);
		return this.queryManager.selectList(JobBatch.class, condition);
	}
	
	/**
	 * 박스 실적을 RFID로 전송
	 * 
	 * @param domain
	 * @param batch
	 * @return
	 */
	private void sendBoxResults(Domain domain, JobBatch batch) {		
		if(LogisConstants.isDasJobType(batch.getJobType())) {
			this.dasBoxSendSvc.sendBoxResults(domain, batch);
		}
	}
	
	/**
	 * 피킹 실적을 WMS로 전송
	 * 
	 * @param domain
	 * @param batch
	 * @return
	 */
	private void sendPickResults(Domain domain, JobBatch batch) {
		this.pickResultSendSvc.sendPickingResults(domain, batch);
	}

}
