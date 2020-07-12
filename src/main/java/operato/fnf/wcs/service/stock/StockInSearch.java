package operato.fnf.wcs.service.stock;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;

public class StockInSearch extends AbstractQueryService {
	protected JobBatch getRackBatch(String rackCd) {
		Query conds = new Query(1, 1);
		conds.addFilter("domainId", Domain.currentDomainId());
		conds.addFilter("status", "RUN");
		conds.addFilter("equipType", "Rack");
		conds.addFilter("equipCd", rackCd);
		JobBatch rackBatch = queryManager.select(JobBatch.class, conds);
		
		return rackBatch;
	}
}
