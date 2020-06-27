package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * 상세 분배 지시내역 - DAS, DPS
 */
@Table(name = "mhe_dr", idStrategy = GenerationRule.UUID, uniqueFields="whCd,workUnit,shiptoId,outbNo,locationCd,itemCd", indexes = {
	@Index(name = "ix_mhe_dr_01", columnList = "wh_cd,work_unit,shipto_id,outb_no,location_cd,item_cd"),
	@Index(name = "ix_mhe_dr_02", columnList = "work_unit,biz_type,ref_no"),
	@Index(name = "ix_mhe_dr_03", columnList = "work_unit,status,box_no,box_id,box_input_seq"),
	@Index(name = "ix_mhe_dr_04", columnList = "work_unit,chute_no,item_cd")
})
public class WcsMheDr extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -5836349611745073509L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;
	
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;
	
	@Column (name = "strr_nm", nullable = true, length = 50)
	private String strrNm;
	
	@Column (name = "work_date", nullable = false, length = 8)
	private String workDate;

	@Column (name = "work_unit", nullable = false, length = 20)
	private String workUnit;
	
	@Column (name = "biz_type", length = 10)
	private String bizType;
	
	@Column (name = "wave_no", nullable = true, length = 20)
	private String waveNo;

	@Column (name = "workseq_no", nullable = true, length = 5)
	private String workseqNo;
	
	@Column (name = "outb_no", nullable = false, length = 20)
	private String outbNo;
	
	@Column (name = "ref_no", nullable = true, length = 30)
	private String refNo;
	
	@Column (name = "ref_detl_no", nullable = true, length = 50)
	private String refDetlNo;
	
	@Column (name = "chute_no", nullable = true, length = 10)
	private String chuteNo;
	
	@Column (name = "outb_ect_date", nullable = true, length = 8)
	private String outbEctDate;

	@Column (name = "shipto_id", nullable = false, length = 100)
	private String shiptoId;
	
	@Column (name = "shipto_nm", nullable = true, length = 200)
	private String shiptoNm;

	@Column (name = "cust_id", nullable = true, length = 100)
	private String custId;
	
	@Column (name = "cust_nm", nullable = true, length = 200)
	private String custNm;

	@Column (name = "addr_1", nullable = true, length = 500)
	private String addr1;
	
	@Column (name = "addr_2", nullable = true, length = 500)
	private String addr2;
	
	@Column (name = "zip_no", nullable = true, length = 10)
	private String zipNo;
	
	@Column (name = "tel_no", nullable = true, length = 20)
	private String telNo;
	
	@Column (name = "region_cd", nullable = true, length = 20)
	private String regionCd;
	
	@Column (name = "region_nm", nullable = true, length = 20)
	private String regionNm;
	
	@Column (name = "course_cd", nullable = true, length = 20)
	private String courseCd;
	
	@Column (name = "course_nm", nullable = true, length = 20)
	private String courseNm;
	
	@Column (name = "shipowner_cd", nullable = true, length = 20)
	private String shipownerCd;
	
	@Column (name = "carrier_cd", nullable = true, length = 20)
	private String carrierCd;
	
	@Column (name = "carrier_nm", nullable = true, length = 30)
	private String carrierNm;

	@Column (name = "zone_cd", nullable = true, length = 10)
	private String zoneCd;
	
	@Column (name = "location_cd", nullable = false, length = 30)
	private String locationCd;
	
	@Column (name = "pick_seq", nullable = true, length = 10)
	private Integer pickSeq;

	@Column (name = "assort_yn", nullable = true, length = 1)
	private String assortYn;

	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;
	
	@Column (name = "item_nm", nullable = true, length = 200)
	private String itemNm;
	
	@Column (name = "item_season", nullable = true, length = 10)
	private String itemSeason;
	
	@Column (name = "item_style", nullable = true, length = 30)
	private String itemStyle;
	
	@Column (name = "item_color", nullable = true, length = 10)
	private String itemColor;
	
	@Column (name = "item_size", nullable = true, length = 10)
	private String itemSize;
	
	@Column (name = "barcode", nullable = true, length = 100)
	private String barcode;

	@Column (name = "barcode2", nullable = true, length = 100)
	private String barcode2;
	
	@Column (name = "pick_qty", nullable = true, length = 10)
	private Integer pickQty;

	@Column (name = "cmpt_qty", nullable = true, length = 10)
	private Integer cmptQty;

	@Column (name = "multiply_qty", nullable = true, length = 10)
	private Integer multiplyQty;
	
	@Column (name = "ins_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date insDatetime;

	@Column (name = "ins_person_id", nullable = true, length = 20)
	private String insPersonId;
	
	@Column (name = "mhe_no", nullable = true, length = 20)
	private String mheNo;
	
	@Column (name = "mhe_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date mheDatetime;
	
	@Column (name = "indirect_item_yn", nullable = true, length = 1)
	private String indirectItemYn;

	@Column (name = "assort_in_qty", nullable = true, length = 10)
	private Integer assortInQty;

	@Column (name = "item_bcd", nullable = true, length = 20)
	private String itemBcd;

	@Column (name = "item_gcd", nullable = true, length = 20)
	private String itemGcd;

	@Column (name = "outb_tcd", nullable = true, length = 20)
	private String outbTcd;
	
	/**
	 * 온라인 합/단포 구분 (D: 단포, H: 합포)
	 */
	@Column (name = "pack_tcd", nullable = true, length = 10)
	private String packTcd;
	
	@Column (name = "cell_cd", nullable = true, length = 20)
	private String cellCd;
	
	@Column (name = "rfid_item_yn", nullable = true, length = 1)
	private String rfidItemYn;

	@Column (name = "dps_assign_yn", nullable = true, length = 1)
	private String dpsAssignYn;
	
	@Column (name = "dps_assign_at", nullable = true)
	private Date dpsAssignAt;
	
	@Column (name = "box_input_seq", nullable = true)
	private Integer boxInputSeq;
	
	/**
	 * DPS 트레이 박스 번호
	 */
	@Column (name = "box_no", nullable = true, length = 20)
	private String boxNo;

	/**
	 * DPS 유니크 박스 ID
	 */
	@Column (name = "box_id", nullable = true, length = 30)
	private String boxId;
	
	/**
	 * 송장 번호
	 */
	@Column (name = "waybill_no", nullable = true, length = 30)
	private String waybillNo;

	@Column (name = "box_input_at", nullable = true)
	private Date boxInputAt;
	
	@Column (name = "box_input_if_yn", nullable = true, length = 1)
	private String boxInputIfYn;
	
	@Column (name = "box_input_if_at", nullable = true)
	private Date boxInputIfAt;
	
	@Column (name = "pick_result_if_at", nullable = true)
	private Date pickResultIfAt;
	
	@Column (name = "box_result_if_at", nullable = true)
	private Date boxResultIfAt;
		
	@Column (name = "status", nullable = true, length = 1)
	private String status;
	
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

	public String getStrrNm() {
		return strrNm;
	}

	public void setStrrNm(String strrNm) {
		this.strrNm = strrNm;
	}

	public String getWorkDate() {
		return workDate;
	}

	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}

	public String getWorkUnit() {
		return workUnit;
	}

	public void setWorkUnit(String workUnit) {
		this.workUnit = workUnit;
	}

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getWaveNo() {
		return waveNo;
	}

	public void setWaveNo(String waveNo) {
		this.waveNo = waveNo;
	}

	public String getWorkseqNo() {
		return workseqNo;
	}

	public void setWorkseqNo(String workseqNo) {
		this.workseqNo = workseqNo;
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

	public String getRefDetlNo() {
		return refDetlNo;
	}

	public void setRefDetlNo(String refDetlNo) {
		this.refDetlNo = refDetlNo;
	}

	public String getChuteNo() {
		return chuteNo;
	}

	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
	}

	public String getOutbEctDate() {
		return outbEctDate;
	}

	public void setOutbEctDate(String outbEctDate) {
		this.outbEctDate = outbEctDate;
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

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCustNm() {
		return custNm;
	}

	public void setCustNm(String custNm) {
		this.custNm = custNm;
	}

	public String getAddr1() {
		return addr1;
	}

	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}

	public String getAddr2() {
		return addr2;
	}

	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}

	public String getZipNo() {
		return zipNo;
	}

	public void setZipNo(String zipNo) {
		this.zipNo = zipNo;
	}

	public String getTelNo() {
		return telNo;
	}

	public void setTelNo(String telNo) {
		this.telNo = telNo;
	}

	public String getRegionCd() {
		return regionCd;
	}

	public void setRegionCd(String regionCd) {
		this.regionCd = regionCd;
	}

	public String getRegionNm() {
		return regionNm;
	}

	public void setRegionNm(String regionNm) {
		this.regionNm = regionNm;
	}

	public String getCourseCd() {
		return courseCd;
	}

	public void setCourseCd(String courseCd) {
		this.courseCd = courseCd;
	}

	public String getCourseNm() {
		return courseNm;
	}

	public void setCourseNm(String courseNm) {
		this.courseNm = courseNm;
	}

	public String getShipownerCd() {
		return shipownerCd;
	}

	public void setShipownerCd(String shipownerCd) {
		this.shipownerCd = shipownerCd;
	}

	public String getCarrierCd() {
		return carrierCd;
	}

	public void setCarrierCd(String carrierCd) {
		this.carrierCd = carrierCd;
	}

	public String getCarrierNm() {
		return carrierNm;
	}

	public void setCarrierNm(String carrierNm) {
		this.carrierNm = carrierNm;
	}

	public String getZoneCd() {
		return zoneCd;
	}

	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}

	public String getLocationCd() {
		return locationCd;
	}

	public void setLocationCd(String locationCd) {
		this.locationCd = locationCd;
	}

	public Integer getPickSeq() {
		return pickSeq;
	}

	public void setPickSeq(Integer pickSeq) {
		this.pickSeq = pickSeq;
	}

	public String getAssortYn() {
		return assortYn;
	}

	public void setAssortYn(String assortYn) {
		this.assortYn = assortYn;
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

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}

	public Integer getCmptQty() {
		return cmptQty;
	}

	public void setCmptQty(Integer cmptQty) {
		this.cmptQty = cmptQty;
	}

	public Integer getMultiplyQty() {
		return multiplyQty;
	}

	public void setMultiplyQty(Integer multiplyQty) {
		this.multiplyQty = multiplyQty;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}

	public String getInsPersonId() {
		return insPersonId;
	}

	public void setInsPersonId(String insPersonId) {
		this.insPersonId = insPersonId;
	}

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
	}

	public Date getMheDatetime() {
		return mheDatetime;
	}

	public void setMheDatetime(Date mheDatetime) {
		this.mheDatetime = mheDatetime;
	}

	public String getIndirectItemYn() {
		return indirectItemYn;
	}

	public void setIndirectItemYn(String indirectItemYn) {
		this.indirectItemYn = indirectItemYn;
	}

	public Integer getAssortInQty() {
		return assortInQty;
	}

	public void setAssortInQty(Integer assortInQty) {
		this.assortInQty = assortInQty;
	}

	public String getItemBcd() {
		return itemBcd;
	}

	public void setItemBcd(String itemBcd) {
		this.itemBcd = itemBcd;
	}

	public String getItemGcd() {
		return itemGcd;
	}

	public void setItemGcd(String itemGcd) {
		this.itemGcd = itemGcd;
	}

	public String getOutbTcd() {
		return outbTcd;
	}

	public void setOutbTcd(String outbTcd) {
		this.outbTcd = outbTcd;
	}

	public String getPackTcd() {
		return packTcd;
	}

	public void setPackTcd(String packTcd) {
		this.packTcd = packTcd;
	}

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}

	public String getRfidItemYn() {
		return rfidItemYn;
	}

	public void setRfidItemYn(String rfidItemYn) {
		this.rfidItemYn = rfidItemYn;
	}

	public String getDpsAssignYn() {
		return dpsAssignYn;
	}

	public void setDpsAssignYn(String dpsAssignYn) {
		this.dpsAssignYn = dpsAssignYn;
	}

	public Date getDpsAssignAt() {
		return dpsAssignAt;
	}

	public void setDpsAssignAt(Date dpsAssignAt) {
		this.dpsAssignAt = dpsAssignAt;
	}

	public Integer getBoxInputSeq() {
		return boxInputSeq;
	}

	public void setBoxInputSeq(Integer boxInputSeq) {
		this.boxInputSeq = boxInputSeq;
	}

	public String getBoxNo() {
		return boxNo;
	}

	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	public Date getBoxInputAt() {
		return boxInputAt;
	}

	public void setBoxInputAt(Date boxInputAt) {
		this.boxInputAt = boxInputAt;
	}

	public String getBoxInputIfYn() {
		return boxInputIfYn;
	}

	public void setBoxInputIfYn(String boxInputIfYn) {
		this.boxInputIfYn = boxInputIfYn;
	}

	public Date getBoxInputIfAt() {
		return boxInputIfAt;
	}

	public void setBoxInputIfAt(Date boxInputIfAt) {
		this.boxInputIfAt = boxInputIfAt;
	}

	public Date getPickResultIfAt() {
		return pickResultIfAt;
	}

	public void setPickResultIfAt(Date pickResultIfAt) {
		this.pickResultIfAt = pickResultIfAt;
	}

	public Date getBoxResultIfAt() {
		return boxResultIfAt;
	}

	public void setBoxResultIfAt(Date boxResultIfAt) {
		this.boxResultIfAt = boxResultIfAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
