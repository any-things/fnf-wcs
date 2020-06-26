package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;

/**
 * DAS에서 올려줄 박스 취소 실적
 * 
 * @author shortstop
 */
@Table(name = "mhe_das_box_cancel", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_das_box_cancel_01", columnList = "batch_no"),
	@Index(name = "ix_das_box_cancel_02", columnList = "mhe_no"),
	@Index(name = "ix_das_box_cancel_03", columnList = "sort_date"),
	@Index(name = "ix_das_box_cancel_04", columnList = "box_id"),
	@Index(name = "ix_das_box_cancel_05", columnList = "prcs_yn"),
	@Index(name = "ix_das_box_cancel_06", columnList = "created_at")
})
public class DasBoxCancel extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 766270331205773518L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "wh_cd", length = 20)
	private String whCd;

	@Column (name = "batch_no", length = 20)
	private String batchNo;

	@Column (name = "mhe_no", length = 10)
	private String mheNo;

	@Column (name = "strr_id", length = 20)
	private String strrId;

	@Column (name = "sort_date", length = 8)
	private String sortDate;

	@Column (name = "sort_seq", length = 3)
	private String sortSeq;

	@Column (name = "box_id", length = 30)
	private String boxId;

	@Column (name = "prcs_yn", length = 1)
	private String prcsYn;
	
	@Column(name = OrmConstants.TABLE_FIELD_CREATED_AT, type = ColumnType.DATETIME)
	private Date createdAt;
  
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

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getPrcsYn() {
		return prcsYn;
	}

	public void setPrcsYn(String prcsYn) {
		this.prcsYn = prcsYn;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
