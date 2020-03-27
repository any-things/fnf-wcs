package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 반품 분류 박스별 분류 내역
 *  WCS => WMS
 * @author yang
 *
 */
@Table(name = "rtn_sort_dr", idStrategy = GenerationRule.UUID
     , dataSourceType=DataSourceType.DATASOURCE 
     , uniqueFields="whCd,mheNo,sortDate,sortSeq,itemCd,boxNo", indexes = {
})
public class WmsRtnSortDr extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 77262717811305511L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	@PrimaryKey
	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;

	@Column (name = "strr_id", nullable = false, length = 10)
	private String strrId;

	@PrimaryKey
	@Column (name = "sort_date", nullable = false, length = 10)
	private String sortDate;
	
	@PrimaryKey
	@Column (name = "sort_seq", nullable = false, length = 10)
	private String sortSeq;
	
	@PrimaryKey
	@Column (name = "item_cd", nullable = false, length = 10)
	private String itemCd;
	
	@PrimaryKey
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
