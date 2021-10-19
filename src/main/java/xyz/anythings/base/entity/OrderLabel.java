package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "order_labels", idStrategy = GenerationRule.UUID)
public class OrderLabel extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 789276685704369381L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "order_id", nullable = false, length = 40)
	private String orderId;

	@Column (name = "sku_amount", length = 19)
	private Float skuAmount;

	@Column (name = "rcv_nm", length = 100)
	private String rcvNm;

	@Column (name = "rcv_tel_no1", length = 100)
	private String rcvTelNo1;

	@Column (name = "rcv_tel_no2", length = 100)
	private String rcvTelNo2;

	@Column (name = "rcv_addr1", length = 900)
	private String rcvAddr1;

	@Column (name = "rcv_addr2", length = 900)
	private String rcvAddr2;

	@Column (name = "rcv_addr3", length = 900)
	private String rcvAddr3;

	@Column (name = "rcv_addr4", length = 900)
	private String rcvAddr4;

	@Column (name = "rcv_zip_cd", length = 30)
	private String rcvZipCd;

	@Column (name = "payment_type", length = 20)
	private String paymentType;

	@Column (name = "order_req_nm", length = 100)
	private String orderReqNm;

	@Column (name = "order_req2_nm", length = 100)
	private String orderReq2Nm;

	@Column (name = "ajst_cust_no", length = 40)
	private String ajstCustNo;

	@Column (name = "snd_no", length = 40)
	private String sndNo;

	@Column (name = "snd_nm", length = 100)
	private String sndNm;

	@Column (name = "snd_tel_no", length = 40)
	private String sndTelNo;

	@Column (name = "snd_zip_cd", length = 30)
	private String sndZipCd;

	@Column (name = "snd_addr", length = 900)
	private String sndAddr;

	@Column (name = "snd_etc_addr", length = 900)
	private String sndEtcAddr;

	@Column (name = "snd_mobile_no", length = 40)
	private String sndMobileNo;

	@Column (name = "snd_sale_no", length = 40)
	private String sndSaleNo;

	@Column (name = "snd_cust_no", length = 40)
	private String sndCustNo;

	@Column (name = "b2c_cust_id", length = 40)
	private String b2cCustId;

	@Column (name = "b2c_cust_mgr_id", length = 40)
	private String b2cCustMgrId;

	@Column (name = "buyer_po_cd", length = 30)
	private String buyerPoCd;

	@Column (name = "sup_price_ex_vat", length = 19)
	private Float supPriceExVat;

	@Column (name = "sup_price_in_vat", length = 19)
	private Float supPriceInVat;

	@Column (name = "resv_type", length = 20)
	private String resvType;

	@Column (name = "order_sku_status", length = 10)
	private String orderSkuStatus;

	@Column (name = "dlv_region_cd", length = 30)
	private String dlvRegionCd;

	@Column (name = "dlv_channel", length = 50)
	private String dlvChannel;

	@Column (name = "dlv_type", length = 20)
	private String dlvType;

	@Column (name = "pass_stop_cd", length = 30)
	private String passStopCd;

	@Column (name = "pass_stop_nm", length = 100)
	private String passStopNm;

	@Column (name = "dlv_memo")
	private String dlvMemo;

	@Column (name = "lbl_val1", length = 100)
	private String lblVal1;

	@Column (name = "lbl_val2", length = 100)
	private String lblVal2;

	@Column (name = "lbl_val3", length = 100)
	private String lblVal3;

	@Column (name = "lbl_val4", length = 100)
	private String lblVal4;

	@Column (name = "lbl_val5", length = 100)
	private String lblVal5;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Float getSkuAmount() {
		return skuAmount;
	}

	public void setSkuAmount(Float skuAmount) {
		this.skuAmount = skuAmount;
	}

	public String getRcvNm() {
		return rcvNm;
	}

	public void setRcvNm(String rcvNm) {
		this.rcvNm = rcvNm;
	}

	public String getRcvTelNo1() {
		return rcvTelNo1;
	}

	public void setRcvTelNo1(String rcvTelNo1) {
		this.rcvTelNo1 = rcvTelNo1;
	}

	public String getRcvTelNo2() {
		return rcvTelNo2;
	}

	public void setRcvTelNo2(String rcvTelNo2) {
		this.rcvTelNo2 = rcvTelNo2;
	}

	public String getRcvAddr1() {
		return rcvAddr1;
	}

	public void setRcvAddr1(String rcvAddr1) {
		this.rcvAddr1 = rcvAddr1;
	}

	public String getRcvAddr2() {
		return rcvAddr2;
	}

	public void setRcvAddr2(String rcvAddr2) {
		this.rcvAddr2 = rcvAddr2;
	}

	public String getRcvAddr3() {
		return rcvAddr3;
	}

	public void setRcvAddr3(String rcvAddr3) {
		this.rcvAddr3 = rcvAddr3;
	}

	public String getRcvAddr4() {
		return rcvAddr4;
	}

	public void setRcvAddr4(String rcvAddr4) {
		this.rcvAddr4 = rcvAddr4;
	}

	public String getRcvZipCd() {
		return rcvZipCd;
	}

	public void setRcvZipCd(String rcvZipCd) {
		this.rcvZipCd = rcvZipCd;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getOrderReqNm() {
		return orderReqNm;
	}

	public void setOrderReqNm(String orderReqNm) {
		this.orderReqNm = orderReqNm;
	}

	public String getOrderReq2Nm() {
		return orderReq2Nm;
	}

	public void setOrderReq2Nm(String orderReq2Nm) {
		this.orderReq2Nm = orderReq2Nm;
	}

	public String getAjstCustNo() {
		return ajstCustNo;
	}

	public void setAjstCustNo(String ajstCustNo) {
		this.ajstCustNo = ajstCustNo;
	}

	public String getSndNo() {
		return sndNo;
	}

	public void setSndNo(String sndNo) {
		this.sndNo = sndNo;
	}

	public String getSndNm() {
		return sndNm;
	}

	public void setSndNm(String sndNm) {
		this.sndNm = sndNm;
	}

	public String getSndTelNo() {
		return sndTelNo;
	}

	public void setSndTelNo(String sndTelNo) {
		this.sndTelNo = sndTelNo;
	}

	public String getSndZipCd() {
		return sndZipCd;
	}

	public void setSndZipCd(String sndZipCd) {
		this.sndZipCd = sndZipCd;
	}

	public String getSndAddr() {
		return sndAddr;
	}

	public void setSndAddr(String sndAddr) {
		this.sndAddr = sndAddr;
	}

	public String getSndEtcAddr() {
		return sndEtcAddr;
	}

	public void setSndEtcAddr(String sndEtcAddr) {
		this.sndEtcAddr = sndEtcAddr;
	}

	public String getSndMobileNo() {
		return sndMobileNo;
	}

	public void setSndMobileNo(String sndMobileNo) {
		this.sndMobileNo = sndMobileNo;
	}

	public String getSndSaleNo() {
		return sndSaleNo;
	}

	public void setSndSaleNo(String sndSaleNo) {
		this.sndSaleNo = sndSaleNo;
	}

	public String getSndCustNo() {
		return sndCustNo;
	}

	public void setSndCustNo(String sndCustNo) {
		this.sndCustNo = sndCustNo;
	}

	public String getB2cCustId() {
		return b2cCustId;
	}

	public void setB2cCustId(String b2cCustId) {
		this.b2cCustId = b2cCustId;
	}

	public String getB2cCustMgrId() {
		return b2cCustMgrId;
	}

	public void setB2cCustMgrId(String b2cCustMgrId) {
		this.b2cCustMgrId = b2cCustMgrId;
	}

	public String getBuyerPoCd() {
		return buyerPoCd;
	}

	public void setBuyerPoCd(String buyerPoCd) {
		this.buyerPoCd = buyerPoCd;
	}

	public Float getSupPriceExVat() {
		return supPriceExVat;
	}

	public void setSupPriceExVat(Float supPriceExVat) {
		this.supPriceExVat = supPriceExVat;
	}

	public Float getSupPriceInVat() {
		return supPriceInVat;
	}

	public void setSupPriceInVat(Float supPriceInVat) {
		this.supPriceInVat = supPriceInVat;
	}

	public String getResvType() {
		return resvType;
	}

	public void setResvType(String resvType) {
		this.resvType = resvType;
	}

	public String getOrderSkuStatus() {
		return orderSkuStatus;
	}

	public void setOrderSkuStatus(String orderSkuStatus) {
		this.orderSkuStatus = orderSkuStatus;
	}

	public String getDlvRegionCd() {
		return dlvRegionCd;
	}

	public void setDlvRegionCd(String dlvRegionCd) {
		this.dlvRegionCd = dlvRegionCd;
	}

	public String getDlvChannel() {
		return dlvChannel;
	}

	public void setDlvChannel(String dlvChannel) {
		this.dlvChannel = dlvChannel;
	}

	public String getDlvType() {
		return dlvType;
	}

	public void setDlvType(String dlvType) {
		this.dlvType = dlvType;
	}

	public String getPassStopCd() {
		return passStopCd;
	}

	public void setPassStopCd(String passStopCd) {
		this.passStopCd = passStopCd;
	}

	public String getPassStopNm() {
		return passStopNm;
	}

	public void setPassStopNm(String passStopNm) {
		this.passStopNm = passStopNm;
	}

	public String getDlvMemo() {
		return dlvMemo;
	}

	public void setDlvMemo(String dlvMemo) {
		this.dlvMemo = dlvMemo;
	}

	public String getLblVal1() {
		return lblVal1;
	}

	public void setLblVal1(String lblVal1) {
		this.lblVal1 = lblVal1;
	}

	public String getLblVal2() {
		return lblVal2;
	}

	public void setLblVal2(String lblVal2) {
		this.lblVal2 = lblVal2;
	}

	public String getLblVal3() {
		return lblVal3;
	}

	public void setLblVal3(String lblVal3) {
		this.lblVal3 = lblVal3;
	}

	public String getLblVal4() {
		return lblVal4;
	}

	public void setLblVal4(String lblVal4) {
		this.lblVal4 = lblVal4;
	}

	public String getLblVal5() {
		return lblVal5;
	}

	public void setLblVal5(String lblVal5) {
		this.lblVal5 = lblVal5;
	}	
}