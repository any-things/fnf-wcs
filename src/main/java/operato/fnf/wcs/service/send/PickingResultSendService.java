package operato.fnf.wcs.service.send;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 피킹 실적 전송 서비스
 * 
 * @author shortstop
 */
@Component
public class PickingResultSendService extends AbstractQueryService {

	/**
	 * 실적 업데이트 쿼리
	 */
	private String wmsResultUpdateQuery = "update mhe_dr set mhe_no = :mheNo, cmpt_qty = :pickedQty, mhe_datetime = :pickedAt where wh_cd = :whCd and work_unit = :batchId and ref_no = :orderNo and outb_no = :outbNo and shipto_id = :shiptoId and item_cd = :skuCd and location_cd = :locationCd";
	
	/**
	 * 피킹 실적 전송
	 * 
	 * @param domain
	 * @param batch
	 */
	public void sendPickingResults(Domain domain, JobBatch batch) {
		// 1. 박스 완료 실적 조회
		List<WcsMheDr> pickList = this.searchPickResultList(batch);
		
		if(ValueUtil.isNotEmpty(pickList)) {
			IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
			
			// 2. WMS에 전송
			for(WcsMheDr pickResult : pickList) {
				// WCS 주문별 실적 정보 I/F
				Map<String, Object> params = ValueUtil.newMap("whCd,batchId,orderNo,outbNo,shiptoId,skuCd,locationCd,pickedQty,pickedAt,mheNo", FnFConstants.WH_CD_ICF, batch.getId(), pickResult.getRefNo(), pickResult.getOutbNo(), pickResult.getShiptoId(), pickResult.getItemCd(), pickResult.getLocationCd(), pickResult.getCmptQty(), pickResult.getMheDatetime(), batch.getEquipGroupCd());
				wmsQueryMgr.executeBySql(this.wmsResultUpdateQuery, params);
				
				// WCS 주문 정보에 실적 전송 플래그 설정
				pickResult.setPickResultIfAt(pickResult.getMheDatetime());
			}
			
			// 3. 피킹 실적 전송 시간 업데이트
			this.queryManager.updateBatch(pickList, "pickResultIfAt");
		}
	}
	
	/**
	 * 확정 처리된 데이터 조회
	 * 
	 * @param batch
	 * @return
	 */
	private List<WcsMheDr> searchPickResultList(JobBatch batch) {
		Query condition = new Query();
		condition.addFilter("whCd", "ICF");
		condition.addFilter("workUnit", batch.getId());
		condition.addFilter("pickResultIfAt", LogisConstants.IS_NULL, LogisConstants.EMPTY_STRING);
		condition.addFilter("cmptQty", LogisConstants.GREATER_THAN_EQUAL, 1);
		condition.addOrder("boxInputSeq", true);
		condition.addOrder("mheDatetime", true);
		return this.queryManager.selectList(WcsMheDr.class, condition);
	}

}
