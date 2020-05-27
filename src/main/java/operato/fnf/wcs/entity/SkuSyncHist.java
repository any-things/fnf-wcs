package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

/**
 * 상품 동기화 이력 
 * 
 * @author shortstop
 */
@Table(name = "sku_sync_hists", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_sku_sync_hists_01", columnList = "domain_id,sync_time"),
	@Index(name = "ix_sku_sync_hists_02", columnList = "domain_id,created_at")
})
public class SkuSyncHist extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 128695640988625317L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "sync_time", length = 20)
	private String syncTime;

	@Column (name = "sync_cnt")
	private Integer syncCnt;

	@Column (name = "err_sku_cd", length = 50)
	private String errSkuCd;

	@Column (name = "err_msg", length = 4000)
	private String errMsg;

	@Column (name = "status", length = 5)
	private String status;
	
	public SkuSyncHist() {
	}

	public SkuSyncHist(String syncTime, Integer syncCnt) {
		this.syncTime = syncTime;
		this.syncCnt = syncCnt;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(String syncTime) {
		this.syncTime = syncTime;
	}

	public Integer getSyncCnt() {
		return syncCnt;
	}

	public void setSyncCnt(Integer syncCnt) {
		this.syncCnt = syncCnt;
	}

	public String getErrSkuCd() {
		return errSkuCd;
	}

	public void setErrSkuCd(String errSkuCd) {
		this.errSkuCd = errSkuCd;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
