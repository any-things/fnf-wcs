package operato.logis.das.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * 출고용 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class DasQueryStore extends AbstractQueryStore {
	
	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/logis/das/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/logis/das/query/ansi/"; 
	}
	
	/**
	 * WMS I/F 테이블로 부터 출고 BatchReceipt 데이터를 조회
	 * 
	 * @return
	 */
	public String getOrderSummaryToReceive() {
		return this.getQueryByPath("batch/OrderSummaryToReceive");
	}

}
