package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "box_items", idStrategy = GenerationRule.UUID)
public class BoxItem extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 434956497492551752L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "box_pack_id", nullable = false, length = 40)
	private String boxPackId;

	@Column (name = "order_id", length = 40)
	private String orderId;

	@Column (name = "order_no", length = 40)
	private String orderNo;

	@Column (name = "order_line_no", length = 40)
	private String orderLineNo;

	@Column (name = "order_detail_id", length = 40)
	private String orderDetailId;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "shop_cd", length = 30)
	private String shopCd;

	@Column (name = "sku_cd", nullable = false, length = 30)
	private String skuCd;

	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	@Column (name = "sku_wt", length = 19)
	private Float skuWt;

	@Column (name = "pack_type", length = 20)
	private String packType;

	@Column (name = "pick_qty", length = 12)
	private Integer pickQty;

	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	@Column (name = "cancel_qty", length = 12)
	private Integer cancelQty;

	@Column (name = "pass_flag", length = 1)
	private Boolean passFlag;

	@Column (name = "status", length = 10)
	private String status;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBoxPackId() {
		return boxPackId;
	}

	public void setBoxPackId(String boxPackId) {
		this.boxPackId = boxPackId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public Float getSkuWt() {
		return skuWt;
	}

	public void setSkuWt(Float skuWt) {
		this.skuWt = skuWt;
	}

	public String getPackType() {
		return packType;
	}

	public void setPackType(String packType) {
		this.packType = packType;
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

	public Integer getCancelQty() {
		return cancelQty;
	}

	public void setCancelQty(Integer cancelQty) {
		this.cancelQty = cancelQty;
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
}
