package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "wcs_cells", idStrategy = GenerationRule.UUID, uniqueFields="id", indexes = {
	@Index(name = "ix_wcs_cells_0", columnList = "id", unique = true)
})
public class WcsCell extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 378148115739415020L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "wh_cd", length = 20)
	private String whCd;

	@Column (name = "wcell_no", length = 20)
	private String wcellNo;

	@Column (name = "building_tcd", length = 20)
	private String buildingTcd;

	@Column (name = "floor_tcd", length = 20)
	private String floorTcd;

	@Column (name = "space_cbm")
	private Float spaceCbm;

	@Column (name = "zone_cd", length = 20)
	private String zoneCd;

	@Column (name = "site_tcd", length = 20)
	private String siteTcd;

	@Column (name = "strloc_tcd", length = 20)
	private String strlocTcd;

	@Column (name = "wcell_strg_cd", length = 20)
	private String wcellStrgCd;

	@Column (name = "flag", length = 1)
	private String flag;

	@Column (name = "gen_date", length = 8)
	private String genDate;

	@Column (name = "item_cd", length = 64)
	private String itemCd;

	@Column (name = "used_cbm")
	private Float usedCbm;

	@Column (name = "used_rate")
	private Float usedRate;
  
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

	public String getWcellNo() {
		return wcellNo;
	}

	public void setWcellNo(String wcellNo) {
		this.wcellNo = wcellNo;
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

	public Float getSpaceCbm() {
		return spaceCbm;
	}

	public void setSpaceCbm(Float spaceCbm) {
		this.spaceCbm = spaceCbm;
	}

	public String getZoneCd() {
		return zoneCd;
	}

	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}

	public String getSiteTcd() {
		return siteTcd;
	}

	public void setSiteTcd(String siteTcd) {
		this.siteTcd = siteTcd;
	}

	public String getStrlocTcd() {
		return strlocTcd;
	}

	public void setStrlocTcd(String strlocTcd) {
		this.strlocTcd = strlocTcd;
	}

	public String getWcellStrgCd() {
		return wcellStrgCd;
	}

	public void setWcellStrgCd(String wcellStrgCd) {
		this.wcellStrgCd = wcellStrgCd;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getGenDate() {
		return genDate;
	}

	public void setGenDate(String genDate) {
		this.genDate = genDate;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public Float getUsedCbm() {
		return usedCbm;
	}

	public void setUsedCbm(Float usedCbm) {
		this.usedCbm = usedCbm;
	}

	public Float getUsedRate() {
		return usedRate;
	}

	public void setUsedRate(Float usedRate) {
		this.usedRate = usedRate;
	}	
}
