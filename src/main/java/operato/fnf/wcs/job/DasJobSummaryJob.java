package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.batch.DasJobSummaryService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.CurrentDbTime;
import xyz.anythings.base.util.LogisBaseUtil;
import xyz.anythings.sys.ConfigConstants;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.DateUtil;
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
public class DasJobSummaryJob extends AbstractQueryService {

	/**
	 * Event Publisher
	 */
	@Autowired
	private ApplicationEventPublisher eventPublisher;
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
	 * 작업 서머리를 위한 서비스
	 */
	@Autowired
	private DasJobSummaryService jobSummarySvc;
	
	/**
	 * 매 시각 2, 12, 22, 32, 42, 52분마다 실행되어 실행 데이터에 대한 서머리 처리 
	 */
	@Transactional
	//@Scheduled(cron="0 2,12,22,32,42,52 * * * *")
	@Scheduled(cron="0 0/2 * * * *")
	public void summaryJob() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. Database로 부터 현재 시간, 분 정보 추출
		CurrentDbTime currentTime = LogisBaseUtil.currentDbDateTime();
		String date = currentTime.getDateStr();
		int hour = currentTime.getHour();
		int minute = currentTime.getMinute();
		
		// 10분 전으로 설정 
		if(minute < 10) {
			minute = 55;
			// 0시 라면 전날 23시 55분 기준으로 맞춘다.
			if(hour == 0) {
				date = DateUtil.addDateToStr(currentTime.getCurrentTime(), -1);
				hour = 23;
			// 그렇지 않으면 한 시간 전 5분으로 설정
			} else {
				hour -= 1;
			}
		} else {
			minute -= 10;
		}
				
		// 3. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			// 3.1 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 3.2 진행 중인 배치 리스트 조회
				List<JobBatch> batches = this.searchRunningBatches(domain.getId());
				
				if(ValueUtil.isNotEmpty(batches)) {
					for(JobBatch batch : batches) {
						// 3.3 작업 10분당 서머리 계산 처리
						this.processSummaryJob(batch, date, hour, minute);
					}
				}
			} catch (Exception e) {
				// 3.4 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_SUMMARY_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 3.5 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
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
	 * 시작된 Wave 리스트를 조회
	 * 
	 * @param domainId
	 * @return
	 */
	private List<JobBatch> searchRunningBatches(Long domainId) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("status", JobBatch.STATUS_RUNNING);
		condition.addFilter("jobType", LogisConstants.JOB_TYPE_DAS);
		return this.queryManager.selectList(JobBatch.class, condition);
	}

	/**
	 * 10분대 별 실적 서머리 작업 처리
	 * 
	 * @param batch
	 * @param date
	 * @param hour
	 * @param minute
	 */
	private void processSummaryJob(JobBatch batch, String date, int hour, int minute) {
		this.jobSummarySvc.summary10MinuteJobs(batch, date, hour, minute);
	}

}
