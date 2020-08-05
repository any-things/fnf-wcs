package operato.fnf.wcs.service.model;

public class DpsProductivity implements Comparable<DpsProductivity> {
	private String workDate;
	private String strrId;
	private String packTcd;
	private String workType;
	private String hourMin;
	private Integer refNoCnt;
	private Integer itemCdCnt;
	private Integer doneQty;
	private float workers;
	private float workMinutes;
	private float workHours;
	private float ph;
	private float php;
	private Integer seq;
	
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
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
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
	public float getWorkers() {
		return workers;
	}
	public void setWorkers(float workers) {
		this.workers = workers;
	}
	public float getWorkMinutes() {
		return workMinutes;
	}
	public void setWorkMinutes(float workMinutes) {
		this.workMinutes = workMinutes;
	}
	public float getWorkHours() {
		return workHours;
	}
	public void setWorkHours(float workHours) {
		this.workHours = workHours;
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
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	@Override
	public int compareTo(DpsProductivity o) {
		int i = (this.getWorkDate() + this.getSeq() + this.getWorkType())
				.compareTo(o.getWorkDate() + this.getSeq() + o.getWorkType());
		return i;
	}
}
