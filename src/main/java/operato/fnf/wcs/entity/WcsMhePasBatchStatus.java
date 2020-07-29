package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_pas_batch_status", idStrategy = GenerationRule.UUID, uniqueFields="mheNo,wcsBatchNo,pasBatchNo", indexes = {
		@Index(name = "ix_mhe_pas_batch_status_01", columnList = "mhe_no,wcs_batch_no,pas_batch_no", unique = true)
	})
public class WcsMhePasBatchStatus extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 304397296243144420L;
	
	/**
	 * 가동준비완료 : 0
	 */
	public static final String STATUS_READY = "0";
	
	/**
	 * 개시(작업중) : 1
	 */
	public static final String STATUS_RUNNING = "1";
	
	/**
	 * 작업 종료 상태 : 2
	 */
	public static final String STATUS_STOP = "2";
	/**
	 * 일시정지 상태 :3
	 */
	public static final String STATUS_PAUSE = "3";
	/**
	 * 강제 초기화 상태 :9
	 */
	public static final String STATUS_RESET = "9";
	

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;
	
	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;

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

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
	}
}
