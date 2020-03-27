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
 * 반품 분류 장비 자체 배치 헤더 정보
 * WCS => WMS
 * @author yang
 *
 */
@Table(name = "rtn_sort_hr", idStrategy = GenerationRule.UUID
     , dataSourceType=DataSourceType.DATASOURCE 
     , uniqueFields="whCd,mheNo,sortDate,sortSeq", indexes = {
})
public class WmsRtnSortHr extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 5380911312353807384L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;

	@PrimaryKey
	@Column (name = "mhe_no", nullable = false, length = 10)
	private String mheNo;
	
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;
	
	@PrimaryKey
	@Column (name = "sort_date", nullable = false, length = 8)
	private String sortDate;

	@PrimaryKey
	@Column (name = "sort_seq", nullable = false, length = 3)
	private String sortSeq;
	
	@Column (name = "status", nullable = false, length = 1)
	private String status;
	
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}
}
