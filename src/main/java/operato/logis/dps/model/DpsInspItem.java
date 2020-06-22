package operato.logis.dps.model;

import xyz.anythings.base.entity.SKU;

/**
 * DPS 검수 항목 모델
 * 
 * @author shortstop
 */
public class DpsInspItem {
	
	/**
	 * 매장 코드 
	 */
	private String shopCd;
	/**
	 * 상품 코드 
	 */
	private String skuCd;
	/**
	 * 상품 명 
	 */
	private String skuNm;
	/**
	 * 상품 바코드 
	 */
	private String skuBarcd;
	/**
	 * 상품 바코드2
	 */
	private String skuBarcd2;
	/**
	 * 상품 스타일 
	 */
	private String skuStyle;
	/**
	 * 상품 컬러 
	 */
	private String skuColor;
	/**
	 * 상품 사이즈 
	 */
	private String skuSize;
	/**
	 * 피킹 수량 
	 */
	private Integer pickedQty;
	/**
	 * 검수 확인 수량
	 */
	private Integer confirmQty;
	/**
	 * 상품 중량
	 */
	private Float skuWeight;
	/**
	 * RFID 검수 필요 여부
	 */
	private String rfidItemYn;
	
	public DpsInspItem() {
	}
	
	public DpsInspItem(String shopCd, String skuCd, String skuNm, String skuBarcd, Integer pickedQty, Float skuWeight) {
		this.shopCd = shopCd;
		this.skuCd = skuCd;
		this.skuNm = skuNm;
		this.skuBarcd = skuBarcd;
		this.pickedQty = pickedQty;
		this.setSkuWeight(skuWeight);
	}
	
	public DpsInspItem(String shopCd, String skuCd, String skuNm, String skuBarcd, Integer pickedQty) {
		this.shopCd = shopCd;
		this.skuCd = skuCd;
		this.skuNm = skuNm;
		this.skuBarcd = skuBarcd;
		this.pickedQty = pickedQty;
	}
	
	public DpsInspItem(SKU sku, Integer pickedQty) {
		this.skuCd = sku.getSkuCd();
		this.skuNm = sku.getSkuNm();
		this.skuBarcd = sku.getSkuBarcd();
		this.pickedQty = pickedQty;
	}
	
	public String getShopCd() {
		return shopCd;
	}

	public void setShopCd(String shopCd) {
		this.shopCd = shopCd;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public String getSkuBarcd() {
		return skuBarcd;
	}

	public void setSkuBarcd(String skuBarcd) {
		this.skuBarcd = skuBarcd;
	}

	public String getSkuBarcd2() {
		return skuBarcd2;
	}

	public void setSkuBarcd2(String skuBarcd2) {
		this.skuBarcd2 = skuBarcd2;
	}

	public String getSkuStyle() {
		return skuStyle;
	}

	public void setSkuStyle(String skuStyle) {
		this.skuStyle = skuStyle;
	}

	public String getSkuColor() {
		return skuColor;
	}

	public void setSkuColor(String skuColor) {
		this.skuColor = skuColor;
	}

	public String getSkuSize() {
		return skuSize;
	}

	public void setSkuSize(String skuSize) {
		this.skuSize = skuSize;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public Integer getConfirmQty() {
		return confirmQty;
	}

	public void setConfirmQty(Integer confirmQty) {
		this.confirmQty = confirmQty;
	}

	public Float getSkuWeight() {
		return skuWeight;
	}

	public void setSkuWeight(Float skuWeight) {
		this.skuWeight = skuWeight;
	}

	public String getRfidItemYn() {
		return rfidItemYn;
	}

	public void setRfidItemYn(String rfidItemYn) {
		this.rfidItemYn = rfidItemYn;
	}

}
