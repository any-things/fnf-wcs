package xyz.anythings.base.service.api;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.event.ICategorizeEvent;
import xyz.anythings.base.event.IClassifyErrorEvent;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.IClassifyOutEvent;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.base.model.Category;

/**
 * 분류 공통 (Picking & Assorting) 트랜잭션 서비스 API
 * 
 * 	1. 분류 모듈 정보
 * 		1) 작업 유형 
 * 		2) 작업 배치별 표시기 설정 정보
 * 		3) 작업 배치별 작업 설정 정보
 *  2. 중분류
 *  	1) 중분류
 * 	3. 투입
 * 		1) 스캔한 ID가 투입 유형에 따라 유효한 지 체크 및 어떤 투입 유형인 지 판단
 * 		2) 분류 작업을 위한 투입
 * 	4. 소분류 처리
 * 		1) 분류 작업 처리
 * 		2) Out 처리
 *  5. 기타
 *  	1) 모든 배치 작업이 완료되었는지 여부
 *  	2) 배치 시작 시 추가 처리
 *  	3) 배치 마감 시 추가 처리
 *  	4) 분류 처리 에러 시 예외 처리
 * 
 * @author shortstop
 */
public interface IClassificationService {

	/**
	 * 1-1. 분류 모듈 정보 : 분류 서비스 모듈의 작업 유형 (DAS, RTN, DPS, QPS) 리턴 
	 * 
	 * @return
	 */
	public String getJobType();
	
	/**
	 * 1-2. 분류 모듈 정보 : 작업 배치별 작업 설정 정보
	 * 
	 * @param batchId
	 * @return
	 */
	public JobConfigSet getJobConfigSet(String batchId);
		
	/**
	 * 2-1. 중분류 이벤트
	 *  
	 * @param event
	 * @return
	 */
	public Category categorize(ICategorizeEvent event);
	
	/**
	 * 3-1. 셀에 분류 코드를 매핑
	 * 
	 * @param batch
	 * @param cellCd
	 * @param classCd
	 * @param params
	 * @return
	 */
	public Object classCellMapping(JobBatch batch, String cellCd, String classCd, Object ... params);
		
	/**
	 * 셀에 공 박스를 매핑
	 * 
	 * @param batch
	 * @param cellCd
	 * @param boxId
	 * @return
	 */
	public Object boxCellMapping(JobBatch batch, String cellCd, String boxId);
		
	/**
	 * 3-1. 투입 ID로 유효성 체크 및 투입 유형을 찾아서 리턴 
	 * 
	 * @param batch
	 * @param inputId
	 * @param params
	 * @return LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_...
	 */
	public String checkInput(JobBatch batch, String inputId, Object ... params);
	
	/**
	 * 3-2. 분류 설비에 투입 처리
	 * 
	 * @param inputEvent
	 * @return
	 */
	public Object input(IClassifyInEvent inputEvent);
	
	/**
	 * 4-1. 소분류 : 분류 처리 작업
	 * 
	 * @param exeEvent 분류 처리 이벤트
	 * @return
	 */
	public Object classify(IClassifyRunEvent exeEvent);
	
	/**
	 * 4-2. 소분류 : 분류 처리 결과 처리 (DAS, DPS, 반품 - 풀 박스 처리 후 호출, 소터 - 단위 상품별 분류 처리 시 I/F로 넘어온 후 호출)
	 * 
	 * @param outputEvent
	 * @return
	 */
	public Object output(IClassifyOutEvent outputEvent);
	
	/**
	 * 5-1. 기타 : 배치 내 모든 분류 작업이 완료되었는지 여부 
	 * 
	 * @param batch
	 * @return
	 */
	public boolean checkEndClassifyAll(JobBatch batch);
		
	/**
	 * 5-2. 기타 : 분류 서비스 모듈별 작업 시작 중 추가 처리
	 * 
	 * @param batch
	 */
	public void batchStartAction(JobBatch batch);
	
	/**
	 * 5-3. 기타 : 분류 서비스 모듈별 작업 마감 중 추가 처리
	 * 
	 * @param batch
	 */
	public void batchCloseAction(JobBatch batch);
	
	/**
	 * 5-4. 기타 : 분류 작업 처리시 에러 핸들링
	 * 
	 * @param errorEvent
	 */
	public void handleClassifyException(IClassifyErrorEvent errorEvent);
	
}
