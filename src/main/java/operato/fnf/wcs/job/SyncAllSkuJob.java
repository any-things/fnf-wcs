package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WmsMheItemBarcode;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.rest.SettingController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.SettingUtil;
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
	@Scheduled(cron="0 0/1 * * * *")
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
						// 4.4 상품 수신 처리 (SKU 테이블) 
						this.syncSkuList1(domain.getId(), skuList);
						// 4.5 상품 수신 처리 (코텍 사용 테이블)
						this.syncSkuList2(domain.getId(), skuList);
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
	private void syncSkuList1(Long domainId, List<WmsMheItemBarcode> fromSkuList) {
		
		Map<String, Object> condition = ValueUtil.newMap("domainId,comCd", domainId, FnFConstants.FNF_COM_CD);
		
		for(WmsMheItemBarcode fromSku : fromSkuList) {
			condition.put("skuCd", fromSku.getItemCd());
			SKU toSku = this.queryManager.selectByCondition(SKU.class, condition);
			
			if(toSku == null) {
				toSku = new SKU();
				toSku.setDomainId(domainId);
				toSku.setSkuNm(LogisConstants.SPACE);
			}
			
			toSku.setComCd(FnFConstants.FNF_COM_CD);
			toSku.setSkuCd(fromSku.getItemCd());
			toSku.setSkuBarcd(fromSku.getBarcode2());
			toSku.setSkuBarcd2(fromSku.getBarcode());
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
	
	/**
	 * I상품 수신 처리
	 * 
	 * @param domainId
	 * @param fromSkuList
	 */
	private void syncSkuList2(Long domainId, List<WmsMheItemBarcode> fromSkuList) {
		
		String skuMasterTable = this.getItemTable(domainId);
		String selectSql = this.getSkuSelectSql(skuMasterTable);
		String insertSql = this.getSkuInsertSql(skuMasterTable);
		String updateSql = this.getSkuUpdateSql(skuMasterTable);
		
		for(WmsMheItemBarcode fromSku : fromSkuList) {
			Map<String, Object> valueMap = ValueUtil.newMap("itemCd,brandCd,barcode,barcode2,itemSeason,itemStyle,itemColor,itemSize,itemGcd,itemGcdNm,floorCd,updDatetime", fromSku.getItemCd(), fromSku.getBrand(), fromSku.getBarcode(), fromSku.getBarcode2(), fromSku.getItemSeason(), fromSku.getItemStyle(), fromSku.getItemColor(), fromSku.getItemSize(), fromSku.getItemGcd(), fromSku.getItemGcdNm(), fromSku.getFloorCd(), fromSku.getUpdDatetime());
			int count = this.queryManager.selectSizeBySql(selectSql, ValueUtil.newMap("itemCd", fromSku.getItemCd()));
			
			if(count == 0) {
				this.queryManager.executeBySql(insertSql, valueMap);
			} else {
				this.queryManager.executeBySql(updateSql, valueMap);
			}
		}
	}
	
	/**
	 * 상품 조회 SQL
	 * 
	 * @param skuMasterTable
	 * @return
	 */
	private String getSkuSelectSql(String skuMasterTable) {
		return "select item_cd from " + skuMasterTable + " where item_cd = :itemCd";
	}
	
	/**
	 * 상품 추가 SQL
	 * 
	 * @param skuMasterTable
	 * @return
	 */
	private String getSkuInsertSql(String skuMasterTable) {
		return "insert into " + skuMasterTable + " (brand, item_cd, barcode, barcode2, item_season, item_style, item_color, item_size, item_gcd, item_gcd_nm, floor_cd, upd_datetime) values(:brandCd, :itemCd, :barcode, :barcode2, :itemSeason, :itemStyle, :itemColor, :itemSize, :itemGcd, :itemGcdNm, :floorCd, :updDatetime)";
	}
	
	/**
	 * 상품 업데이트 SQL
	 * 
	 * @param skuMasterTable
	 * @return
	 */
	private String getSkuUpdateSql(String skuMasterTable) {
		return "update " + skuMasterTable + " set brand = :brandCd, barcode = :barcode, barcode2 = :barcode2, item_season = :itemSeason, item_style = :itemStyle, item_color = :itemColor, item_size = :itemSize, item_gcd = :itemGcd, item_gcd_nm = :itemGcdNm, floor_cd = :floorCd, upd_datetime = :updDatetime where item_cd = :itemCd";
	}

	/**
	 * MHE_ITEM_BARCODE 테이블
	 * 
	 * @return
	 */
	private String getItemTable(Long domainId) {
		return SettingUtil.getValue(domainId, "fnf.item_barcode.table.name", "mhe_item_barcode_ope2");
	}

}
