package operato.fnf.wcs.service.model;

public class FloorStock {
	private String floorTcd;
	private Integer totalCbm;
	private Integer usedCbm;
	private Integer pcsQty;
	private Integer avaCbm;
	private Integer usedRate;
	
	public String getFloorTcd() {
		return floorTcd;
	}
	public void setFloorTcd(String floorTcd) {
		this.floorTcd = floorTcd;
	}
	public Integer getTotalCbm() {
		return totalCbm;
	}
	public void setTotalCbm(Integer totalCbm) {
		this.totalCbm = totalCbm;
	}
	public Integer getUsedCbm() {
		return usedCbm;
	}
	public void setUsedCbm(Integer usedCbm) {
		this.usedCbm = usedCbm;
	}
	public Integer getPcsQty() {
		return pcsQty;
	}
	public void setPcsQty(Integer pcsQty) {
		this.pcsQty = pcsQty;
	}
	public Integer getAvaCbm() {
		return avaCbm;
	}
	public void setAvaCbm(Integer avaCbm) {
		this.avaCbm = avaCbm;
	}
	public Integer getUsedRate() {
		return usedRate;
	}
	public void setUsedRate(Integer usedRate) {
		this.usedRate = usedRate;
	}
}
