package operato.fnf.wcs.query.store;

import org.springframework.stereotype.Component;

import xyz.anythings.sys.service.AbstractQueryStore;
import xyz.elidom.sys.SysConstants;

/**
 * FnF 출고용 쿼리 스토어
 * 
 * @author shortstop
 */
@Component
public class FnFDasQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/fnf/wcs/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/fnf/wcs/query/ansi/";		
	}

	/**
	 * 작업 배치별 최종 10분대 별 작업 실적 서머리 조회
	 * 
	 * @return
	 */
	public String getDasBatchTotalResultSummary() {
		return this.getQueryByPath("das/BatchTotalMinuteResultSummary");
	}
	
	/**
	 * 작업 배치별 10분대 별 작업 실적 서머리 조회
	 * 
	 * @return
	 */
	public String getDasCalc10MinuteResultSummary() {
		return this.getQueryByPath("das/Calc10MinuteResultSummary");
	}
	
	/**
	 * 작업 배치별 설비 중단 시간
	 * 
	 * @return
	 */
	public String getDasEquipmentIdleTime() {
		return this.getQueryByPath("das/EquipmentIdleTime");
	}

}
