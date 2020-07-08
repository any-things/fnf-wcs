package operato.fnf.wcs.service.batch;

import java.util.List;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;

public class RecallBatchService extends AbstractQueryService {

	protected List<WmsMheHr> queryRecallBatch(String workDate, String jobType) throws Exception {
		// 1. 회수주문 WmsMheHr를 조회
		Query conds = new Query();
		conds.addFilter("whCd", FnFConstants.WH_CD_ICF);
		conds.addFilter("bizType", jobType);
		conds.addFilter("workDate", workDate);
		conds.addFilter("delYn", "Y");
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		List<WmsMheHr> delWaveList = wmsQueryMgr.selectList(WmsMheHr.class, conds);
		
		return delWaveList;
	}
}
