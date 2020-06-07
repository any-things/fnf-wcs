package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 반품지시 차수 대상 SKU 정보
 * 반품검수 EX-PAS
 * 
 * @author yang
 */
@Table(name = "wcs_rtn_chasu_sku"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
    , dataSourceType=DataSourceType.DATASOURCE 
    , uniqueFields="strrId,season,type,seq,itemCd")
public class WmsWcsRtnChasuSku extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3601219852210491698L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "strr_id", nullable = false, length = 2)
	private String strrId;
	
	@PrimaryKey
	@Column (name = "season", nullable = false, length = 4)
	private String season;
	
	@PrimaryKey
	@Column (name = "type", nullable = false, length = 3)
	private String type;
	
	@PrimaryKey
	@Column (name = "seq", nullable = false, length = 10)
	private Integer seq;
	
	@Column (name = "partcode", nullable = false, length = 18)
	private String partcode;
	
	@PrimaryKey
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;
	
	@Column (name = "item_season", nullable = true, length = 10)
	private String itemSeason;
	
	@Column (name = "item_gcd", nullable = true, length = 30)
	private String itemGcd;
	
	@Column (name = "item_style", nullable = true, length = 30)
	private String itemStyle;
	
	@Column (name = "item_color", nullable = true, length = 10)
	private String itemColor;

	@Column (name = "item_size", nullable = true, length = 10)
	private String itemSize;

	@Column (name = "barcode1", nullable = true, length = 20)
	private String barcode1;

	@Column (name = "barcode2", nullable = true, length = 20)
	private String barcode2;

	@Column (name = "exp_qty", nullable = false, length = 10)
	private Integer expQty;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getSeason() {
		return season;
	}

	public void setSeason(String season) {
		this.season = season;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getPartcode() {
		return partcode;
	}

	public void setPartcode(String partcode) {
		this.partcode = partcode;
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

	public String getItemGcd() {
		return itemGcd;
	}

	public void setItemGcd(String itemGcd) {
		this.itemGcd = itemGcd;
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

	public String getBarcode1() {
		return barcode1;
	}

	public void setBarcode1(String barcode1) {
		this.barcode1 = barcode1;
	}

	public String getBarcode2() {
		return barcode2;
	}

	public void setBarcode2(String barcode2) {
		this.barcode2 = barcode2;
	}

	public Integer getExpQty() {
		return expQty;
	}

	public void setExpQty(Integer expQty) {
		this.expQty = expQty;
	}
}
