package operato.fnf.wcs.service.model;

public class OnlineOutSkuSum {
	private String workDate;
	private String itemCd;
	private Integer outPcsQty;
	private Integer outSkuTimes;
	private Integer outOrdCnt;
	
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
	public Integer getOutPcsQty() {
		return outPcsQty;
	}
	public void setOutPcsQty(Integer outPcsQty) {
		this.outPcsQty = outPcsQty;
	}
	public Integer getOutSkuTimes() {
		return outSkuTimes;
	}
	public void setOutSkuTimes(Integer outSkuTimes) {
		this.outSkuTimes = outSkuTimes;
	}
	public Integer getOutOrdCnt() {
		return outOrdCnt;
	}
	public void setOutOrdCnt(Integer outOrdCnt) {
		this.outOrdCnt = outOrdCnt;
	}
}
