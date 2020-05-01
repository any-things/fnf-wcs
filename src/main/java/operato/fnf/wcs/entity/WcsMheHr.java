package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * 피킹/분배 지시 내역을 송신하고, 확정 수량을 수신 - DAS, DPS
 */
@Table(name = "mhe_hr", idStrategy = GenerationRule.UUID, uniqueFields="whCd,workUnit", indexes = {
	@Index(name = "ix_mhe_hr_01", columnList = "wh_cd,work_unit", unique = true)
})
public class WcsMheHr extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -8555254391446018815L;

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
	
	@Column (name = "biz_type", length = 10)
	private String bizType;
	
	@Column (name = "mhe_no", length = 10)
	private String mheNo;

	@Column (name = "wave_no", length = 20)
	private String waveNo;

	@Column (name = "workseq_no", length = 20)
	private String workseqNo;
	
	@Column (name = "pick_qty", length = 10)
	private Integer pickQty;
	
	@Column (name = "cmpt_qty", length = 10)
	private Integer cmptQty;
	
	@Column (name = "status", length = 1)
	private String status;
	
	@Column (name = "rcv_datetime", type = ColumnType.DATETIME)
	private Date rcvDatetime;
	
	@Column (name = "cnf_datetime", type = ColumnType.DATETIME)
	private Date cnfDatetime;
	
	@Column (name = "end_datetime", type = ColumnType.DATETIME)
	private Date endDatetime;

	@Column (name = "ins_datetime", type = ColumnType.DATETIME)
	private Date insDatetime;
	
	@Column (name = "ins_person_id", length = 20)
	private String insPersonId;

	@Column (name = "descr", length = 200)
	private String descr;

	@Column (name = "prcs_yn", length = 1)
	private String prcsYn;

	@Column (name = "prcs_datetime", type = ColumnType.DATETIME)
	private Date prcsDatetime;
	
	@Column (name = "pas_chute_tcd", length = 10)
	private String pasChuteTcd;

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

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getRcvDatetime() {
		return rcvDatetime;
	}

	public void setRcvDatetime(Date rcvDatetime) {
		this.rcvDatetime = rcvDatetime;
	}

	public Date getCnfDatetime() {
		return cnfDatetime;
	}

	public void setCnfDatetime(Date cnfDatetime) {
		this.cnfDatetime = cnfDatetime;
	}

	public Date getEndDatetime() {
		return endDatetime;
	}

	public void setEndDatetime(Date endDatetime) {
		this.endDatetime = endDatetime;
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

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getPrcsYn() {
		return prcsYn;
	}

	public void setPrcsYn(String prcsYn) {
		this.prcsYn = prcsYn;
	}

	public Date getPrcsDatetime() {
		return prcsDatetime;
	}

	public void setPrcsDatetime(Date prcsDatetime) {
		this.prcsDatetime = prcsDatetime;
	}

	public String getPasChuteTcd() {
		return pasChuteTcd;
	}

	public void setPasChuteTcd(String pasChuteTcd) {
		this.pasChuteTcd = pasChuteTcd;
	}

}
