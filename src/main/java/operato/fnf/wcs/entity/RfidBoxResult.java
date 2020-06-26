package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Index;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;

@Table(name = "rfid_box_results", idStrategy = GenerationRule.UUID, uniqueFields="whCd,brandCd,outDate,batchId,equipType,boxId", indexes = {
	@Index(name = "ix_rfid_box_results_0", columnList = "wh_cd,brand_cd,out_date,batch_id,equip_type,box_id", unique = true),
	@Index(name = "ix_rfid_box_results_1", columnList = "batch_id,insp_result"),
	@Index(name = "ix_rfid_box_results_2", columnList = "invoice_id"),
	@Index(name = "ix_rfid_box_results_3", columnList = "box_id")
})
public class RfidBoxResult extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 715655136778200694L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "wh_cd", length = 20)
	private String whCd;

	@Column (name = "brand_cd", length = 10)
	private String brandCd;

	@Column (name = "out_date", length = 8)
	private String outDate;

	@Column (name = "batch_id", length = 20)
	private String batchId;

	@Column (name = "equip_type", length = 2)
	private String equipType;

	@Column (name = "box_id", length = 20)
	private String boxId;

	@Column (name = "invoice_id", length = 50)
	private String invoiceId;

	@Column (name = "insp_result", length = 1)
	private String inspResult;
	
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

	public String getBrandCd() {
		return brandCd;
	}

	public void setBrandCd(String brandCd) {
		this.brandCd = brandCd;
	}

	public String getOutDate() {
		return outDate;
	}

	public void setOutDate(String outDate) {
		this.outDate = outDate;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getInspResult() {
		return inspResult;
	}

	public void setInspResult(String inspResult) {
		this.inspResult = inspResult;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
