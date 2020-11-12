package xyz.anythings.base.entity;

import xyz.anythings.base.LogisConstants;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.sys.util.ValueUtil;

@Table(name = "job_instances", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_job_instances_1", columnList = "batch_id,domain_id"),
	@Index(name = "ix_job_instances_2", columnList = "order_no,batch_id"),
	@Index(name = "ix_job_instances_3", columnList = "box_id,invoice_id,batch_id"),
	@Index(name = "ix_job_instances_4", columnList = "status,equip_type,equip_cd,sub_equip_cd,batch_id"),
	@Index(name = "ix_job_instances_5", columnList = "status,job_date,job_seq,batch_id"),
	@Index(name = "ix_job_instances_6", columnList = "input_seq,sub_equip_cd,sku_cd,status,batch_id")
})
public class JobInstance extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 886823091901247100L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", length = 40, nullable = false)
	private String batchId;
	
	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", nullable = false, length = 12)
	private Integer jobSeq;

	@Column (name = "job_type", nullable = false, length = 20)
	private String jobType;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "shop_cd", nullable = false, length = 30)
	private String shopCd;

	@Column (name = "shop_nm", length = 40)
	private String shopNm;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", nullable = false, length = 30)
	private String equipCd;

	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	@Column (name = "ind_cd", length = 30)
	private String indCd;

	@Column (name = "order_no", nullable = false, length = 40)
	private String orderNo;

	@Column (name = "sku_cd", nullable = false, length = 30)
	private String skuCd;

	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	@Column (name = "input_seq", length = 12)
	private Integer inputSeq;

	@Column (name = "box_type_cd", length = 30)
	private String boxTypeCd;

	@Column (name = "box_id", length = 30)
	private String boxId;

	@Column (name = "invoice_id", length = 40)
	private String invoiceId;

	@Column (name = "box_pack_id", length = 40)
	private String boxPackId;

	@Column (name = "box_in_qty", length = 12)
	private Integer boxInQty;
	
	@Column (name = "pick_qty", length = 12)
	private Integer pickQty;

	@Column (name = "picking_qty", length = 12)
	private Integer pickingQty;

	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	@Column (name = "color_cd", length = 10)
	private String colorCd;

	@Column (name = "order_type", length = 20)
	private String orderType;

	/**
	 * 소 분류 용
	 */
	@Column (name = "class_cd", length = 30)
	private String classCd;
	
	/**
	 * 방면 분류 용
	 */
	@Column (name = "box_class_cd", length = 30)
	private String boxClassCd;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "input_at", length = 22)
	private String inputAt;

	@Column (name = "pick_started_at", length = 22)
	private String pickStartedAt;

	@Column (name = "pick_ended_at", length = 22)
	private String pickEndedAt;

	@Column (name = "boxed_at", length = 22)
	private String boxedAt;
	
	@Ignore
	private String stationCd;
	
	@Ignore
	private String gwPath;
	
	@Ignore
	private String sideCd;
	
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public Integer getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(Integer jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getShopCd() {
		return shopCd;
	}

	public void setShopCd(String shopCd) {
		this.shopCd = shopCd;
	}

	public String getShopNm() {
		return shopNm;
	}

	public void setShopNm(String shopNm) {
		this.shopNm = shopNm;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getEquipNm() {
		return equipNm;
	}

	public void setEquipNm(String equipNm) {
		this.equipNm = equipNm;
	}

	public String getSubEquipCd() {
		return subEquipCd;
	}

	public void setSubEquipCd(String subEquipCd) {
		this.subEquipCd = subEquipCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Integer getInputSeq() {
		return inputSeq;
	}

	public void setInputSeq(Integer inputSeq) {
		this.inputSeq = inputSeq;
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
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

	public String getBoxPackId() {
		return boxPackId;
	}

	public void setBoxPackId(String boxPackId) {
		this.boxPackId = boxPackId;
	}

	public Integer getBoxInQty() {
		return boxInQty;
	}

	public void setBoxInQty(Integer boxInQty) {
		this.boxInQty = boxInQty;
	}

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}

	public Integer getPickingQty() {
		return pickingQty;
	}

	public void setPickingQty(Integer pickingQty) {
		this.pickingQty = pickingQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public String getColorCd() {
		return colorCd;
	}

	public void setColorCd(String colorCd) {
		this.colorCd = colorCd;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public String getClassCd() {
		return this.classCd;
	}
	
	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getBoxClassCd() {
		return boxClassCd;
	}

	public void setBoxClassCd(String boxClassCd) {
		this.boxClassCd = boxClassCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInputAt() {
		return inputAt;
	}

	public void setInputAt(String inputAt) {
		this.inputAt = inputAt;
	}

	public String getPickStartedAt() {
		return pickStartedAt;
	}

	public void setPickStartedAt(String pickStartedAt) {
		this.pickStartedAt = pickStartedAt;
	}

	public String getPickEndedAt() {
		return pickEndedAt;
	}

	public void setPickEndedAt(String pickEndedAt) {
		this.pickEndedAt = pickEndedAt;
	}

	public String getBoxedAt() {
		return boxedAt;
	}

	public void setBoxedAt(String boxedAt) {
		this.boxedAt = boxedAt;
	}
	
	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public String getGwPath() {
		return gwPath;
	}

	public void setGwPath(String gwPath) {
		this.gwPath = gwPath;
	}
	
	public String getSideCd() {
		return this.sideCd;
	}
	
	public void setSideCd(String sideCd) {
		this.sideCd = sideCd;
	}

	/**
	 * 아직 완료되지 않은 작업인지 체크
	 * 
	 * @return
	 */
	public boolean isTodoJob() {
		return (ValueUtil.isEmpty(this.status) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_CANCEL) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_PICKING) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_WAIT) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_INPUT));
	}
	
	/**
	 * 이미 완료된 작업인지 체크
	 * 
	 * @return
	 */
	public boolean isDoneJob() {
		return !this.isTodoJob();
	}

}
