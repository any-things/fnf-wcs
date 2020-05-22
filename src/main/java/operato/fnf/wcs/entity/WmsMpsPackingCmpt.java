package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Table;

/**
 * DPS 패킹 정보 전송 테이블 
 * 
 * @author shortstop
 */
@Table(name = "mps_packing_cmpt", idStrategy = GenerationRule.COMPLEX_KEY, dataSourceType=DataSourceType.DATASOURCE, uniqueFields="interfaceCrtDt,interfaceNo", indexes = {
})
public class WmsMpsPackingCmpt extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -7822805963342664892L;

	@Ignore
	private String id;
	
	@Column (name = "interface_crt_dt", nullable = false, length = 8)
	private String interfaceCrtDt;
	
	@Column (name = "interface_no", nullable = false, length = 15)
	private String interfaceNo;
	
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	@Column (name = "box_id", nullable = false, length = 20)
	private String boxId;
	
	@Column (name = "ref_no", nullable = true, length = 30)
	private String refNo;
	
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;
	
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;
		
	@Column (name = "pack_qty", nullable = true)
	private Integer packQty;
	
	@Column (name = "outb_ect_date", nullable = true, length = 8)
	private String outbEctDate;
	
	@Column (name = "if_crt_id", nullable = true, length = 20)
	private String ifCrtId;
	
	@Column (name = "if_crt_dtm", nullable = true, length = 14)
	private String ifCrtDtm;
	
	@Column (name = "if_chk", nullable = true, length = 10)
	private String ifChk;
	
	@Column (name = "if_chk_dtm", nullable = true, length = 14)
	private String ifChkDtm;
	
	@Column (name = "if_err_msg", nullable = true, length = 500)
	private String ifErrMsg;

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

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
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

	public Integer getPackQty() {
		return packQty;
	}

	public void setPackQty(Integer packQty) {
		this.packQty = packQty;
	}

	public String getOutbEctDate() {
		return outbEctDate;
	}

	public void setOutbEctDate(String outbEctDate) {
		this.outbEctDate = outbEctDate;
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

	public String getIfErrMsg() {
		return ifErrMsg;
	}

	public void setIfErrMsg(String ifErrMsg) {
		this.ifErrMsg = ifErrMsg;
	}

}
