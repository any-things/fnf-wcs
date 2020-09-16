package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.batch.DasAutoReceiveBatchService;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.BeanUtil;

@Component
public class DasBatchAutoReceiveJob extends AbstractFnFJob {
	
	@Transactional
	@Scheduled(initialDelay=90000, fixedDelay=119000)
	public void job() {
		if(!this.isJobEnabeld()) {
//			return;
		}
		
		List<Domain> domainList = domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			DomainContext.setCurrentDomain(domain);
			BeanUtil.get(DasAutoReceiveBatchService.class).dasAutoReceiveBatchService(new HashMap<>());
		}
	}
}
