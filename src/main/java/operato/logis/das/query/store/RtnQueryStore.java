package operato.logis.das.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * 반품용 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class RtnQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/das/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/das/query/ansi/"; 
	}
	
	/**
	 * WMS I/F 테이블로 부터 반품 BatchReceipt 데이터를 조회
	 * 
	 * @return
	 */
	public String getOrderSummaryToReceive() {
		return this.getQueryByPath("batch/OrderSummaryToReceive");
	}
	
	/**
	 * WMS I/F 테이블로 부터  주문수신 완료된 데이터 변경('Y')
	 * 
	 * @return
	 */
	public String getWmsIfToReceiptUpdateQuery() {
		return this.getQueryByPath("batch/WmsIfToReceiptUpdate");
	}
	
	/**
	 * 작업 지시를 위해 주문 가공 완료 요약 (주문 개수, 상품 개수, PCS) 정보 조회
	 *
	 * @return
	 */
	public String getRtnInstructionSummaryDataQuery() {
		return this.getQueryByPath("instruction/InstructionSummaryData");
	}

}
