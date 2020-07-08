package operato.fnf.wcs.service.model;

import operato.fnf.wcs.entity.RfidBoxResult;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(ignoreDdl = true, dataSourceType=DataSourceType.DATASOURCE, idStrategy = GenerationRule.NONE)
public class RfidResult extends RfidBoxResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = 41123123L;
	
	private String noWaybill;
	private String resultSt;
	private String tpStatus;
	private String dmBfRecv;
	private String dmAfRecv;
	private String dsErrmsg;
	private String stageCd;
	private String equipType;
	private String equipCd;
	private String equipGroupCd;
	private String equipNm;
	
	public String getNoWaybill() {
		return noWaybill;
	}
	public void setNoWaybill(String noWaybill) {
		this.noWaybill = noWaybill;
	}
	public String getResultSt() {
		return resultSt;
	}
	public void setResultSt(String resultSt) {
		this.resultSt = resultSt;
	}
	public String getTpStatus() {
		return tpStatus;
	}
	public void setTpStatus(String tpStatus) {
		this.tpStatus = tpStatus;
	}
	public String getDmBfRecv() {
		return dmBfRecv;
	}
	public void setDmBfRecv(String dmBfRecv) {
		this.dmBfRecv = dmBfRecv;
	}
	public String getDmAfRecv() {
		return dmAfRecv;
	}
	public void setDmAfRecv(String dmAfRecv) {
		this.dmAfRecv = dmAfRecv;
	}
	public String getDsErrmsg() {
		return dsErrmsg;
	}
	public void setDsErrmsg(String dsErrmsg) {
		this.dsErrmsg = dsErrmsg;
	}
	public String getEquipCd() {
		return equipCd;
	}
	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}
	public String getStageCd() {
		return stageCd;
	}
	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}
	public String getEquipType() {
		return equipType;
	}
	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}
	public String getEquipGroupCd() {
		return equipGroupCd;
	}
	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}
	public String getEquipNm() {
		return equipNm;
	}
	public void setEquipNm(String equipNm) {
		this.equipNm = equipNm;
	}
}
