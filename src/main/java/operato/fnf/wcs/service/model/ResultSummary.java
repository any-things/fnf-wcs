package operato.fnf.wcs.service.model;

/**
 * 10분대별 실적 서머리 모델
 * 
 * @author shortstop
 */
public class ResultSummary {

	/**
	 * 작업 배치 ID
	 */
	private String batchId;
	/**
	 * 작업 일자
	 */
	private String workDate;
	/**
	 * 작업 시간대
	 */
	private String hour;
	/**
	 * 작업 분대 (10, 20, 30, 40, 50, 60)
	 */
	private String minute;
	/**
	 * 실적 수량 
	 */
	private Integer pickedQty;
	
	public String getBatchId() {
		return batchId;
	}
	
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	
	public String getWorkDate() {
		return workDate;
	}
	
	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}
	
	public String getHour() {
		return hour;
	}
	
	public void setHour(String hour) {
		this.hour = hour;
	}
	
	public String getMinute() {
		return minute;
	}
	
	public void setMinute(String minute) {
		this.minute = minute;
	}
	
	public Integer getPickedQty() {
		return pickedQty;
	}
	
	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

}
