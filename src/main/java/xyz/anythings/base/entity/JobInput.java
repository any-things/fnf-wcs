package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "job_inputs", idStrategy = GenerationRule.UUID, uniqueFields="batchId,inputSeq", indexes = {
	@Index(name = "ix_job_inputs_0", columnList = "batch_id,input_seq", unique = true),
	@Index(name = "ix_job_inputs_1", columnList = "batch_id,equip_type,equip_cd,station_cd"),
	@Index(name = "ix_job_inputs_2", columnList = "batch_id,sku_cd"),
	@Index(name = "ix_job_inputs_3", columnList = "batch_id,box_id")
	
})
public class JobInput extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 605913873681399709L;
	
	/**
	 * 투입 상태 : 대기 (W)
	 */
	public static final String INPUT_STATUS_WAIT = "W";
	/**
	 * 투입 상태 : 진행 (R)
	 */
	public static final String INPUT_STATUS_RUNNING = "R";
	/**
	 * 투입 상태 : 완료 (F)
	 */
	public static final String INPUT_STATUS_FINISHED = "F";

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	@Column (name = "equip_type", nullable = false, length = 20)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "station_cd", length = 30)
	private String stationCd;

	@Column (name = "input_seq", nullable = false, length = 12)
	private Integer inputSeq;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "sku_cd", length = 30)
	private String skuCd;

	@Column (name = "order_no", length = 40)
	private String orderNo;

	@Column (name = "box_id", length = 30)
	private String boxId;

	@Column (name = "box_type", length = 20)
	private String boxType;

	@Column (name = "color_cd", length = 10)
	private String colorCd;

	@Column (name = "input_type", length = 20)
	private String inputType;

	@Column (name = "input_qty", length = 12)
	private Integer inputQty;

	@Column (name = "status", length = 10)
	private String status;
	
	@Ignore
	private String skuNm;
	
	@Ignore
	private Integer skuQty;
	
	@Ignore
	private Integer planQty;
	
	@Ignore
	private Integer resultQty;
	
	@Ignore
	private Boolean isMyZoneIsLast;
	
	@Ignore
	private Integer myZoneProgressRate;
	
	@Ignore
	private Boolean isSelectedItem;

  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public Integer getInputSeq() {
		return inputSeq;
	}

	public void setInputSeq(Integer inputSeq) {
		this.inputSeq = inputSeq;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public String getBoxType() {
		return boxType;
	}

	public void setBoxType(String boxType) {
		this.boxType = boxType;
	}

	public String getColorCd() {
		return colorCd;
	}

	public void setColorCd(String colorCd) {
		this.colorCd = colorCd;
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public Integer getInputQty() {
		return inputQty;
	}

	public void setInputQty(Integer inputQty) {
		this.inputQty = inputQty;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}
	
	public Integer getSkuQty() {
		return this.skuQty;
	}
	
	public void setSkuQty(Integer skuQty) {
		this.skuQty = skuQty;
	}

	public Integer getPlanQty() {
		return planQty;
	}

	public void setPlanQty(Integer planQty) {
		this.planQty = planQty;
	}

	public Integer getResultQty() {
		return resultQty;
	}

	public void setResultQty(Integer resultQty) {
		this.resultQty = resultQty;
	}

	public Boolean getIsMyZoneIsLast() {
		return isMyZoneIsLast;
	}

	public void setIsMyZoneIsLast(Boolean isMyZoneIsLast) {
		this.isMyZoneIsLast = isMyZoneIsLast;
	}

	public Integer getMyZoneProgressRate() {
		return myZoneProgressRate;
	}

	public void setMyZoneProgressRate(Integer myZoneProgressRate) {
		this.myZoneProgressRate = myZoneProgressRate;
	}

	public Boolean getIsSelectedItem() {
		return isSelectedItem;
	}

	public void setIsSelectedItem(Boolean isSelectedItem) {
		this.isSelectedItem = isSelectedItem;
	}

}
