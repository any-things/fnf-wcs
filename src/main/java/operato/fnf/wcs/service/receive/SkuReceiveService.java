package operato.fnf.wcs.service.receive;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.base.event.master.SkuReceiptEvent;
import xyz.anythings.base.service.impl.AbstractLogisService;

/**
 * 상품 수신 서비스
 * 
 * @author shortstop
 */
@Component
public class SkuReceiveService extends AbstractLogisService {

	
	/**
	 * 상품 수신 - 예정 수량 조회
	 *  
	 * @param event
	 */
	@EventListener(classes = SkuReceiptEvent.class, condition = "#event.receiveType == 'sku' and #event.eventStep == 1")
	public void readyToReceive(SkuReceiptEvent event) {
		
	}
	
	/**
	 * 상품 수신
	 *  
	 * @param event
	 */
	@EventListener(classes = SkuReceiptEvent.class, condition = "#event.receiveType == 'sku' and #event.eventStep == 2")
	public void startToReceive(SkuReceiptEvent event) {
		
	}
}
