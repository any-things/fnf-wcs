package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Table;

/**
 * WMS 온라인 주문 취소 수량 가감, 현재 주문 출고 오더 정보
 */
@Table(name = "dps_actual_order", ignoreDdl = true, idStrategy = GenerationRule.UUID, dataSourceType=DataSourceType.DATASOURCE)
public class WmsDpsActualOrder extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 2300297509181542667L;

	@Ignore
	private String id;
	
	@Column (name = "wh_cd", length = 50)
	private String whCd;

	@Column (name = "ref_no", length = 50)
	private String refNo;

	@Column (name = "strr_id", length = 50)
	private String strrId;
	
	@Column (name = "item_cd", length = 30)
	private String itemCd;

	@Column (name = "item_season", length = 10)
	private String itemSeason;
	
	@Column (name = "item_style", length = 30)
	private String itemStyle;
	
	@Column (name = "item_color", length = 10)
	private String itemColor;
	
	@Column (name = "item_size", length = 10)
	private String itemSize;

	/**
	 * 출고 예정 주문 수량
	 */
	@Column (name = "outb_ect_qty", length = 10)
	private Integer outbEctQty;
	
	/**
	 * 출고 대상 수량 (취소 수량 가감)
	 */
	@Column (name = "to_pick_qty", length = 10)
	private Integer toPickQty;
	
	/**
	 * 완료된 수량
	 */
	@Column (name = "done_qty", length = 10)
	private Integer doneQty;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWhCd() {
		return whCd;
	}

	public void setWhCd(String whCd) {
		this.whCd = whCd;
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

	public String getItemStyle() {
		return itemStyle;
	}

	public void setItemStyle(String itemStyle) {
		this.itemStyle = itemStyle;
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

	public Integer getOutbEctQty() {
		return outbEctQty;
	}

	public void setOutbEctQty(Integer outbEctQty) {
		this.outbEctQty = outbEctQty;
	}

	public Integer getToPickQty() {
		return toPickQty;
	}

	public void setToPickQty(Integer toPickQty) {
		this.toPickQty = toPickQty;
	}

	public Integer getDoneQty() {
		return doneQty;
	}

	public void setDoneQty(Integer doneQty) {
		this.doneQty = doneQty;
	}

}
