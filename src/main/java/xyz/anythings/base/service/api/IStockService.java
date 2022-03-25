package xyz.anythings.base.service.api;

import java.util.List;

import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.entity.Stocktaking;

/**
 * 재고 관련 서비스 API
 * 
 * @author shortstop
 */
@Component
public interface IStockService {
	
	/**
	 * 셀 재고 조회
	 * 
	 * @param domainId
	 * @param cellCd
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public Stock findStock(Long domainId, String cellCd, boolean exceptionWhenEmpty);
			
	/**
	 * 재고 정보를 조회 
	 * 
	 * @param batch
	 * @param cellCd
	 * @param comCd
	 * @param skuCd
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public Stock findStock(Long domainId, String cellCd, String comCd, String skuCd, boolean exceptionWhenEmpty);
		
	/**
	 * 셀 재고 조회 없으면 생성
	 * 
	 * @param domainId
	 * @param cellCd
	 * @return
	 */
	public Stock findOrCreateStock(Long domainId, String cellCd);
	
	/**
	 * 셀 재고 조회 없으면 생성
	 * 
	 * @param domainId
	 * @param cellCd
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	public Stock findOrCreateStock(Long domainId, String cellCd, String comCd, String skuCd);
	
	/**
	 * 셀 재고 조회 없으면 생성
	 * 
	 * @param domainId
	 * @param cellCd
	 * @param sku
	 * @return
	 */
	public Stock findOrCreateStock(Long domainId, String cellCd, SKU sku);
	
	/**
	 * 재고 생성
	 * 
	 * @param domainId
	 * @param cellCd
	 * @param sku
	 * @return
	 */
	public Stock createStock(Long domainId, String cellCd, SKU sku);
	
	/**
	 * 재고 생성
	 * 
	 * @param domainId
	 * @param cellCd
	 * @param comCd
	 * @param skuCd
	 * @param skuNm
	 * @return
	 */
	public Stock createStock(Long domainId, String cellCd, String comCd, String skuCd, String skuNm);
	
	/**
	 * 재고 추가
	 * 
	 * @param stock
	 * @param tranCd
	 * @param addQty
	 * @return
	 */
	public Stock addStock(Stock stock, String tranCd, int addQty);
	
	/**
	 * 재고 차감
	 * 
	 * @param stock
	 * @param tranCd
	 * @param removeQty
	 * @return
	 */
	public Stock removeStock(Stock stock, String tranCd, int removeQty);
	
	/**
	 * 재고 조정 처리
	 * 
	 * @param domainId
	 * @param trxCd 트랜잭션 코드 
	 * @param cellCd 셀 코드 
	 * @param comCd 고객사 코드 
	 * @param skuCd 상품 코드 
	 * @param adjustQty 추가 (혹은 감소) 수량
	 * @return
	 */
	public Stock adjustStock(Long domainId, String trxCd, String cellCd, String comCd, String skuCd, int adjustQty);
		
	/**
	 * 고정식 여부에 skuCd가 적치되어 있는 재고 조회 
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param fixedFlag
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	public List<Stock> searchStocksBySku(Long domainId, String equipType, String equipCd, String comCd, String skuCd);
	
	/**
	 * 고정식 여부에 skuCd가 적치되어 있는 재고 조회 
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param fixedFlag
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	public List<Stock> searchStocksBySku(Long domainId, String equipType, String equipCd, Boolean fixedFlag, String comCd, String skuCd);
	
	/**
	 * 추천 로케이션 조회 
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param comCd
	 * @param skuCd
	 * @param fixedFlag
	 * @return
	 */
	public List<Stock> searchRecommendCells(Long domainId, String equipType, String equipCd, String comCd, String skuCd, Boolean fixedFlag);
	
	/**
	 * 로케이션의 주문, 할당, 재고 수량 계산 
	 * 
	 * @param stock
	 * @return
	 */
	public Stock calcuateOrderStock(Stock stock);
	
	/**
	 * 상품별 현재 배치의 재고 현황 조회
	 * 
	 * @param domainId
	 * @param batchId
	 * @param equipType
	 * @param equipCd
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	public Stock calculateSkuOrderStock(Long domainId, String batchId, String equipType, String equipCd, String comCd, String skuCd);
		
	/**
	 * 재고 보충을 위한 상품 투입 수량 계산 
	 * 
	 * @param batchId
	 * @param stock
	 * @return
	 */
	public int calcSkuInputQty(String batchId, Stock stock);
	
	/**
	 * 재고 보충
	 * 
	 * @param domainId
	 * @param cellCd
	 * @param sku
	 * @param loadQty
	 * @return
	 */
	public Stock supplyStock(Long domainId, String cellCd, SKU sku, int loadQty);
	
	/**
	 * 재고 실사 시작
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCdList
	 */
	public void startStocktaking(Long domainId, String equipType, List<String> equipCdList);
	
	/**
	 * 오늘 날짜의 센터, 호기별 재고 실사를 실행한다.
	 * 
	 * @param domainId
	 * @param today
	 * @param equipType
	 * @param equipCd
	 */
	public void startStocktaking(Long domainId, String today, String equipType, String equipCd);
	
	/**
	 * 재고 실사 종료 
	 * 
	 * @param domainId
	 * @param stocktakingId
	 */
	public void finishStocktaking(Long domainId, String stocktakingId);
	
	/**
	 * 피킹 재고 차감
	 * 
	 * @param domainId
	 * @param equipType 설비 유형 
	 * @param equipCd 설비 코드 
	 * @param cellCd 셀 코드 
	 * @param comCd 고객사 코드 
	 * @param skuCd 상품 코드 
	 * @param addQty 추가 (혹은 감소) 수량
	 */
	public void removeStockForPicking(Long domainId, String equipType, String equipCd, String cellCd, String comCd, String skuCd, int addQty);
	
	/**
	 * 재고 실사 시 재고 조정 처리 
	 * 
	 * @param domainId
	 * @param stocktakingId
	 * @param indCd
	 * @param fromQty
	 * @param toQty
	 * @return
	 */
	public Stock adjustStock(Long domainId, String stocktakingId, String indCd, int fromQty, int toQty);

	/**
	 * 오늘 날짜에 해당하는 센터 및 호기에 해당하는 가장 최신의 재고 실사 정보 조회 
	 * 
	 * @param domainId
	 * @param date
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public Stocktaking findLatestStocktaking(Long domainId, String date, String equipType, String equipCd);
	
	/**
	 * LED Bar 사용 여부 설정을 토글
	 * 
	 * @param domainId
	 * @param on
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public boolean toggleLedSettingForStock(Long domainId, boolean on, String equipType, String equipCd);
	
	/**
	 * 호기의 재고 부족 셀의 모든 LED 점등
	 *  
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public int ledOnShortageStocks(Long domainId, String equipType, String equipCd);
	
	/**
	 * 호기의 모든 LED 소등
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public int ledOffByEquip(Long domainId, String equipType, String equipCd);
	
}
