package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * 상품 마스터 바코드 정보 (View)
 * 상품 마스터 정보 총괄 (입고 컨베이어, 반품EX-PAS)
 */
@Table(name = "mhe_item_barcode"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
	, dataSourceType=DataSourceType.DATASOURCE
	, uniqueFields="itemCd")
public class WmsMheItemBarcode extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3205507953948255097L;

	@Ignore
	private String id;

	@Column (name = "brand", nullable = true, length = 20)
	private String brand;
	
	@PrimaryKey
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;

	@Column (name = "barcode", nullable = true, length = 20)
	private String barcode;
	
	@Column (name = "barcode2", nullable = true, length = 20)
	private String barcode2;
	
	@Column (name = "item_season", nullable = true, length = 10)
	private String itemSeason;
	
	@Column (name = "item_style", nullable = true, length = 30)
	private String itemStyle;
	
	@Column (name = "item_color", nullable = true, length = 10)
	private String itemColor;
	
	@Column (name = "item_size", nullable = true, length = 10)
	private String itemSize;
	
	@Column (name = "item_gcd", nullable = true, length = 30)
	private String itemGcd;
	
	@Column (name = "item_gcd_nm", nullable = true, length = 100)
	private String itemGcdNm;
	
	@Column (name = "floor_cd", nullable = true, length = 10)
	private String floorCd;
	
	//@Column (name = "rfid_item_yn", nullable = true, length = 1)
	@Ignore
	private String rfidItemYn;
	
	@Column (name = "upd_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date updDatetime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
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

	public String getFloorCd() {
		return floorCd;
	}

	public void setFloorCd(String floorCd) {
		this.floorCd = floorCd;
	}

	public String getRfidItemYn() {
		return rfidItemYn;
	}

	public void setRfidItemYn(String rfidItemYn) {
		this.rfidItemYn = rfidItemYn;
	}

	public Date getUpdDatetime() {
		return updDatetime;
	}

	public void setUpdDatetime(Date updDatetime) {
		this.updDatetime = updDatetime;
	}

}
