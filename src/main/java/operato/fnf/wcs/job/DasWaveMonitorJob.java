package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.service.batch.DasCloseBatchService;
import operato.fnf.wcs.service.batch.DasStartBatchService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.sys.ConfigConstants;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
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
public class DasWaveMonitorJob extends AbstractQueryService {

	/**
	 * 이중화 서버의 양쪽에서 모두 처리되지 않게 한 쪽 서버에서 실행되도록 설정으로 처리하기 위함
	 * application.properties 설정 - job.scheduler.enable=true/false 설정 필요 (이중화 서버 한 대는 true, 나머지 서버는 false로 설정, 한 대만 운영시 true로 설정)
	 */
	@Autowired
	private Environment env;
	/**
	 * Domain Controller
	 */
	@Autowired
	private DomainController domainCtrl;
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
	 * 매 1분 마다 실행되어 작업 배치 상태 모니터링 후 변경된 Wave에 대해서 JobBatch에 반영
	 */
	@Transactional
	@Scheduled(cron="0 0/1 * * * *")
	public void monitorWave() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			// 2-1. 시작된 Wave 리스트를 조회한 후 존재한다면 처리
			this.processStartedWaveList(domain.getId());
			
			// 2-2. 종료된 Wave 리스트를 조회한 후 존재한다면 처리
			this.processFinishedWaveList(domain.getId());
		}
	}

	/**
	 * 서버의 Job Scheduler가 활성화 되었는지 여부
	 * 
	 * @return
	 */
	private boolean isJobEnabeld() {
		return ValueUtil.toBoolean(this.env.getProperty(ConfigConstants.JOB_SCHEDULER_ENABLED, LogisConstants.FALSE_STRING)); 
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
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
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
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("status", "C");
		condition.addFilter("prcsYn", LogisConstants.Y_CAP_STRING);
		condition.addFilter("prcsDatetime", LogisConstants.IS_BLANK, LogisConstants.EMPTY_STRING);
		return this.queryManager.selectList(WcsMheHr.class, condition);
	}
	
}
