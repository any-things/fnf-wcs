package operato.fnf.wcs.service.model;

public class RecommandSku {
	private String id;
	private String workType;
	private String sumDate;
	private String skuCd;
	private Integer scopeDays;
	private Float outbDaysRate;
	private Float outbCountRate;
	private Integer durationDays;
	private Integer scopeDaysPcsQty;
	private Integer scopeDaysOrdCnt;
	private Integer scopeDaysSkuCnt;
	private Integer scopeAvgPcsQty;
	private Float popularIndex;
	private Integer durationPcs;
	private Integer needQty;

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

	public Integer getScopeAvgPcsQty() {
		return scopeAvgPcsQty;
	}

	public void setScopeAvgPcsQty(Integer scopeAvgPcsQty) {
		this.scopeAvgPcsQty = scopeAvgPcsQty;
	}

	public Float getPopularIndex() {
		return popularIndex;
	}

	public void setPopularIndex(Float popularIndex) {
		this.popularIndex = popularIndex;
	}

	public Integer getDurationPcs() {
		return durationPcs;
	}

	public void setDurationPcs(Integer durationPcs) {
		this.durationPcs = durationPcs;
	}	

	public Integer getNeedQty() {
		return needQty;
	}

	public void setNeedQty(Integer needQty) {
		this.needQty = needQty;
	}
}
