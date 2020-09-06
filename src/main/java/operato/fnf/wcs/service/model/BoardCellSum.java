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
}
