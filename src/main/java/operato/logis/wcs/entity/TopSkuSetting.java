package operato.logis.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "top_sku_settings", idStrategy = GenerationRule.UUID)
public class TopSkuSetting extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 633172857776651963L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "work_type")
	private String workType;

	@Column (name = "scope_days")
	private Integer scopeDays;

	@Column (name = "outb_days_rate")
	private Float outbDaysRate;

	@Column (name = "outb_qty_rate")
	private Float outbQtyRate;

	@Column (name = "duration_days")
	private Integer durationDays;
  
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

	public Float getOutbQtyRate() {
		return outbQtyRate;
	}

	public void setOutbQtyRate(Float outbQtyRate) {
		this.outbQtyRate = outbQtyRate;
	}

	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}	
}
