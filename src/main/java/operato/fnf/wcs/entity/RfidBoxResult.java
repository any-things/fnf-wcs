package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * RFID 데이터베이스 if_pasdelivery_send 테이블에 대한 링크 테이블의 엔티티
 * 
 * @author shortstop
 */
@Table(name = "if_pasdelivery_send", ignoreDdl = true, idStrategy = GenerationRule.NONE)
public class RfidBoxResult extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 715655136778200694L;

	@Ignore
	private String id;
	
	@PrimaryKey
	@Column (name = "cd_warehouse", length = 10)
	private String cdWarehouse;

	@PrimaryKey
	@Column (name = "cd_brand", length = 10)
	private String cdBrand;

	@PrimaryKey
	@Column (name = "tp_machine", length = 2)
	private String tpMachine;

	@PrimaryKey
	@Column (name = "no_box", length = 20)
	private String noBox;

	@Column (name = "no_waybill", length = 50)
	private String noWaybill;

	@Column (name = "result_st", length = 1)
	private String resultSt;
	
	@Column (name = "tp_status", length = 1)
	private String tpStatus;
	
	@Column (name = "dm_bf_send", length = 14)
	private String dmBfSend;
	
	@Column (name = "dm_af_send", length = 14)
	private String dmAfSend;
	
	@Column (name = "ds_errmsg", length = 4000)
	private String dsErrmsg;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getNoBox() {
		return noBox;
	}

	public void setNoBox(String noBox) {
		this.noBox = noBox;
	}

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

	public String getDmBfSend() {
		return dmBfSend;
	}

	public void setDmBfSend(String dmBfSend) {
		this.dmBfSend = dmBfSend;
	}

	public String getDmAfSend() {
		return dmAfSend;
	}

	public void setDmAfSend(String dmAfSend) {
		this.dmAfSend = dmAfSend;
	}

	public String getDsErrmsg() {
		return dsErrmsg;
	}

	public void setDsErrmsg(String dsErrmsg) {
		this.dsErrmsg = dsErrmsg;
	}

}
