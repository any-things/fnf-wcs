package operato.fnf.wcs.service.model;

public class DpsPopularSku implements Comparable<DpsPopularSku> {
	private String id;
	private String workType;
	private String sumDate;
	private String skuCd;
	private Integer scopeDays;
	private Float outbDaysRate;
	private Float outbCountRate;
	private Integer durationDays;
	private Integer pcsRank;
	private Integer timesRank;
	private Integer scopeDaysPcsQty;
	private Integer scopeDaysOrdCnt;
	private Integer scopeDaysSkuCnt;
	private Float scopeAvgPcsQty;
	private Float popularIndex;
	private Float durationPcs;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
	}
	public String getSumDate() {
		return sumDate;
	}
	public void setSumDate(String sumDate) {
		this.sumDate = sumDate;
	}
	public String getSkuCd() {
		return skuCd;
	}
	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}
	public Integer getScopeDays() {
		return scopeDays;
	}
	public void setScopeDays(Integer scopeDays) {
		this.scopeDays = scopeDays;
	}
	public Float getOutbDaysRate() {
		return outbDaysRate;
	}
	public void setOutbDaysRate(Float outbDaysRate) {
		this.outbDaysRate = outbDaysRate;
	}
	public Float getOutbCountRate() {
		return outbCountRate;
	}
	public void setOutbCountRate(Float outbCountRate) {
		this.outbCountRate = outbCountRate;
	}
	public Integer getDurationDays() {
		return durationDays;
	}
	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}
	public Integer getPcsRank() {
		return pcsRank;
	}
	public void setPcsRank(Integer pcsRank) {
		this.pcsRank = pcsRank;
	}
	public Integer getTimesRank() {
		return timesRank;
	}
	public void setTimesRank(Integer timesRank) {
		this.timesRank = timesRank;
	}
	public Integer getScopeDaysPcsQty() {
		return scopeDaysPcsQty;
	}
	public void setScopeDaysPcsQty(Integer scopeDaysPcsQty) {
		this.scopeDaysPcsQty = scopeDaysPcsQty;
	}
	public Integer getScopeDaysOrdCnt() {
		return scopeDaysOrdCnt;
	}
	public void setScopeDaysOrdCnt(Integer scopeDaysOrdCnt) {
		this.scopeDaysOrdCnt = scopeDaysOrdCnt;
	}
	public Integer getScopeDaysSkuCnt() {
		return scopeDaysSkuCnt;
	}
	public void setScopeDaysSkuCnt(Integer scopeDaysSkuCnt) {
		this.scopeDaysSkuCnt = scopeDaysSkuCnt;
	}
	public Float getScopeAvgPcsQty() {
		return scopeAvgPcsQty;
	}
	public void setScopeAvgPcsQty(Float scopeAvgPcsQty) {
		this.scopeAvgPcsQty = scopeAvgPcsQty;
	}
	public Float getPopularIndex() {
		return popularIndex;
	}
	public void setPopularIndex(Float popularIndex) {
		this.popularIndex = popularIndex;
	}
	public Float getDurationPcs() {
		return durationPcs;
	}
	public void setDurationPcs(Float durationPcs) {
		this.durationPcs = durationPcs;
	}
	@Override
	public int compareTo(DpsPopularSku o) {
		int i = (this.getPopularIndex()).compareTo(o.getPopularIndex());
		return i;
	}
}
