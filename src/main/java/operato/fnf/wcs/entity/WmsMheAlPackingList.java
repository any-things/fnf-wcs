package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
/*
 * 오토라벨러 송장 발행
 * 오토라벨러 송장 발행 내역 조회
 */
@Table(name = "mhe_al_packing_list", idStrategy = GenerationRule.UUID
     , dataSourceType=DataSourceType.DATASOURCE 
     , uniqueFields="waybillNo,boxId,itemCd", indexes = {
    @Index(name = "mhe_al_packing_list_ix1", columnList = "waybill_no", unique = false),
    @Index(name = "mhe_al_packing_list_ix2", columnList = "box_id", unique = false)
})
public class WmsMheAlPackingList extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -343230908418838995L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "waybill_no", nullable = false, length = 30)
	private String waybillNo;
	
	@Column (name = "ship_center_no", nullable = true, length = 20)
	private String shipCenterNo;
	
	@Column (name = "ship_center_nm", nullable = true, length = 20)
	private String shipCenterNm;
	
	@PrimaryKey
	@Column (name = "box_id", nullable = false, length = 20)
	private String boxId;
	
	@Column (name = "strr_id", nullable = true, length = 20)
	private String strrId;
	
	@Column (name = "strr_nm", nullable = true, length = 100)
	private String strrNm;
	
	@Column (name = "shipto_id", nullable = true, length = 100)
	private String shiptoId;
	
	@Column (name = "shipto_nm", nullable = true, length = 100)
	private String shiptoNm;
	
	@Column (name = "shipo_addr", nullable = true, length = 1500)
	private String shipoAddr;
	
	@Column (name = "shipto_zip", nullable = true, length = 15)
	private String shiptoZip;
	
	@Column (name = "shipto_tel_no", nullable = true, length = 30)
	private String shiptoTelNo;
	
	@Column (name = "assort", nullable = true, length = 4000)
	private String assort;
	
	@PrimaryKey
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;
	
	@Column (name = "item_nm", nullable = true, length = 120)
	private String item_nm;
	
	@Column (name = "pick_qty", nullable = true, length = 10)
	private Integer pickQty;
	
	@Column (name = "item_style", nullable = true, length = 30)
	private String itemStyle;

	@Column (name = "item_color", nullable = true, length = 10)
	private String itemColor;

	@Column (name = "item_size", nullable = true, length = 10)
	private String itemSize;

	@Column (name = "carrier_tcd", nullable = true, length = 20)
	private String carrierTcd;

	@Column (name = "carrier_nm", nullable = true, length = 4000)
	private String carrierNm;

	@Column (name = "outb_ect_date", nullable = true, length = 10)
	private String outbEctDate;

	@Column (name = "outb_no", nullable = true, length = 20)
	private String outbNo;

	@Column (name = "ref_no", nullable = true, length = 30)
	private String refNo;
	
	@Column (name = "outb_tcd", nullable = true, length = 30)
	private String outbTcd;
	
	@Column (name = "outb_tcd_nm", nullable = true, length = 4000)
	private String outbTcdNm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	public String getShipCenterNo() {
		return shipCenterNo;
	}

	public void setShipCenterNo(String shipCenterNo) {
		this.shipCenterNo = shipCenterNo;
	}

	public String getShipCenterNm() {
		return shipCenterNm;
	}

	public void setShipCenterNm(String shipCenterNm) {
		this.shipCenterNm = shipCenterNm;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getStrrNm() {
		return strrNm;
	}

	public void setStrrNm(String strrNm) {
		this.strrNm = strrNm;
	}

	public String getShiptoId() {
		return shiptoId;
	}

	public void setShiptoId(String shiptoId) {
		this.shiptoId = shiptoId;
	}

	public String getShiptoNm() {
		return shiptoNm;
	}

	public void setShiptoNm(String shiptoNm) {
		this.shiptoNm = shiptoNm;
	}

	public String getShipoAddr() {
		return shipoAddr;
	}

	public void setShipoAddr(String shipoAddr) {
		this.shipoAddr = shipoAddr;
	}

	public String getShiptoZip() {
		return shiptoZip;
	}

	public void setShiptoZip(String shiptoZip) {
		this.shiptoZip = shiptoZip;
	}

	public String getShiptoTelNo() {
		return shiptoTelNo;
	}

	public void setShiptoTelNo(String shiptoTelNo) {
		this.shiptoTelNo = shiptoTelNo;
	}

	public String getAssort() {
		return assort;
	}

	public void setAssort(String assort) {
		this.assort = assort;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getItem_nm() {
		return item_nm;
	}

	public void setItem_nm(String item_nm) {
		this.item_nm = item_nm;
	}

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
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

	public String getCarrierTcd() {
		return carrierTcd;
	}

	public void setCarrierTcd(String carrierTcd) {
		this.carrierTcd = carrierTcd;
	}

	public String getCarrierNm() {
		return carrierNm;
	}

	public void setCarrierNm(String carrierNm) {
		this.carrierNm = carrierNm;
	}

	public String getOutbEctDate() {
		return outbEctDate;
	}

	public void setOutbEctDate(String outbEctDate) {
		this.outbEctDate = outbEctDate;
	}

	public String getOutbNo() {
		return outbNo;
	}

	public void setOutbNo(String outbNo) {
		this.outbNo = outbNo;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getOutbTcd() {
		return outbTcd;
	}

	public void setOutbTcd(String outbTcd) {
		this.outbTcd = outbTcd;
	}

	public String getOutbTcdNm() {
		return outbTcdNm;
	}

	public void setOutbTcdNm(String outbTcdNm) {
		this.outbTcdNm = outbTcdNm;
	}
}
