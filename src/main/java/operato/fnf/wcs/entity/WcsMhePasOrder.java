package operato.fnf.wcs.entity;

import java.util.Date; 
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_pas_order", idStrategy = GenerationRule.UUID, uniqueFields="batchNo,jobDate,jobType,boxId,chuteNo,skuCd,mheNo", indexes = {
		@Index(name = "ix_mhe_pas_order_01", columnList = "batch_no,job_date,job_type,box_id,chute_no,sku_cd,mhe_no", unique = true)
	})
public class WcsMhePasOrder extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 304397296243144420L;
	
	/**
	 * JOB_TYPE : 출고
	 */
	public static final String JOB_TYPE_DAS = "1";	
	
	/**
	 * JOB_TYPE : 반품
	 */
	public static final String JOB_TYPE_RTN = "0";
	
	/**
	 * 출고인 경우 BOX_ID
	 */
	public static final String DAS_BOX_ID = "9";

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_no", nullable = false, length = 20)
	private String batchNo;

	@Column (name = "job_date", nullable = false, length = 8)
	private String jobDate;

	@Column (name = "job_type", nullable = false, length = 1)
	private String jobType;

	@Column (name = "box_id", nullable = false, length = 8)
	private String boxId;

	@Column (name = "chute_no", nullable = false, length = 3)
	private String chuteNo;

	@Column (name = "sku_cd", nullable = false, length = 20)
	private String skuCd;

	@Column (name = "sku_bcd", length = 20)
	private String skuBcd;
	
	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;

	@Column (name = "shop_cd", length = 20)
	private String shopCd;

	@Column (name = "shop_nm", length = 200)
	private String shopNm;

	@Column (name = "order_qty", length = 10)
	private Integer orderQty;

	@Column (name = "if_yn", length = 1)
	private String ifYn;

	@Column (name = "ins_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date insDatetime;

	@Column (name = "upd_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date updDatetime;
	
	@Column (name = "input_date", length = 8)
	private String inputDate;
	
	@Column (name = "strr_id", length = 20)
	private String strrId;
  
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

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getChuteNo() {
		return chuteNo;
	}

	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuBcd() {
		return skuBcd;
	}

	public void setSkuBcd(String skuBcd) {
		this.skuBcd = skuBcd;
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

	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}

	public String getIfYn() {
		return ifYn;
	}

	public void setIfYn(String ifYn) {
		this.ifYn = ifYn;
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

	public String getInputDate() {
		return inputDate;
	}

	public void setInputDate(String inputDate) {
		this.inputDate = inputDate;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}
}
