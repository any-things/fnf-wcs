package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * 매장 반품 검수 예정정보
 * 반품검수 EX-PAS
 * 
 * @author yang
 */
@Table(name = "wmt_uif_wcs_inb_rtn_cnfm"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
    , dataSourceType=DataSourceType.DATASOURCE 
    , uniqueFields="interfaceCrtDt,interfaceNo")
public class WmsWmtUifWcsInbRtnCnfm extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 3998809543420435760L;

	@Ignore
	private String id;
	
	@PrimaryKey
	@Column (name = "interface_crt_dt", nullable = false, length = 8)
	private String interfaceCrtDt;
	
	@PrimaryKey
	@Column (name = "interface_no", nullable = false, length = 15)
	private String interfaceNo;
	
	@Column (name = "wh_cd", nullable = true, length = 20)
	private String whCd;
	
	@Column (name = "strr_id", nullable = true, length = 20)
	private String strrId;
	
	@Column (name = "inb_tcd", nullable = true, length = 30)
	private String inbTcd;
	
	@Column (name = "inb__date", nullable = true, length = 8)
	private String inbDate;

	@Column (name = "ref_season", nullable = true, length = 10)
	private String refSeason;

	@Column (name = "ref_no", nullable = true, length = 30)
	private String refNo;
	
	@Column (name = "rtn_box_seq", nullable = true, length = 5)
	private String rtnBoxSeq;

	@Column (name = "suppr_id", nullable = true, length = 20)
	private String supprId;
	
	@Column (name = "suppr_nm", nullable = true, length = 100)
	private String supprNm;
	
	@Column (name = "rtn_ship_yn", nullable = true, length = 1)
	private String rtnShipYn;
	
	@Column (name = "ref_detl_no", nullable = true, length = 30)
	private String refDetlNo;

	@Column (name = "item_cd", nullable = true, length = 20)
	private String itemCd;
	
	@Column (name = "inb_ect_qty", nullable = true, length = 10)
	private Integer inbEctQty;
	
	@Column (name = "inb_cmpt_qty", nullable = true, length = 10)
	private Integer inbCmptQty;
	
	@Column (name = "rtrn_tcd", nullable = true, length = 20)
	private String rtrnTcd;
	
	@Column (name = "shop_rtn_type", nullable = true, length = 3)
	private String shopRtnType;
	
	@Column (name = "shop_rtn_seq", nullable = true, length = 10)
	private Integer shopRtnSeq;
	
	@Column (name = "if_crt_dtm", nullable = true, length = 14)
	private String ifCrtDtm;
	
	@Column (name = "if_chk", nullable = true, length = 1)
	private String ifChk;

	@Column (name = "if_chk_dtm", nullable = true, length = 14)
	private String ifChkDtm;
	
	@Column (name = "wcs_if_chk", nullable = true, length = 1)
	private String wcsIfChk;

	@Column (name = "wcs_if_chk_dtm", nullable = true, length = 14)
	private String wcsIfChkDtm;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getInbTcd() {
		return inbTcd;
	}

	public void setInbTcd(String inbTcd) {
		this.inbTcd = inbTcd;
	}

	public String getInbDate() {
		return inbDate;
	}

	public void setInbDate(String inbDate) {
		this.inbDate = inbDate;
	}

	public String getRefSeason() {
		return refSeason;
	}

	public void setRefSeason(String refSeason) {
		this.refSeason = refSeason;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getRtnBoxSeq() {
		return rtnBoxSeq;
	}

	public void setRtnBoxSeq(String rtnBoxSeq) {
		this.rtnBoxSeq = rtnBoxSeq;
	}

	public String getSupprId() {
		return supprId;
	}

	public void setSupprId(String supprId) {
		this.supprId = supprId;
	}

	public String getSupprNm() {
		return supprNm;
	}

	public void setSupprNm(String supprNm) {
		this.supprNm = supprNm;
	}

	public String getRtnShipYn() {
		return rtnShipYn;
	}

	public void setRtnShipYn(String rtnShipYn) {
		this.rtnShipYn = rtnShipYn;
	}

	public String getRefDetlNo() {
		return refDetlNo;
	}

	public void setRefDetlNo(String refDetlNo) {
		this.refDetlNo = refDetlNo;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public Integer getInbEctQty() {
		return inbEctQty;
	}

	public void setInbEctQty(Integer inbEctQty) {
		this.inbEctQty = inbEctQty;
	}

	public Integer getInbCmptQty() {
		return inbCmptQty;
	}

	public void setInbCmptQty(Integer inbCmptQty) {
		this.inbCmptQty = inbCmptQty;
	}

	public String getRtrnTcd() {
		return rtrnTcd;
	}

	public void setRtrnTcd(String rtrnTcd) {
		this.rtrnTcd = rtrnTcd;
	}

	public String getShopRtnType() {
		return shopRtnType;
	}

	public void setShopRtnType(String shopRtnType) {
		this.shopRtnType = shopRtnType;
	}

	public Integer getShopRtnSeq() {
		return shopRtnSeq;
	}

	public void setShopRtnSeq(Integer shopRtnSeq) {
		this.shopRtnSeq = shopRtnSeq;
	}

	public String getIfCrtDtm() {
		return ifCrtDtm;
	}

	public void setIfCrtDtm(String ifCrtDtm) {
		this.ifCrtDtm = ifCrtDtm;
	}

	public String getIfChk() {
		return ifChk;
	}

	public void setIfChk(String ifChk) {
		this.ifChk = ifChk;
	}

	public String getIfChkDtm() {
		return ifChkDtm;
	}

	public void setIfChkDtm(String ifChkDtm) {
		this.ifChkDtm = ifChkDtm;
	}

	public String getWcsIfChk() {
		return wcsIfChk;
	}

	public void setWcsIfChk(String wcsIfChk) {
		this.wcsIfChk = wcsIfChk;
	}

	public String getWcsIfChkDtm() {
		return wcsIfChkDtm;
	}

	public void setWcsIfChkDtm(String wcsIfChkDtm) {
		this.wcsIfChkDtm = wcsIfChkDtm;
	}
}
