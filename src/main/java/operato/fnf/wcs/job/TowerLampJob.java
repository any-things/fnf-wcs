package operato.fnf.wcs.job;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.TowerLamp;
import operato.fnf.wcs.query.store.FnFDpsQueryStore;
import operato.fnf.wcs.rest.TowerLampController;
import operato.fnf.wcs.service.model.LampCellStatus;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.ValueUtil;

/**
 * 경광등 표시 작업
 * 
 * @author shortstop
 */
@Component
public class TowerLampJob extends AbstractFnFJob {

	/**
	 * 현재 작업이 진행 중인지 여부
	 */
	private boolean jobRunning = false;
	/**
	 * DPS Query Store
	 */
	@Autowired
	private FnFDpsQueryStore fnfDpsQueryStore;
	/**
	 * 경광등 컨트롤러
	 */
	@Autowired
	private TowerLampController towerLampCtrl;
	/**
	 * 경광등 ON 상태
	 */
	private String lampOnStatus = "ON";
	
	/**
	 * 매 2분 마다 실행되어 DPS 재고 기반으로 경광등 표시기 동기화
	 */
	@Transactional
	@Scheduled(cron="15 0/1 * * * *")
	public void syncTowerLamp() {
		// 스케줄링 활성화 여부 && 이전 작업이 진행 중인 여부 체크
		if(!this.isJobEnabeld() || this.jobRunning) {
			return;
		}
		
		// 1. 작업 중 플래그 Up
		this.jobRunning = true;
		
		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		// 3. 모든 도메인에 대해서 ...
		for(Domain domain : domainList) {
			// 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 진행 중인 배치 리스트 조회
				List<JobBatch> batches = this.searchRunningBatches(domain.getId());
				
				if(ValueUtil.isNotEmpty(batches)) {
					for(JobBatch batch : batches) {
						// 3.1 경광등 상태 리스트 조회
						List<TowerLamp> towerLampList = this.searchTowerLampAndStatus(batch);
						
						// 3.2 경광등 상태 업데이트
						this.requestTowerLampLight(domain, batch, towerLampList);
					}
				}
				
			} catch(Exception e) {
				// 3.3 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_TOWER_LAMP_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 3.4 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
		
		// 4. 작업 중 플래그 리셋
		this.jobRunning = false;
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
		condition.addFilter("jobType", LogisConstants.JOB_TYPE_DPS);
		condition.addOrder("instructedAt", true);
		return this.queryManager.selectList(JobBatch.class, condition);
	}
	
	/**
	 * 모든 경광등 상태 리스트 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<TowerLamp> searchTowerLampAndStatus(JobBatch batch) {
		String sql = this.fnfDpsQueryStore.getTowerLampStatus();
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd", batch.getDomainId(), batch.getEquipType(), batch.getEquipCd());
		List<LampCellStatus> lampStatusList = this.queryManager.selectListBySql(sql, params, LampCellStatus.class, 0, 0);
		
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		List<TowerLamp> lampList = this.queryManager.selectList(TowerLamp.class, condition);
		
		for(TowerLamp lamp : lampList) {
			LampCellStatus lampStatusY = this.findLampCellStatus(lampStatusList, lamp, LogisConstants.Y_CAP_STRING);
			if(lampStatusY == null) {
				lampStatusY = new LampCellStatus(lamp.getTowerLampCd(), LogisConstants.Y_CAP_STRING, 0); 
				lampStatusList.add(lampStatusY);
			}
			
			LampCellStatus lampStatusN = this.findLampCellStatus(lampStatusList, lamp, LogisConstants.N_CAP_STRING);
			if(lampStatusN == null) {
				lampStatusN = new LampCellStatus(lamp.getTowerLampCd(), LogisConstants.N_CAP_STRING, 0); 
				lampStatusList.add(lampStatusN);
			}
			
			int totalCellCnt = lampStatusY.getCellCnt() + lampStatusN.getCellCnt();
			float emptyCellPercent = ValueUtil.toFloat(lampStatusN.getCellCnt()) / ValueUtil.toFloat(totalCellCnt) * 100.0f;
			
			if(emptyCellPercent >= 80.0f) {
				lamp.setLampG(this.lampOnStatus);
				
			} else if(emptyCellPercent >= 50.0f) {
				lamp.setLampA(this.lampOnStatus);
				
			} else {
				lamp.setLampR(this.lampOnStatus);
			}
		}
				
		return lampList;
	}
	
	/**
	 * 경광등 상태 추출
	 * 
	 * @param lampStatusList
	 * @param lamp
	 * @param emptyYn
	 * @return
	 */
	private LampCellStatus findLampCellStatus(List<LampCellStatus> lampStatusList, TowerLamp lamp, String emptyYn) {
		
		for(LampCellStatus lampStatus : lampStatusList) {
			if(ValueUtil.isEqualIgnoreCase(lamp.getTowerLampCd(), lampStatus.getTowerLampCd()) && ValueUtil.isEqualIgnoreCase(emptyYn, lampStatus.getEmptyYn())) {
				return lampStatus;
			}
		}
		
		return null;
	}
	
	/**
	 * 모든 경광등 상태 리스트 조회
	 * 
	 * @param domain
	 * @param batch
	 * @param towerList
	 */
	private void requestTowerLampLight(Domain domain, JobBatch batch, List<TowerLamp> towerList) {
		this.towerLampCtrl.multipleUpdate(towerList);
	}

}
