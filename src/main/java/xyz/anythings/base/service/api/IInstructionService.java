package xyz.anythings.base.service.api;

import java.util.List;
import java.util.Map;

import xyz.anythings.base.entity.JobBatch;

/**
 * 작업 지시 서비스 API
 * 	1. 작업 지시를 위한 거래처 별 호기/로케이션 할당 정보 조회
 *  2. 작업 지시 - 주문 가공 정보로 부터 작업 지시 정보 생성
 *  3. 토탈 피킹
 *  4. 작업 병합 - 메인 작업 배치에서 선택한 작업 배치를 배치 병합
 *  5. 작업 취소 - 작업 지시한 내용을 취소
 * 
 * @author shortstop
 */
public interface IInstructionService {
	
	/**
	 * 작업 지시를 위한 거래처 별 호기/로케이션 할당 정보 조회
	 * 
	 * @param batch
	 * @param params
	 * @return
	 */
	public Map<String, Object> searchInstructionData(JobBatch batch, Object ... params);
		
	/**
	 * 작업 배치에 대한 작업 지시 생성 
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	public int instructBatch(JobBatch batch, List<String> equipIdList, Object ... params);
	
	/**
	 * 토털 피킹 지시
	 * 
	 * @param batch
	 * @param equipList
	 * @param params
	 * @return
	 */
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object ... params);
		
	/**
	 * 작업 병합 - 메인 작업 배치에서 선택한 작업 배치를 배치 병합 
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 * @return
	 */
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object ... params);		
	
	/**
	 * 작업 지시 취소 
	 * 
	 * @param batch
	 * @return
	 */
	public int cancelInstructionBatch(JobBatch batch);

}
