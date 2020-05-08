package operato.fnf.wcs.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.sys.ConfigConstants;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.util.ValueUtil;

/**
 * FnF용 추상화된 작업
 * 
 * @author shortstop
 */
public class AbstractFnFJob extends AbstractQueryService {
	/**
	 * Event Publisher
	 */
	@Autowired
	protected ApplicationEventPublisher eventPublisher;
	/**
	 * 이중화 서버의 양쪽에서 모두 처리되지 않게 한 쪽 서버에서 실행되도록 설정으로 처리하기 위함
	 * application.properties 설정 - job.scheduler.enable=true/false 설정 필요 (이중화 서버 한 대는 true, 나머지 서버는 false로 설정, 한 대만 운영시 true로 설정)
	 */
	@Autowired
	protected Environment env;
	/**
	 * Domain Controller
	 */
	@Autowired
	protected DomainController domainCtrl;
	
	/**
	 * 서버의 Job Scheduler가 활성화 되었는지 여부
	 * 
	 * @return
	 */
	protected boolean isJobEnabeld() {
		return ValueUtil.toBoolean(this.env.getProperty(ConfigConstants.JOB_SCHEDULER_ENABLED, LogisConstants.FALSE_STRING)); 
	}

}
