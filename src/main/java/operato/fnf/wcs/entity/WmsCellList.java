package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "vms_cell_list"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
	, dataSourceType=DataSourceType.DATASOURCE)
public class WmsCellList extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 837266182387975146L;

	@Column (name = "wh_cd", length = 20)
	private String whCd;

	@Column (name = "building_tcd", length = 20)
	private String buildingTcd;

	@Column (name = "floor_tcd", length = 20)
	private String floorTcd;

	@Column (name = "wcell_no", length = 20)
	private String wcellNo;

	@Column (name = "zone_cd", length = 20)
	private String zoneCd;

	@Column (name = "wcell_strg_cd", length = 20)
	private String wcellStrgCd;

	@Column (name = "strloc_tcd", length = 20)
	private String strlocTcd;

	@Column (name = "site_tcd", length = 20)
	private String siteTcd;

	@Column (name = "space_cbm")
	private Float spaceCbm;
  
	public String getWhCd() {
		return whCd;
	}

	public void setWhCd(String whCd) {
		this.whCd = whCd;
	}

	public String getBuildingTcd() {
		return buildingTcd;
	}

	public void setBuildingTcd(String buildingTcd) {
		this.buildingTcd = buildingTcd;
	}

	public String getFloorTcd() {
		return floorTcd;
	}

	public void setFloorTcd(String floorTcd) {
		this.floorTcd = floorTcd;
	}

	public String getWcellNo() {
		return wcellNo;
	}

	public void setWcellNo(String wcellNo) {
		this.wcellNo = wcellNo;
	}

	public String getZoneCd() {
		return zoneCd;
	}

	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}

	public String getWcellStrgCd() {
		return wcellStrgCd;
	}

	public void setWcellStrgCd(String wcellStrgCd) {
		this.wcellStrgCd = wcellStrgCd;
	}

	public String getStrlocTcd() {
		return strlocTcd;
	}

	public void setStrlocTcd(String strlocTcd) {
		this.strlocTcd = strlocTcd;
	}

	public String getSiteTcd() {
		return siteTcd;
	}

	public void setSiteTcd(String siteTcd) {
		this.siteTcd = siteTcd;
	}

	public Float getSpaceCbm() {
		return spaceCbm;
	}

	public void setSpaceCbm(Float spaceCbm) {
		this.spaceCbm = spaceCbm;
	}
}
