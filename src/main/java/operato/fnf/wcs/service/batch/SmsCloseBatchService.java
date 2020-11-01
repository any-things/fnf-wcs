package operato.fnf.wcs.service.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.entity.WcsMheDasRtnBoxRslt;
import operato.fnf.wcs.entity.WcsMheDr;
import operato.fnf.wcs.entity.WcsMheHr;
import operato.fnf.wcs.entity.WmsMheBox;
import operato.fnf.wcs.entity.WmsMheDr;
import operato.fnf.wcs.entity.WmsMheHr;
import operato.fnf.wcs.entity.WmsRtnSortDr;
import operato.fnf.wcs.entity.WmsRtnSortHr;
import operato.fnf.wcs.service.send.SmsInspSendService;
import operato.logis.sms.SmsConstants;
import operato.logis.wcs.service.impl.WcsBatchProgressService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.ValueUtil;

/**
 * SMS 작업 배치 종료 서비스
 * 
 * 
 */
@Component
public class SmsCloseBatchService extends AbstractQueryService {
	/**
	 * DAS 작업 서머리 서비스
	 */
	@Autowired
	private JobSummaryService jobSummarySvc;
	
	/**
	 * 반품 검수완료 Box 전송 서비스
	 */
	@Autowired
	private SmsInspSendService smsInspSendSvc;
	
	/**
	 * WCS 배치 생산성 정보 업데이트 서비스 
	 */
	@Autowired
	private WcsBatchProgressService progressSvc;
	
	/**
	 * SMS 작업 배치 종료
	 * 
	 * @param batch
	 */
	public void closeBatch(JobBatch batch) {
		// 1. 10분 생산성 최종 마감
		// 작업해야함 실적을 가지고 쿼리 생성해야한다.
		this.closeProductivity(batch);
		
		if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SRTN)) {
			// 2. 배치에 반영
			this.setBatchInfoOnClosing(batch);
			// 3. 반품검수결과 WMS 전송
			this.sendInspBoxScanResultToWms(batch);
			// 4. WMS MHE_HR 테이블에 마감 전송
			this.sendRtnBoxResultToWms(batch);
		} else if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SDAS)) {
			Query batchConds = new Query();
			batchConds.addFilter("batchGroupId", batch.getBatchGroupId());
			List<JobBatch> batchGroupList = this.queryManager.selectList(JobBatch.class, batchConds);
			for (JobBatch jobBatch : batchGroupList) {
				// 2. 배치에 반영
				this.setBatchInfoOnClosing(jobBatch);
				// 3. WMS에 박스 실적 한 번에 전송
				this.sendAllBoxToWms(jobBatch);
				// 4. WMS에 최종 피킹 실적을 한 번에 전송
				this.sendAllPickingToWms(jobBatch);
				// 5. WMS MHE_HR 테이블에 반영
				this.closeWmsWave(jobBatch);
				// 6. WCS MHE_HR 테이블에 반영
				this.closeWcsWave(jobBatch);
			}
		} else if(ValueUtil.isEqual(batch.getJobType(), SmsConstants.JOB_TYPE_SDPS)) {
			// 2. 배치에 반영
			this.setBatchInfoOnClosing(batch);
			Query batchConds = new Query();
			batchConds.addFilter("batchGroupId", batch.getBatchGroupId());
			List<JobBatch> batchGroupList = this.queryManager.selectList(JobBatch.class, batchConds);
			for (JobBatch jobBatch : batchGroupList) {
				// 3. WMS MHE_HR 테이블에 반영
				this.closeWmsWave(jobBatch);
				// 4. WCS MHE_HR 테이블에 반영
				this.closeWcsWave(jobBatch);
			}
		}
		
	}
	
	/**
	 * 배치 종료시에 설정할 정보 설정
	 * 
	 * @param batch
	 */
	private void setBatchInfoOnClosing(JobBatch batch) {
		if(ValueUtil.isEmpty(batch.getFinishedAt())) {
			batch.setFinishedAt(new Date());
		}
		this.progressSvc.updateBatchProductionResult(batch, batch.getFinishedAt());
		batch.setStatus(JobBatch.STATUS_END);		
		this.queryManager.update(batch, "status", "finishedAt", "resultBoxQty", "resultOrderQty", "resultPcs", "progressRate", "equipRuntime", "uph", "updatedAt");
	}
	
	/**
	 * 작업 배치 생산성 최종 마감
	 * 
	 * @param batch
	 */
	private void closeProductivity(JobBatch batch) {
		if(ValueUtil.isEmpty(batch.getFinishedAt())) {
			batch.setFinishedAt(new Date());
		}
		
		// 작업 해야함.
		this.jobSummarySvc.summaryTotalBatchJobs(batch);
	}
	
	/**
	 * 소터 실적으로 검수정보가 없는 정보들은 매장 반품예정 검수 스캔결과(WMT_UIF_IMP_MHE_RTN_SCAN) 테이블로 전송
	 */
	public void sendInspBoxScanResultToWms(JobBatch batch) {
		if(ValueUtil.isEqualIgnoreCase(SmsConstants.JOB_TYPE_SRTN, batch.getJobType()) && ValueUtil.isEqualIgnoreCase(batch.getBatchType(), FnFConstants.ORDER_RECEIVE_WMS)
				&& ValueUtil.isEqualIgnoreCase(batch.getRfidYn(), LogisConstants.N_CAP_STRING)) {
			this.smsInspSendSvc.sendInspBoxScanResultToWms(batch);
		}
	}
	
	/**
	 * 반품 실적 WMS Interface
	 */
	@SuppressWarnings("unchecked")
	public void sendRtnBoxResultToWms(JobBatch batch) {
		String jobBatchSql = "select brand_cd, equip_cd from job_batches where batch_group_id = :batchGroupId group by brand_cd, equip_cd";
		Map<String, Object> query = ValueUtil.newMap("batchGroupId", batch.getBatchGroupId());
		List<JobBatch> jobBatches = this.queryManager.selectListBySql(jobBatchSql, query, JobBatch.class, 0, 0);
		
		Query mainConds = new Query();
		mainConds.addFilter("id", batch.getBatchGroupId());
		JobBatch mainBatch = this.queryManager.select(JobBatch.class, mainConds);
		
		String sql = "SELECT nvl(max(TO_NUMBER(SORT_SEQ)), 0) + 1 AS seq FROM RTN_SORT_HR WHERE WH_CD = :whCd AND MHE_NO = :mheNo AND SORT_DATE = :sortDate";
		Map<String, Object> conds = ValueUtil.newMap("whCd,mheNo,sortDate", FnFConstants.WH_CD_ICF, batch.getEquipCd(), mainBatch.getJobDate().replaceAll("-", ""));
		Map<String, Object> maxSeq = this.getDataSourceQueryManager(WmsRtnSortHr.class).selectBySql(sql, conds, Map.class);
		
		int jobSeq = ValueUtil.toInteger(maxSeq.get("seq"));
		Map<String, Object> brandList = new HashMap<String, Object>();
		
		int ifYnCnt = this.queryManager.selectSize(WcsMheDasRtnBoxRslt.class, ValueUtil.newMap("batchNo,ifYn", batch.getBatchGroupId(), LogisConstants.CAP_Y_STRING));
		
		
		if(ifYnCnt == 0) {
			for (JobBatch jobBatch : jobBatches) {
				brandList.put(jobBatch.getBrandCd(), jobSeq);
				WmsRtnSortHr rtnSortHr = new WmsRtnSortHr();
				rtnSortHr.setWhCd(FnFConstants.WH_CD_ICF);
				rtnSortHr.setMheNo(jobBatch.getEquipCd());
				rtnSortHr.setStrrId(jobBatch.getBrandCd());
				rtnSortHr.setSortDate(mainBatch.getJobDate().replaceAll("-", ""));
				rtnSortHr.setSortSeq(ValueUtil.toString(jobSeq));
				rtnSortHr.setStatus("A");
				rtnSortHr.setInsDatetime(new Date());
				this.getDataSourceQueryManager(WmsRtnSortHr.class).insert(rtnSortHr);
				jobSeq++;
			}
		}
		
		Query wmsCondition = new Query();
		wmsCondition.addFilter("WH_CD", FnFConstants.WH_CD_ICF);
		wmsCondition.addFilter("BATCH_NO", batch.getBatchGroupId());
		wmsCondition.addFilter("DEL_YN", LogisConstants.N_CAP_STRING);
		wmsCondition.addFilter("IF_YN", LogisConstants.N_CAP_STRING);
		List<WcsMheDasRtnBoxRslt> boxList = this.queryManager.selectList(WcsMheDasRtnBoxRslt.class, wmsCondition);
		List<WmsRtnSortDr> rtnSortDrList = new ArrayList<WmsRtnSortDr>(boxList.size());
		
		for (WcsMheDasRtnBoxRslt rtnBox : boxList) {
			WmsRtnSortDr sortDr = new WmsRtnSortDr();
			sortDr.setWhCd(FnFConstants.WH_CD_ICF);
			sortDr.setMheNo(rtnBox.getMheNo());
			sortDr.setStrrId(rtnBox.getStrrId());
			sortDr.setSortDate(rtnBox.getSortDate());
			sortDr.setSortSeq(ValueUtil.toString(brandList.get(rtnBox.getStrrId())) == null ? "1" : ValueUtil.toString(brandList.get(rtnBox.getStrrId())));
			sortDr.setItemCd(rtnBox.getItemCd());
			sortDr.setBoxNo(rtnBox.getBoxNo());
			sortDr.setCmptQty(rtnBox.getCmptQty());
			sortDr.setInsDatetime(new Date());
			
			rtnSortDrList.add(sortDr);
			
			rtnBox.setIfYn(LogisConstants.CAP_Y_STRING);
			rtnBox.setCnfDatetime(new Date());
		}
		this.getDataSourceQueryManager(WmsRtnSortDr.class).insertBatch(rtnSortDrList);
		this.queryManager.updateBatch(boxList);
	}
	
	/**
	 * 박스 전송 실적을 한 번에 전송
	 * 
	 * @param batch
	 * @return
	 */
	private int sendAllBoxToWms(JobBatch batch) {
		// 배치별 박스 실적 모두 조회
		Query condition = new Query();
		condition.addFilter("workUnit", batch.getId());
		// 박스 취소가 아닌 모든 박스 실적을 전송
		condition.addFilter("delYn", SysConstants.NOT_EQUAL, LogisConstants.Y_CAP_STRING);
		List<WcsMheBox> wcsBoxList = this.queryManager.selectList(WcsMheBox.class, condition);
		List<WmsMheBox> wmsBoxList = new ArrayList<WmsMheBox>();
		
		// 배치별 박스 실적 WMS로 전송
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheBox.class);
		for(WcsMheBox fromBox : wcsBoxList) {
			WmsMheBox toBox = ValueUtil.populate(fromBox, new WmsMheBox());
			wmsBoxList.add(toBox);
			
			if(wmsBoxList.size() >= 500) {
				wmsQueryMgr.insertBatch(wmsBoxList);
				wmsBoxList.clear();
			}
		}
		
		if(!wmsBoxList.isEmpty()) {
			wmsQueryMgr.insertBatch(wmsBoxList);
			wmsBoxList.clear();
		}
		
		// 박스 전송 플래그 및 시간 업데이트
		String sql = "update mhe_box set cnf_datetime = now() where work_unit = :batchId and del_yn != 'Y'";
		this.queryManager.executeBySql(sql, ValueUtil.newMap("batchId", batch.getId()));
		
		// 박스 리스트 사이즈 리턴
		return wcsBoxList.size();
	}
	
	/**
	 * 피킹 실적을 한 번에 전송
	 * 
	 * @param batch
	 * @return
	 */
	private int sendAllPickingToWms(JobBatch batch) {
		// WCS 배치 주문 정보 모두 조회
		Query condition = new Query();
		condition.addSelect("wh_cd", "work_unit", "shipto_id", "outb_no", "location_cd", "item_cd", "cmpt_qty");
		condition.addFilter("workUnit", batch.getId());
		List<WcsMheDr> wcsOrders = this.queryManager.selectList(WcsMheDr.class, condition);
		
		// WMS 배치 주문 정보에 업데이트
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheDr.class);
		String sql = "update mhe_dr set cmpt_qty = :cmptQty where wh_cd = :whCd and work_unit = :workUnit and shipto_id = :shiptoId and outb_no = :outbNo and location_cd = :locationCd and item_cd = :itemCd";
		int updatedCount = 0;
		
		for(WcsMheDr wcsOrder : wcsOrders) {
			Map<String, Object> params = ValueUtil.newMap("whCd,workUnit,shiptoId,outbNo,locationCd,itemCd,cmptQty", wcsOrder.getWhCd(), wcsOrder.getWorkUnit(), wcsOrder.getShiptoId(), wcsOrder.getOutbNo(), wcsOrder.getLocationCd(), wcsOrder.getItemCd(), wcsOrder.getCmptQty());
			updatedCount += wmsQueryMgr.executeBySql(sql, params);
		}
		
		// 업데이트 개수 리턴
		return updatedCount;
	}
	
	/**
	 * WMS Wave를 마감 처리 
	 * 
	 * @param batch
	 * @param wcsMheHr
	 */
	private void closeWmsWave(JobBatch batch) {
		Query wcsHrConds = new Query();
		wcsHrConds.addFilter("whCd", FnFConstants.WH_CD_ICF);
		wcsHrConds.addFilter("workUnit", batch.getId());
		WcsMheHr wcsMheHr = this.queryManager.selectByCondition(WcsMheHr.class, wcsHrConds);
		
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsMheHr.class);
		Query condition = new Query();
		condition.addFilter("whCd", FnFConstants.WH_CD_ICF);
		condition.addFilter("workUnit", batch.getId());
		WmsMheHr wmsWave = wmsQueryMgr.selectByCondition(WmsMheHr.class, condition);
		
		if(wmsWave != null) {
			wmsWave.setCmptQty(wcsMheHr.getCmptQty());
			wmsWave.setStatus("C");
			wmsWave.setCnfDatetime(new Date());
			wmsQueryMgr.update(wmsWave, "cmptQty", "status", "cnfDatetime");
		}		
	}
	
	/**
	 * WCS Wave 마감 처리
	 * 
	 * @param batch
	 * @param wcsMheHr
	 */
	private void closeWcsWave(JobBatch batch) {
		Query wcsHrConds = new Query();
		wcsHrConds.addFilter("whCd", FnFConstants.WH_CD_ICF);
		wcsHrConds.addFilter("workUnit", batch.getId());
		WcsMheHr wcsMheHr = this.queryManager.selectByCondition(WcsMheHr.class, wcsHrConds);
		
		Date currentTime = new Date();
		wcsMheHr.setStatus("F");
		wcsMheHr.setCnfDatetime(currentTime);
		wcsMheHr.setEndDatetime(currentTime);
		wcsMheHr.setPrcsDatetime(currentTime);
		wcsMheHr.setPrcsYn(LogisConstants.Y_CAP_STRING);
		this.queryManager.update(wcsMheHr, "status", "cnfDatetime", "endDatetime", "prcsDatetime", "prcsYn");		
	}

}
