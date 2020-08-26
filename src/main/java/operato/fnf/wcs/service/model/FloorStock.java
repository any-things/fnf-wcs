package operato.fnf.wcs.service.model;

public class FloorStock {
	private String floorTcd;
	private Long totalCbm;
	private Long usedCbm;
	private Long pcsQty;
	private Long avaCbm;
	private Long usedRate;
	
	public String getFloorTcd() {
		return floorTcd;
	}
	public void setFloorTcd(String floorTcd) {
		this.floorTcd = floorTcd;
	}
	public Long getTotalCbm() {
		return totalCbm;
	}
	public void setTotalCbm(Long totalCbm) {
		this.totalCbm = totalCbm;
	}
	public Long getUsedCbm() {
		return usedCbm;
	}
	public void setUsedCbm(Long usedCbm) {
		this.usedCbm = usedCbm;
	}
	public Long getPcsQty() {
		return pcsQty;
	}
	public void setPcsQty(Long pcsQty) {
		this.pcsQty = pcsQty;
	}
	public Long getAvaCbm() {
		return avaCbm;
	}
	public void setAvaCbm(Long avaCbm) {
		this.avaCbm = avaCbm;
	}
	public Long getUsedRate() {
		return usedRate;
	}
	public void setUsedRate(Long usedRate) {
		this.usedRate = usedRate;
	}
}
