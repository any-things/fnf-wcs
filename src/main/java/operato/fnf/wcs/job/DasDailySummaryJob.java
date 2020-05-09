package operato.fnf.wcs.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.batch.DasJobSummaryService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.CurrentDbTime;
import xyz.anythings.base.util.LogisBaseUtil;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 일별 실적 서머리 잡
 * 	- 매일 오전 5시에 완료된 작업 배치의 작업 실적을 DailyProdSummary 테이블에 반영
 * 
 * @author shortstop
 */
@Component
public class DasDailySummaryJob extends AbstractFnFJob {

	/**
	 * 작업 서머리를 위한 서비스
	 */
	@Autowired
	private DasJobSummaryService jobSummarySvc;
	
	/**
	 * 매일 오전 5시에 실행되어 일별 서머리 처리 
	 */
	@Transactional
	@Scheduled(cron="0 0 5 * * *")
	public void summaryJob() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. Database로 부터 현재 시간, 분 정보 추출
		CurrentDbTime currentTime = LogisBaseUtil.currentDbDateTime();
		String prevDate = DateUtil.addDateToStr(currentTime.getCurrentTime(), - 1);
		
		// 3. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			// 3.1 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 3.2 전날 완료된 배치 리스트 조회
				List<JobBatch> batches = this.searchRunningOrClosedBatches(domain.getId(), prevDate);
				
				if(ValueUtil.isNotEmpty(batches)) {
					for(JobBatch batch : batches) {
						this.jobSummarySvc.summaryDailyBatchJobs(batch, prevDate);
					}
				}
			} catch (Exception e) {
				// 3.4 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_DAILY_SUMMARY_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 3.5 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
	}
	
	/**
	 * 진행 중인 혹은 완료된 배치 리스트를 조회
	 * 
	 * @param domainId
	 * @param date
	 * @return
	 */
	private List<JobBatch> searchRunningOrClosedBatches(Long domainId, String date) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("jobDate", date);
		condition.addFilter("status", LogisConstants.IN, ValueUtil.toList(JobBatch.STATUS_RUNNING, JobBatch.STATUS_END));
		condition.addFilter("jobType", LogisConstants.JOB_TYPE_DAS);
		return this.queryManager.selectList(JobBatch.class, condition);
	}

}
