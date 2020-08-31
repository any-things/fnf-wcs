package operato.fnf.wcs.service.model;

import operato.fnf.wcs.entity.DpsTodayPerformance;

@SuppressWarnings("serial")
public class DpsOrderDetail extends DpsTodayPerformance {
	private String itemGroup;

	public String getItemGroup() {
		return itemGroup;
	}

	public void setItemGroup(String itemGroup) {
		this.itemGroup = itemGroup;
	}
}
