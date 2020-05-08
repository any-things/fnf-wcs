package operato.fnf.wcs.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xyz.elidom.sys.util.DateUtil;

/**
 * 일별 실적 서머리 잡
 * 	- 매일 오전 5시에 완료된 작업 배치의 작업 실적을 DailyProdSummary 테이블에 반영
 * 
 * @author shortstop
 */
@Component
public class DailySummaryJob {

	/**
	 * 매일 오전 5시에 실행되어 일별 서머리 처리 
	 */
	@Transactional
	@Scheduled(cron="0 0 5 * * *")
	public void summaryJob() {
		System.out.println(DateUtil.currentTimeStr());
	}
	
}
