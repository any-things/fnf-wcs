package operato.fnf.wcs.service.model;

/**
 * 경광등 별 빈 셀 현황
 * 
 * @author shortstop
 */
public class LampCellStatus {
	/**
	 * 경광등 코드 
	 */
	private String towerLampCd;
	/**
	 * 빈 셀 여부 
	 */
	private String emptyYn;
	/**
	 * 셀 개수
	 */
	private Integer cellCnt;
	
	public LampCellStatus() {
	}
	
	public LampCellStatus(String towerLampCd, String emptyYn, Integer cellCnt) {
		this.towerLampCd = towerLampCd;
		this.emptyYn = emptyYn;
		this.cellCnt = cellCnt;
	}

	public String getTowerLampCd() {
		return towerLampCd;
	}

	public void setTowerLampCd(String towerLampCd) {
		this.towerLampCd = towerLampCd;
	}

	public String getEmptyYn() {
		return emptyYn;
	}

	public void setEmptyYn(String emptyYn) {
		this.emptyYn = emptyYn;
	}

	public Integer getCellCnt() {
		return cellCnt;
	}

	public void setCellCnt(Integer cellCnt) {
		this.cellCnt = cellCnt;
	}

}
