package operato.fnf.wcs.service.model;

public class DpsOrderDetail {
	private String outbEctYmd;
	private String refNo;
	private String strrId;
	private String itemCd;
	private Integer orderQty;
	private String packTcd;
	private Integer seq;
	private String itemGroup;
	
	public String getOutbEctYmd() {
		return outbEctYmd;
	}
	public void setOutbEctYmd(String outbEctYmd) {
		this.outbEctYmd = outbEctYmd;
	}
	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	public String getStrrId() {
		return strrId;
	}
	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}
	public String getItemCd() {
		return itemCd;
	}
	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}
	public Integer getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}
	public String getPackTcd() {
		return packTcd;
	}
	public void setPackTcd(String packTcd) {
		this.packTcd = packTcd;
	}
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	public String getItemGroup() {
		return itemGroup;
	}
	public void setItemGroup(String itemGroup) {
		this.itemGroup = itemGroup;
	}
}
