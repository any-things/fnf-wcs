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
 * 매장 반품예정 WMS 사전 검수 박스 정보 송신
 * 반품검수 EX-PAS
 * 
 * @author yang
 */
@Table(name = "wmt_wcs_exp_inb_rtn_pre_scan"
	, ignoreDdl = true
	, idStrategy = GenerationRule.UUID
    , dataSourceType=DataSourceType.DATASOURCE 
    , uniqueFields="interfaceCrtDt,interfaceNo")
public class WmsWmtWcsExpInbRtnPreScan extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -1698217610854922769L;

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

	@Column (name = "ref_no", nullable = true, length = 30)
	private String refNo;

	@Column (name = "if_chk", nullable = true, length = 1)
	private String ifChk;

	@Column (name = "ins_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date insDatetime;

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

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getIfChk() {
		return ifChk;
	}

	public void setIfChk(String ifChk) {
		this.ifChk = ifChk;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}
}
