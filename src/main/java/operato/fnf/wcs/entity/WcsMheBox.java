package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * 박스별 패킹 내역 수신 - DAS, 소터 (DPS 해당 없음)
 */
@Table(name = "mhe_box", idStrategy = GenerationRule.UUID, uniqueFields="whCd,workUnit,shiptoId,itemCd,boxNo,boxSeq", indexes = {
	@Index(name = "ix_mhe_box_01", columnList = "wh_cd,work_unit,shipto_id,item_cd,box_no,box_seq", unique = true),
	@Index(name = "ix_mhe_box_02", columnList = "work_unit,outb_no,box_no"),
	@Index(name = "ix_mhe_box_03", columnList = "work_date,strr_id,mhe_no,box_no")
})
public class WcsMheBox extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3601219852210491698L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;
	
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;

	@Column (name = "work_date", nullable = false, length = 8)
	private String workDate;
	
	@Column (name = "work_unit", nullable = false, length = 20)
	private String workUnit;
	
	@Column (name = "wave_no", nullable = true, length = 20)
	private String waveNo;
	
	@Column (name = "workseq_no", nullable = true, length = 5)
	private String workseqNo;
	
	@Column (name = "outb_no", nullable = true, length = 20)
	private String outbNo;
	
	@Column (name = "shipto_id", nullable = false, length = 20)
	private String shiptoId;
	
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;
	
	@Column (name = "box_no", nullable = false, length = 30)
	private String boxNo;
	
	@Column (name = "box_seq", nullable = true, length = 10)
	private Integer boxSeq;
	
	@Column (name = "cmpt_qty", nullable = true, length = 10)
	private Integer cmptQty;
	
	@Column (name = "mhe_no", nullable = true, length = 20)
	private String mheNo;
	
	@Column (name = "mhe_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date mheDatetime;
	
	/**
	 * RFID 전송 여부
	 */
	@Column (name = "if_yn", length = 1)
	private String ifYn;
	
	/**
	 * RFID 전송 시간
	 */
	@Column (name = "if_datetime", type = ColumnType.DATETIME)
	private Date ifDatetime;
	
	/**
	 * WMS 전송 시간
	 */
	@Column (name = "cnf_datetime", type = ColumnType.DATETIME)
	private Date cnfDatetime;
	
	@Column (name = "del_yn", length = 1)
	private String delYn;
	
	@Column (name = "del_datetime", type = ColumnType.DATETIME)
	private Date delDatetime;

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

	public String getShiptoId() {
		return shiptoId;
	}

	public void setShiptoId(String shiptoId) {
		this.shiptoId = shiptoId;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getBoxNo() {
		return boxNo;
	}

	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}

	public Integer getBoxSeq() {
		return boxSeq;
	}

	public void setBoxSeq(Integer boxSeq) {
		this.boxSeq = boxSeq;
	}

	public Integer getCmptQty() {
		return cmptQty;
	}

	public void setCmptQty(Integer cmptQty) {
		this.cmptQty = cmptQty;
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

	public String getIfYn() {
		return ifYn;
	}

	public void setIfYn(String ifYn) {
		this.ifYn = ifYn;
	}

	public Date getIfDatetime() {
		return ifDatetime;
	}

	public void setIfDatetime(Date ifDatetime) {
		this.ifDatetime = ifDatetime;
	}

	public Date getCnfDatetime() {
		return cnfDatetime;
	}

	public void setCnfDatetime(Date cnfDatetime) {
		this.cnfDatetime = cnfDatetime;
	}

	public String getDelYn() {
		return delYn;
	}

	public void setDelYn(String delYn) {
		this.delYn = delYn;
	}

	public Date getDelDatetime() {
		return delDatetime;
	}

	public void setDelDatetime(Date delDatetime) {
		this.delDatetime = delDatetime;
	}

}
