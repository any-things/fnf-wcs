package operato.fnf.wcs.job;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.send.SmsInspSendService;
import operato.logis.sms.SmsConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.ValueUtil;

/**
 * SMS 반품 검수 박스 정보 소터 전송 Job
 * 
 * 
 */
@Component
public class SmsInspResultSendJob extends AbstractFnFJob {
	
	/**
	 * 반품 검수완료 Box 전송 서비스
	 */
	@Autowired
	private SmsInspSendService smsInspSendSvc;
	
	/**
	 * 매 100초 마다  
	 */
	@Transactional
	@Scheduled(initialDelay=100000, fixedDelay=60000)
	public void inspBoxJob() {
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
						// 1. 검수완료된 or 검수예정정보 반품 박스정보를 Sorter에 전송한다.
						this.sendInspBoxResults(domain, batch);
						// 2. Sorter 실적을 가지고 검수실적을 올려준다.
						// 소터 실적이 계속 변경되기 떄문에...
//						this.sendInspBoxScanResultToWms(batch);
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
		condition.addFilter("jobType", LogisConstants.IN, ValueUtil.toList(SmsConstants.JOB_TYPE_SRTN));
		condition.addOrder("jobType", false);
		condition.addOrder("instructedAt", true);
		List<JobBatch> jobBatches = this.queryManager.selectList(JobBatch.class, condition);
		List<String> mainBatchIds = AnyValueUtil.filterValueListBy(jobBatches, "batchGroupId");
		
		if(ValueUtil.isNotEmpty(mainBatchIds)) {
			Query query = AnyOrmUtil.newConditionForExecution(domainId);
			query.addFilter("status", LogisConstants.NOT_EQUAL, JobBatch.STATUS_END);
			query.addFilter("batchGroupId", LogisConstants.IN, mainBatchIds);
			query.addOrder("jobType", false);
			query.addOrder("instructedAt", true);
			return this.queryManager.selectList(JobBatch.class, query);
		} else {
			return new ArrayList<JobBatch>();
		}
	}
	
	/**
	 * 반품 검수완료 Box를 Sorter로 전송 서비스
	 */
	private void sendInspBoxResults(Domain domain, JobBatch batch) {
		if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SRTN, batch.getJobType())) {
			this.smsInspSendSvc.sendInspBoxResults(domain, batch);
		}
	}
	
	/**
	 * 소터 실적으로 검수정보가 없는 정보들은 매장 반품예정 검수 스캔결과(WMT_UIF_IMP_MHE_RTN_SCAN) 테이블로 전송
	 */
	/*private void sendInspBoxScanResultToWms(JobBatch batch) {
		if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SRTN, batch.getJobType())) {
			this.smsInspSendSvc.sendInspBoxScanResultToWms(batch);
		}
	}*/

}
