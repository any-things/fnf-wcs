package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.dps.DpsBatchAutoMerge;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.Setting;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;

@Component
public class DpsBatchAutoMergeJob extends AbstractFnFJob {
private final String JOB_STATUS = "dps.auto.merge.processing";
	
	@Transactional
	@Scheduled(initialDelay=30000, fixedDelay=20000)
	public void job() {
		if(!this.isJobEnabeld()) {
//			return;
		}
		
		String isRunning = SettingUtil.getValue(1l, JOB_STATUS);
		if ("Y".equals(isRunning)) {
			return;
		}
		BeanUtil.get(DpsBatchAutoMergeJob.class).updateJobStatus("Y");
		
		List<Domain> domainList = domainCtrl.domainList();
		
		try {
			for(Domain domain : domainList) {
				DomainContext.setCurrentDomain(domain);
				BeanUtil.get(DpsBatchAutoMerge.class).dpsBatchAutoMerge(new HashMap<>());
			}
		} catch(Exception e) {
			logger.error("DasBatchAutoReceiveJob error~~", e);
		} finally {
			BeanUtil.get(DpsBatchAutoMergeJob.class).updateJobStatus("N");
		}
		
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Setting updateJobStatus(String value) {
		Query conds = new Query(0, 1);
		conds.addFilter("name", JOB_STATUS);
		Setting setting = queryManager.selectByCondition(true, Setting.class, conds);
		
		setting.setValue(value);
		queryManager.update(setting);
		
		return setting;
	}
}
