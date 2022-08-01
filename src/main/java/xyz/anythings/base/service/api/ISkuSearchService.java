package xyz.anythings.base.service.api;

import java.util.List;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;

/**
 * 분류 처리를 위한 SKU 검색 / 조회 서비스 API
 * 
 * @author shortstop
 */
public interface ISkuSearchService {
	
	/**
	 * 작업 배치 소속 분류 작업 중에 작업자가 상품 코드 혹은 상품 바코드 정보를 스캔했을 때 유효성 체크
	 * 
	 * @param batch 작업 배치
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @return
	 */
	public String validateSkuCd(JobBatch batch, String skuCd);
		
	/**
	 * 작업 배치 그룹 범위 내에서 상품 코드 혹은 상품 바코드로 상품 리스트 조회 - 보통 조회 결과가 하나이겠지만 고객사가 다른 동일 SKU가 존재할 수 있어서 두 개 이상도 가능하다
	 * 
	 * @param batch 작업 배치
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @param todoOnly 처리한 것은 제외하고 처리할 대상 상품에 대해서만 조회할 지 여부
	 * @param exceptionWhenEmpty 상품이 존재하지 않은 경우 예외 발생 여부
	 * @return
	 */
	public List<SKU> searchListInBatchGroup(JobBatch batch, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty);
	
	/**
	 * 작업 배치 그룹 범위 내에서 고객사 코드와 상품 코드 혹은 상품 바코드로 상품 리스트 조회 - 보통 조회 결과가 하나이겠지만 고객사가 다른 동일 SKU가 존재할 수 있어서 두 개 이상도 가능하다
	 * 
	 * @param batch 작업 배치
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @param todoOnly 처리한 것은 제외하고 처리할 대상 상품에 대해서만 조회할 지 여부
	 * @param exceptionWhenEmpty 상품이 존재하지 않은 경우 예외 발생 여부
	 * @return
	 */
	public List<SKU> searchListInBatchGroup(JobBatch batch, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty);
	
	/**
	 * 작업 배치 범위 내에서 상품 코드 혹은 상품 바코드로 상품 리스트 조회 - 보통 조회 결과가 하나이겠지만 고객사가 다른 동일 SKU가 존재할 수 있어서 두 개 이상도 가능하다
	 * 
	 * @param batch 작업 배치
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @param todoOnly 처리한 것은 제외하고 처리할 대상 상품에 대해서만 조회할 지 여부
	 * @param exceptionWhenEmpty 상품이 존재하지 않은 경우 예외 발생 여부
	 * @return
	 */
	public List<SKU> searchListInBatch(JobBatch batch, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty);
	
	/**
	 * 작업 배치 범위 내에서 고객사 코드와 상품 코드 혹은 상품 바코드로 상품 리스트 조회 - 보통 조회 결과가 하나이겠지만 고객사가 다른 동일 SKU가 존재할 수 있어서 두 개 이상도 가능하다
	 * 
	 * @param batch 작업 배치 
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @param todoOnly 처리한 것은 제외하고 처리할 대상 상품에 대해서만 조회할 지 여부
	 * @param exceptionWhenEmpty 상품이 존재하지 않은 경우 예외 발생 여부
	 * @return
	 */
	public List<SKU> searchListInBatch(JobBatch batch, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty);
	
	/**
	 * 작업 배치 및 작업 스테이션 범위 내에서 고객사 코드와 상품 코드 혹은 상품 바코드로 상품 리스트 조회 - 보통 조회 결과가 하나이겠지만 고객사가 다른 동일 SKU가 존재할 수 있어서 두 개 이상도 가능하다
	 * 
	 * @param batch 작업 배치
	 * @param stationCd 작업 스테이션 코드 
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @param todoOnly 처리한 것은 제외하고 처리할 대상 상품에 대해서만 조회할 지 여부
	 * @param exceptionWhenEmpty 상품이 존재하지 않은 경우 예외 발생 여부
	 * @return
	 */
	public List<SKU> searchListInBatch(JobBatch batch, String stationCd, String comCd, String skuCd, boolean todoOnly, boolean exceptionWhenEmpty);
	
	/**
	 * 상품 코드 or 상품 바코드 or 기타 바코드로 상품 마스터에서 상품 조회
	 * 
	 * @param skuCd 상품 코드 혹은 상품 바코드 등
	 * @return
	 */
	public List<SKU> searchList(JobBatch batch, String skuCd);
	
	/**
	 * SKU 키(고객사 코드, 상품 코드)로 창고 내 SKU 조회
	 * 
	 * @param domainId
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 Only
	 * @param exceptionFlag
	 * @return
	 */
	public SKU findSku(Long domainId, String comCd, String skuCd, boolean exceptionFlag);
	
	/**
	 * 창고 내 고객사 코드, 상품 코드, 상품 바코드로 SKU 조회
	 * 
	 * @param domainId
	 * @param stageCd 스테이지 코드
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 Only
	 * @param skuBarcd 상품 바코드 Only
	 * @param exceptionFlag
	 * @return
	 */
	public SKU findSku(Long domainId, String stageCd, String comCd, String skuCd, String skuBarcd, boolean exceptionFlag);
	
	/**
	 * 조회 조건으로 SKU 조회
	 * 
	 * @param domainId
	 * @param exceptionFlag
	 * @param selectFields
	 * @param paramNames
	 * @param paramValues
	 * @return
	 */
	public SKU findSKU(Long domainId, boolean exceptionFlag, String selectFields, String paramNames, Object ... paramValues);
	
	/**
	 * SKU 중량 조회 
	 * 
	 * @param domainId
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 Only
	 * @param exceptionFlag
	 * @return
	 */
	public Float findSkuWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag);
	
	/**
	 * SKU 중량 kg/g 형태로 조회
	 * 
	 * @param domainId
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 Only
	 * @param toUnit 중량 단위 g or kg
	 * @param exceptionFlag
	 * @return
	 */
	public Float findSkuWeight(Long domainId, String comCd, String skuCd, String toUnit, boolean exceptionFlag);
	
	/**
	 * SKU의 중량 관련 모든 정보를 SKU 오브젝트 형태로 리턴
	 * 
	 * @param domainId
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 Only
	 * @param exceptionFlag
	 * @return
	 */
	public SKU getSkuWeight(Long domainId, String comCd, String skuCd, boolean exceptionFlag);
	
	/**
	 * SKU의 중량 관련 모든 정보를 중량 단위 toUnit을 적용하여 SKU 오브젝트 형태로 리턴 
	 * 
	 * @param domainId
	 * @param comCd 고객사 코드
	 * @param skuCd 상품 코드 Only
	 * @param toUnit 중량 단위 g or kg
	 * @param exceptionFlag
	 * @return
	 */
	public SKU getSkuWeight(Long domainId, String comCd, String skuCd, String toUnit, boolean exceptionFlag);
	
}