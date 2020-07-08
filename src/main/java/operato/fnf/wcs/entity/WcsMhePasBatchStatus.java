package operato.fnf.wcs.entity;

import java.util.Date; 
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_pas_batch_status", idStrategy = GenerationRule.UUID, uniqueFields="wcs_batch_no,pas_batch_no", indexes = {
		@Index(name = "ix_mhe_pas_batch_status_01", columnList = "wcs_batch_no,pas_batch_no", unique = true)
	})
public class WcsMhePasBatchStatus extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 304397296243144420L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "wcs_batch_no", nullable = false, length = 20)
	private String wcsBatchNo;

	@Column (name = "pas_batch_no", nullable = false, length = 20)
	private String pasBatchNo;

	@Column (name = "status", length = 1)
	private String status;

	@Column (name = "ins_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date insDatetime;

	@Column (name = "upd_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date updDatetime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWcsBatchNo() {
		return wcsBatchNo;
	}

	public void setWcsBatchNo(String wcsBatchNo) {
		this.wcsBatchNo = wcsBatchNo;
	}

	public String getPasBatchNo() {
		return pasBatchNo;
	}

	public void setPasBatchNo(String pasBatchNo) {
		this.pasBatchNo = pasBatchNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}

	public Date getUpdDatetime() {
		return updDatetime;
	}

	public void setUpdDatetime(Date updDatetime) {
		this.updDatetime = updDatetime;
	}
}
