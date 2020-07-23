package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;

@Table(name = "batch_receipt_items", idStrategy = GenerationRule.UUID, uniqueFields="batchReceiptId,itemType,batchId", indexes = {
	@Index(name = "ix_batch_receipt_items_0", columnList = "batch_receipt_id,item_type,batch_id", unique = true)
})
public class BatchReceiptItem extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 393286420975539725L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_receipt_id", nullable = false, length = 40)
	private String batchReceiptId;

	@Column (name = "item_type", nullable = false, length = 20)
	private String itemType;

	@Column (name = "batch_id", length = 40)
	private String batchId;
	
	@Column (name = "job_seq", length = 12)
	private String jobSeq;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;
	
	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "equip_type", length = 20)
	private String equipType;
	
	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "wms_batch_no", length = 40)
	private String wmsBatchNo;

	@Column (name = "wcs_batch_no", length = 40)
	private String wcsBatchNo;

	@Column (name = "total_orders", length = 12)
	private Integer totalOrders;

	@Column (name = "total_pcs", length = 12)
	private Integer totalPcs;

	@Column (name = "total_records", nullable = false, length = 12)
	private Integer totalRecords;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "message", length = 1000)
	private String message;

	@Column (name = "skip_flag", length = 1)
	private Boolean skipFlag;
	
	@Column (name = "brand_cd", length = 2)
	private String brandCd;
	
	@Column (name = "season_cd", length = 4)
	private String seasonCd;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchReceiptId() {
		return batchReceiptId;
	}

	public void setBatchReceiptId(String batchReceiptId) {
		this.batchReceiptId = batchReceiptId;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	
	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getAreaCd() {
		return areaCd;
	}

	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getWmsBatchNo() {
		return wmsBatchNo;
	}

	public void setWmsBatchNo(String wmsBatchNo) {
		this.wmsBatchNo = wmsBatchNo;
	}

	public String getWcsBatchNo() {
		return wcsBatchNo;
	}

	public void setWcsBatchNo(String wcsBatchNo) {
		this.wcsBatchNo = wcsBatchNo;
	}

	public Integer getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(Integer totalOrders) {
		this.totalOrders = totalOrders;
	}

	public Integer getTotalPcs() {
		return totalPcs;
	}

	public void setTotalPcs(Integer totalPcs) {
		this.totalPcs = totalPcs;
	}

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getSkipFlag() {
		return skipFlag;
	}

	public void setSkipFlag(Boolean skipFlag) {
		this.skipFlag = skipFlag;
	}
	
	public String getJobType() {
		return this.jobType;
	}
	
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	
	public String getBrandCd() {
		return brandCd;
	}

	public void setBrandCd(String brandCd) {
		this.brandCd = brandCd;
	}

	public String getSeasonCd() {
		return seasonCd;
	}

	public void setSeasonCd(String seasonCd) {
		this.seasonCd = seasonCd;
	}

	/**
	 * 상태 업데이트
	 * 
	 * @param status
	 * @param errMsg
	 */
	public void updateStatusImmediately(String status, String errMsg) {
		this.setStatus(status);
		this.setMessage(errMsg);
		
		BeanUtil.get(IQueryManager.class).update(this, "status", "message");
	}	
}
