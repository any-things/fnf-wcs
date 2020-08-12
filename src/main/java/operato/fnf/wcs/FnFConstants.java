package operato.fnf.wcs;

/**
 * FNF 관련 상수
 * 
 * @author shortstop
 */
public class FnFConstants {
	/**
	 * FnF 이천 물류 센터 코드
	 */
	public static final String WH_CD_ICF = "ICF";
	/**
	 * FnF 화주사 코드
	 */
	public static final String FNF_COM_CD = "FnF";
	/**
	 * 주문 전체 취소
	 */
	public static final String ORDER_CANCEL_ALL = "CANCEL_ALL";
	
	/**
	 * 송장 발행 코드 - 주문 전체 취소 
	 */
	public static final String INVOICE_RES_CODE_ORDER_CANCEL_ALL = "MSG_FNF159_1";
	
	/**
	 * 박스 투입 시 박스 분할해서 투입해야 할 주문 수량
	 */
	public static final String BOX_INPUT_SPLIT_BOX_PCS = "fnf.box.input.split.pcs";
	
	/**
	 * PAS 배치 가동준비완료 상태
	 */
	public static final String PAS_BATCH_READY = "0";
	
	/**
	 * PAS 배치 개시 상태
	 */
	public static final String PAS_BATCH_RUNNING = "1";
	
	/**
	 * PAS 배치 종료 상태
	 */
	public static final String PAS_BATCH_STOP = "2";
	
	/**
	 * PAS 배치 일시정지 상태
	 */
	public static final String PAS_BATCH_PAUSE = "3";
	
	/**
	 * SMS ORDER 수신 방식 - WMS
	 */
	public static final String ORDER_RECEIVE_WMS = "W";
	
	/**
	 * SMS ORDER 수신 방식 - UPLOAD
	 */
	public static final String ORDER_RECEIVE_UPLOAD = "U";
	/**
	 * SMS UPLOAD ORDER SEASON - S
	 */
	public static final String UPLOAD_ORDER_SEASON = "S";
}
