package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "dps_bcr_if_data", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_dps_bcr_if_data_01", columnList = "proc_yn"),
	@Index(name = "ix_dps_bcr_if_data_02", columnList = "waybill_no")
})
public class DpsBcrIfData extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 652782250347455504L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "proc_yn", length = 1)
	private String procYn;

	@Column (name = "waybill_no", length = 64)
	private String waybillNo;

	@Column (name = "scan_seq")
	private Integer scanSeq;

	@Column (name = "box_no", length = 64)
	private String boxNo;

	@Column (name = "status", length = 32)
	private String status;

	@Column (name = "error_msg", length = 1024)
	private String errorMsg;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProcYn() {
		return procYn;
	}

	public void setProcYn(String procYn) {
		this.procYn = procYn;
	}

	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	public Integer getScanSeq() {
		return scanSeq;
	}

	public void setScanSeq(Integer scanSeq) {
		this.scanSeq = scanSeq;
	}

	public String getBoxNo() {
		return boxNo;
	}

	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}	
}
