package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

/**
 * DPS용 박스별 검수 완료 내역 - RFID
 */
@Table(name = "if_rfidhistory_recv", idStrategy = GenerationRule.NONE, dataSourceType=DataSourceType.DATASOURCE, uniqueFields="cdWarehouse,cdBrand,tpMachine,dtDelivery,dsBatchNo,noBox,ifCdItem")
public class RfidDpsInspResult extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SERIAL UID
	 */
	private static final long serialVersionUID = -1235778332911005593L;	

	/**
	 * I/F일자 
	 */
	@Column (name = "dt_if_date", nullable = false, length = 8)
	private String dtIfDate;
	
	/**
	 * I/F순번 
	 */
	@Column (name = "no_if_seq", nullable = false)
	private Integer noIfSeq;
	
	/**
	 * I: 입력, D: 삭제
	 */
	@Column (name = "tp_gubun", nullable = false, length = 1)
	private String tpGubun;
	
	/**
	 * 회사코드 
	 */
	@Column (name = "cd_company", nullable = false, length = 10)
	private String cdCompany;
	
	/**
	 * 사업부코드/브랜드 
	 */
	@Column (name = "cd_depart", nullable = false, length = 10)
	private String cdDepart;
	
	/**
	 * 처리일자
	 */
	@Column (name = "dt_date", nullable = false, length = 8)
	private String dtDate;
	
	/**
	 * 매장코드
	 */
	@Column (name = "cd_shop", nullable = false, length = 10)
	private String cdShop;
	
	/**
	 * 온라인출고 송장번호
	 */
	@Column (name = "cd_bill", nullable = false, length = 50)
	private String cdBill;
	
	/**
	 * 고정 '1'
	 */
	@Column (name = "cd_subbill", nullable = false, length = 1)
	private String cdSubbill;
	
	/**
	 * RFID코드
	 */
	@Column (name = "cd_rfiduid", nullable = false, length = 8)
	private String cdRfiduid;
	
	/**
	 * RFID상태값 - 온라인출고 '42' 고정
	 */
	@Column (name = "tp_history", nullable = false, length = 50)
	private String tpHistory;
	
	/**
	 * 수신 상태 (0: 수신 전, 1: 수신, 9: ERROR)
	 */
	@Column (name = "tp_status", nullable = false, length = 1)
	private String tpStatus;
	
	/**
	 * 등록 업체('WMS','WCS','SMS','ERP')
	 */
	@Column (name = "cd_register", nullable = false, length = 40)
	private String cdRegister;
	
	/**
	 * 수신 전 일시
	 */
	@Column (name = "dm_bf_recv", nullable = false, length = 14)
	private String dmBfRecv;
	
	/**
	 * 수신일시
	 */
	@Column (name = "dm_af_recv", length = 14)
	private String dmAfRecv;
	
	/**
	 * 오류메세지
	 */
	@Column (name = "ds_errmsg", length = 4000)
	private String dsErrmsg;

	public String getDtIfDate() {
		return dtIfDate;
	}

	public void setDtIfDate(String dtIfDate) {
		this.dtIfDate = dtIfDate;
	}

	public Integer getNoIfSeq() {
		return noIfSeq;
	}

	public void setNoIfSeq(Integer noIfSeq) {
		this.noIfSeq = noIfSeq;
	}

	public String getTpGubun() {
		return tpGubun;
	}

	public void setTpGubun(String tpGubun) {
		this.tpGubun = tpGubun;
	}

	public String getCdCompany() {
		return cdCompany;
	}

	public void setCdCompany(String cdCompany) {
		this.cdCompany = cdCompany;
	}

	public String getCdDepart() {
		return cdDepart;
	}

	public void setCdDepart(String cdDepart) {
		this.cdDepart = cdDepart;
	}

	public String getDtDate() {
		return dtDate;
	}

	public void setDtDate(String dtDate) {
		this.dtDate = dtDate;
	}

	public String getCdShop() {
		return cdShop;
	}

	public void setCdShop(String cdShop) {
		this.cdShop = cdShop;
	}

	public String getCdBill() {
		return cdBill;
	}

	public void setCdBill(String cdBill) {
		this.cdBill = cdBill;
	}

	public String getCdSubbill() {
		return cdSubbill;
	}

	public void setCdSubbill(String cdSubbill) {
		this.cdSubbill = cdSubbill;
	}

	public String getCdRfiduid() {
		return cdRfiduid;
	}

	public void setCdRfiduid(String cdRfiduid) {
		this.cdRfiduid = cdRfiduid;
	}

	public String getTpHistory() {
		return tpHistory;
	}

	public void setTpHistory(String tpHistory) {
		this.tpHistory = tpHistory;
	}

	public String getTpStatus() {
		return tpStatus;
	}

	public void setTpStatus(String tpStatus) {
		this.tpStatus = tpStatus;
	}

	public String getCdRegister() {
		return cdRegister;
	}

	public void setCdRegister(String cdRegister) {
		this.cdRegister = cdRegister;
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

}
