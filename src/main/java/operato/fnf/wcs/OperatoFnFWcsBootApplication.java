package operato.fnf.wcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = { "xyz.anythings.*", "xyz.elidom.*", "operato.*" })
@ImportResource({ "classpath:/WEB-INF/application-context.xml", "classpath:/WEB-INF/dataSource-context.xml" })
public class OperatoFnFWcsBootApplication implements SchedulingConfigurer {
	
	public static void main(String[] args) {
		SpringApplication.run(OperatoFnFWcsBootApplication.class, args);
	}
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler poolScheduler = new ThreadPoolTaskScheduler();
		poolScheduler.setBeanName("FnfPool");
		poolScheduler.setPoolSize(20);
		poolScheduler.setThreadNamePrefix("fnf-");
		poolScheduler.setWaitForTasksToCompleteOnShutdown(false);
		poolScheduler.initialize();
		taskRegistrar.setTaskScheduler(poolScheduler);
	}

}