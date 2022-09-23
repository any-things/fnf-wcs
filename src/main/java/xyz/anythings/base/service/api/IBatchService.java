package xyz.anythings.base.service.api;

import xyz.anythings.base.entity.JobBatch;

/**
 * 배치 작업 서비스 API
 *  1. 스테이지, 일자, 설비별 등의 조건으로 진행 중인 배치 조회
 *  2. 배치 마감, 배치 그룹 마감
 * 
 * @author shortstop
 */
public interface IBatchService {
		
	/**
	 * 작업 배치 마감이 가능한 지 여부 체크 
	 * 
	 * @param batch 작업 배치
	 * @param closeForcibly 강제 마감 여부
	 * @return 작업 배치 마감 가능 여부
	 */
	public void isPossibleCloseBatch(JobBatch batch, boolean closeForcibly);
	
	/**
	 * 작업 배치 작업 마감 
	 * 
	 * @param batch 작업 배치
	 * @param forcibly 강제 종료 여부
	 */
	public void closeBatch(JobBatch batch, boolean forcibly);
	
	/**
	 * 배치 그룹 마감이 가능한 지 여부 체크 
	 * 
	 * @param domainId 도메인 ID
	 * @param batchGroupId 작업 배치 그룹 ID
	 * @param closeForcibly 강제 마감 여부
	 * @return 작업 배치 마감 가능 여부
	 */
	public void isPossibleCloseBatchGroup(Long domainId, String batchGroupId, boolean closeForcibly);
	
	/**
	 * 배치 그룹 마감 
	 * 
	 * @param domainId 도메인 ID
	 * @param batchGroupId 작업 배치 그룹 ID
	 * @param forcibly 강제 종료 여부
	 * @return 마감된 배치 수
	 */
	public int closeBatchGroup(Long domainId, String batchGroupId, boolean forcibly);
	
	/**
	 * 작업 배치 취소가 가능한 지 여부 체크 
	 * 
	 * @param batch 작업 배치
	 * @return 작업 배치 마감 가능 여부
	 */
	public void isPossibleCancelBatch(JobBatch batch);
	
	/**
	 * 작업 배치 호기 전환이 가능한 지 여부 체크 
	 * 
	 * @param batch 작업 배치
	 * @param toEquipCd 전환할 호기
	 */
	public void isPossibleChangeEquipment(JobBatch batch, String toEquipCd);
	
	/**
	 * 배치 호기 전환
	 * 
	 * @param batch 작업 배치
	 * @param toEquipCd 전환할 호기
	 */
	public void changeEquipment(JobBatch batch, String toEquipCd);	
}
