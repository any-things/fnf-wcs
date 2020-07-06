package operato.fnf.wcs.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.service.batch.DasCloseBatchService;
import operato.fnf.wcs.service.batch.DasRecallBatchService;
import operato.fnf.wcs.service.batch.DasStartBatchService;
import operato.logis.wcs.service.impl.WcsBatchProgressService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * Wave 상태를 모니터링해서 WCS 작업 배치 테이블에 상태 정보를 반영하는 잡
 * 	- 매 3분 마다 MHE_HR 테이블을 모니터링 하다가 Wave의 상태가 변경되는 경우 JobBatch에 반영
 *  - MHE_HR 테이블의 status가 'B'이고 prcsYn가 'N'인 경우 JobBatch 상태를 RUN으로 변경하고 WMS 동일 I/F 테이블에 전송
 *  - MHE_HR 테이블의 status가 'C'이고 prcsYn가 'N'인 경우 JobBatch 상태를 END로 변경하고 WMS 동일 I/F 테이블에 전송
 * 
 * @author shortstop
 */
@Component
public class WaveMonitorJob extends AbstractFnFJob {
	
	/**
	 * 작업 배치 시작을 위한 서비스
	 */
	@Autowired
	private DasStartBatchService startBatchSvc;
	/**
	 * 작업 배치 종료를 위한 서비스
	 */	
	@Autowired
	private DasCloseBatchService closeBatchSvc;
	/**
	 * 작업 배치 생산성 계산 서비스
	 */
	@Autowired
	private WcsBatchProgressService progressSvc;
	
	/**
	 * 매 2분 마다 실행되어 작업 배치 상태 모니터링 후 변경된 Wave에 대해서 JobBatch에 반영
	 */
	@Transactional
	//@Scheduled(cron="45 0/1 * * * *")
	@Scheduled(initialDelay=125000, fixedDelay=60000)
	public void monitorWave() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		WaveMonitorJob monitorJob = BeanUtil.get(WaveMonitorJob.class);
		
		for(Domain domain : domainList) {
			// 2.1 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 회수처리
				BeanUtil.get(DasRecallBatchService.class).dasRecallBatchService(new HashMap<>());
			} catch(Exception e) {
				// 2.4. 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_BATCH_RECALL_PROCESS_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
			}
			
			try {
				// 2.2 종료된 Wave 리스트를 조회한 후 존재한다면 처리
				monitorJob.processFinishedWaveList(domain);
				
				// 2.4 진행 중인 Wave 작업 진행율 업데이트
				monitorJob.updateWaveProgressRate(domain);
				
				// 2.3 시작된 Wave 리스트를 조회한 후 존재한다면 처리
				monitorJob.processStartedWaveList(domain);
				
			} catch(Exception e) {
				// 2.4. 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_BATCH_MONITOR_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
			} finally {
				// 2.5. 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
	}
	
	/**
	 * 시작된 Wave 리스트를 조회한 후 JobBatch에 반영
	 * 
	 * @param domain
	 */
	public void processStartedWaveList(Domain domain) {
		List<WcsMheHr> waveList = this.searchStartedWaveList(domain);
		
		if(ValueUtil.isNotEmpty(waveList)) {
			for(WcsMheHr wave : waveList) {
				try {
					this.startBatchSvc.startBatch(domain.getId(), wave);
				} catch (Exception e) {
					ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_BATCH_START_ERROR", e, null, true, true);
					this.eventPublisher.publishEvent(errorEvent);
				}
			}
		}
	}
	
	/**
	 * 종료된 Wave 리스트를 조회한 후 JobBatch에 반영
	 * 
	 * @param domain
	 */
	public void processFinishedWaveList(Domain domain) {
		List<WcsMheHr> waveList = this.searchFinishedWaveList(domain);
		
		if(ValueUtil.isNotEmpty(waveList)) {
			for(WcsMheHr wave : waveList) {
				try {
					this.closeBatchSvc.closeBatch(domain.getId(), wave);
				} catch (Exception e) {
					ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_BATCH_CLOSE_ERROR", e, null, true, true);
					this.eventPublisher.publishEvent(errorEvent);
				}
			}
		}
	}
	
	/**
	 * 진행 중인 Wave 리스트를 조회한 후 JobBatch에 작업 진행율 반영
	 * 
	 * @param domain
	 */
	public void updateWaveProgressRate(Domain domain) {
		List<JobBatch> batchList = this.searchRunningWaveList(domain);
		
		if(ValueUtil.isNotEmpty(batchList)) {
			for(JobBatch batch : batchList) {
				try {
					// 1. 작업 진행율, 설비 가동 시간, UPH 계산 
					this.progressSvc.updateBatchProductionResult(batch, new Date());
					// 2. 배치 정보 업데이트
					this.queryManager.update(batch, "resultPcs", "resultOrderQty", "resultBoxQty", "progressRate", "uph", "equipRuntime");
				} catch (Exception e) {
					ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_BATCH_UPDATE_PROGRESS_ERROR", e, null, true, true);
					this.eventPublisher.publishEvent(errorEvent);
				}
			}
		}
	}
	
	/**
	 * 시작된 Wave 리스트를 조회
	 * 
	 * @param domain
	 * @return
	 */
	private List<WcsMheHr> searchStartedWaveList(Domain domain) {
		Query condition = new Query();
		condition.addFilter("whCd", FnFConstants.WH_CD_ICF);
		condition.addFilter("bizType", "SHIPBYDAS");
		condition.addFilter("status", "B");
		condition.addFilter("prcsYn", LogisConstants.N_CAP_STRING);
		return this.queryManager.selectList(WcsMheHr.class, condition);
	}
	
	/**
	 * 완료된 Wave 리스트를 조회
	 * 
	 * @param domain
	 * @return
	 */
	private List<WcsMheHr> searchFinishedWaveList(Domain domain) {
		Query condition = new Query();
		condition.addFilter("whCd", FnFConstants.WH_CD_ICF);
		condition.addFilter("status", "C");
		condition.addFilter("bizType", "SHIPBYDAS");
		condition.addFilter("endDatetime", LogisConstants.IS_NULL, LogisConstants.EMPTY_STRING);
		condition.addOrder("bizType", false);
		condition.addOrder("endDatetime", true);
		return this.queryManager.selectList(WcsMheHr.class, condition);
	}
	
	/**
	 * 진행 중인 Wave 리스트를 조회
	 * 
	 * @param domain
	 * @return
	 */
	private List<JobBatch> searchRunningWaveList(Domain domain) {
		Query condition = new Query();
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.addOrder("jobType", false);
		condition.addOrder("instructedAt", true);
		return this.queryManager.selectList(JobBatch.class, condition);
	}

}
