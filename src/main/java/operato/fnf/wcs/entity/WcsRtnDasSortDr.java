package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 반품 분류 박스별 분류 내역
 * 
 * @author yang
 */
@Table(name = "mhe_rtn_das_sort_dr", idStrategy = GenerationRule.UUID, uniqueFields="whCd,mheNo,sortDate,sortSeq,itemCd,boxNo", indexes = {
	@Index(name = "ix_rtn_das_sort_dr_01", columnList = "wh_cd,mhe_no,sort_date,sort_seq,item_cd,box_no", unique = true)
})
public class WcsRtnDasSortDr extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 77262717811305511L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;

	@Column (name = "strr_id", nullable = false, length = 10)
	private String strrId;

	@Column (name = "sort_date", nullable = false, length = 10)
	private String sortDate;
	
	@Column (name = "sort_seq", nullable = false, length = 10)
	private String sortSeq;
	
	@Column (name = "item_cd", nullable = false, length = 10)
	private String itemCd;
	
	@Column (name = "box_no", nullable = false, length = 10)
	private String boxNo;
	
	@Column (name = "cmpt_qty", nullable = false, length = 10)
	private Integer cmptQty;
	
	@Column (name = "ins_datetime", type = ColumnType.DATETIME)
	private Date insDatetime;

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

	public String getMheNo() {
		return mheNo;
	}

	public void setMheNo(String mheNo) {
		this.mheNo = mheNo;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getSortDate() {
		return sortDate;
	}

	public void setSortDate(String sortDate) {
		this.sortDate = sortDate;
	}

	public String getSortSeq() {
		return sortSeq;
	}

	public void setSortSeq(String sortSeq) {
		this.sortSeq = sortSeq;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getBoxNo() {
		return boxNo;
	}

	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}

	public Integer getCmptQty() {
		return cmptQty;
	}

	public void setCmptQty(Integer cmptQty) {
		this.cmptQty = cmptQty;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}

	
	
}
