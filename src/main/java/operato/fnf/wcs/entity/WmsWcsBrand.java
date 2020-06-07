package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 브랜드 목록
 * 공통
 * 
 * @author yang
 */
@Table(name = "wcs_brand"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
	, dataSourceType=DataSourceType.DATASOURCE 
	, uniqueFields="strrId")
public class WmsWcsBrand extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -2166235825705061495L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;
	
	@Column (name = "strr_nm", nullable = false, length = 20)
	private String strrNm;

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

	public String getStrrNm() {
		return strrNm;
	}

	public void setStrrNm(String strrNm) {
		this.strrNm = strrNm;
	}

}
