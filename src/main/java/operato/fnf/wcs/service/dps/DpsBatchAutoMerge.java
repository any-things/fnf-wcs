package operato.fnf.wcs.service.dps;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class DpsBatchAutoMerge extends AbstractQueryService {

	/**
	 * 서비스 디스패처
	 */
	@Autowired
	private LogisServiceDispatcher serviceDispatcher;
	
	public ResponseObj dpsBatchAutoMerge(Map<String, Object> params) throws Exception {
		
		// 작업진행중 batch조회 
		Query mainConds = new Query();
		mainConds.addFilter("status", "");	// FIXME
		List<JobBatch> mainJobBatches = queryManager.selectList(JobBatch.class, mainConds);
		
		if (mainJobBatches.size() == 0 || mainJobBatches.size() > 1) {
			return new ResponseObj();
		}
		
		JobBatch jobBatch = mainJobBatches.get(0);
		
		// 
		Query subConds = new Query();
		subConds.addFilter("status", "");	// FIXME
		List<JobBatch> subJobBatches = queryManager.selectList(JobBatch.class, subConds);
		
		if (subJobBatches.size() == 0) {
			return new ResponseObj();
		}
		
		for (JobBatch obj: subJobBatches) {
			try {				
				BeanUtil.get(DpsBatchAutoMerge.class).doMerge(jobBatch.getId(), obj.getId());
			} catch(Exception e) {
				logger.error("DpsBatchAutoMerge error~~", e);
			}
		}
		
		ResponseObj resp = new ResponseObj();
		return resp;
	}
	
	public void doMerge(String mainBatchId, String subBatchId) {
		// 1. 병합할 메인 배치 정보 조회 
		JobBatch mainBatch = this.findWithLock(true, mainBatchId, true);
		// 2. 병합될 배치 정보 조회 
		JobBatch sourceBatch = this.findWithLock(true, subBatchId, true);
		// 3. 작업 배치 병합
		this.serviceDispatcher.getInstructionService(mainBatch).mergeBatch(mainBatch, sourceBatch);
	}
	
	/**
	 * 작업 배치를 락을 걸면서 조회
	 * 
	 * @param exceptionWhenEmpty
	 * @param batchId
	 * @param findConfigSet
	 * @return
	 */
	private JobBatch findWithLock(boolean exceptionWhenEmpty, String batchId, boolean findConfigSet) {
		JobBatch batch = AnyEntityUtil.findEntityByIdByUnselectedWithLock(exceptionWhenEmpty, JobBatch.class, batchId, "jobConfigSet", "indConfigSet");
		
		if(batch == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.JobBatch", batchId);
		} else {
			if(findConfigSet) {
				if(ValueUtil.isNotEmpty(batch.getIndConfigSetId())) {
					batch.setIndConfigSet(AnyEntityUtil.findEntityById(false, IndConfigSet.class, batch.getIndConfigSetId()));
				}
				
				if(ValueUtil.isNotEmpty(batch.getJobConfigSetId())) {
					batch.setJobConfigSet(AnyEntityUtil.findEntityById(false, JobConfigSet.class, batch.getJobConfigSetId()));
				}
			}
		}
		
		return batch;
	}
}
