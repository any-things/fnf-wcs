package xyz.anythings.base.entity;

import java.util.ArrayList;
import java.util.List;

import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 주문 수신 요약 마스터
 * 
 * @author shortstop
 */
@Table(name = "batch_receipts", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_batch_receipts_0", columnList = "domain_id,com_cd,job_date,job_seq")
})
public class BatchReceipt extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 673601041015828394L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", length = 10)
	private String jobSeq;

	@Column (name = "status", length = 10)
	private String status;
	
	@Ignore
	private List<BatchReceiptItem> items;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getAreaCd() {
		return areaCd;
	}

	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public List<BatchReceiptItem> getItems() {
		return this.items;
	}
	
	public void setItems(List<BatchReceiptItem> items) {
		this.items = items;
	}
	
	public void addItem(BatchReceiptItem item) {
		if(this.items == null) {
			this.items = new ArrayList<BatchReceiptItem>();
		}
		
		this.items.add(item);
	}
	
	/**
	 * 배치 Receipt 의 현재 상태를 가져온다. 
	 * @param batchReceipt
	 * @return
	 */
	public String getCurrentStatus() {
		BatchReceipt checkReceipt = AnyEntityUtil.findEntityById(false, BatchReceipt.class, this.getId());
		this.setStatus(checkReceipt.getStatus());
		return checkReceipt.getStatus();
	}
	
	/**
	 * 상태 업데이트 
	 * @param status
	 */
	public void updateStatusImmediately(String status) {
		this.setStatus(status);
		BeanUtil.get(IQueryManager.class).update(this, "status");
	}
	
	/**
	 * BatchReceipt JobSeq 데이터 구하기 
	 * @param domainId
	 * @param areaCd
	 * @param stageCd
	 * @param comCd
	 * @param jobDate
	 * @return
	 */
	public static int newBatchReceiptJobSeq(Long domainId, String areaCd, String stageCd, String comCd, String jobDate) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("jobSeq");
		condition.addFilter("comCd", comCd);
		condition.addFilter("areaCd", areaCd);
		condition.addFilter("stageCd", stageCd);
		condition.addFilter("jobDate", jobDate);
		condition.addOrder("jobSeq", false);
		
		List<BatchReceipt> jobSeqList = queryManager.selectList(BatchReceipt.class, condition);
		return (ValueUtil.isEmpty(jobSeqList) ? 0 : ValueUtil.toInteger(jobSeqList.get(0).getJobSeq())) + 1;
	}
}
