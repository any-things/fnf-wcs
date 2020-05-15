package operato.fnf.wcs.service.assign;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.query.store.FnFDpsQueryStore;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 할당 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsJobAssignService extends AbstractQueryService {

	@Autowired
	private FnFDpsQueryStore dpsQueryStore;
	
	/**
	 * 작업 배치 별 작업 할당 처리
	 * 
	 * @param domain
	 * @param batch
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void assignBatchJobs(Domain domain, JobBatch batch) {
		// 1. 작업 배치 내 모든 재고 중에 가장 많은 재고 순으로 조회
		String sql = this.dpsQueryStore.getStocksForJobAssign();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType,equipCd,activeFlag", batch.getDomainId(), batch.getId(), batch.getEquipType(), batch.getEquipCd(), true);
		List<Stock> stockList = this.queryManager.selectListBySql(sql, params, Stock.class, 0, 0);
		
		if(ValueUtil.isEmpty(stockList)) {
			return;
		}
		
		// 2. 배치 내 SKU가 적치된 재고 수량을 기준으로 많은 재고 조회
		for(Stock stock : stockList) {
			// 2.1 재고의 상품이 필요한 주문번호 검색
			List<WcsMheDr> orders = this.searchOrdersForAssign(batch, stock);
		}
	}
	
	private List<WcsMheDr> searchOrdersForAssign(JobBatch batch, Stock stock) {
		return null;
	}

}
