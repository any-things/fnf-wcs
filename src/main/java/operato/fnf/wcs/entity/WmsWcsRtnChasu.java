package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * 반품지시 차수 정보
 * 반품검수 EX-PAS
 */
@Table(name = "wcs_rtn_chasu", idStrategy = GenerationRule.UUID
     , dataSourceType=DataSourceType.DATASOURCE 
     , uniqueFields="strrId,season,type,seq", indexes = {
})
public class WmsWcsRtnChasu extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -7022107674329423325L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "strr_id", nullable = false, length = 2)
	private String strrId;
	
	@PrimaryKey
	@Column (name = "season", nullable = false, length = 4)
	private String season;
	
	@PrimaryKey
	@Column (name = "type", nullable = false, length = 3)
	private String type;
	
	@PrimaryKey
	@Column (name = "seq", nullable = false, length = 10)
	private Integer seq;
	
	@Column (name = "date_from", nullable = true, length = 20)
	private String dateFrom;

	@Column (name = "date_to", nullable = true, length = 20)
	private String dateTo;
	
	@Column (name = "src_remark", nullable = true, length = 1000)
	private String srcRemark;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getSeason() {
		return season;
	}

	public void setSeason(String season) {
		this.season = season;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getSrcRemark() {
		return srcRemark;
	}

	public void setSrcRemark(String srcRemark) {
		this.srcRemark = srcRemark;
	}

}
