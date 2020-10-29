package operato.fnf.wcs.job;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.batch.SmsCloseBatchService;
import operato.fnf.wcs.service.send.DasBoxSendService;
import operato.fnf.wcs.service.send.PickingResultSendService;
import operato.fnf.wcs.service.send.SmsInspSendService;
import operato.logis.sms.SmsConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * SMS(SDAS/SDPS) 박스 처리 실적 전송 Job
 * 
 * 
 */
@Component
public class SmsResultSendJob extends AbstractFnFJob {
	
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
	 * SMS Box 전송 서비스
	 */
	@Autowired
	private SmsInspSendService smsInspSendSvc;
	/**
	 * SRTN Box 전송 서비스
	 */
	@Autowired
	private SmsCloseBatchService smsCloseBatchSvc;
	
	private final String JOB_STATUS = "srtn.sending.processing";
	
	/**
	 * 매 20초 마다  
	 */
	@Transactional
	@Scheduled(initialDelay=20000, fixedDelay=20000)
	public void inspBoxJob() {
		// 스케줄링 활성화 여부 체크
		if(!this.isJobEnabeld()) {
			return;
		}
		
		String isRunning = SettingUtil.getValue(1l, JOB_STATUS);
		if ("Y".equals(isRunning)) {
			return;
		}
		
		BeanUtil.get(SmsResultSendJob.class).updateJobStatus("Y");
		
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
						if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SRTN)) {
							this.sendSrtnBoxResults(batch);
						} else {
							// 1. DAS에서 올려 준 피킹 실적을 WMS에 피킹 실적 전송 - 별도 트랜잭션
							this.sendPickResults(domain, batch);
							// 2. SDAS인 경우 RFID에 박스 실적 전송 - 박스 실적 전송 & 박스 취소까지 처리 - 별도 트랜잭션
							// this.sendBoxResults(domain, batch);
							// 3. SDPS인 경우 Job Instances 테이블에 박스 실적 전송
							this.sendSdpsBoxResults(domain, batch);
						}
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
		
		BeanUtil.get(SmsResultSendJob.class).updateJobStatus("N");
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Setting updateJobStatus(String value) {
		Query conds = new Query(0, 1);
		conds.addFilter("name", JOB_STATUS);
		Setting setting = queryManager.selectByCondition(true, Setting.class, conds);
		
		setting.setValue(value);
		queryManager.update(setting);
		
		return setting;
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
		condition.addFilter("jobType", LogisConstants.IN, ValueUtil.toList(SmsConstants.JOB_TYPE_SDAS, SmsConstants.JOB_TYPE_SDPS, SmsConstants.JOB_TYPE_SRTN));
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
	 * 피킹 실적을 WMS로 전송
	 * 
	 * @param domain
	 * @param batch
	 * @return
	 */
	private void sendPickResults(Domain domain, JobBatch batch) {
		// 기존 서비스 그대로 이용
		this.pickResultSendSvc.sendPickingResults(domain, batch);
	}
	
	/**
	 * 박스 실적을 RFID로 전송
	 * 
	 * @param domain
	 * @param batch
	 * @return
	 */
	@SuppressWarnings("unused")
	private void sendBoxResults(Domain domain, JobBatch batch) {
		if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SDAS)) {
			this.dasBoxSendSvc.sendBoxResults(domain, batch);
		}
	}
	
	/**
	 * 박스 실적을 Job Instances 로 전송
	 * 
	 * @param domain
	 * @param batch
	 * @return
	 */
	private void sendSdpsBoxResults(Domain domain, JobBatch batch) {
		if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SDPS)) {
			this.smsInspSendSvc.sendSdpsBoxResults(domain, batch);
		}
	}
	
	/**
	 * 박스 실적을 WMS 로 전송
	 * 
	 * @param domain
	 * @param batch
	 * @return
	 */
	private void sendSrtnBoxResults(JobBatch batch) {
		if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SRTN)) {
			this.smsCloseBatchSvc.sendRtnBoxResultToWms(batch);
		}
	}
}
