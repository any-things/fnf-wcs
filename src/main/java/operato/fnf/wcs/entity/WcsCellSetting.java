package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "wcs_cell_setting", idStrategy = GenerationRule.UUID, uniqueFields="id")
public class WcsCellSetting extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 317862547093029909L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "sku_cnt")
	private Integer skuCnt;

	@Column (name = "min_used_rate")
	private Float minUsedRate;

	@Column (name = "max_used_rate")
	private Float maxUsedRate;

	@Column (name = "idle_loc_rate")
	private Float idleLocRate;

	@Column (name = "refresh_term")
	private Integer refreshTerm;

	@Column (name = "flag")
	private String flag;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getSkuCnt() {
		return skuCnt;
	}

	public void setSkuCnt(Integer skuCnt) {
		this.skuCnt = skuCnt;
	}

	public Float getMinUsedRate() {
		return minUsedRate;
	}

	public void setMinUsedRate(Float minUsedRate) {
		this.minUsedRate = minUsedRate;
	}

	public Float getMaxUsedRate() {
		return maxUsedRate;
	}

	public void setMaxUsedRate(Float maxUsedRate) {
		this.maxUsedRate = maxUsedRate;
	}

	public Float getIdleLocRate() {
		return idleLocRate;
	}

	public void setIdleLocRate(Float idleLocRate) {
		this.idleLocRate = idleLocRate;
	}

	public Integer getRefreshTerm() {
		return refreshTerm;
	}

	public void setRefreshTerm(Integer refreshTerm) {
		this.refreshTerm = refreshTerm;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}	
}
