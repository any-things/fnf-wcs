package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mps_express_waybill_print", ignoreDdl = true, dataSourceType=DataSourceType.DATASOURCE)
public class WmsItemGroup extends xyz.elidom.orm.entity.basic.AbstractStamp {
	private static final long serialVersionUID = -7012780399286209661L;
	
	@Column (name = "item_gcd", length = 50)
	private String itemGcd;

	@Column (name = "itemgrp_snm", length = 100)
	private String itemgrpSnm;
}
