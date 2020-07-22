package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

/**
 * DAS용 박스별 패킹 내역 - RFID
 */
@Table(name = "if_pasdelivery_recv"
	, ignoreDdl = true
	, idStrategy = GenerationRule.NONE
	, dataSourceType=DataSourceType.DATASOURCE
	, uniqueFields="cdWarehouse,cdBrand,tpMachine,dtDelivery,dsBatchNo,noBox,ifCdItem")
public class RfidBoxItem extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 2998894256610015614L;
	
	/**
	 * WMS물류센터코드(WH_CD)
	 */
	@Column (name = "cd_warehouse", nullable = false, length = 10)
	private String cdWarehouse;
	
	/**
	 * WMS브랜드(STRR_ID)
	 */
	@Column (name = "cd_brand", nullable = false, length = 10)
	private String cdBrand;
	
	/**
	 * 장비구분(1:PAS, 2:DAS)
	 */
	@Column (name = "tp_machine", nullable = false, length = 2)
	private String tpMachine;

	/**
	 * 출고일자 (YYYYMMDD)
	 */
	@Column (name = "dt_delivery", nullable = false, length = 8)
	private String dtDelivery;
	
	/**
	 * PAS배치번호
	 */
	@Column (name = "ds_batch_no", nullable = false, length = 20)
	private String dsBatchNo;
	
	/**
	 * 박스고유번호
	 */
	@Column (name = "no_box", nullable = false, length = 20)
	private String noBox;
	
	/**
	 * 상품코드(시즌,STYLE,색상,사이즈 포함)/아소트코드, 아소트구분 'Y'=아소트 'N'=솔리드
	 */
	@Column (name = "if_cd_item", nullable = false, length = 50)
	private String ifCdItem;
	
	/**
	 * 아소트구분( Y:아소트, N:솔리드)
	 */
	@Column (name = "yn_assort", length = 1)
	private String ynAssort;
	
	/**
	 * 매장코드(실배송지)
	 */
	@Column (name = "cd_shop", length = 20)
	private String cdShop;
	
	/**
	 * 출고구분(1:출고, 2:분류)
	 */
	@Column (name = "tp_delivery", length = 1)
	private String tpDelivery;
	
	/**
	 * 출고유형코드
	 */
	@Column (name = "outb_tcd", length = 30)
	private String outbTcd;
	
	/**
	 * 슈트번호 - 개별출고 안넘어옴
	 */
	@Column (name = "ds_shuteno", length = 10)
	private String dsShuteno;
	
	/**
	 * 운송장번호
	 */
	@Column (name = "no_waybill", length = 50)
	private String noWaybill;
	
	/**
	 * WMS출고번호
	 */
	@Column (name = "outb_no", length = 20)
	private String outbNo;
	
	/**
	 * ERP주문번호
	 */
	@Column (name = "ref_no", length = 50)
	private String refNo;
	
	/**
	 * 중량
	 */
	@Column (name = "no_weight", length = 19)
	private Float noWeight;
	
	/**
	 * 실적수량
	 */
	@Column (name = "qt_delivery", length = 12)
	private Integer qtDelivery;
	
	/**
	 * 등록일시
	 */
	@Column (name = "dm_bf_recv", length = 14)
	private String dmBfRecv;
	
	/**
	 * 삭제여부('Y': 삭제 N:정상) - 삭제검수결과수신시 'Y' 업데이트
	 */
	@Column (name = "yn_cancel", length = 1)
	private String ynCancel;
	
	/**
	 * 삭제수신일시
	 */
	@Column (name = "dm_af_recv", length = 14)
	private String dmAfRecv;
	
	/**
	 * 0:중량수신전, 1:중량수신
	 */
	@Column (name = "tp_weight", length = 1)
	private String tpWeight;
	
	/**
	 * 0:출고데이터미생성, 1:출고데이터생성
	 */
	@Column (name = "tp_send", length = 1)
	private String tpSend;

	@Column (name = "cd_equipment")
	private String cdEquipment;
	
	public String getCdWarehouse() {
		return cdWarehouse;
	}

	public void setCdWarehouse(String cdWarehouse) {
		this.cdWarehouse = cdWarehouse;
	}

	public String getCdBrand() {
		return cdBrand;
	}

	public void setCdBrand(String cdBrand) {
		this.cdBrand = cdBrand;
	}

	public String getTpMachine() {
		return tpMachine;
	}

	public void setTpMachine(String tpMachine) {
		this.tpMachine = tpMachine;
	}

	public String getDtDelivery() {
		return dtDelivery;
	}

	public void setDtDelivery(String dtDelivery) {
		this.dtDelivery = dtDelivery;
	}

	public String getDsBatchNo() {
		return dsBatchNo;
	}

	public void setDsBatchNo(String dsBatchNo) {
		this.dsBatchNo = dsBatchNo;
	}

	public String getNoBox() {
		return noBox;
	}

	public void setNoBox(String noBox) {
		this.noBox = noBox;
	}

	public String getIfCdItem() {
		return ifCdItem;
	}

	public void setIfCdItem(String ifCdItem) {
		this.ifCdItem = ifCdItem;
	}

	public String getYnAssort() {
		return ynAssort;
	}

	public void setYnAssort(String ynAssort) {
		this.ynAssort = ynAssort;
	}

	public String getCdShop() {
		return cdShop;
	}

	public void setCdShop(String cdShop) {
		this.cdShop = cdShop;
	}

	public String getTpDelivery() {
		return tpDelivery;
	}

	public void setTpDelivery(String tpDelivery) {
		this.tpDelivery = tpDelivery;
	}

	public String getOutbTcd() {
		return outbTcd;
	}

	public void setOutbTcd(String outbTcd) {
		this.outbTcd = outbTcd;
	}

	public String getDsShuteno() {
		return dsShuteno;
	}

	public void setDsShuteno(String dsShuteno) {
		this.dsShuteno = dsShuteno;
	}

	public String getNoWaybill() {
		return noWaybill;
	}

	public void setNoWaybill(String noWaybill) {
		this.noWaybill = noWaybill;
	}

	public String getOutbNo() {
		return outbNo;
	}

	public void setOutbNo(String outbNo) {
		this.outbNo = outbNo;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public Float getNoWeight() {
		return noWeight;
	}

	public void setNoWeight(Float noWeight) {
		this.noWeight = noWeight;
	}

	public Integer getQtDelivery() {
		return qtDelivery;
	}

	public void setQtDelivery(Integer qtDelivery) {
		this.qtDelivery = qtDelivery;
	}

	public String getDmBfRecv() {
		return dmBfRecv;
	}

	public void setDmBfRecv(String dmBfRecv) {
		this.dmBfRecv = dmBfRecv;
	}

	public String getYnCancel() {
		return ynCancel;
	}

	public void setYnCancel(String ynCancel) {
		this.ynCancel = ynCancel;
	}

	public String getDmAfRecv() {
		return dmAfRecv;
	}

	public void setDmAfRecv(String dmAfRecv) {
		this.dmAfRecv = dmAfRecv;
	}

	public String getTpWeight() {
		return tpWeight;
	}

	public void setTpWeight(String tpWeight) {
		this.tpWeight = tpWeight;
	}

	public String getTpSend() {
		return tpSend;
	}

	public void setTpSend(String tpSend) {
		this.tpSend = tpSend;
	}

	public String getCdEquipment() {
		return cdEquipment;
	}

	public void setCdEquipment(String cdEquipment) {
		this.cdEquipment = cdEquipment;
	}

}
