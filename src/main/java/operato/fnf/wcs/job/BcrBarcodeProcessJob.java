package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.bcr.BcrBarcodeProcess;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.BeanUtil;

@Component
public class BcrBarcodeProcessJob extends AbstractFnFJob {
	
	@Transactional
	@Scheduled(initialDelay=30000, fixedDelay=30000)
	public void summaryJob() throws Exception {
		// 1. 스케줄링 활성화 여부
		if(!this.isJobEnabeld()) {
			return;
		}
		
		// 2. Database로 부터 현재 시간, 분 정보 추출
		// CurrentDbTime currentTime = LogisBaseUtil.currentDbDateTime();
		
		// 3. 모든 도메인 조회
		List<Domain> domainList = this.domainCtrl.domainList();
		
		for(Domain domain : domainList) {
			DomainContext.setCurrentDomain(domain);
			
			Map<String, Object> params = new HashMap<>();
			params.put("domainId", domain.getId());
			BeanUtil.get(BcrBarcodeProcess.class).bcrBarcodeProcess(params);
		}
	}
}
