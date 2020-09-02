package operato.fnf.wcs.service.model;

public class BoardRackStock {
	private String whCd;
	private String zoneCd;
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
	private Integer spaceCbm;
	private Float usedRate;
	private Integer boxQty;
	private Integer pcsQty;
	
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
	public Integer getSpaceCbm() {
		return spaceCbm;
	}
	public void setSpaceCbm(Integer spaceCbm) {
		this.spaceCbm = spaceCbm;
	}
	public Float getUsedRate() {
		return usedRate;
	}
	public void setUsedRate(Float usedRate) {
		this.usedRate = usedRate;
	}
	public Integer getBoxQty() {
		return boxQty;
	}
	public void setBoxQty(Integer boxQty) {
		this.boxQty = boxQty;
	}
	public Integer getPcsQty() {
		return pcsQty;
	}
	public void setPcsQty(Integer pcsQty) {
		this.pcsQty = pcsQty;
	}
}
