package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.service.batch.DasCloseBatchService;
import operato.fnf.wcs.service.batch.DasStartBatchService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DAS 용 모니터링 잡
 * 	- 매 1분 마다 MHE_HR 테이블을 모니터링 하다가 Wave의 상태가 변경되는 경우 JobBatch에 반영
 *  - MHE_HR 테이블의 status가 'B'이고 prcsYn가 'N'인 경우 JobBatch 상태를 RUN으로 변경하고 WMS 동일 I/F 테이블에 전송
 *  - MHE_HR 테이블의 status가 'C'이고 prcsYn가 'N'인 경우 JobBatch 상태를 END로 변경하고 WMS 동일 I/F 테이블에 전송
 * 
 * @author shortstop
 */
@Component
public class DasWaveMonitorJob extends AbstractFnFJob {
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
	 * 매 3분 마다 실행되어 작업 배치 상태 모니터링 후 변경된 Wave에 대해서 JobBatch에 반영
	 */
	@Transactional
	@Scheduled(cron="0 0/3 * * * *")
	public void monitorWave() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			// 2.1 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 2.2 시작된 Wave 리스트를 조회한 후 존재한다면 처리
				this.processStartedWaveList(domain.getId());
			
				// 2.3 종료된 Wave 리스트를 조회한 후 존재한다면 처리
				this.processFinishedWaveList(domain.getId());
				
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
	 * @param domainId
	 */
	public void processStartedWaveList(Long domainId) {
		List<WcsMheHr> waveList = this.searchStartedWaveList(domainId);
		
		if(ValueUtil.isNotEmpty(waveList)) {
			for(WcsMheHr wave : waveList) {
				this.startBatchSvc.startBatch(domainId, wave);
			}
		}
	}
	
	/**
	 * 종료된 Wave 리스트를 조회한 후 JobBatch에 반영
	 * 
	 * @param domainId
	 */
	public void processFinishedWaveList(Long domainId) {
		List<WcsMheHr> waveList = this.searchFinishedWaveList(domainId);
		
		if(ValueUtil.isNotEmpty(waveList)) {
			for(WcsMheHr wave : waveList) {
				this.closeBatchSvc.closeBatch(domainId, wave);
			}
		}
	}
	
	/**
	 * 시작된 Wave 리스트를 조회
	 * 
	 * @param domainId
	 * @return
	 */
	private List<WcsMheHr> searchStartedWaveList(Long domainId) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("status", "B");
		condition.addFilter("prcsYn", LogisConstants.N_CAP_STRING);
		return this.queryManager.selectList(WcsMheHr.class, condition);
	}
	
	/**
	 * 완료된 Wave 리스트를 조회
	 * 
	 * @param domainId
	 * @return
	 */
	private List<WcsMheHr> searchFinishedWaveList(Long domainId) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("status", "C");
		condition.addFilter("endDatetime", LogisConstants.IS_NULL, LogisConstants.EMPTY_STRING);
		return this.queryManager.selectList(WcsMheHr.class, condition);
	}
	
}
