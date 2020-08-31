package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "dps_today_performance", ignoreDdl = true, idStrategy = GenerationRule.UUID)
public class DpsTodayPerformance extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 250739559752625422L;

	@Column (name = "wh_cd", length = 32)
	private String whCd;

	@Column (name = "outb_ect_ymd", length = 32)
	private String outbEctYmd;

	@Column (name = "ref_no", length = 32)
	private String refNo;

	@Column (name = "strr_id", length = 32)
	private String strrId;

	@Column (name = "item_cd", length = 32)
	private String itemCd;

	@Column (name = "item_season", length = 32)
	private String itemSeason;

	@Column (name = "item_color", length = 32)
	private String itemColor;

	@Column (name = "item_size", length = 32)
	private String itemSize;

	@Column (name = "order_qty")
	private Integer orderQty;

	@Column (name = "done_qty")
	private Integer doneQty;

	@Column (name = "pack_tcd", length = 32)
	private String packTcd;
  
	public String getWhCd() {
		return whCd;
	}

	public void setWhCd(String whCd) {
		this.whCd = whCd;
	}

	public String getOutbEctYmd() {
		return outbEctYmd;
	}

	public void setOutbEctYmd(String outbEctYmd) {
		this.outbEctYmd = outbEctYmd;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
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

	public String getItemSeason() {
		return itemSeason;
	}

	public void setItemSeason(String itemSeason) {
		this.itemSeason = itemSeason;
	}

	public String getItemColor() {
		return itemColor;
	}

	public void setItemColor(String itemColor) {
		this.itemColor = itemColor;
	}

	public String getItemSize() {
		return itemSize;
	}

	public void setItemSize(String itemSize) {
		this.itemSize = itemSize;
	}

	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}

	public Integer getDoneQty() {
		return doneQty;
	}

	public void setDoneQty(Integer doneQty) {
		this.doneQty = doneQty;
	}

	public String getPackTcd() {
		return packTcd;
	}

	public void setPackTcd(String packTcd) {
		this.packTcd = packTcd;
	}	
}
