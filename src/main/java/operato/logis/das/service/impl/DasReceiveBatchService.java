package operato.logis.das.service.impl;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.base.event.main.BatchReceiveEvent;
import xyz.anythings.sys.service.AbstractQueryService;

/**
 * DAS, 반품 주문 수신용 서비스
 * 
 * @author shortstop
 */
@Component
public class DasReceiveBatchService extends AbstractQueryService {
	
	/**
	 * 주문 정보 수신을 위한 수신 서머리 정보 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 10 and #event.eventStep == 1 and (#event.jobType == 'DAS')")
	public void handleReadyToReceive(BatchReceiveEvent event) { 		
		event.setExecuted(true);
	}
	
	
	/**
	 * 주문 정보 수신 시작
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 20 and #event.eventStep == 1 and (#event.jobType == 'DAS')")
	public void handleStartToReceive(BatchReceiveEvent event) {
		event.setExecuted(true);
	}
	
	/**
	 * 주문 수신 취소
	 * 
	 * @param event
	 */
	@EventListener(classes = BatchReceiveEvent.class, condition = "#event.isExecuted() == false and #event.eventType == 30 and #event.eventStep == 1 and (#event.jobType == 'DAS')")
	public void handleCancelReceived(BatchReceiveEvent event) {
		event.setExecuted(true);
	}
}
