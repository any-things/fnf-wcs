package operato.fnf.wcs.service.model;

public class DpsProductivity {
	private String workDate;
	private String strrId;
	private String packTcd;
	private String hourMin;
	private Integer refNoCnt;
	private Integer itemCdCnt;
	private Integer doneQty;
	private Integer workers;
	private Integer workMinutes;
	private Integer workHours;
	private float ph;
	private float php;
	
	public String getWorkDate() {
		return workDate;
	}
	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}
	public String getStrrId() {
		return strrId;
	}
	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}
	public String getPackTcd() {
		return packTcd;
	}
	public void setPackTcd(String packTcd) {
		this.packTcd = packTcd;
	}
	public String getHourMin() {
		return hourMin;
	}
	public void setHourMin(String hourMin) {
		this.hourMin = hourMin;
	}
	public Integer getRefNoCnt() {
		return refNoCnt;
	}
	public void setRefNoCnt(Integer refNoCnt) {
		this.refNoCnt = refNoCnt;
	}
	public Integer getItemCdCnt() {
		return itemCdCnt;
	}
	public void setItemCdCnt(Integer itemCdCnt) {
		this.itemCdCnt = itemCdCnt;
	}
	public Integer getDoneQty() {
		return doneQty;
	}
	public void setDoneQty(Integer doneQty) {
		this.doneQty = doneQty;
	}
	public Integer getWorkers() {
		return workers;
	}
	public void setWorkers(Integer workers) {
		this.workers = workers;
	}
	public Integer getWorkMinutes() {
		return workMinutes;
	}
	public void setWorkMinutes(Integer workMinutes) {
		this.workMinutes = workMinutes;
	}
	public Integer getWorkHours() {
		return workHours;
	}
	public void setWorkHours(Integer workHours) {
		this.workHours = workHours;
	}
	public float getPh() {
		return ph;
	}
	public void setPh(float ph) {
		this.ph = ph;
	}
	public float getPhp() {
		return php;
	}
	public void setPhp(float php) {
		this.php = php;
	}
}
