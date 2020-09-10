package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "wcs_imp_dps_repl_buffer_rcmd", ignoreDdl = true, idStrategy = GenerationRule.UUID, dataSourceType=DataSourceType.DATASOURCE)
public class WmsImpDpsReplBufferRcmd extends xyz.elidom.orm.entity.basic.AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -343210908418838995L;
	
	private String interfaceCrtDt;
	private String interfaceNo;
	private String whCd;
	private String strrId;
	private String itemCd;
	private Integer safetyDay;	// 안전재고일
	private Integer agvShipQty;	// 평균출고수량
	private Integer safetyQty;	// 안전재고
	private Integer wcsNeedQty;	// wcs필요재고
	private Float itemPrty;	//상품우선순위(인기도)
	private String ifCrtId;	// 생성자
	private String ifCrtDtm;	// 생성일시
	private String ifCheck;	// IF확인여부
	private String ifCheckDtm;	// IF확인일시
	private String ifErrMsg;	// IF에러메세지
	
	public String getInterfaceCrtDt() {
		return interfaceCrtDt;
	}
	public void setInterfaceCrtDt(String interfaceCrtDt) {
		this.interfaceCrtDt = interfaceCrtDt;
	}
	public String getInterfaceNo() {
		return interfaceNo;
	}
	public void setInterfaceNo(String interfaceNo) {
		this.interfaceNo = interfaceNo;
	}
	public String getWhCd() {
		return whCd;
	}
	public void setWhCd(String whCd) {
		this.whCd = whCd;
	}
	public String getStrrId() {
		return strrId;
	}
	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}
	public String getItemCd() {
		return itemCd;
	}
	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}
	public Integer getSafetyDay() {
		return safetyDay;
	}
	public void setSafetyDay(Integer safetyDay) {
		this.safetyDay = safetyDay;
	}
	public Integer getAgvShipQty() {
		return agvShipQty;
	}
	public void setAgvShipQty(Integer agvShipQty) {
		this.agvShipQty = agvShipQty;
	}
	public Integer getSafetyQty() {
		return safetyQty;
	}
	public void setSafetyQty(Integer safetyQty) {
		this.safetyQty = safetyQty;
	}
	public Integer getWcsNeedQty() {
		return wcsNeedQty;
	}
	public void setWcsNeedQty(Integer wcsNeedQty) {
		this.wcsNeedQty = wcsNeedQty;
	}
	public Float getItemPrty() {
		return itemPrty;
	}
	public void setItemPrty(Float itemPrty) {
		this.itemPrty = itemPrty;
	}
	public String getIfCrtId() {
		return ifCrtId;
	}
	public void setIfCrtId(String ifCrtId) {
		this.ifCrtId = ifCrtId;
	}
	public String getIfCrtDtm() {
		return ifCrtDtm;
	}
	public void setIfCrtDtm(String ifCrtDtm) {
		this.ifCrtDtm = ifCrtDtm;
	}
	public String getIfCheck() {
		return ifCheck;
	}
	public void setIfCheck(String ifCheck) {
		this.ifCheck = ifCheck;
	}
	public String getIfCheckDtm() {
		return ifCheckDtm;
	}
	public void setIfCheckDtm(String ifCheckDtm) {
		this.ifCheckDtm = ifCheckDtm;
	}
	public String getIfErrMsg() {
		return ifErrMsg;
	}
	public void setIfErrMsg(String ifErrMsg) {
		this.ifErrMsg = ifErrMsg;
	}
}
