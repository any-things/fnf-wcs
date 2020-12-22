package xyz.anythings.base.service.api;

import java.util.List;
import java.util.Map;

import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.elidom.dbist.dml.Page;

/**
 * 작업 상태 서비스 API
 * 
 * 	1. 배치 작업 조회
 * 		1) 배치별 작업 진행율 조회  
 * 	2. 투입 정보 조회
 * 		1) 배치별, 호기별, 사이드 별 투입 리스트 조회
 * 		2) 배치별, 사이드 별 최근 투입 정보 조회
 * 		3) 배치별 다음 처리할 릴레이 시퀀스 조회
 * 	3. 작업 조회 
 * 		1) 조회 조건으로 작업 리스트 조회
 * 		2) 조회 조건으로 표시기 점등을 위한 작업 리스트 조회
 *  4. 처리 박스 조회
 * 		1) 조회 조건으로 처리한 박스 리스트 조회
 * 		2) 박스 내 내품 내역 조회
 *  5. 기타
 *  	1) 배치별 (B2B - 상품별, B2C - 주문별) 셀에 남은 총 처리할 수량 계산
 *  	2) 배치별 (B2B - 상품별, B2C - 주문별) 셀에 박싱 되지 않은 분류 처리한 누적 분류 수량
 *  	3) 배치별 (B2B - 상품별, B2C - 주문별) 셀에 박싱 되지 않은 분류 처리할 남은 수량 searchInputList계산
 * 
 * @author shortstop
 */
public interface IJobStatusService {

	/**
	 * 1-1. 배치 작업 조회 - 작업 배치 작업 진행 요약 정보
	 * 
	 * @param batch
	 * @return
	 */
	public BatchProgressRate getBatchProgressSummary(JobBatch batch);
		
	/**
	 * 2-1. 투입 정보 조회 (리스트)
	 * 
	 * @param batch
	 * @param equipCd
	 * @param stationCd
	 * @param selectedInputId -- 탭 리스트 조회시 기본으로 선택될 기준 탭 (투입) ID 
	 * @return
	 */
	public List<JobInput> searchInputList(JobBatch batch, String equipCd, String stationCd, String selectedInputId);
	
	/**
	 * 2-1. 호기 내 투입 정보 조회 (페이지네이션)
	 * 
	 * @param batch
	 * @param equipCd
	 * @param status
	 * @param page
	 * @param limit
	 * @return
	 */
	public Page<JobInput> paginateInputList(JobBatch batch, String equipCd, String status, int page, int limit);
	
	/**
	 * 2-1. 작업 존 내 투입 정보 조회 (페이지네이션)
	 * 
	 * @param batch
	 * @param equipCd
	 * @param stationCd
	 * @param status
	 * @param page
	 * @param limit
	 * @return
	 */
	public Page<JobInput> paginateInputList(JobBatch batch, String equipCd, String stationCd, String status, int page, int limit);
	
	/**
	 * 2-2. 투입 정보 조회 - 가장 최근에 투입된 투입 정보 조회
	 * 
	 * @param batch
	 * @return
	 */
	public JobInput findLatestInput(JobBatch batch);
	
	/**
	 * 2-3. 투입 정보 조회 - 다음 처리할 릴레이 시퀀스를 조회 - 락을 걸고 처리해야 함 ...
	 * 
	 * @param batchId
	 * @return
	 */
	public Integer findNextInputSeq(JobBatch batch);
	
	/**
	 * 3-1. 작업 조회 - 설비, 작업 존 별 투입 정보에 매핑된 투입 작업 리스트 조회
	 * 
	 * @param batch
	 * @param input
	 * @param stationCd
	 * @return
	 */
	public List<JobInstance> searchInputJobList(JobBatch batch, JobInput input, String stationCd);
	
	/**
	 * 3-1. 작업 조회 - 작업 배치 내에 모든 조회 조건으로 투입 작업 리스트를 조회
	 * 
	 * @param batch
	 * @param condition
	 * @return
	 */
	public List<JobInstance> searchInputJobList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 3-1. 작업 조회 - 작업 배치 내에 피킹 중인 작업 리스트를 조회
	 *  
	 * @param batch 작업 배치 
	 * @param stationCd 작업 스테이션 
	 * @param classCd 소분류 코드 (일반적으로 B2C인 경우 주문 번호, B2B 출고인 경우 매장 코드, 반품인 경우 상품 코드
	 * @return
	 */
	public List<JobInstance> searchPickingJobList(JobBatch batch, String stationCd, String classCd);
	
	/**
	 * 3-1. 작업 조회 - 작업 배치 내에 모든 조회 조건으로 작업 리스트를 조회
	 *  
	 * @param batch 작업 배치 
	 * @param condition 조회 조건 
	 * @return
	 */
	public List<JobInstance> searchPickingJobList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 3-1. 작업 조회 - 피킹 처리를 위한 작업 인스턴스 ID로 작업 데이터 조회
	 *  
	 * @param domainId 도메인 ID
	 * @param jobInstanceId 작업 인스턴스 ID
	 * @return
	 */
	public JobInstance findPickingJob(Long domainId, String jobInstanceId);
	
	/**
	 * 3-1. 작업 조회 - 조회 조건으로 작업 리스트 조회
	 * 
	 * @param batch
	 * @param condition
	 * @return
	 */
	public List<JobInstance> searchJobList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 4-1. 처리 박스 조회 - 셀의 풀 박스 처리를 위해 분류 처리 후 박싱이 안 된 마지막 작업 데이터 조회
	 * 
	 * @param batch
	 * @param subEquipCd 셀 or 슈트 등
	 * @return
	 */
	public JobInstance findUnboxedJob(JobBatch batch, String subEquipCd);
	
	/**
	 * 4-1. 처리 박스 조회 - 셀에 박싱 처리된 마지막 박스 조회
	 * 
	 * @param batch
	 * @param subEquipCd 셀 or 슈트 등
	 * @return
	 */
	public BoxPack findLatestBox(JobBatch batch, String subEquipCd);
	
	/**
	 * 4-2. 처리 박스 조회 - 조회 조건으로 박스 페이지네이션 조회 
	 * 
	 * @param batch
	 * @param condition
	 * @param page
	 * @param limit
	 * @return
	 */
	public Page<BoxPack> paginateBoxList(JobBatch batch, Map<String, Object> condition, int page, int limit);
	
	/**
	 * 4-2. 처리 박스 조회 - 조회 조건으로 박스 리스트 조회 
	 * 
	 * @param batch
	 * @param condition
	 * @return
	 */
	public List<BoxPack> searchBoxList(JobBatch batch, Map<String, Object> condition);
	
	/**
	 * 4-2. 처리 박스 조회 - 박스 ID로 박스 내품 내역 조회
	 * 
	 * @param domainId
	 * @param boxPackId
	 * @return
	 */
	public List<BoxItem> searchBoxItems(Long domainId, String boxPackId);
		
	/**
	 * 6-1. 기타 : 배치별 (B2B - 상품별, B2C - 주문별) 셀에 남은 총 처리할 수량 계산
	 * 
	 * @param job
	 * @return
	 */
	public int totalOrderQtyByJob(JobInstance job);
	
	/**
	 * 6-2. 기타 : 배치별 (B2B - 상품별, B2C - 주문별) 셀에 박싱 되지 않은 분류 처리한 누적 분류 수량
	 * 
	 * @param job
	 * @return
	 */
	public int totalPickedQtyByJob(JobInstance job);
	
	/**
	 * 6-3. 기타 : 배치별 (B2B - 상품별, B2C - 주문별) 셀에 박싱 되지 않은 분류 처리할 남은 수량 계산
	 * 
	 * @param job
	 * @return
	 */
	public int totalPickQtyByJob(JobInstance job);
	
	/**
	 * 6-4. 박스 입수 수량, 박스 수량, 낱개 수량으로 총 낱개 수량을 계산 
	 * 
	 * @param boxInQty
	 * @param boxQty 박스 수량 
	 * @param pcsQty 낱개 수량
	 * @return
	 */
	public int toPcsQty(Integer boxInQty, Integer boxQty, Integer pcsQty);
	
	/**
	 * 6-5. 박스 입수 수량, 박스 수량, 낱개 수량으로 총 낱개 수량을 계산
	 * 
	 * @param job
	 * @param boxQty
	 * @param pcsQty
	 * @return
	 */
	public int toPcsQty(JobInstance job, Integer boxQty, Integer pcsQty);

}
