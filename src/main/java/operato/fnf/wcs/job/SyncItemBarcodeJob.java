package operato.fnf.wcs.job;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WmsMheItemBarcode;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.rest.SettingController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ThreadUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 상품 바코드 동기화 잡
 * 
 * @author shortstop
 */
@Component
public class SyncItemBarcodeJob extends AbstractFnFJob {

	/**
	 * 현재 할당 작업이 진행 중인지 여부
	 */
	private boolean syncJobRunning = false;
	/**
	 * 상품 수신 시 한 번에 받을 개수
	 */
	private int skuPageLimit = 1000;
	/**
	 * 상품 마지막 수신 시간
	 */
	private String settingLastUpdatedAt = "fnf.sku.latest.received.at";
	/**
	 * 상품 소스 조회 쿼리 
	 */
	private String skuSearchSql = "select * from mhe_item_barcode where upd_datetime > :lastUpdatedAt order by item_cd asc";
	/**
	 * 세팅 컨트롤러
	 */
	@Autowired
	private SettingController settingCtrl;
	
	/**
	 * 10분 주기로 상품 정보 업데이트 시간으로 동기화 처리
	 */
	@Transactional
	@Scheduled(cron="0 5,15,25,35,45,55 * * * *")
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
				// 4.1 상품 수신 마지막 업데이트 시간
				Setting setting = this.settingCtrl.findByName(domain.getId(), this.settingLastUpdatedAt);
				String lastUpdatedAtStr = setting.getValue();
				Date lastUpdatedAt = DateUtil.parse(lastUpdatedAtStr, "yyyy-MM-dd HH:mm:ss.SSS");
				
				// 4.2 설정 상태가 end가 아니면 상품 수신 처리 
				if(lastUpdatedAt != null) {
					// 4.3 현재 수신 페이지 정보 업데이트 
					int skuCurrentPage = 1;
					IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheItemBarcode.class);
					List<WmsMheItemBarcode> skuList = wmsQueryMgr.selectListBySql(this.skuSearchSql, ValueUtil.newMap("lastUpdatedAt", lastUpdatedAt), WmsMheItemBarcode.class, skuCurrentPage, this.skuPageLimit);
					
					while(xyz.elidom.sys.util.ValueUtil.isNotEmpty(skuList)) {
						// 4.4 상품 수신 처리 
						lastUpdatedAt = this.syncSkuList(domain.getId(), skuList, lastUpdatedAt);
						
						// 4.5 5초간 쉬었다가 
						ThreadUtil.sleep(5000);
						
						// 4.6 다시 조회 
						skuCurrentPage += 1;
						skuList = wmsQueryMgr.selectListBySql(this.skuSearchSql, ValueUtil.newMap("lastUpdatedAt", lastUpdatedAt), WmsMheItemBarcode.class, skuCurrentPage, this.skuPageLimit);
					}
					
					// 4.7 상품 마지막 업데이트 시간 업데이트 ...
					setting.setValue(DateUtil.dateTimeStr(lastUpdatedAt, "yyyy-MM-dd HH:mm:ss.SSS"));
					this.settingCtrl.update(setting.getId(), setting);
				}
			} catch(Exception e) {
				// 예외 처리
				ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "JOB_SYNC_ITEM_BARCODE_ERROR", e, null, true, true);
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
	 * @param lastUpdatedAt
	 * @return
	 */
	private Date syncSkuList(Long domainId, List<WmsMheItemBarcode> fromSkuList, Date lastUpdatedAt) {
		
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
			
			if(fromSku.getUpdDatetime().compareTo(lastUpdatedAt) == 1) {
				lastUpdatedAt = fromSku.getUpdDatetime();
			}
		}
		
		return lastUpdatedAt;
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
