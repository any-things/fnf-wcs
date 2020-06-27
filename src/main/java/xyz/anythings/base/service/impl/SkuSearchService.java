package xyz.anythings.base.service.impl;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WmsMheItemBarcode;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.query.store.BatchQueryStore;
import xyz.anythings.base.service.api.ISkuSearchService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * SKU 검색 / 조회 서비스 API 기본 구현
 * 
 * @author shortstop
 */
@Component("baseSkuSearchService")
public class SkuSearchService extends AbstractLogisService implements ISkuSearchService {

	/**
	 * 데이터소스 매니저
	 */
	@Autowired
	private DataSourceManager dataSourceMgr;
	
	@Override
	public String validateSkuCd(JobBatch batch, String skuCd) {
		skuCd = ValueUtil.isEmpty(skuCd) ? skuCd : skuCd.trim();
		
		// 1. SKU 바코드 최대 길이 체크
		int maxLength = this.getSkuCdMaxLength(batch.getDomainId());
		if(maxLength > 1) {
			skuCd = (skuCd.length() > maxLength) ? skuCd.substring(0, maxLength) : skuCd;
		}
		
		// 2. 상품 코드 유효성 체크 설정에 따라서 정규표현식 체크
		String skuCdRule = this.getSkuValidationRule(batch);
		if(ValueUtil.isNotEmpty(skuCdRule)) {
			if(!Pattern.matches(skuCdRule, skuCd)) {
				// 상품 코드가 유효하지 않습니다.
				throw ThrowUtil.newAIsInvalid("terms.label.sku_cd");
			}
		}
		
		return skuCd;
	}
	
	@Override
	public List<SKU> searchListInBatchGroup(JobBatch batch, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, batch.getComCd(), skuCd, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatchGroup(JobBatch batch, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		skuCd = this.validateSkuCd(batch, skuCd);
		String[] skuCodeFields = this.getSkuSearchConditionFields(batch);
		
		String sql = BeanUtil.get(BatchQueryStore.class).getSearchSkuInBatchGroupQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,comCd", batch.getDomainId(), batch.getBatchGroupId(), comCd);
		if(todoOnly) {
			params.put(SysConstants.ENTITY_FIELD_STATUS, LogisConstants.JOB_STATUS_WIPC);
		}
		
		for(String skuCodeField : skuCodeFields) {
			params.put(skuCodeField, skuCd);
		}
		
		return this.searchSkuList(skuCd, sql, params, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, batch.getComCd(), skuCd, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		return this.searchListInBatch(batch, null, comCd, skuCd, todoOnly, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchListInBatch(JobBatch batch, String stationCd, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty) {
		String[] skuCodeFields = this.getSkuSearchConditionFields(batch);
		String sql = BeanUtil.get(BatchQueryStore.class).getSearchSkuInBatchQuery();
		
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd", batch.getDomainId(), batch.getId(), comCd);
		if(todoOnly) {
			params.put(SysConstants.ENTITY_FIELD_STATUS, LogisConstants.JOB_STATUS_WIPC);
		}
		
		for(String skuCodeField : skuCodeFields) {
			params.put(skuCodeField, skuCd);
		}
		
		return this.searchSkuList(skuCd, sql, params, exceptionWhenEmpty);
	}
	
	@Override
	public List<SKU> searchList(JobBatch batch, String skuCd) {
		String[] skuCodeFields = this.getSkuSearchConditionFields(batch);
		String selectFields = this.getSkuSelectFieldsByOrder(batch);
		Map<String, Object> params = ValueUtil.newMap("batchId", batch.getId());
		
		StringJoiner sql = new StringJoiner(LogisConstants.LINE_SEPARATOR);
		sql.add("SELECT DISTINCT ").add(selectFields).add(" FROM MHE_DR WHERE WORK_UNIT = :batchId AND (");
		int idx = 0;
		for(String skuCodeField : skuCodeFields) {
			params.put(skuCodeField, skuCd);
			sql.add((idx > 0 ? "or" : "") + " " + FormatUtil.toUnderScore(skuCodeField) + " = :" + skuCodeField);
			idx++;
		}
		
		sql.add(")");
		return this.queryManager.selectListBySql(sql.toString(), params, SKU.class, 0, 0);
	}
	
	/**
	 * 조회 쿼리, 파라미터로 상품 조회
	 * 
	 * @param skuCd
	 * @param sql
	 * @param params
	 * @param exceptionWhenEmpty
	 * @return
	 */
	protected List<SKU> searchSkuList(String skuCd, String sql, Map<String, Object> params, boolean exceptionWhenEmpty) {
		List<SKU> skuList = this.queryManager.selectListBySql(sql, params, SKU.class, 0, 0);
		
		if(ValueUtil.isEmpty(skuList) && exceptionWhenEmpty) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU", skuCd));
		}
		
		return skuList;
	}

	@Override
	public SKU findSku(Long domainId, String comCd, String skuCd, boolean exceptionWhenEmpty) {
		/*Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("comCd", comCd);
		condition.addFilter("skuCd", skuCd);
		SKU sku = this.queryManager.selectByCondition(SKU.class, condition);*/
		
		IQueryManager wmsQueryMgr = this.dataSourceMgr.getQueryManager(WmsMheItemBarcode.class);
		String sql = "select :comCd as com_cd, item_cd as sku_cd, barcode2 as sku_barcd, barcode as sku_barcd2, '' as sku_nm, 1 as box_in_qty, brand as brand_cd, item_season as season_cd, item_style as style_cd, item_color as color_cd, item_size as size_cd, floor_cd as sku_class, item_gcd as sku_type, item_gcd_nm as sku_desc from mhe_item_barcode where item_cd = :skuCd";
		Map<String, Object> params = ValueUtil.newMap("comCd,skuCd", FnFConstants.FNF_COM_CD, skuCd);
		SKU sku = wmsQueryMgr.selectBySql(sql, params, SKU.class);
		
		if(sku == null && exceptionWhenEmpty) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU", skuCd));
		}

		return sku;
	}

	@Override
	public SKU findSku(Long domainId, String stageCd, String comCd, String skuCd, String skuBarcd, boolean exceptionFlag) {
		String selectFields = this.getSkuSelectFields(domainId, stageCd);
		return findSKU(domainId, exceptionFlag, selectFields, "comCd,skuCd,skuBarcd", comCd, skuCd, skuBarcd);
	}
	
	@Override
	public SKU findSKU(Long domainId, boolean exceptionWhenEmpty, String selectFields, String paramNames, Object ... paramValues) {
		SKU sku = AnyEntityUtil.findEntityBy(domainId, false, SKU.class, selectFields, paramNames, paramValues);

		if(sku == null && exceptionWhenEmpty) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU"));
		}

		return sku;
	}

	@Override
	public Float findSkuWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag) {
		SKU sku = this.getSkuForWeight(domainId, comCd, skuCd, exceptionFlag);
		return (sku == null || sku.getSkuWt() == null) ? 0.0f : sku.getSkuWt();
	}

	@Override
	public Float findSkuWeight(Long domainId, String comCd, String skuCd, String toUnit, boolean exceptionFlag) {
		SKU sku = this.getSkuForWeight(domainId, comCd, skuCd, exceptionFlag);
		Float weight = (sku == null) ? 0.0f : sku.getSkuWt();

		if(ValueUtil.isNotEmpty(weight) && weight > 0.0f) {
			weight = this.convertWeightToUnit(weight, sku.getWtUnit(), LogisCodeConstants.WEIGHT_UNIT_KG);
		}

		return weight;
	}
	
	@Override
	public SKU getSkuWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag) {
		return this.getSkuWeight(domainId, comCd, skuCd, null, exceptionFlag);
	}
	
	@Override
	public SKU getSkuWeight(Long domainId, String comCd, String skuCd, String toUnit, boolean exceptionFlag) {
		SKU sku = this.getSkuForWeight(domainId, comCd, skuCd, exceptionFlag);
		Float skuWt = sku.getSkuWt();
		
		if(ValueUtil.isNotEmpty(skuWt) && skuWt > 0.0f) {
			skuWt = this.convertWeightToUnit(skuWt, sku.getWtUnit(), LogisCodeConstants.WEIGHT_UNIT_KG);
			sku.setSkuWt(skuWt);
		}
		
		return sku;
	}
	
	/**
	 * 고객사 코드, 상품 코드로 상품 조회
	 * 
	 * @param comCd
	 * @param skuCd
	 * @param exceptionFlag
	 * @return
	 */
	protected SKU getSkuForWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, 1, 1, "sku_wt", "wt_unit");
		condition.addFilter("comCd", comCd);
		condition.addFilter("skuCd", skuCd);
		SKU sku = this.queryManager.selectByCondition(SKU.class, condition);

		if(sku == null && exceptionFlag) {
			throw ThrowUtil.newValidationErrorWithNoLog(ThrowUtil.notFoundRecordMsg("terms.menu.SKU", skuCd));
		}
		
		return sku;
	}
	
	/**
	 * 중량 단위 변환 (KG -> G, G -> KG)
	 * 
	 * @param skuWeight
	 * @param skuWtUnit
	 * @param toUnit
	 * @return
	 */
	protected Float convertWeightToUnit(Float skuWeight, String skuWtUnit, String toUnit) {
		if(ValueUtil.isNotEmpty(skuWeight) && skuWeight > 0.0f) {
			if(!ValueUtil.isEqualIgnoreCase(skuWtUnit, toUnit)) {
				// 1. KG -> G
				if(ValueUtil.isEqualIgnoreCase(toUnit, LogisCodeConstants.WEIGHT_UNIT_G)) {
					skuWeight = (skuWeight * 1000.0f);
				// 2. G -> KG
				} else if(ValueUtil.isEqualIgnoreCase(toUnit, LogisCodeConstants.WEIGHT_UNIT_KG)) {
					skuWeight = (skuWeight / 1000.0f);
				}
			}
		}

		return skuWeight;
	}
	
	/**
	 * 상품 코드 유효성 체크 룰
	 * 
	 * @param batch
	 * @return
	 */
	private String getSkuValidationRule(JobBatch batch) {
		return SettingUtil.getValue(batch.getDomainId(), "fnf.sku.validate.sku_cd.rule", null);
	}
	
	/**
	 * 주문 테이블에서 상품 조회 시 필드
	 * 
	 * @param batch
	 * @return
	 */
	private String getSkuSelectFieldsByOrder(JobBatch batch) {
		return SettingUtil.getValue(batch.getDomainId(), "fnf.sku.search.select.byorder.fields", "*");
	}
	
	/**
	 * 상품 테이블에서 상품 조회 필드
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	private String getSkuSelectFields(Long domainId, String stageCd) {
		//String selectFields = BatchJobConfigUtil.getSkuSearchSelectFields(batch);
		return SettingUtil.getValue(domainId, "fnf.sku.search.select.fields", "*");
	}
	
	/**
	 * 상품 검색 조건 필드
	 * 
	 * @param batch
	 * @return
	 */
	private String[] getSkuSearchConditionFields(JobBatch batch) {
		//String[] skuCodeFields = BatchJobConfigUtil.getSkuSearchConditionFields(batch);
		String searchCondFields = SettingUtil.getValue(batch.getDomainId(), "fnf.sku.search.condition.fields", "itemCd");
		return searchCondFields.split(LogisConstants.COMMA);
	}
	
	/**
	 * 상품 코드 최대 사이즈 
	 * 
	 * @param domainId
	 * @return
	 */
	private int getSkuCdMaxLength(Long domainId) {
		// int maxLength = BatchJobConfigUtil.getMaxBarcodeSize(batch);
		return ValueUtil.toInteger(SettingUtil.getValue(domainId, "fnf.sku.code.max.length", "19"));
	}

}
