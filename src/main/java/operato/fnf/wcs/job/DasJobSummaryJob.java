package operato.fnf.wcs.job;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.entity.WmsMheBox;
import operato.fnf.wcs.service.batch.DasJobSummaryService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DAS 용 10분대 실적 서머리 잡
 * 	- 매 10분 마다 MHE_BOX 테이블을 모니터링하면서 추가된 작업이 있으면 10분 실적 데이터 테이블(Productivity)에 반영
 * 
 * @author shortstop
 */
@Component
public class DasJobSummaryJob extends AbstractFnFJob {
	/**
	 * 작업 서머리를 위한 서비스
	 */
	@Autowired
	private DasJobSummaryService jobSummarySvc;
	
	/**
	 * 매 시각 2, 12, 22, 32, 42, 52분마다 실행되어 실행 데이터에 대한 서머리 처리 
	 */
	@Transactional
	@Scheduled(cron="0 2,12,22,32,42,52 * * * *")
	public void summaryJob() {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. Database로 부터 현재 시간, 분 정보 추출
		Object[] timeInfo = getCurrentHourMinutes();
		String date = ValueUtil.toString(timeInfo[0]);
		int hour = ValueUtil.toInteger(timeInfo[1]);
		int minute = ValueUtil.toInteger(timeInfo[2]);
						
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
						
						// 3.4 WMS 실적 전송
						this.sendBoxResultToWms(batch, date);
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
	
	/**
	 * 박스 실적을 WMS로 전송
	 * 
	 * @param batch
	 * @param date
	 * @return
	 */
	private void sendBoxResultToWms(JobBatch batch, String date) {
		// 1. 전송되지 않은 박스 실적 조회
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("ifYn", LogisConstants.NOT_EQUAL, LogisConstants.Y_CAP_STRING);
		List<WcsMheBox> boxList = this.queryManager.selectList(WcsMheBox.class, condition);
		
		// 2. WMS에 전송
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheBox.class);
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidBoxItem.class);
		Date currentTime = new Date();
		
		for(WcsMheBox box : boxList) {
			// 3. WMS 전송 데이터 생성 
			WmsMheBox wmsBox = ValueUtil.populate(box, new WmsMheBox());
			wmsQueryMgr.insert(wmsBox);
			
			// 4. RFID 전송 데이터 생성 - TODO 데이터 맞는지 체크
			RfidBoxItem rfidBoxItem = new RfidBoxItem();
			rfidBoxItem.setCdWarehouse(box.getWhCd());
			rfidBoxItem.setCdBrand(box.getStrrId());
			rfidBoxItem.setTpMachine("2");
			rfidBoxItem.setDtDelivery(box.getWorkDate());
			rfidBoxItem.setDsBatchNo(box.getWorkUnit());
			rfidBoxItem.setNoBox(box.getBoxNo());
			rfidBoxItem.setIfCdItem(box.getItemCd());
			rfidBoxItem.setYnAssort(LogisConstants.N_CAP_STRING);
			rfidBoxItem.setCdShop(box.getShiptoId());
			// TODO 1 : 출고, 2 : 분류 - 2가 맞는지 체크
			rfidBoxItem.setTpDelivery("2");
			// TODO WcsMheBox에 OutbTcd 매핑 데이터 없음 - 체크 
			rfidBoxItem.setOutbTcd(null);
			// TODO WcsMheBox에 운송장 번호 없음 - 체크 
			rfidBoxItem.setNoWaybill(null);
			rfidBoxItem.setDsShuteno(null);
			rfidBoxItem.setOutbNo(box.getOutbNo());
			// TODO TODO WcsMheBox에 주문 번호 없음 - 체크 
			rfidBoxItem.setRefNo(null);
			rfidBoxItem.setNoWeight(null);
			rfidBoxItem.setQtDelivery(box.getCmptQty());
			rfidBoxItem.setDmBfRecv(DateUtil.dateStr(new Date(), "YYYYMMDDHHMMSS"));
			rfidBoxItem.setYnCancel(LogisConstants.N_CAP_STRING);
			rfidBoxItem.setTpWeight("0");
			rfidBoxItem.setTpSend("0");
			rfidQueryMgr.insert(rfidBoxItem);
			
			// 5. WCS에 전송 플래그 ...
			box.setIfYn(LogisConstants.Y_CAP_STRING);
			box.setIfDatetime(currentTime);
		}
		
		// 6. WMS 전송 플래그 남기기
		AnyOrmUtil.updateBatch(boxList, 100, "ifYn", "ifDatetime");
	}

}
