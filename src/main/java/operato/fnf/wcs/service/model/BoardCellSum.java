package operato.fnf.wcs.service.model;

import java.util.ArrayList;
import java.util.List;

public class BoardCellSum {
	private String location;
	private Integer cellItemCount = 0;
	private List<String> skuCds = new ArrayList<>();
	private Float capacity = 0f;
	private Float used = 0f;
	private Float cellUsedRate = 0f;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Integer getCellItemCount() {
		return cellItemCount;
	}
	public void setCellItemCount(Integer cellItemCount) {
		this.cellItemCount = cellItemCount;
	}
	public List<String> getSkuCds() {
		return skuCds;
	}
	public void setSkuCds(List<String> skuCds) {
		this.skuCds = skuCds;
	}
	public void addSkuCd(String skuCd) {
		this.skuCds.add(skuCd);
		
		this.cellItemCount = this.skuCds.size();
	}
	public Float getCapacity() {
		return capacity;
	}
	public void setCapacity(Float capacity) {
		this.capacity = capacity;
	}
	public Float getUsed() {
		return used;
	}
	public void setUsed(Float used) {
		this.used = used;
		this.calcUsedRate();
	}
	public Float getCellUsedRate() {
		return cellUsedRate;
	}
	public void setCellUsedRate(Float cellUsedRate) {
		this.cellUsedRate = cellUsedRate;
	}
	public void calcUsedRate() {
		this.cellUsedRate = this.used / this.capacity * 100;
	}
}
