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
 * 중량검수내역수신
 * DAS, 소터, DPC
 * 
 * @author yang
 */
@Table(name = "outb_box_weight"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
	, dataSourceType=DataSourceType.DATASOURCE
	, uniqueFields="boxId")
public class WmsOutbBoxWeight extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -4441032325964241806L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "box_id", nullable = false, length = 30)
	private String boxId;
	
	@Column (name = "weight", nullable = true, length = 19)
	private Float weight;
	
	@Column (name = "ins_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date insDatetime;
	
	@Column (name = "if_yn", nullable = true, length = 1)
	private String ifYn;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}

	public String getIfYn() {
		return ifYn;
	}

	public void setIfYn(String ifYn) {
		this.ifYn = ifYn;
	}
}
