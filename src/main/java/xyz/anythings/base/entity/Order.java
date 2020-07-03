package xyz.anythings.base.entity;

import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "orders", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_orders_0", columnList = "domain_id,batch_id,order_no"),
	@Index(name = "ix_orders_1", columnList = "domain_id,batch_id,wms_batch_no,wcs_batch_no"),
	@Index(name = "ix_orders_2", columnList = "domain_id,job_date,job_seq,area_cd,stage_cd,equip_type,equip_cd,sub_equip_cd,status"),
	@Index(name = "ix_orders_3", columnList = "domain_id,batch_id,box_id,invoice_id,box_type_cd"),
	@Index(name = "ix_orders_4", columnList = "domain_id,batch_id,class_cd,box_class_cd,sku_cd,shop_cd")
})
public class Order extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 166602676775758234L;
	
	/**
	 * 상태 W : 대기 
	 */
	public static final String STATUS_WAIT = "W";	
	
	/**
	 * 상태 C : 수신 취소  
	 */
	public static final String STATUS_CANCEL = "C";
	
	/**
	 * 상태 T : 대상 분류    
	 */
	public static final String STATUS_TYPE = "T";
	
	/**
	 * 상태 I : 작업 지시   
	 */
	public static final String STATUS_INSTRUCT = "I";
	
	/**
	 * 상태 A : 작업 할당 
	 */
	public static final String STATUS_ASSIGN = "A";
	
	/**
	 * 상태 R : 진행 중
	 */
	public static final String STATUS_RUNNING = "R";
	
	/**
	 * 상태 F : 완료
	 */
	public static final String STATUS_FINISHED = "F";

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "wms_batch_no", length = 40)
	private String wmsBatchNo;

	@Column (name = "wcs_batch_no", length = 40)
	private String wcsBatchNo;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", length = 10)
	private String jobSeq;

	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "order_date", length = 10)
	private String orderDate;

	@Column (name = "order_no", nullable = false, length = 40)
	private String orderNo;

	@Column (name = "order_line_no", length = 40)
	private String orderLineNo;

	@Column (name = "order_detail_id", length = 40)
	private String orderDetailId;

	@Column (name = "cust_order_no", length = 40)
	private String custOrderNo;

	@Column (name = "cust_order_line_no", length = 40)
	private String custOrderLineNo;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "equip_type", length = 30)
	private String equipType;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	@Column (name = "shop_cd", length = 30)
	private String shopCd;

	@Column (name = "shop_nm", length = 40)
	private String shopNm;

	@Column (name = "sku_cd", nullable = false, length = 30)
	private String skuCd;

	@Column (name = "sku_barcd", length = 30)
	private String skuBarcd;
	
	@Column (name = "sku_barcd2", length = 30)
	private String skuBarcd2;
	
	@Column (name = "sku_type", length = 20)
	private String skuType;

	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	@Column (name = "box_type_cd", length = 30)
	private String boxTypeCd;

	@Column (name = "box_in_qty", length = 12)
	private Integer boxInQty;

	@Column (name = "order_qty", nullable = false, length = 12)
	private Integer orderQty;

	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	@Column (name = "boxed_qty", length = 12)
	private Integer boxedQty;

	@Column (name = "cancel_qty", length = 12)
	private Integer cancelQty;

	@Column (name = "box_id", length = 40)
	private String boxId;

	@Column (name = "invoice_id", length = 40)
	private String invoiceId;

	@Column (name = "order_type", length = 20)
	private String orderType;

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

	@Column (name = "vehicle_no", length = 40)
	private String vehicleNo;

	@Column (name = "lot_no", length = 40)
	private String lotNo;

	@Column (name = "from_zone_cd", length = 30)
	private String fromZoneCd;

	@Column (name = "from_cell_cd", length = 30)
	private String fromCellCd;

	@Column (name = "to_zone_cd", length = 30)
	private String toZoneCd;

	@Column (name = "to_cell_cd", length = 30)
	private String toCellCd;

	@Column (name = "status", length = 10)
	private String status;
  
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

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
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

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderLineNo() {
		return orderLineNo;
	}

	public void setOrderLineNo(String orderLineNo) {
		this.orderLineNo = orderLineNo;
	}

	public String getOrderDetailId() {
		return orderDetailId;
	}

	public void setOrderDetailId(String orderDetailId) {
		this.orderDetailId = orderDetailId;
	}

	public String getCustOrderNo() {
		return custOrderNo;
	}

	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}

	public String getCustOrderLineNo() {
		return custOrderLineNo;
	}

	public void setCustOrderLineNo(String custOrderLineNo) {
		this.custOrderLineNo = custOrderLineNo;
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

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
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

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuBarcd() {
		return skuBarcd;
	}

	public void setSkuBarcd(String skuBarcd) {
		this.skuBarcd = skuBarcd;
	}

	public String getSkuBarcd2() {
		return skuBarcd2;
	}

	public void setSkuBarcd2(String skuBarcd2) {
		this.skuBarcd2 = skuBarcd2;
	}

	public String getSkuType() {
		return skuType;
	}

	public void setSkuType(String skuType) {
		this.skuType = skuType;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
	}

	public Integer getBoxInQty() {
		return boxInQty;
	}

	public void setBoxInQty(Integer boxInQty) {
		this.boxInQty = boxInQty;
	}

	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public Integer getBoxedQty() {
		return boxedQty;
	}

	public void setBoxedQty(Integer boxedQty) {
		this.boxedQty = boxedQty;
	}

	public Integer getCancelQty() {
		return cancelQty;
	}

	public void setCancelQty(Integer cancelQty) {
		this.cancelQty = cancelQty;
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

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
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

	public String getVehicleNo() {
		return vehicleNo;
	}

	public void setVehicleNo(String vehicleNo) {
		this.vehicleNo = vehicleNo;
	}

	public String getLotNo() {
		return lotNo;
	}

	public void setLotNo(String lotNo) {
		this.lotNo = lotNo;
	}

	public String getFromZoneCd() {
		return fromZoneCd;
	}

	public void setFromZoneCd(String fromZoneCd) {
		this.fromZoneCd = fromZoneCd;
	}

	public String getFromCellCd() {
		return fromCellCd;
	}

	public void setFromCellCd(String fromCellCd) {
		this.fromCellCd = fromCellCd;
	}

	public String getToZoneCd() {
		return toZoneCd;
	}

	public void setToZoneCd(String toZoneCd) {
		this.toZoneCd = toZoneCd;
	}

	public String getToCellCd() {
		return toCellCd;
	}

	public void setToCellCd(String toCellCd) {
		this.toCellCd = toCellCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	
	
	/**
	 * ID로 작업 배치 조회
	 *
	 * @param stageCd Stage ID
	 * @param id 배치 ID
	 * @param withLock 테이블 락을 걸지 여부
	 * @param exceptionWhenEmpty 조회 결과가 null이면 예외 발생 여부
	 * @return
	 */
	public static Order find(Long domainId, String batchId,String skuCd, boolean withLock, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batch_id", batchId);
		if(!skuCd.isEmpty())
			condition.addFilter("sku_cd", skuCd);
		
		Order order = withLock ?
				BeanUtil.get(IQueryManager.class).selectByConditionWithLock(Order.class, condition) :
				BeanUtil.get(IQueryManager.class).selectByCondition(Order.class, condition);

		if(order == null && exceptionWhenEmpty) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Order", batchId);
		}

		return order;
	}
}
