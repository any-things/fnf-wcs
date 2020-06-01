package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Table;

/**
 * 출력 송장 인쇄 정보 조회를 위한 WMS I/F 테이블
 */
@Table(name = "mps_express_waybill_print", idStrategy = GenerationRule.UUID, dataSourceType=DataSourceType.DATASOURCE)
public class WmsExpressWaybillPrint extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -7012780399286209661L;

	@Ignore
	private String id;
	
	@Column (name = "wh_cd", length = 50)
	private String whCd;

	@Column (name = "box_id", length = 50)
	private String boxId;

	@Column (name = "carrier_tcd", length = 50)
	private String carrierTcd;

	@Column (name = "waybill_no", length = 50)
	private String waybillNo;

	@Column (name = "online_order_no", length = 50)
	private String onlineOrderNo;

	@Column (name = "waybill_dt", length = 50)
	private String waybillDt;

	@Column (name = "waybill_ds", length = 50)
	private String waybillDs;

	@Column (name = "cmpt_yn", length = 50)
	private String cmptYn;

	@Column (name = "print_group", length = 50)
	private String printGroup;

	@Column (name = "strr_id", length = 50)
	private String strrId;

	@Column (name = "rep_outb_no", length = 50)
	private String repOutbNo;

	@Column (name = "cvraddr", length = 50)
	private String cvraddr;

	@Column (name = "shipto_id", length = 50)
	private String shiptoId;

	@Column (name = "shipto_nm", length = 50)
	private String shiptoNm;

	@Column (name = "shipto_nm_mask", length = 50)
	private String shiptoNmMask;

	@Column (name = "shipto_tel_no", length = 50)
	private String shiptoTelNo;

	@Column (name = "shipto_tel_mask", length = 50)
	private String shiptoTelMask;

	@Column (name = "cust_id", length = 50)
	private String custId;

	@Column (name = "cust_nm", length = 50)
	private String custNm;

	@Column (name = "shipto_zip", length = 50)
	private String shiptoZip;

	@Column (name = "sndprsnaddr", length = 50)
	private String sndprsnaddr;

	@Column (name = "send_nm", length = 50)
	private String sendNm;

	@Column (name = "send_tel_no", length = 50)
	private String sendTelNo;

	@Column (name = "send_info", length = 50)
	private String sendInfo;

	@Column (name = "fare_nm", length = 50)
	private String fareNm;

	@Column (name = "item_nm", length = 50)
	private String itemNm;

	@Column (name = "rmk_comment", length = 50)
	private String rmkComment;

	@Column (name = "total_qty", length = 50)
	private String totalQty;

	@Column (name = "boxtyp", length = 50)
	private String boxtyp;

	@Column (name = "clntmgmcustcd", length = 50)
	private String clntmgmcustcd;

	@Column (name = "cntrlarccd", length = 50)
	private String cntrlarccd;

	@Column (name = "farediv", length = 50)
	private String farediv;

	@Column (name = "prngdivcd", length = 50)
	private String prngdivcd;

	@Column (name = "orderno", length = 50)
	private String orderno;

	@Column (name = "hub_cod_1", length = 50)
	private String hubCod_1;

	@Column (name = "tml_cod_2", length = 50)
	private String tmlCod_2;

	@Column (name = "tml_nam_3", length = 50)
	private String tmlNam_3;

	@Column (name = "dom_mid_4", length = 50)
	private String domMid_4;

	@Column (name = "cen_cod_5", length = 50)
	private String cenCod_5;

	@Column (name = "dom_pdz_6", length = 50)
	private String domPdz_6;

	@Column (name = "cen_nam_7", length = 50)
	private String cenNam_7;

	@Column (name = "es_nam_8", length = 50)
	private String esNam_8;

	@Column (name = "s_tml_cod_9", length = 50)
	private String sTmlCod_9;

	@Column (name = "s_tml_nam_10", length = 50)
	private String sTmlNam_10;

	@Column (name = "pdz_nam_12", length = 50)
	private String pdzNam_12;

	@Column (name = "fare_std_13", length = 50)
	private String fareStd_13;

	@Column (name = "dom_rgn_15", length = 50)
	private String domRgn_15;

	@Column (name = "user_def1", length = 50)
	private String userDef1;

	@Column (name = "user_def2", length = 50)
	private String userDef2;

	@Column (name = "user_def3", length = 50)
	private String userDef3;

	@Column (name = "user_def4", length = 50)
	private String userDef4;

	@Column (name = "user_def5", length = 50)
	private String userDef5;

	@Column (name = "pg", length = 50)
	private String pg;

	@Column (name = "chute_no", length = 50)
	private String chuteNo;

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

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getCarrierTcd() {
		return carrierTcd;
	}

	public void setCarrierTcd(String carrierTcd) {
		this.carrierTcd = carrierTcd;
	}

	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	public String getOnlineOrderNo() {
		return onlineOrderNo;
	}

	public void setOnlineOrderNo(String onlineOrderNo) {
		this.onlineOrderNo = onlineOrderNo;
	}

	public String getWaybillDt() {
		return waybillDt;
	}

	public void setWaybillDt(String waybillDt) {
		this.waybillDt = waybillDt;
	}

	public String getWaybillDs() {
		return waybillDs;
	}

	public void setWaybillDs(String waybillDs) {
		this.waybillDs = waybillDs;
	}

	public String getCmptYn() {
		return cmptYn;
	}

	public void setCmptYn(String cmptYn) {
		this.cmptYn = cmptYn;
	}

	public String getPrintGroup() {
		return printGroup;
	}

	public void setPrintGroup(String printGroup) {
		this.printGroup = printGroup;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getRepOutbNo() {
		return repOutbNo;
	}

	public void setRepOutbNo(String repOutbNo) {
		this.repOutbNo = repOutbNo;
	}

	public String getCvraddr() {
		return cvraddr;
	}

	public void setCvraddr(String cvraddr) {
		this.cvraddr = cvraddr;
	}

	public String getShiptoId() {
		return shiptoId;
	}

	public void setShiptoId(String shiptoId) {
		this.shiptoId = shiptoId;
	}

	public String getShiptoNm() {
		return shiptoNm;
	}

	public void setShiptoNm(String shiptoNm) {
		this.shiptoNm = shiptoNm;
	}

	public String getShiptoNmMask() {
		return shiptoNmMask;
	}

	public void setShiptoNmMask(String shiptoNmMask) {
		this.shiptoNmMask = shiptoNmMask;
	}

	public String getShiptoTelNo() {
		return shiptoTelNo;
	}

	public void setShiptoTelNo(String shiptoTelNo) {
		this.shiptoTelNo = shiptoTelNo;
	}

	public String getShiptoTelMask() {
		return shiptoTelMask;
	}

	public void setShiptoTelMask(String shiptoTelMask) {
		this.shiptoTelMask = shiptoTelMask;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCustNm() {
		return custNm;
	}

	public void setCustNm(String custNm) {
		this.custNm = custNm;
	}

	public String getShiptoZip() {
		return shiptoZip;
	}

	public void setShiptoZip(String shiptoZip) {
		this.shiptoZip = shiptoZip;
	}

	public String getSndprsnaddr() {
		return sndprsnaddr;
	}

	public void setSndprsnaddr(String sndprsnaddr) {
		this.sndprsnaddr = sndprsnaddr;
	}

	public String getSendNm() {
		return sendNm;
	}

	public void setSendNm(String sendNm) {
		this.sendNm = sendNm;
	}

	public String getSendTelNo() {
		return sendTelNo;
	}

	public void setSendTelNo(String sendTelNo) {
		this.sendTelNo = sendTelNo;
	}

	public String getSendInfo() {
		return sendInfo;
	}

	public void setSendInfo(String sendInfo) {
		this.sendInfo = sendInfo;
	}

	public String getFareNm() {
		return fareNm;
	}

	public void setFareNm(String fareNm) {
		this.fareNm = fareNm;
	}

	public String getItemNm() {
		return itemNm;
	}

	public void setItemNm(String itemNm) {
		this.itemNm = itemNm;
	}

	public String getRmkComment() {
		return rmkComment;
	}

	public void setRmkComment(String rmkComment) {
		this.rmkComment = rmkComment;
	}

	public String getTotalQty() {
		return totalQty;
	}

	public void setTotalQty(String totalQty) {
		this.totalQty = totalQty;
	}

	public String getBoxtyp() {
		return boxtyp;
	}

	public void setBoxtyp(String boxtyp) {
		this.boxtyp = boxtyp;
	}

	public String getClntmgmcustcd() {
		return clntmgmcustcd;
	}

	public void setClntmgmcustcd(String clntmgmcustcd) {
		this.clntmgmcustcd = clntmgmcustcd;
	}

	public String getCntrlarccd() {
		return cntrlarccd;
	}

	public void setCntrlarccd(String cntrlarccd) {
		this.cntrlarccd = cntrlarccd;
	}

	public String getFarediv() {
		return farediv;
	}

	public void setFarediv(String farediv) {
		this.farediv = farediv;
	}

	public String getPrngdivcd() {
		return prngdivcd;
	}

	public void setPrngdivcd(String prngdivcd) {
		this.prngdivcd = prngdivcd;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getHubCod_1() {
		return hubCod_1;
	}

	public void setHubCod_1(String hubCod_1) {
		this.hubCod_1 = hubCod_1;
	}

	public String getTmlCod_2() {
		return tmlCod_2;
	}

	public void setTmlCod_2(String tmlCod_2) {
		this.tmlCod_2 = tmlCod_2;
	}

	public String getTmlNam_3() {
		return tmlNam_3;
	}

	public void setTmlNam_3(String tmlNam_3) {
		this.tmlNam_3 = tmlNam_3;
	}

	public String getDomMid_4() {
		return domMid_4;
	}

	public void setDomMid_4(String domMid_4) {
		this.domMid_4 = domMid_4;
	}

	public String getCenCod_5() {
		return cenCod_5;
	}

	public void setCenCod_5(String cenCod_5) {
		this.cenCod_5 = cenCod_5;
	}

	public String getDomPdz_6() {
		return domPdz_6;
	}

	public void setDomPdz_6(String domPdz_6) {
		this.domPdz_6 = domPdz_6;
	}

	public String getCenNam_7() {
		return cenNam_7;
	}

	public void setCenNam_7(String cenNam_7) {
		this.cenNam_7 = cenNam_7;
	}

	public String getEsNam_8() {
		return esNam_8;
	}

	public void setEsNam_8(String esNam_8) {
		this.esNam_8 = esNam_8;
	}

	public String getsTmlCod_9() {
		return sTmlCod_9;
	}

	public void setsTmlCod_9(String sTmlCod_9) {
		this.sTmlCod_9 = sTmlCod_9;
	}

	public String getsTmlNam_10() {
		return sTmlNam_10;
	}

	public void setsTmlNam_10(String sTmlNam_10) {
		this.sTmlNam_10 = sTmlNam_10;
	}

	public String getPdzNam_12() {
		return pdzNam_12;
	}

	public void setPdzNam_12(String pdzNam_12) {
		this.pdzNam_12 = pdzNam_12;
	}

	public String getFareStd_13() {
		return fareStd_13;
	}

	public void setFareStd_13(String fareStd_13) {
		this.fareStd_13 = fareStd_13;
	}

	public String getDomRgn_15() {
		return domRgn_15;
	}

	public void setDomRgn_15(String domRgn_15) {
		this.domRgn_15 = domRgn_15;
	}

	public String getUserDef1() {
		return userDef1;
	}

	public void setUserDef1(String userDef1) {
		this.userDef1 = userDef1;
	}

	public String getUserDef2() {
		return userDef2;
	}

	public void setUserDef2(String userDef2) {
		this.userDef2 = userDef2;
	}

	public String getUserDef3() {
		return userDef3;
	}

	public void setUserDef3(String userDef3) {
		this.userDef3 = userDef3;
	}

	public String getUserDef4() {
		return userDef4;
	}

	public void setUserDef4(String userDef4) {
		this.userDef4 = userDef4;
	}

	public String getUserDef5() {
		return userDef5;
	}

	public void setUserDef5(String userDef5) {
		this.userDef5 = userDef5;
	}

	public String getPg() {
		return pg;
	}

	public void setPg(String pg) {
		this.pg = pg;
	}

	public String getChuteNo() {
		return chuteNo;
	}

	public void setChuteNo(String chuteNo) {
		this.chuteNo = chuteNo;
	}

}
