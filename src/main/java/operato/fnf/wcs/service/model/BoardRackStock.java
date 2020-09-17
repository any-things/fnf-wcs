package operato.fnf.wcs.service.model;

public class BoardRackStock {
	private String whCd;
	private String zoneCd;
	private String wcellNo;
	private String location;
	private String itemCd;
	private String itemNm;
	private String itemGcd;
	private String itemGnm;
	private String brand;
	private String season;
	private String color;
	private String style;
	private String size;
	private Float spaceCbm;
	private Float usedCbm;
	private Float usedRate;
	private Float boxQty;
	private Float pcsQty;
	
	private String velocity;	// 출고빈도: A, B, C
	private Float erpSaleRate = 0f;	// 판매율
	
	public String getWhCd() {
		return whCd;
	}
	public void setWhCd(String whCd) {
		this.whCd = whCd;
	}
	public String getZoneCd() {
		return zoneCd;
	}
	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}
	public String getWcellNo() {
		return wcellNo;
	}
	public void setWcellNo(String wcellNo) {
		this.wcellNo = wcellNo;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getItemCd() {
		return itemCd;
	}
	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}
	public String getItemNm() {
		return itemNm;
	}
	public void setItemNm(String itemNm) {
		this.itemNm = itemNm;
	}
	public String getItemGcd() {
		return itemGcd;
	}
	public void setItemGcd(String itemGcd) {
		this.itemGcd = itemGcd;
	}
	public String getItemGnm() {
		return itemGnm;
	}
	public void setItemGnm(String itemGnm) {
		this.itemGnm = itemGnm;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getSeason() {
		return season;
	}
	public void setSeason(String season) {
		this.season = season;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public Float getSpaceCbm() {
		return spaceCbm;
	}
	public void setSpaceCbm(Float spaceCbm) {
		this.spaceCbm = spaceCbm;
	}
	public Float getUsedCbm() {
		return usedCbm;
	}
	public void setUsedCbm(Float usedCbm) {
		this.usedCbm = usedCbm;
	}
	public Float getUsedRate() {
		return usedRate;
	}
	public void setUsedRate(Float usedRate) {
		this.usedRate = usedRate;
	}
	public Float getBoxQty() {
		return boxQty;
	}
	public void setBoxQty(Float boxQty) {
		this.boxQty = boxQty;
	}
	public Float getPcsQty() {
		return pcsQty;
	}
	public void setPcsQty(Float pcsQty) {
		this.pcsQty = pcsQty;
	}
	public String getVelocity() {
		return velocity;
	}
	public void setVelocity(String velocity) {
		this.velocity = velocity;
	}
	public Float getErpSaleRate() {
		return erpSaleRate;
	}
	public void setErpSaleRate(Float erpSaleRate) {
		this.erpSaleRate = erpSaleRate;
	}
}
