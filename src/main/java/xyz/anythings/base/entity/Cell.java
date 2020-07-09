package xyz.anythings.base.entity;

import java.util.Map;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "cells", idStrategy = GenerationRule.UUID, uniqueFields="domainId,cellCd", indexes = {
	@Index(name = "ix_cells_0", columnList = "domain_id,cell_cd", unique = true),
	@Index(name = "ix_cells_1", columnList = "domain_id,ind_cd"),
	@Index(name = "ix_cells_2", columnList = "domain_id,equip_type,equip_cd,cell_seq"),
	@Index(name = "ix_cells_3", columnList = "domain_id,station_cd"),
	@Index(name = "ix_cells_4", columnList = "domain_id,active_flag")
})
public class Cell extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 489777465475485570L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "station_cd", length = 30)
	private String stationCd;

	@Column (name = "equip_type", nullable = false, length = 20)
	private String equipType;

	@Column (name = "equip_cd", nullable = false, length = 30)
	private String equipCd;

	@Column (name = "cell_cd", nullable = false, length = 30)
	private String cellCd;

	@Column (name = "wms_cell_cd", length = 30)
	private String wmsCellCd;

	@Column (name = "equip_zone", length = 30)
	private String equipZone;

	@Column (name = "ind_cd", length = 30)
	private String indCd;
	
	@Column (name = "ind_seq", length = 12)
	private Integer indSeq;	

	@Column (name = "channel_no", length = 40)
	private String channelNo;
	
	@Column (name = "pan_no", length = 40)
	private String panNo;

	@Column (name = "side_cd", length = 30)
	private String sideCd;

	@Column (name = "cell_seq", length = 12)
	private Integer cellSeq;

	@Column (name = "printer_cd", length = 30)
	private String printerCd;
	
	@Column (name = "tower_lamp_cd", length = 30)
	private String towerLampCd;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;
	
	@Column (name = "class_cd", length = 30)
	private String classCd;
	
	@Column (name = "category_flag", length = 1)
	private Boolean categoryFlag;
	
	@Column (name = "batch_id", length = 40)
	private String batchId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
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

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}

	public String getWmsCellCd() {
		return wmsCellCd;
	}

	public void setWmsCellCd(String wmsCellCd) {
		this.wmsCellCd = wmsCellCd;
	}

	public String getEquipZone() {
		return equipZone;
	}

	public void setEquipZone(String equipZone) {
		this.equipZone = equipZone;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}
	
	public Integer getIndSeq() {
		return indSeq;
	}

	public void setIndSeq(Integer indSeq) {
		this.indSeq = indSeq;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getPanNo() {
		return panNo;
	}

	public void setPanNo(String panNo) {
		this.panNo = panNo;
	}

	public String getSideCd() {
		return sideCd;
	}

	public void setSideCd(String sideCd) {
		this.sideCd = sideCd;
	}

	public Integer getCellSeq() {
		return cellSeq;
	}

	public void setCellSeq(Integer cellSeq) {
		this.cellSeq = cellSeq;
	}

	public String getPrinterCd() {
		return printerCd;
	}

	public void setPrinterCd(String printerCd) {
		this.printerCd = printerCd;
	}

	public String getTowerLampCd() {
		return towerLampCd;
	}

	public void setTowerLampCd(String towerLampCd) {
		this.towerLampCd = towerLampCd;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}
	  
	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public Boolean getCategoryFlag() {
		return categoryFlag;
	}

	public void setCategoryFlag(Boolean categoryFlag) {
		this.categoryFlag = categoryFlag;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	@Override
	public void afterUpdate() {
		super.afterUpdate();
		
		// 재고의 active_flag 업데이트
		String sql = "update stocks set active_flag = :activeFlag where domain_id = :domainId and cell_cd = :cellCd";
		Map<String, Object> params = ValueUtil.newMap("domainId,cellCd,activeFlag", this.domainId, this.cellCd, this.activeFlag);
		BeanUtil.get(IQueryManager.class).executeBySql(sql, params);
	}

}
