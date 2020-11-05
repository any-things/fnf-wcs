package operato.fnf.wcs.entity;

import java.util.Date; 
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_das_rtn_com_rslt", idStrategy = GenerationRule.UUID, uniqueFields = "batchNo,jobDate,itemCd", indexes = {
		@Index(name = "ix_mhe_das_rtn_com_1", columnList = "batch_no,job_date,item_cd", unique = true),
		@Index(name = "ix_mhe_das_rtn_com_2", columnList = "batch_no,cell_no,item_cd") })
public class WcsMheDasRtnComRslt extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 861352805527335884L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_no", nullable = false, length = 20)
	private String batchNo;

	@Column (name = "job_date", nullable = false, length = 8)
	private String jobDate;

	@Column (name = "cell_no", length = 10)
	private String cellNo;

	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;

	@Column (name = "work_qty", length = 5)
	private String workQty;

	@Column (name = "ins_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date insDatetime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getCellNo() {
		return cellNo;
	}

	public void setCellNo(String cellNo) {
		this.cellNo = cellNo;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getWorkQty() {
		return workQty;
	}

	public void setWorkQty(String workQty) {
		this.workQty = workQty;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}
}
