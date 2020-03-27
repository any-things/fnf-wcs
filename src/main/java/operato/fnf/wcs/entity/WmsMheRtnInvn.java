package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 반품 분류 대기 재고 정보
 *  - 반품 분류 대기 재고 내역을 장비에서 수신하여 배치 생성시 활용
 *  - WMS => WCS
 * @author yang
 *
 */
@Table(name = "mhe_rtn_invn", idStrategy = GenerationRule.UUID
     , dataSourceType=DataSourceType.DATASOURCE 
     , uniqueFields="whCd,strrId,itemCd", indexes = {
})
public class WmsMheRtnInvn extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 3768428030881103133L;


	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;

	@PrimaryKey
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;

	@PrimaryKey
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;
	
	@Column (name = "item_gcd", length = 30)
	private String itemGcd;

	@Column (name = "item_gcd_nm", length = 100)
	private String itemGcdNm;
	
	@Column (name = "item_season", length = 10)
	private String itemSeason;
	
	@Column (name = "item_style", length = 30)
	private String itemStyle;
	
	@Column (name = "item_color", length = 10)
	private String itemColor;
	
	@Column (name = "item_size", length = 10)
	private String itemSize;
	
	@Column (name = "invn_qty", length = 10)
	private Integer invnQty;
	
	@Column (name = "barcode", length = 20)
	private String barcode;

	@Column (name = "barcode2", length = 20)
	private String barcode2;

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

	public String getItemGcd() {
		return itemGcd;
	}

	public void setItemGcd(String itemGcd) {
		this.itemGcd = itemGcd;
	}

	public String getItemGcdNm() {
		return itemGcdNm;
	}

	public void setItemGcdNm(String itemGcdNm) {
		this.itemGcdNm = itemGcdNm;
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

	public Integer getInvnQty() {
		return invnQty;
	}

	public void setInvnQty(Integer invnQty) {
		this.invnQty = invnQty;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getBarcode2() {
		return barcode2;
	}

	public void setBarcode2(String barcode2) {
		this.barcode2 = barcode2;
	}
	
	
}
