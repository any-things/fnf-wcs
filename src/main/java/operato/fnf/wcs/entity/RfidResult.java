package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

/**
 * RFID 검수 실적
 * 
 * @author shortstop
 */
@Table(name = "rfid_results", idStrategy = GenerationRule.UUID, uniqueFields="rfidId", indexes = {
	@Index(name = "ix_rfid_results_0", columnList = "rfid_id", unique = true)
})
public class RfidResult extends xyz.elidom.orm.entity.basic.DomainCreateStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 459883625456404875L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", length = 40)
	private String batchId;

	@Column (name = "job_date", length = 10)
	private String jobDate;

	@Column (name = "brand_cd", length = 10)
	private String brandCd;

	@Column (name = "rfid_id", length = 40)
	private String rfidId;

	@Column (name = "invoice_id", length = 40)
	private String invoiceId;

	@Column (name = "box_id", length = 30)
	private String boxId;

	@Column (name = "order_no", length = 30)
	private String orderNo;

	@Column (name = "shop_cd", length = 30)
	private String shopCd;

	@Column (name = "sku_cd", length = 50)
	private String skuCd;

	@Column (name = "order_qty")
	private Integer orderQty;
  
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

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getBrandCd() {
		return brandCd;
	}

	public void setBrandCd(String brandCd) {
		this.brandCd = brandCd;
	}

	public String getRfidId() {
		return rfidId;
	}

	public void setRfidId(String rfidId) {
		this.rfidId = rfidId;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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

	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}	
}
