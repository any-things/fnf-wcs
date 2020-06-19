package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WmsMheItemBarcode;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.rest.SettingController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.ValueUtil;

/**
 * 모든 상품 수신 잡 
 * 
 * @author shortstop
 */
@Component
public class SyncAllSkuJob extends AbstractFnFJob {
	/**
	 * 현재 할당 작업이 진행 중인지 여부
	 */
	private boolean syncJobRunning = false;
	/**
	 * 상품 수신 페이지 
	 */
	private int skuCurrentPage = 0;
	/**
	 * 상품 수신 시 한 번에 받을 개수
	 */
	private int skuPageLimit = 1000;
	/**
	 * 현재 수신 페이지 설정 명
	 */
	private String currentPageSettingName = "fnf.sku.receive.current.page";
	/**
	 * 고객사 코드
	 */
	private String comCd = "FnF";
	/**
	 * 상품 소스 조회 쿼리 
	 */
	private String skuSearchSql = "select * from mhe_item_barcode order by item_cd asc";
	/**
	 * 무 조건 
	 */
	private Map<String, Object> noCondition = new HashMap<String, Object>(1);
	/**
	 * 세팅 컨트롤러
	 */
	@Autowired
	private SettingController settingCtrl;
	
	@Transactional
	//@Scheduled(cron="0 0/1 * * * *")
	public void syncJob() {
		// 스케줄링 활성화 여부 && 이전 작업이 진행 중인 여부 체크
		if(!this.isJobEnabeld() || this.syncJobRunning) {
			return;
		}

		// 2. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		// 3. 모든 도메인에 대해서 ...
		for(Domain domain : domainList) {
			// 현재 도메인 설정
			DomainContext.setCurrentDomain(domain);
			
			try {
				// 4.1 상품 수신 현재 페이지
				Setting setting = this.settingCtrl.findByName(domain.getId(), this.currentPageSettingName);
				String currentPage = ValueUtil.isEmpty(setting.getValue()) ? "0" : setting.getValue();
				
				// 4.2 설정 상태가 end가 아니면 상품 수신 처리 
				if(!ValueUtil.isEqualIgnoreCase(currentPage, "end")) {
					// 4.3 현재 수신 페이지 정보 업데이트 
					this.skuCurrentPage = ValueUtil.toInteger(currentPage) + 1;
					IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheItemBarcode.class);
					List<WmsMheItemBarcode> skuList = wmsQueryMgr.selectListBySql(this.skuSearchSql, this.noCondition, WmsMheItemBarcode.class, this.skuCurrentPage, this.skuPageLimit);
					boolean noMoreSku = skuList.isEmpty();
					
					if(!noMoreSku) {
						// 4.4 상품 수신 처리 
						this.syncSkuList(domain.getId(), skuList);
					}
					
					// 4.5 상품 수신 현재 페이지 업데이트 ...
					setting.setValue(noMoreSku ? "end" : "" + this.skuCurrentPage);
					this.settingCtrl.update(setting.getId(), setting);
				}
			} catch(Exception e) {
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_SKU_SYNC_ALL_ERROR", e, null, true, true);
				this.eventPublisher.publishEvent(errorEvent);
				
			} finally {
				// 스레드 로컬 변수에서 currentDomain 리셋 
				DomainContext.unsetAll();
			}
		}
		
		// 5. 작업 중 플래그 리셋
		this.syncJobRunning = false;
	}

	/**
	 * 상품 수신 처리
	 * 
	 * @param domainId
	 * @param fromSkuList
	 */
	private void syncSkuList(Long domainId, List<WmsMheItemBarcode> fromSkuList) {
		
		Map<String, Object> condition = ValueUtil.newMap("domainId,comCd", domainId, this.comCd);
		
		for(WmsMheItemBarcode fromSku : fromSkuList) {
			condition.put("skuCd", fromSku.getItemCd());
			SKU toSku = this.queryManager.selectByCondition(SKU.class, condition);
			
			if(toSku == null) {
				toSku = new SKU();
				toSku.setDomainId(domainId);
				toSku.setSkuNm(LogisConstants.SPACE);
			}
			
			toSku.setComCd(this.comCd);
			toSku.setSkuCd(fromSku.getItemCd());
			toSku.setSkuBarcd(fromSku.getBarcode2());
			toSku.setBrandCd(fromSku.getBrand());
			toSku.setSeasonCd(fromSku.getItemSeason());
			toSku.setStyleCd(fromSku.getItemStyle());
			toSku.setColorCd(fromSku.getItemColor());
			toSku.setSizeCd(fromSku.getItemSize());
			toSku.setSkuClass(fromSku.getFloorCd());
			toSku.setSkuType(fromSku.getItemGcd());
			toSku.setSkuDesc(fromSku.getItemGcdNm());
			this.queryManager.upsert(toSku);
		}
	}

}
