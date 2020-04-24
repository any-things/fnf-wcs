package xyz.anythings.base.entity;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Table(name = "job_batches", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_job_batches_0", columnList = "domain_id,job_type,job_date,status"),
	@Index(name = "ix_job_batches_1", columnList = "domain_id,wms_batch_no"),
	@Index(name = "ix_job_batches_2", columnList = "domain_id,batch_group_id"),
	@Index(name = "ix_job_batches_3", columnList = "domain_id,equip_type,equip_cd"),
	@Index(name = "ix_job_batches_4", columnList = "domain_id,brand_cd,season_cd"),
	@Index(name = "ix_job_batches_5", columnList = "domain_id,area_cd,stage_cd")
})
public class JobBatch extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 594615629904242255L;

	/**
	 * I/F 데이터 수신 중 상태 : RECEIVING
	 */
	public static final String STATUS_RECEIVE = "RECEIVING";
	/**
	 * 대기 상태 : WAIT
	 */
	public static final String STATUS_WAIT = "WAIT";
	/**
	 * 작업 지시 가능 상태 : READY
	 */
	public static final String STATUS_READY = "READY";
	/**
	 * 작업 지시 가능 상태 : READY
	 */
	public static final String STATUS_MERGED = "MERGED";
	/**
	 * 진행 상태 : RUNNING
	 */
	public static final String STATUS_RUNNING = "RUN";
	/**
	 * 완료 상태 : END
	 */
	public static final String STATUS_END = "END";
	/**
	 * 주문 취소 상태 : CANCEL
	 */
	public static final String STATUS_CANCEL = "CANCEL";
	
	/**
	 * 배치 ID (WMS의 WorkUnit 코드)
	 */
	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	/**
	 * WMS의 WAVE 번호
	 */
	@Column (name = "wms_batch_no", length = 40)
	private String wmsBatchNo;

	/**
	 * 배치 ID (WMS의 WorkUnit 코드)
	 */
	@Column (name = "wcs_batch_no", length = 40)
	private String wcsBatchNo;

	/**
	 * WMS의 WAVE 번호
	 */
	@Column (name = "batch_group_id", length = 40)
	private String batchGroupId;

	/**
	 * 배치 타이틀
	 */
	@Column (name = "title", length = 255)
	private String title;
	
	/**
	 * 브랜드 코드
	 */
	@Column (name = "brand_cd", length = 2)
	private String brandCd;
	
	/**
	 * 시즌 코드
	 */
	@Column (name = "season_cd", length = 4)
	private String seasonCd;
	
	@Column (name = "com_cd", length = 30)
	private String comCd;

	/**
	 * 작업 유형 (WMS의 BIZ_TYPE)
	 */
	@Column (name = "job_type", nullable = false, length = 20)
	private String jobType;
	
	/**
	 * 반품인 경우 (반품 유형 - A : 시즌 반품, B : 지시 반품), 그 외의 경우 (일반 슈트 배정여부 - A : 일반, B : 행사예외 출고분)
	 */
	@Column (name = "batch_type", length = 1)
	private String batchType;

	/**
	 * 작업 일자
	 */
	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;
	
	/**
	 * 반품 배치인 경우 반품 등록 가능일 To
	 */
	@Column (name = "job_date_to", length = 10)
	private String jobDateTo;

	@Column (name = "job_seq", nullable = false, length = 12)
	private String jobSeq;

	@Column (name = "area_cd", length = 30)
	private String areaCd;

	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "equip_type", length = 20)
	private String equipType;
	
	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	@Column (name = "parent_order_qty", nullable = false, length = 12)
	private Integer parentOrderQty;

	@Column (name = "batch_order_qty", nullable = false, length = 12)
	private Integer batchOrderQty;

	@Column (name = "result_order_qty", length = 12)
	private Integer resultOrderQty;

	@Column (name = "parent_pcs", nullable = false, length = 12)
	private Integer parentPcs;

	@Column (name = "batch_pcs", nullable = false, length = 12)
	private Integer batchPcs;

	@Column (name = "result_pcs", length = 12)
	private Integer resultPcs;

	@Column (name = "progress_rate", length = 19)
	private Float progressRate;
	
	@Column (name = "input_workers", length = 12)
	private Integer inputWorkers;
	
	@Column (name = "uph", length = 19)
	private Float uph;
	
	@Column (name = "equip_runtime", length = 12)
	private Integer equipRuntime;

	@Column (name = "instructed_at", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date instructedAt;

	@Column (name = "finished_at", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date finishedAt;

	@Column (name = "last_input_seq", length = 12)
	private Integer lastInputSeq;

	@Column (name = "status", length = 10)
	private String status;
	
	@Column (name = "job_config_set_id", length = 40)
	private String jobConfigSetId;
	
	@Column (name = "ind_config_set_id", length = 40)
	private String indConfigSetId;

	@Relation(field = "jobConfigSetId")
	private JobConfigSet jobConfigSet;

	@Relation(field = "indConfigSetId")
	private IndConfigSet indConfigSet;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWmsBatchNo() {
		return wmsBatchNo;
	}

	public void setWmsBatchNo(String wmsBatchNo) {
		this.wmsBatchNo = wmsBatchNo;
	}

	public String getWcsBatchNo() {
		return wcsBatchNo;
	}

	public void setWcsBatchNo(String wcsBatchNo) {
		this.wcsBatchNo = wcsBatchNo;
	}

	public String getBatchGroupId() {
		return batchGroupId;
	}

	public void setBatchGroupId(String batchGroupId) {
		this.batchGroupId = batchGroupId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBrandCd() {
		return brandCd;
	}

	public void setBrandCd(String brandCd) {
		this.brandCd = brandCd;
	}

	public String getSeasonCd() {
		return seasonCd;
	}

	public void setSeasonCd(String seasonCd) {
		this.seasonCd = seasonCd;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getBatchType() {
		return batchType;
	}

	public void setBatchType(String batchType) {
		this.batchType = batchType;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobDateTo() {
		return jobDateTo;
	}

	public void setJobDateTo(String jobDateTo) {
		this.jobDateTo = jobDateTo;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
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
	
	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
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

	public String getEquipNm() {
		return equipNm;
	}

	public void setEquipNm(String equipNm) {
		this.equipNm = equipNm;
	}

	public Integer getParentOrderQty() {
		return parentOrderQty;
	}

	public void setParentOrderQty(Integer parentOrderQty) {
		this.parentOrderQty = parentOrderQty;
	}

	public Integer getBatchOrderQty() {
		return batchOrderQty;
	}

	public void setBatchOrderQty(Integer batchOrderQty) {
		this.batchOrderQty = batchOrderQty;
	}

	public Integer getResultOrderQty() {
		return resultOrderQty;
	}

	public void setResultOrderQty(Integer resultOrderQty) {
		this.resultOrderQty = resultOrderQty;
	}

	public Integer getParentPcs() {
		return parentPcs;
	}

	public void setParentPcs(Integer parentPcs) {
		this.parentPcs = parentPcs;
	}

	public Integer getBatchPcs() {
		return batchPcs;
	}

	public void setBatchPcs(Integer batchPcs) {
		this.batchPcs = batchPcs;
	}

	public Integer getResultPcs() {
		return resultPcs;
	}

	public void setResultPcs(Integer resultPcs) {
		this.resultPcs = resultPcs;
	}

	public Float getProgressRate() {
		return progressRate;
	}

	public void setProgressRate(Float progressRate) {
		this.progressRate = progressRate;
	}

	public Integer getInputWorkers() {
		return inputWorkers;
	}

	public void setInputWorkers(Integer inputWorkers) {
		this.inputWorkers = inputWorkers;
	}

	public Float getUph() {
		return uph;
	}

	public void setUph(Float uph) {
		this.uph = uph;
	}

	public Integer getEquipRuntime() {
		return equipRuntime;
	}

	public void setEquipRuntime(Integer equipRuntime) {
		this.equipRuntime = equipRuntime;
	}

	public Date getInstructedAt() {
		return instructedAt;
	}

	public void setInstructedAt(Date instructedAt) {
		this.instructedAt = instructedAt;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public Integer getLastInputSeq() {
		return lastInputSeq;
	}

	public void setLastInputSeq(Integer lastInputSeq) {
		this.lastInputSeq = lastInputSeq;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getJobConfigSetId() {
		return this.jobConfigSetId;
	}
	
	public void setJobConfigSetId(String jobConfigSetId) {
		this.jobConfigSetId = jobConfigSetId;
	}
	
	public String getIndConfigSetId() {
		return this.indConfigSetId;
	}
	
	public void setIndConfigSetId(String indConfigSetId) {
		this.indConfigSetId = indConfigSetId;
	}
	
	public JobConfigSet getJobConfigSet() {
		return jobConfigSet;
	}

	public void setJobConfigSet(JobConfigSet jobConfigSet) {
		this.jobConfigSet = jobConfigSet;

		if(this.jobConfigSet != null) {
			String refId = this.jobConfigSet.getId();
			if (refId != null )
				this.jobConfigSetId = refId;
		}
	}

	public IndConfigSet getIndConfigSet() {
		return indConfigSet;
	}

	public void setIndConfigSet(IndConfigSet indConfigSet) {
		this.indConfigSet = indConfigSet;

		if(this.indConfigSet != null) {
			String refId = this.indConfigSet.getId();
			if (refId != null)
				this.indConfigSetId = refId;
		}
	}
		
	/**
	 * 상태 업데이트, 즉시 반영
	 *  
	 * @param status
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public void updateStatusImmediately(String status) {
		this.updateStatus(status);
	}
	
	/**
	 * 상태 업데이트
	 * 
	 * @param status
	 */
	public void updateStatus(String status) {
		this.setStatus(status);
		
		if(ValueUtil.isEqual(JobBatch.STATUS_CANCEL, status)) {
			this.setJobSeq("0");
			BeanUtil.get(IQueryManager.class).update(this, "jobSeq", "status");
		} else {
			BeanUtil.get(IQueryManager.class).update(this, "status");
		}
	}

	/**
	 * 현재 잡 시퀀스의 최대 값을 가져온다.
	 * 
	 * @param domainId
	 * @param comCd
	 * @param areaCd
	 * @param stageCd
	 * @param jobDate
	 * @return
	 */
	public static int getMaxJobSeq(Long domainId, String comCd, String areaCd, String stageCd, String jobDate) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("jobSeq");
		condition.addFilter("comCd", comCd);
		condition.addFilter("areaCd", areaCd);
		condition.addFilter("stageCd", stageCd);
		condition.addFilter("jobDate", jobDate);
		condition.addOrder("jobSeq", false);
		List<JobBatch> jobSeqList = queryManager.selectList(JobBatch.class, condition);
		String maxJobSeq = (ValueUtil.isEmpty(jobSeqList) ? "0" : jobSeqList.get(0).getJobSeq());
		return ValueUtil.toInteger(maxJobSeq);
	}
	
	/**
	 * JobBatch 데이터 생성
	 * 
	 * @param batchId
	 * @param jobSeq
	 * @param batchReceipt
	 * @param receiptItem
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public static JobBatch createJobBatch(String batchId, String jobSeq, BatchReceipt batchReceipt, BatchReceiptItem receiptItem) {
		JobBatch batch = new JobBatch();
		batch.setId(batchId);
		batch.setBatchGroupId(batchId);
		batch.setWmsBatchNo(receiptItem.getWmsBatchNo());
		batch.setWcsBatchNo(receiptItem.getWcsBatchNo());
		batch.setComCd(receiptItem.getComCd());
		batch.setJobType(receiptItem.getJobType());
		batch.setJobDate(batchReceipt.getJobDate());
		batch.setJobSeq(jobSeq);
		batch.setAreaCd(receiptItem.getAreaCd());
		batch.setStageCd(receiptItem.getStageCd());
		batch.setEquipType(receiptItem.getEquipType());
		batch.setEquipCd(receiptItem.getEquipCd());
		batch.setEquipNm(LogisConstants.EMPTY_STRING);
		batch.setParentOrderQty(receiptItem.getTotalOrders());
		batch.setParentPcs(receiptItem.getTotalPcs());
		batch.setBatchOrderQty(receiptItem.getTotalOrders());
		batch.setBatchPcs(receiptItem.getTotalPcs());
		batch.setStatus(JobBatch.STATUS_RECEIVE);
		BeanUtil.get(IQueryManager.class).insert(batch);
		return batch;
	}

}
