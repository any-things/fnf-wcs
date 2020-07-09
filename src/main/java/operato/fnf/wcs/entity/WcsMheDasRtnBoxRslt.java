package operato.fnf.wcs.entity;

import java.util.Date; 
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_das_rtn_box_rslt", idStrategy = GenerationRule.UUID, uniqueFields = "whCd,mheNo,sortDate,itemCd,boxNo,boxSeq", indexes = {
		@Index(name = "ix_mhe_das_rtn_box_1", columnList = "wh_cd,mhe_no,sort_date,item_cd,box_no,box_seq", unique = true),
		@Index(name = "ix_mhe_das_rtn_box_2", columnList = "mhe_no,sort_date,box_no") })
public class WcsMheDasRtnBoxRslt extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 861352805527335884L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;

	@Column (name = "batch_no", length = 20)
	private String batchNo;

	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;

	@Column (name = "strr_id", length = 20)
	private String strrId;

	@Column (name = "sort_date", nullable = false, length = 8)
	private String sortDate;

	@Column (name = "sort_seq", length = 3)
	private String sortSeq;

	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;

	@Column (name = "box_no", nullable = false, length = 30)
	private String boxNo;

	@Column (name = "box_seq", nullable = false, length = 10)
	private Integer boxSeq;

	@Column (name = "cmpt_qty", length = 10)
	private Integer cmptQty;

	@Column (name = "ins_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date insDatetime;

	/**
	 * WMS 전송 여부 
	 */
	@Column (name = "if_yn", length = 1)
	private String ifYn;

	/**
	 * WMS 전송 시간 
	 */
	@Column (name = "cnf_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date cnfDatetime;

	/**
	 * BOX 삭제 여부 
	 */
	@Column (name = "del_yn", length = 1)
	private String delYn;

	/**
	 * BOX 삭제 시간 
	 */
	@Column (name = "del_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date delDatetime;

	/**
	 * RFID 전송 여부 
	 */
	@Column (name = "rfid_if_yn", length = 1)
	private String rfidIfYn;
	
	/**
	 * RFID 전송 시간 
	 */
	@Column (name = "rfid_cnf_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date rfidCnfDatetime;
  
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

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getSortDate() {
		return sortDate;
	}

	public void setSortDate(String sortDate) {
		this.sortDate = sortDate;
	}

	public String getSortSeq() {
		return sortSeq;
	}

	public void setSortSeq(String sortSeq) {
		this.sortSeq = sortSeq;
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

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}

	public String getIfYn() {
		return ifYn;
	}

	public void setIfYn(String ifYn) {
		this.ifYn = ifYn;
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

	public String getRfidIfYn() {
		return rfidIfYn;
	}

	public void setRfidIfYn(String rfidIfYn) {
		this.rfidIfYn = rfidIfYn;
	}

	public Date getRfidCnfDatetime() {
		return rfidCnfDatetime;
	}

	public void setRfidCnfDatetime(Date rfidCnfDatetime) {
		this.rfidCnfDatetime = rfidCnfDatetime;
	}	
}
