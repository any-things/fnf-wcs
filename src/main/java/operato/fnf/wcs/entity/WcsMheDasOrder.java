package operato.fnf.wcs.entity;

import java.util.Date; 
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_das_order", idStrategy = GenerationRule.UUID, uniqueFields="batchNo,jobDate,jobType,cellNo,item_cd", indexes = {
		@Index(name = "ix_mhe_das_order_01", columnList = "batch_no,job_date,job_type,cell_no,item_cd", unique = true)
	})
public class WcsMheDasOrder extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 294115493292621393L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_no", nullable = false, length = 20)
	private String batchNo;

	@Column (name = "job_date", nullable = false, length = 8)
	private String jobDate;

	@Column (name = "job_type", nullable = false, length = 1)
	private String jobType;

	@Column (name = "cell_no", nullable = false, length = 10)
	private String cellNo;

	@Column (name = "chute_no", length = 3)
	private String chuteNo;

	@Column (name = "shop_cd", length = 20)
	private String shopCd;

	@Column (name = "shop_nm", length = 200)
	private String shopNm;

	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;

	@Column (name = "barcode", length = 20)
	private String barcode;

	@Column (name = "barcode2", length = 20)
	private String barcode2;

	@Column (name = "strr_id", length = 10)
	private String strrId;

	@Column (name = "item_season", length = 10)
	private String itemSeason;

	@Column (name = "item_style", length = 30)
	private String itemStyle;

	@Column (name = "item_color", length = 10)
	private String itemColor;

	@Column (name = "item_size", length = 10)
	private String itemSize;

	@Column (name = "order_qty", length = 10)
	private Integer orderQty;

	@Column (name = "if_yn", length = 1)
	private String ifYn;

	@Column (name = "ins_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date insDatetime;

	@Column (name = "upd_datetime", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date updDatetime;
  
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

	public String getCellNo() {
		return cellNo;
	}

	public void setCellNo(String cellNo) {
		this.cellNo = cellNo;
	}

	public String getChuteNo() {
		return chuteNo;
	}

	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
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

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getBarcode2() {
		return barcode2;
	}

	public void setBarcode2(String barcode2) {
		this.barcode2 = barcode2;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getItemSeason() {
		return itemSeason;
	}

	public void setItemSeason(String itemSeason) {
		this.itemSeason = itemSeason;
	}

	public String getItemStyle() {
		return itemStyle;
	}

	public void setItemStyle(String itemStyle) {
		this.itemStyle = itemStyle;
	}

	public String getItemColor() {
		return itemColor;
	}

	public void setItemColor(String itemColor) {
		this.itemColor = itemColor;
	}

	public String getItemSize() {
		return itemSize;
	}

	public void setItemSize(String itemSize) {
		this.itemSize = itemSize;
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
}
