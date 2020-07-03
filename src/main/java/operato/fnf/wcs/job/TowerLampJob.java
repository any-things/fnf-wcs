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
import xyz.elidom.orm.OrmConstants;
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
	 * 경광등 ON 상태 - 깜빡임
	 */
	private String lampOnStatus = "ON";
	/**
	 * 경광등 OFF 상태  
	 */
	private String lampOffStatus = "OFF";
	
	/**
	 * 경광등 ON 점등 
	 */
	private String lampBlinkStatus = "BLINK";
	
	/**
	 * 매 3분 마다 실행되어 DPS 재고 기반으로 경광등 표시기 동기화
	 */
	@Transactional
	//@Scheduled(cron="15 0/1 * * * *")
	@Scheduled(initialDelay=185000, fixedDelay=60000)
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
				
				// 진행 중인 배치가 있으면 
				if(ValueUtil.isNotEmpty(batches)) {
					for(JobBatch batch : batches) {
						// 3.1 경광등 상태 리스트 조회
						List<TowerLamp> towerLampList = this.searchTowerLampAndStatus(batch);
						// 3.2 경광등 상태 업데이트 
						this.requestTowerLampLight(domain, towerLampList);
					}
				} else {
					// 진행 중인 배치가 없으면
					// 전체 소등 
					Query condition = AnyOrmUtil.newConditionForExecution(domain.getId());
					List<TowerLamp> towerLampList = this.queryManager.selectList(TowerLamp.class, condition);
					
					if(ValueUtil.isNotEmpty(towerLampList)) {
						for(TowerLamp lamp : towerLampList) {
							lamp.setLampR(this.lampOffStatus);
							lamp.setLampG(this.lampOffStatus);
							lamp.setLampA(this.lampOffStatus);
							
							lamp.setCudFlag_(OrmConstants.CUD_FLAG_UPDATE);
						}
						
						// 경광등 상태 업데이트 
						this.requestTowerLampLight(domain, towerLampList);
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
		
		Long domainId = batch.getDomainId();
		String sql = this.fnfDpsQueryStore.getTowerLampStatus();
		Map<String, Object> params = ValueUtil.newMap("domainId,equipType,equipCd", domainId, batch.getEquipType(), batch.getEquipCd());
		List<LampCellStatus> lampStatusList = this.queryManager.selectListBySql(sql, params, LampCellStatus.class, 0, 0);
		
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		List<TowerLamp> lampList = this.queryManager.selectList(TowerLamp.class, condition);
		
		for(TowerLamp lamp : lampList) {
			LampCellStatus lampStatus = this.findLampCellStatus(lampStatusList, lamp);
			
			// 존에 하나라도 빈 셀이 있는 경우에 전체 점등 
			if(lampStatus.getEmptyCellCnt() > 0 ) {
				lamp.setLampR(this.lampOffStatus);
				lamp.setLampG(this.lampBlinkStatus);
				lamp.setLampA(this.lampOffStatus);
			} else {
				lamp.setLampR(this.lampOffStatus);
				lamp.setLampG(this.lampOffStatus);
				lamp.setLampA(this.lampOffStatus);
			}
			
			lamp.setCudFlag_(OrmConstants.CUD_FLAG_UPDATE);

			/* 비율별 경광등 켜기 백업 
			int fillCellCnt = ValueUtil.toInteger(lampStatus.getFillCellCnt(), 0);
			int totalCellCnt = ValueUtil.toInteger(lampStatus.getTotCellCnt(), 0);
			
			float fillCellPercent = (totalCellCnt == 0 || fillCellCnt == 0) ? 0 : ValueUtil.toFloat(fillCellCnt) / ValueUtil.toFloat(totalCellCnt) * 100.0f;
			this.setLampOnSetting(domainId, lamp, fillCellPercent);
			*/
		}
				
		return lampList;
	}
	
	/**
	 * 경광등 상태 추출
	 * 
	 * @param lampStatusList
	 * @param lamp
	 * @return
	 */
	private LampCellStatus findLampCellStatus(List<LampCellStatus> lampStatusList, TowerLamp lamp) {
		
		for(LampCellStatus lampStatus : lampStatusList) {
			if(ValueUtil.isEqualIgnoreCase(lamp.getTowerLampCd(), lampStatus.getTowerLampCd())) {
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
	private void requestTowerLampLight(Domain domain, List<TowerLamp> towerList) {
		this.towerLampCtrl.multipleUpdate(towerList);
	}
	
	/**
	 * 경광등 표시기 On 설정
	 * 
	 * @param domainId
	 * @param lamp
	 * @param fillCellPercent
	 */
	/*private void setLampOnSetting(Long domainId, TowerLamp lamp, float fillCellPercent) {
		
		float healthRate = this.getStockHealthRate(domainId);
		float normalRate = this.getStockNormalRate(domainId);
		
		if(fillCellPercent >= healthRate) {
			lamp.setLampG(this.lampOnStatus);
			lamp.setLampR(this.lampOffStatus);
			lamp.setLampA(this.lampOffStatus);
			
		} else if(fillCellPercent >= normalRate && fillCellPercent < healthRate) {
			lamp.setLampG(this.lampOffStatus);
			lamp.setLampR(this.lampOffStatus);
			lamp.setLampA(this.lampOnStatus);
			
		} else {
			lamp.setLampG(this.lampOffStatus);
			lamp.setLampR(this.lampOnStatus);
			lamp.setLampA(this.lampOffStatus);
		}
		
		lamp.setCudFlag_(OrmConstants.CUD_FLAG_UPDATE);
	}*/
	
	/**
	 * Health 등급 재고 보충율
	 * 
	 * @param domainId
	 * @return
	 */
	/*private float getStockHealthRate(Long domainId) {
		String healthRate = SettingUtil.getValue(domainId, "fnf.stock.health.rate", "80");
		return ValueUtil.toFloat(healthRate);
	}*/

	/**
	 * Normal 등급 재고 보충율
	 * 
	 * @param domainId
	 * @return
	 */
	/*private float getStockNormalRate(Long domainId) {
		String healthRate = SettingUtil.getValue(domainId, "fnf.stock.normal.rate", "40");
		return ValueUtil.toFloat(healthRate);
	}*/
	
}
