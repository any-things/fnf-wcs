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
public class FnFSmsQueryStore extends AbstractQueryStore {

	@Override
	public void initQueryStore(String databaseType) {
		this.databaseType = databaseType;
		this.basePath = "operato/fnf/wcs/query/" + this.databaseType + SysConstants.SLASH;
		this.defaultBasePath = "operato/fnf/wcs/query/ansi/";		
	}
	/**
	 * SRTN 작업 배치별 10분대 별 작업 실적 서머리 조회
	 * 
	 * @return
	 */
	public String getSrtnCalc10MinuteResultSummary() {
		return this.getQueryByPath("srtn/Calc10MinuteResultSummary");
	}
	/**
	 * SDAS 작업 배치별 10분대 별 작업 실적 서머리 조회
	 * 
	 * @return
	 */
	public String getSdasCalc10MinuteResultSummary() {
		return this.getQueryByPath("sdas/Calc10MinuteResultSummary");
	}
	/**
	 * SDPS 작업 배치별 10분대 별 작업 실적 서머리 조회
	 * 
	 * @return
	 */
	public String getSdpsCalc10MinuteResultSummary() {
		return this.getQueryByPath("sdps/Calc10MinuteResultSummary");
	}

}
