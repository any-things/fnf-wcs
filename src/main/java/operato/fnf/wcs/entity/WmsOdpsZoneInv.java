package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "dps_inventory"
	, ignoreDdl = true
	, idStrategy = GenerationRule.NONE
	, dataSourceType=DataSourceType.DATASOURCE)
public class WmsOdpsZoneInv extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 164374134160236093L;

	@Column (name = "wh_cd")
	private String whCd;

	@Column (name = "strr_id")
	private String strrId;

	@Column (name = "item_cd")
	private String itemCd;

	@Column (name = "invn_qty")
	private Integer invnQty;
  
	public String getWhCd() {
		return whCd;
	}

	public void setWhCd(String whCd) {
		this.whCd = whCd;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public Integer getInvnQty() {
		return invnQty;
	}

	public void setInvnQty(Integer invnQty) {
		this.invnQty = invnQty;
	}	
}
