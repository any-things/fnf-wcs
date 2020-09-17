package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "vms_cell_use_rate", ignoreDdl = true, dataSourceType=DataSourceType.DATASOURCE)
public class WmsCellUseRate extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 5380911312111807384L;
	
	private String whCd;
	private String buildingTcd;
	private String floorTcd;
	private Integer totLocCnt;
	private Integer useLocCnt;
	private Float rtUseLoc;
	
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
	public Integer getTotLocCnt() {
		return totLocCnt;
	}
	public void setTotLocCnt(Integer totLocCnt) {
		this.totLocCnt = totLocCnt;
	}
	public Integer getUseLocCnt() {
		return useLocCnt;
	}
	public void setUseLocCnt(Integer useLocCnt) {
		this.useLocCnt = useLocCnt;
	}
	public Float getRtUseLoc() {
		return rtUseLoc;
	}
	public void setRtUseLoc(Float rtUseLoc) {
		this.rtUseLoc = rtUseLoc;
	}
}
