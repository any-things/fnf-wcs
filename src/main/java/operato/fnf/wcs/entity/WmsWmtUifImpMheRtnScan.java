package operato.fnf.wcs.entity;

import java.util.Date;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * 매장 반품예정 검수 스캔 결과
 * 반품검수 EX-PAS
 */
@Table(name = "wmt_uif_imp_mhe_rtn_scan", idStrategy = GenerationRule.UUID
     , dataSourceType=DataSourceType.DATASOURCE 
     , uniqueFields="interfaceCrtDt,interfaceNo", indexes = {
    @Index(name = "wmt_uif_imp_mhe_rtn_scan_id01", columnList = "inb_no,inb_detl_no", unique = false)
})
public class WmsWmtUifImpMheRtnScan extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3601219852210491698L;

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
	
	@Column (name = "inb_no", nullable = true, length = 20)
	private String inbNo;

	@Column (name = "inb_detl_no", nullable = true, length = 30)
	private String inbDetlNo;

	@Column (name = "item_cd", nullable = true, length = 30)
	private String itemCd;
	
	@Column (name = "qty", nullable = true, length = 10)
	private Integer qty;
	
	@Column (name = "dmg_qty", nullable = true, length = 10)
	private Integer dmgQty;

	@Column (name = "new_yn", nullable = true, length = 1)
	private String newYn;

	@Column (name = "ins_person_id", nullable = true, length = 20)
	private String insPersonId;

	@Column (name = "ins_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date insDatetime;
	
	@Column (name = "if_yn", nullable = true, length = 1)
	private String ifYn;

	@Column (name = "prcs_datetime", nullable = true, type = ColumnType.DATETIME)
	private Date prcsDatetime;

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

	public String getInbNo() {
		return inbNo;
	}

	public void setInbNo(String inbNo) {
		this.inbNo = inbNo;
	}

	public String getInbDetlNo() {
		return inbDetlNo;
	}

	public void setInbDetlNo(String inbDetlNo) {
		this.inbDetlNo = inbDetlNo;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public Integer getDmgQty() {
		return dmgQty;
	}

	public void setDmgQty(Integer dmgQty) {
		this.dmgQty = dmgQty;
	}

	public String getNewYn() {
		return newYn;
	}

	public void setNewYn(String newYn) {
		this.newYn = newYn;
	}

	public String getInsPersonId() {
		return insPersonId;
	}

	public void setInsPersonId(String insPersonId) {
		this.insPersonId = insPersonId;
	}

	public Date getInsDatetime() {
		return insDatetime;
	}

	public void setInsDatetime(Date insDatetime) {
		this.insDatetime = insDatetime;
	}

	public String getIfYn() {
		return ifYn;
	}

	public void setIfYn(String ifYn) {
		this.ifYn = ifYn;
	}

	public Date getPrcsDatetime() {
		return prcsDatetime;
	}

	public void setPrcsDatetime(Date prcsDatetime) {
		this.prcsDatetime = prcsDatetime;
	}

}
