package xyz.anythings.base.service.api;

import java.util.List;
import java.util.Map;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.elidom.dbist.dml.Query;

/**
 * 주문 가공 서비스 API
 * 	1. 작업 배치로 부터 주문 가공 정보를 조회
 *  2. 주문 가공을 위한 거래처 별 호기/로케이션 할당 정보 빌드
 *  3. 주문 가공 (할당) 정보 생성
 *  4. 주문 가공 (할당) 정보 리셋
 *  5. 주문 가공 정보 삭제
 *  6. 주문 가공 완료
 * 
 * @author shortstop
 */
public interface IPreprocessService {

	/**
	 * 작업 배치별 주문 가공 리스트 조회 
	 * 
	 * @param batch
	 * @return
	 */
	public List<OrderPreprocess> searchPreprocessList(JobBatch batch);
	
	/**
	 * 주문 가공을 위한 거래처 별 호기/로케이션 할당 정보 빌드
	 * 
	 * @param batch
	 * @param query
	 * @return 작업 배치의 거래처 리스트, 주문 그룹 리스트, 호기 리스트, 거래처 별 물량 요약 정보, 호기별 물량 요약 정보 
	 */
	public Map<String, ?> buildPreprocessSet(JobBatch batch, Query query);
	
	/**
	 * 주문 가공 정보를 생성한다.
	 * 
	 * @param batch
	 * @return 주문 가공 생성 개수
	 */
	public int generatePreprocess(JobBatch batch, Object ... params);
	
	/**
	 * 주문 가공 정보를 삭제한다.
	 * 
	 * @param batch
	 * @return 삭제된 주문 가공 정보 개수
	 */
	public int deletePreprocess(JobBatch batch);

	/**
	 * 주문 가공 완료
	 * 
	 * @param batch 작업 배치
	 * @param params 기타 파라미터
	 * @return
	 */
	public List<JobBatch> completePreprocess(JobBatch batch, Object ... params);

	/**
	 * 주문 가공 (할당) 정보 리셋
	 * 
	 * @param batch
	 * @param isRackReset 로케이션 할당 정보만 리셋할 것인지 (false), 로케이션 할당 및 호기 할당 정보까지 리셋할 것인지 (true)
	 * @param equipCdList
	 */
	public void resetPreprocess(JobBatch batch, boolean isRackReset, List<String> equipCdList);

	/**
	 * 주문 가공 - 주문별 설비 레벨에 할당 (호기, 소터) 
	 * 
	 * @param batch
	 * @param equipCds
	 * @param items
	 * @param automatically
	 * @return
	 */
	public int assignEquipLevel(JobBatch batch, String equipCds, List<OrderPreprocess> items, boolean automatically);
	
	/**
	 * 주문 가공 - 주문별 서브 설비 레벨에 할당 (셀 등) 
	 * 
	 * @param batch
	 * @param equipType
	 * @param equipCd
	 * @param items
	 * @return
	 */
	public int assignSubEquipLevel(JobBatch batch, String equipType, String equipCd, List<OrderPreprocess> items);
}
