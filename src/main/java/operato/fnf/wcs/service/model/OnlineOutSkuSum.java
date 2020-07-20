package operato.fnf.wcs.service.model;

public class OnlineOutSkuSum {
	private String workDate;
	private String itemCd;
	private Integer outQty;
	private Integer outCnt;
	
	public String getWorkDate() {
		return workDate;
	}
	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}
	public String getItemCd() {
		return itemCd;
	}
	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}
	public Integer getOutQty() {
		return outQty;
	}
	public void setOutQty(Integer outQty) {
		this.outQty = outQty;
	}
	public Integer getOutCnt() {
		return outCnt;
	}
	public void setOutCnt(Integer outCnt) {
		this.outCnt = outCnt;
	}
}
