package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Table;

/**
 * 출력 송장 박스 내품 조회를 위한 WMS I/F 테이블
 */
@Table(name = "mps_express_waybill_packinfo", idStrategy = GenerationRule.UUID, dataSourceType=DataSourceType.DATASOURCE)
public class WmsExpressWaybillPackinfo extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -5487516093806045871L;

	@Ignore
	private String id;
	
	@Column (name = "wh_cd", length = 50)
	private String whCd;

	@Column (name = "box_id", length = 50)
	private String boxId;

	@Column (name = "strr_id", length = 50)
	private String carrierTcd;

	@Column (name = "item_season", length = 10)
	private String itemSeason;
	
	@Column (name = "item_style", length = 30)
	private String itemStyle;
	
	@Column (name = "item_color", length = 10)
	private String itemColor;
	
	@Column (name = "item_size", length = 10)
	private String itemSize;

	@Column (name = "item_cd", length = 30)
	private String itemCd;

	@Column (name = "item_nm", length = 30)
	private String itemNm;
	
	@Column (name = "qty")
	private Integer qty;

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

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getCarrierTcd() {
		return carrierTcd;
	}

	public void setCarrierTcd(String carrierTcd) {
		this.carrierTcd = carrierTcd;
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

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getItemNm() {
		return itemNm;
	}

	public void setItemNm(String itemNm) {
		this.itemNm = itemNm;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

}
