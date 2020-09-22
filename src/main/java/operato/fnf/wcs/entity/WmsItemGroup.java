package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "vms_item_group", ignoreDdl = true, dataSourceType=DataSourceType.DATASOURCE)
public class WmsItemGroup extends xyz.elidom.orm.entity.basic.AbstractStamp {
	private static final long serialVersionUID = -7012780399286209661L;
	
	@Column (name = "item_gcd", length = 50)
	private String itemGcd;

	@Column (name = "itemgrp_snm", length = 100)
	private String itemgrpSnm;

	public String getItemGcd() {
		return itemGcd;
	}

	public void setItemGcd(String itemGcd) {
		this.itemGcd = itemGcd;
	}

	public String getItemgrpSnm() {
		return itemgrpSnm;
	}

	public void setItemgrpSnm(String itemgrpSnm) {
		this.itemgrpSnm = itemgrpSnm;
	}
}
