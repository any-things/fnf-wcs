package operato.fnf.wcs.entity;

import java.util.Date; 
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "mhe_pas_rlst", idStrategy = GenerationRule.UUID)
public class WcsMhePasRlst extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 965711965809362155L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_no", nullable = false, length = 20)
	private String batchNo;

	@Column (name = "job_type", nullable = false, length = 1)
	private String jobType;

	@Column (name = "box_id", nullable = false, length = 30)
	private String boxId;

	@Column (name = "chute_no", nullable = false, length = 3)
	private String chuteNo;

	@Column (name = "sku_cd", nullable = false, length = 20)
	private String skuCd;

	@Column (name = "sku_bcd", length = 30)
	private String skuBcd;

	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;

	@Column (name = "qty", length = 10)
	private Integer qty;

	@Column (name = "dmg_qty", length = 10)
	private Integer dmgQty;

	@Column (name = "new_qty", length = 10)
	private Integer newQty;

	@Column (name = "new_yn", length = 1)
	private String newYn;

	@Column (name = "if_yn", length = 1)
	private String ifYn;

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

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public Integer getDmgQty() {
		return dmgQty;
	}

	public void setDmgQty(Integer dmgQty) {
		this.dmgQty = dmgQty;
	}

	public Integer getNewQty() {
		return newQty;
	}

	public void setNewQty(Integer newQty) {
		this.newQty = newQty;
	}

	public String getNewYn() {
		return newYn;
	}

	public void setNewYn(String newYn) {
		this.newYn = newYn;
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
}
