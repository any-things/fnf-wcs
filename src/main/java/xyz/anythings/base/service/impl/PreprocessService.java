package xyz.anythings.base.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.elidom.dbist.dml.Query;

/**
 * 주문 가공 서비스 Facade
 * 
 * @author shortstop
 */
@Component
public class PreprocessService extends AbstractLogisService {

	/**
	 * 작업 배치별 주문 가공 리스트 조회 
	 * 
	 * @param batch
	 * @return
	 */
	public List<OrderPreprocess> searchPreprocessList(JobBatch batch) {
		return this.serviceDispatcher.getPreprocessService(batch).searchPreprocessList(batch);
	}
	
	/**
	 * 주문 가공을 위한 거래처 별 호기/로케이션 할당 정보 빌드
	 * 
	 * @param batch
	 * @param query
	 * @return 작업 배치의 거래처 리스트, 주문 그룹 리스트, 호기 리스트, 거래처 별 물량 요약 정보, 호기별 물량 요약 정보 
	 */
	public Map<String, ?> buildPreprocessSet(JobBatch batch, Query query) {
		return this.serviceDispatcher.getPreprocessService(batch).buildPreprocessSet(batch, query);
	}
	
	/**
	 * 주문 가공 정보를 생성한다.
	 * 
	 * @param batch
	 * @return 주문 가공 생성 개수
	 */
	public int generatePreprocess(JobBatch batch, Object ... params) {
		return this.serviceDispatcher.getPreprocessService(batch).generatePreprocess(batch, params);
	}
	
	/**
	 * 주문 가공 정보를 삭제한다.
	 * 
	 * @param batch
	 * @return 삭제된 주문 가공 정보 개수
	 */
	public int deletePreprocess(JobBatch batch) {
		return this.serviceDispatcher.getPreprocessService(batch).deletePreprocess(batch);
	}

	/**
	 * 주문 가공 완료
	 * 
	 * @param batch 작업 배치
	 * @param params 기타 파라미터
	 * @return
	 */
	public List<JobBatch> completePreprocess(JobBatch batch, Object ... params) {
		return this.serviceDispatcher.getPreprocessService(batch).completePreprocess(batch, params);
	}

	/**
	 * 주문 가공 (할당) 정보 리셋
	 * 
	 * @param batch
	 * @param resetAll 로케이션 할당 정보만 리셋할 것인지 (false), 로케이션 할당 및 호기 할당 정보까지 리셋할 것인지 (true)
	 * @param equipCdList
	 */
	public void resetPreprocess(JobBatch batch, boolean resetAll, List<String> equipCdList) {
		this.serviceDispatcher.getPreprocessService(batch).resetPreprocess(batch, resetAll, equipCdList);
	}

	/**
	 * 주문 가공 - 주문별 설비 레벨에 할당 (호기, 소터) 
	 * 
	 * @param batch
	 * @param equipCds
	 * @param items
	 * @param automatically
	 * @return
	 */
	public int assignEquipLevel(JobBatch batch, String equipCds, List<OrderPreprocess> items, boolean automatically) {
		return this.serviceDispatcher.getPreprocessService(batch).assignEquipLevel(batch, equipCds, items, automatically);
	}
	
	/**
	 * 주문 가공 - 주문별 서브 설비 레벨에 할당 (셀 등) 
	 * 
	 * @param batch
	 * @param equipType
	 * @param equipCd
	 * @param items
	 * @return
	 */
	public int assignSubEquipLevel(JobBatch batch, String equipType, String equipCd, List<OrderPreprocess> items) {
		return this.serviceDispatcher.getPreprocessService(batch).assignSubEquipLevel(batch, equipType, equipCd, items);
	}

}
