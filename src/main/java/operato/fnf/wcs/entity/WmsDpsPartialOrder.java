package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.DataSourceType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/*
 * WMS DPS 부분할당 정보 조회 
 */
@Table(name = "dps_partial_orders"
    , ignoreDdl = true
    , dataSourceType=DataSourceType.DATASOURCE
    , idStrategy = GenerationRule.UUID)
public class WmsDpsPartialOrder extends xyz.elidom.orm.entity.basic.AbstractStamp {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 8834930409246603758L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "wh_cd", nullable = false, length = 20)
	private String whCd;
	
	@PrimaryKey
	@Column (name = "ref_no", nullable = false, length = 30)
	private String refNo;
	
	@Column (name = "shipto_nm", nullable = true, length = 128)
	private String shiptoNm;

	@Column (name = "outb_ect_qty", nullable = true)
	private Integer outbEctQty;
	
	@Column (name = "to_pick_qty", nullable = true)
	private Integer toPickQty;

	@Ignore
	private String assignYn;

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

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getShiptoNm() {
		return shiptoNm;
	}

	public void setShiptoNm(String shiptoNm) {
		this.shiptoNm = shiptoNm;
	}

	public Integer getOutbEctQty() {
		return outbEctQty;
	}

	public void setOutbEctQty(Integer outbEctQty) {
		this.outbEctQty = outbEctQty;
	}

	public Integer getToPickQty() {
		return toPickQty;
	}

	public void setToPickQty(Integer toPickQty) {
		this.toPickQty = toPickQty;
	}

	public String getAssignYn() {
		return assignYn;
	}

	public void setAssignFlag(String assignYn) {
		this.assignYn = assignYn;
	}
}
