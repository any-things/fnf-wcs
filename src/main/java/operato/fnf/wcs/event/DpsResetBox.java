package operato.fnf.wcs.event;

import xyz.anythings.sys.event.model.SysEvent;

/**
 * DPS 주문의 박스 ID를 리셋하기 위한 이벤트
 * 
 * @author shortstop
 */
public class DpsResetBox extends SysEvent {

	/**
	 * 배치 ID
	 */
	private String batchId;
	/**
	 * 주문 번호 
	 */
	private String orderNo;
	/**
	 * 박스 ID
	 */
	private String boxId;
	
	public DpsResetBox(Long domainId, String batchId, String orderNo, String boxId) {
		super.domainId = domainId;
		this.batchId = batchId;
		this.orderNo = orderNo;
		this.boxId = boxId;
	}
	
	public DpsResetBox(Long domainId, String orderNo, String boxId) {
		super.domainId = domainId;
		this.orderNo = orderNo;
		this.boxId = boxId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

}
