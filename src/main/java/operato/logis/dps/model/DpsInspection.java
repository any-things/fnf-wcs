package operato.logis.dps.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DPS 용 출고 검수 모델 
 * 
 * @author shortstop
 */
public class DpsInspection {
	
	/**
	 * 배치 타이틀
	 */
	private String batchTitle;
	/**
	 * 브랜드
	 */
	private String brand;
	/**
	 * 배치 ID
	 */
	private String batchId;
	/**
	 * 주문 번호 
	 */
	private String orderNo;
	/**
	 * 고객사 주문 번호 
	 */
	private String custOrderNo;
	/**
	 * 송장 번호 
	 */
	private String invoiceId;
	/**
	 * 박스 유형 
	 */
	private String boxType;
	/**
	 * 트레이 박스 번호
	 */
	private String trayCd;
	/**
	 * 박스 ID 
	 */
	private String boxId;
	/**
	 * 상태
	 */
	private String status;
	/**
	 * 주문의 총 상품 수량
	 */
	private Integer skuQty;
	/**
	 * 주문의 총 수량
	 */
	private Integer orderQty;
	/**
	 * 예상 중량
	 */
	private Float expWeight;
	/**
	 * 측정 중량
	 */
	private Float msrWeight;
	/**
	 * 하한 중량 - 중량 검수시 사용 
	 */
	private Float minWeight;
	/**
	 * 상한 중량 - 중량 검수시 사용 
	 */
	private Float maxWeight;
	/**
	 * 내품 내역
	 */
	private List<DpsInspItem> items;
	
	public DpsInspection() {
	}

	public String getBatchTitle() {
		return batchTitle;
	}

	public void setBatchTitle(String batchTitle) {
		this.batchTitle = batchTitle;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getCustOrderNo() {
		return custOrderNo;
	}

	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getBoxType() {
		return boxType;
	}

	public void setBoxType(String boxType) {
		this.boxType = boxType;
	}

	public String getTrayCd() {
		return trayCd;
	}

	public void setTrayCd(String trayCd) {
		this.trayCd = trayCd;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public Integer getSkuQty() {
		return skuQty;
	}

	public void setSkuQty(Integer skuQty) {
		this.skuQty = skuQty;
	}

	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Float getExpWeight() {
		return expWeight;
	}

	public void setExpWeight(Float expWeight) {
		this.expWeight = expWeight;
	}

	public Float getMsrWeight() {
		return msrWeight;
	}

	public void setMsrWeight(Float msrWeight) {
		this.msrWeight = msrWeight;
	}

	public Float getMinWeight() {
		return minWeight;
	}

	public void setMinWeight(Float minWeight) {
		this.minWeight = minWeight;
	}

	public Float getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(Float maxWeight) {
		this.maxWeight = maxWeight;
	}

	public List<DpsInspItem> getItems() {
		return items;
	}

	public void setItems(List<DpsInspItem> items) {
		this.items = items;
	}
	
	public void addItem(String shopCd, String skuCd, String skuNm, String skuBarcd, Integer pickedQty) {
		if(this.items == null) {
			this.items = new ArrayList<DpsInspItem>();
		}
		
		this.items.add(new DpsInspItem(shopCd, skuCd, skuNm, skuBarcd, pickedQty));
	}
	
}
