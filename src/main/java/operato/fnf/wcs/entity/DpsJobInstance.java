package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * DPS 작업 할당 및 작업 처리
 */
@Table(name = "dps_job_instances", idStrategy = GenerationRule.UUID, uniqueFields="whCd,workUnit,refNo,cellCd,itemCd", indexes = {
	@Index(name = "ix_dps_job_instances_01", columnList = "wh_cd,work_unit,ref_no,cell_cd,item_cd", unique = true)
})
public class DpsJobInstance extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 948516983197430600L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;
	
	/**
	 * MHE_DR_ID MHE_DR 테이블의 ID
	 */
	@Column (name = "mhe_dr_id", nullable = false, length = 40)
	private String mheDrId;
	
	/**
	 * 창고 코드 : ICF 고정
	 */
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	/**
	 * 브랜드 코드 
	 */
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;
	
	/**
	 * 브랜드 명
	 */
	@Column (name = "strr_nm", nullable = true, length = 50)
	private String strrNm;
	
	/**
	 * 작업일자
	 */
	@Column (name = "work_date", nullable = false, length = 8)
	private String workDate;
	
	/**
	 * 출고예정일
	 */
	@Column (name = "outb_ect_date", nullable = true, length = 8)
	private String outbEctDate;

	/**
	 * 작업 배치 번호
	 */
	@Column (name = "work_unit", nullable = false, length = 20)
	private String workUnit;
	
	/**
	 * 웨이브 번호
	 */
	@Column (name = "wave_no", nullable = true, length = 20)
	private String waveNo;

	/**
	 * 작업 차수
	 */
	@Column (name = "workseq_no", nullable = true, length = 5)
	private String workseqNo;
	
	/**
	 * 설비 코드 (장비 식별 번호)
	 */
	@Column (name = "mhe_no", nullable = true, length = 20)
	private String mheNo;
	
	/**
	 * 온라인 주문번호
	 */
	@Column (name = "ref_no", nullable = false, length = 30)
	private String refNo;
	
	/**
	 * 출고번호
	 */
	@Column (name = "outb_no", nullable = true, length = 20)
	private String outbNo;
	
	/**
	 * 매장 코드 
	 */
	@Column (name = "shipto_id", nullable = true, length = 100)
	private String shiptoId;
	
	/**
	 * 매장 명
	 */
	@Column (name = "shipto_nm", nullable = true, length = 200)
	private String shiptoNm;

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
	
	/**
	 * 상품 바코드
	 */
	@Column (name = "barcode2", nullable = true, length = 100)
	private String barcode2;

	/**
	 * 주문 수량 
	 */
	@Column (name = "pick_qty", nullable = false, length = 10)
	private Integer pickQty;

	/**
	 * 확정 수량 
	 */
	@Column (name = "cmpt_qty", nullable = true, length = 10)
	private Integer cmptQty;
	
	/**
	 * 온라인 합/단포 구분 (D: 단포, H: 합포)
	 */
	@Column (name = "pack_tcd", nullable = true, length = 10)
	private String packTcd;
	
	/**
	 * 셀 번호
	 */
	@Column (name = "cell_cd", nullable = false, length = 20)
	private String cellCd;
	
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
	
	/**
	 * RFID 필수 검수 여부 
	 */
	@Column (name = "rfid_item_yn", nullable = true, length = 1)
	private String rfidItemYn;

	/**
	 * 작업 할당 여부
	 */
	@Column (name = "dps_assign_yn", nullable = true, length = 1)
	private String dpsAssignYn;
	
	/**
	 * 작업 할당 시간 
	 */
	@Column (name = "dps_assign_at", nullable = true)
	private Date dpsAssignAt;
	
	/**
	 * DPS에 박스 투입한 순서 (1부터 순차적 Up)
	 */
	@Column (name = "box_input_seq", nullable = true)
	private Integer boxInputSeq;

	/**
	 * DPS에 박스 투입한 시간
	 */
	@Column (name = "box_input_at", nullable = true)
	private Date boxInputAt;
	
	/**
	 * DPS 설비가 박스 투입된 정보를 가져갔는지 여부 - DPS가 관리  
	 */
	@Column (name = "box_input_if_yn", nullable = true, length = 1)
	private String boxInputIfYn;
	
	/**
	 * DPS 설비가 박스 투입된 정보를 가져간 시간
	 */
	@Column (name = "box_input_if_at", nullable = true)
	private Date boxInputIfAt;
	
	/**
	 * DPS에서 피킹 확정 시간 
	 */
	@Column (name = "mhe_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date mheDatetime;
	
	/**
	 * 박스 실적 전송 시간
	 */
	@Column (name = "box_result_if_at", nullable = true)
	private Date boxResultIfAt;
	
	/**
	 * 검수 시간 
	 */
	@Column (name = "inspected_at", nullable = true)
	private Date inspectedAt;
	
	/**
	 * 검수자
	 */
	@Column (name = "inspector_id", nullable = true, length = 32)
	private String inspectorId;
	
	/**
	 * 상태 A : 작업 할당, I : 박스 투입, B : 박싱 완료, E : 검수 완료, C : 작업 취소
	 */
	@Column (name = "status", nullable = true, length = 1)
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMheDrId() {
		return mheDrId;
	}

	public void setMheDrId(String mheDrId) {
		this.mheDrId = mheDrId;
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

	public String getOutbEctDate() {
		return outbEctDate;
	}

	public void setOutbEctDate(String outbEctDate) {
		this.outbEctDate = outbEctDate;
	}

	public String getWorkUnit() {
		return workUnit;
	}

	public void setWorkUnit(String workUnit) {
		this.workUnit = workUnit;
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

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getOutbNo() {
		return outbNo;
	}

	public void setOutbNo(String outbNo) {
		this.outbNo = outbNo;
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

	public Date getMheDatetime() {
		return mheDatetime;
	}

	public void setMheDatetime(Date mheDatetime) {
		this.mheDatetime = mheDatetime;
	}

	public Date getBoxResultIfAt() {
		return boxResultIfAt;
	}

	public void setBoxResultIfAt(Date boxResultIfAt) {
		this.boxResultIfAt = boxResultIfAt;
	}

	public Date getInspectedAt() {
		return inspectedAt;
	}

	public void setInspectedAt(Date inspectedAt) {
		this.inspectedAt = inspectedAt;
	}

	public String getInspectorId() {
		return inspectorId;
	}

	public void setInspectorId(String inspectorId) {
		this.inspectorId = inspectorId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
