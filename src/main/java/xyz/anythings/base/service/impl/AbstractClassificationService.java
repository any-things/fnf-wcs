package xyz.anythings.base.service.impl;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.service.api.IClassificationService;


/**
 * 분류 공통 (Picking & Assorting) 트랜잭션 서비스 기본 구현
 *  
 * @author yang
 */
public abstract class AbstractClassificationService extends AbstractLogisService implements IClassificationService {
	
	@Override
	public JobConfigSet getJobConfigSet(String batchId) {
		return this.serviceDispatcher.getConfigSetService().getConfigSet(batchId);
	}

	@Override
	public Object classCellMapping(JobBatch batch, String cellCd, String classCd, Object ... params) {
		return null;
	}
}
