package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "box_packs", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_box_packs_0", columnList = "domain_id,batch_id,wcs_batch_no,wms_batch_no"),
	@Index(name = "ix_box_packs_1", columnList = "domain_id,stage_cd,job_date,job_seq"),
	@Index(name = "ix_box_packs_2", columnList = "domain_id,batch_id,equip_type,equip_cd,sub_equip_cd,status"),
	@Index(name = "ix_box_packs_3", columnList = "domain_id,order_no,box_id,invoice_id"),
	@Index(name = "ix_box_packs_4", columnList = "domain_id,batch_id,class_cd,box_class_cd,shop_cd")
})
public class BoxPack extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 258512479109046063L;
	
	/**
	 * 박싱 대기
	 */
	public static final String BOX_STATUS_WAIT = "W";
	/**
	 * 분류 중
	 */
	public static final String BOX_STATUS_ASSORT = "A";
	/**
	 * 박싱 완료
	 */
	public static final String BOX_STATUS_BOXED = "B";
	/**
	 * 검수 완료
	 */
	public static final String BOX_STATUS_EXAMED = "E";
	/**
	 * 포장 완료
	 */
	public static final String BOX_STATUS_PACKED = "P";
	/**
	 * 전송 완료
	 */
	public static final String BOX_STATUS_REPORTED = "R";

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "wcs_batch_no", length = 30)
	private String wcsBatchNo;

	@Column (name = "wms_batch_no", length = 30)
	private String wmsBatchNo;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", length = 12)
	private Integer jobSeq;

	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "order_date", length = 10)
	private String orderDate;
	
	@Column (name = "cust_order_no", length = 40)
	private String custOrderNo;

	@Column (name = "order_no", length = 40)
	private String orderNo;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "shop_cd", length = 30)
	private String shopCd;

	@Column (name = "shop_nm", length = 40)
	private String shopNm;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;
	
	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;

	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	@Column (name = "invoice_id", length = 40)
	private String invoiceId;
	
	/**
	 * 주문 유형
	 */
	@Column (name = "order_type", length = 20)
	private String orderType;	

	@Column (name = "box_type_cd", length = 30)
	private String boxTypeCd;

	@Column (name = "box_id", length = 40)
	private String boxId;

	@Column (name = "box_seq", length = 12)
	private Integer boxSeq;

	@Column (name = "sku_qty", length = 12)
	private Integer skuQty;

	@Column (name = "pick_qty", length = 12)
	private Integer pickQty;

	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	@Column (name = "box_wt", length = 19)
	private Float boxWt;

	@Column (name = "box_wt_min", length = 19)
	private Float boxWtMin;

	@Column (name = "box_wt_max", length = 19)
	private Float boxWtMax;

	/**
	 * 소분류 용
	 */
	@Column (name = "class_cd", length = 30)
	private String classCd;
	
	/**
	 * 방면 분류 용
	 */
	@Column (name = "box_class_cd", length = 30)
	private String boxClassCd;

	@Column (name = "pack_type", length = 20)
	private String packType;

	@Column (name = "insp_type", length = 20)
	private String inspType;

	@Column (name = "pass_flag", length = 1)
	private Boolean passFlag;

	@Column (name = "status", length = 10)
	private String status;

	@Column (name = "inspector_id", length = 32)
	private String inspectorId;

	@Column (name = "insp_started_at", length = 22)
	private String inspStartedAt;

	@Column (name = "insp_ended_at", length = 22)
	private String inspEndedAt;

	@Column (name = "cancel_flag", length = 1)
	private Boolean cancelFlag;
  
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

	public String getWcsBatchNo() {
		return wcsBatchNo;
	}

	public void setWcsBatchNo(String wcsBatchNo) {
		this.wcsBatchNo = wcsBatchNo;
	}

	public String getWmsBatchNo() {
		return wmsBatchNo;
	}

	public void setWmsBatchNo(String wmsBatchNo) {
		this.wmsBatchNo = wmsBatchNo;
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

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getCustOrderNo() {
		return custOrderNo;
	}

	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
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

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	
	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
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

	public Integer getBoxSeq() {
		return boxSeq;
	}

	public void setBoxSeq(Integer boxSeq) {
		this.boxSeq = boxSeq;
	}

	public Integer getSkuQty() {
		return skuQty;
	}

	public void setSkuQty(Integer skuQty) {
		this.skuQty = skuQty;
	}

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public Float getBoxWt() {
		return boxWt;
	}

	public void setBoxWt(Float boxWt) {
		this.boxWt = boxWt;
	}

	public Float getBoxWtMin() {
		return boxWtMin;
	}

	public void setBoxWtMin(Float boxWtMin) {
		this.boxWtMin = boxWtMin;
	}

	public Float getBoxWtMax() {
		return boxWtMax;
	}

	public void setBoxWtMax(Float boxWtMax) {
		this.boxWtMax = boxWtMax;
	}

	public String getClassCd() {
		return classCd;
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

	public String getPackType() {
		return packType;
	}

	public void setPackType(String packType) {
		this.packType = packType;
	}

	public String getInspType() {
		return inspType;
	}

	public void setInspType(String inspType) {
		this.inspType = inspType;
	}

	public Boolean getPassFlag() {
		return passFlag;
	}

	public void setPassFlag(Boolean passFlag) {
		this.passFlag = passFlag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInspectorId() {
		return inspectorId;
	}

	public void setInspectorId(String inspectorId) {
		this.inspectorId = inspectorId;
	}

	public String getInspStartedAt() {
		return inspStartedAt;
	}

	public void setInspStartedAt(String inspStartedAt) {
		this.inspStartedAt = inspStartedAt;
	}

	public String getInspEndedAt() {
		return inspEndedAt;
	}

	public void setInspEndedAt(String inspEndedAt) {
		this.inspEndedAt = inspEndedAt;
	}

	public Boolean getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(Boolean cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

}
