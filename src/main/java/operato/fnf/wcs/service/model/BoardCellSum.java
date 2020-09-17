package operato.fnf.wcs.service.model;

import java.util.ArrayList;
import java.util.List;

public class BoardCellSum {
	private String location;
	private Integer cellItemCount = 0;
	private List<BoardRackStock> items = new ArrayList<>();
	private Float capacity = 0f;
	private Float used = 0f;
	private Float usedRate = 0f;
	private String velocity = " ";	// 출고빈도: A, B, C
	private Float erpSaleRate = 0f;	// 판매율
	private Float cellPcsQty = 0f;	// pcs수
	
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
	public List<BoardRackStock> getItems() {
		return items;
	}
	public void setItems(List<BoardRackStock> items) {
		this.items = items;
	}
	public void addItems(BoardRackStock obj) {
		this.items.add(obj);
		
		this.cellItemCount = this.items.size();
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
	public Float getUsedRate() {
		return usedRate;
	}
	public void setUsedRate(Float usedRate) {
		this.usedRate = usedRate;
	}
	public void setUsed(Float used) {
		this.used = used;
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
	public Float getCellPcsQty() {
		return cellPcsQty;
	}
	public void setCellPcsQty(Float cellPcsQty) {
		this.cellPcsQty = cellPcsQty;
	}
}
