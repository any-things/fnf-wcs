package operato.fnf.wcs.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.service.summary.CalcPopularProduct;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.BeanUtil;

@Component
public class CalcOnlineTopSku extends AbstractFnFJob {
	@Transactional
	@Scheduled(cron="0 0 22 * * 1-5")
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
			//params.put("domainId", domain.getId());
			BeanUtil.get(CalcPopularProduct.class).calcPopularProduct(params);
		}
	}
}
