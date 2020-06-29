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
	 * 빈 셀 개수 
	 */
	private Integer emptyCellCnt;
	
	/**
	 * 채워진 셀 개수 
	 */
	private Integer fillCellCnt;
	/**
	 * 전체 셀 개수
	 */
	private Integer totCellCnt;
	
	public LampCellStatus() {
	}

	public String getTowerLampCd() {
		return towerLampCd;
	}

	public void setTowerLampCd(String towerLampCd) {
		this.towerLampCd = towerLampCd;
	}

	public Integer getEmptyCellCnt() {
		return emptyCellCnt;
	}

	public void setEmptyCellCnt(Integer emptyCellCnt) {
		this.emptyCellCnt = emptyCellCnt;
	}

	public Integer getFillCellCnt() {
		return fillCellCnt;
	}

	public void setFillCellCnt(Integer fillCellCnt) {
		this.fillCellCnt = fillCellCnt;
	}

	public Integer getTotCellCnt() {
		return totCellCnt;
	}

	public void setTotCellCnt(Integer totCellCnt) {
		this.totCellCnt = totCellCnt;
	}
}
