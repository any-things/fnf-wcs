package operato.logis.dps.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.entity.WmsExpressWaybillPackinfo;
import operato.fnf.wcs.entity.WmsExpressWaybillPrint;
import operato.fnf.wcs.service.send.DpsBoxSendService;
import operato.logis.dps.model.DpsInspItem;
import operato.logis.dps.model.DpsInspection;
import operato.logis.dps.query.store.DpsInspectionQueryStore;
import operato.logis.dps.service.api.IDpsInspectionService;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Printer;
import xyz.anythings.base.entity.TrayBox;
import xyz.anythings.base.rest.PrinterController;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * DPS 출고 검수 서비스
 * 
 * @author shortstop
 */
@Component
public class DpsInspectionService extends AbstractInstructionService implements IDpsInspectionService {

	/**
	 * DPS 출고 검수 처리용 쿼리 스토어
	 */	
	@Autowired
	private DpsInspectionQueryStore dpsInspectionQueryStore;
	/**
	 * 박스 실적 전송 서비스
	 */
	@Autowired
	private DpsBoxSendService dpsBoxSendSvc;
	/**
	 * 프린터 컨트롤러
	 */
	@Autowired
	private PrinterController printerCtrl;
	
	/**
	 * 검수 정보 조회
	 * 
	 * @param sql
	 * @param params
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private DpsInspection findInspection(String sql, Map<String, Object> params, boolean exceptionWhenEmpty) {
		
		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		
		if(inspection == null && exceptionWhenEmpty) {
			Object data = (params == null) ? null : (params.containsKey("boxId") ? params.get("boxId") : (params.containsKey("orderNo") ? params.get("orderNo") : (params.containsKey("invoiceId") ? params.get("invoiceId") : null)));
			throw ThrowUtil.newNotFoundRecord("terms.label.box", ValueUtil.toString(data));
		} else {
			return inspection;
		}
	}
	
	/**
	 * 검수 항목 조회 처리 ...
	 * 
	 * @param inspection
	 * @param params
	 * @return
	 */
	private DpsInspection searchInpsectionItems(DpsInspection inspection, Map<String, Object> params) {
		
		if(inspection == null) {
			return null;
		}
		
		if(!params.containsKey("invoiceId")) {
			params.put("invoiceId", inspection.getInvoiceId());
		}
		
		String sql = this.dpsInspectionQueryStore.getSearchInspectionItemsQuery();
		List<DpsInspItem> items = this.queryManager.selectListBySql(sql, params, DpsInspItem.class, 0, 0);
		inspection.setItems(items);
		return inspection;
	}
	
	@Override
	public DpsInspection findInspectionByInput(JobBatch batch, String inputType, String inputId, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		
		if(ValueUtil.isEqualIgnoreCase(inputType, "box") || ValueUtil.isEqualIgnoreCase(inputType, "tray")) {
			params.put("boxId", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "orderNo")) {
			params.put("orderNo", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "invoiceId")) {
			params.put("invoiceId", inputId);
		}

		DpsInspection inspection = this.findInspection(sql, params, exceptionWhenEmpty);
		if(inspection != null && (ValueUtil.isEqualIgnoreCase(inputType, "box") || ValueUtil.isEqualIgnoreCase(inputType, "tray"))) {
			inspection.setBoxType(inputType);
		}
		
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByTray(JobBatch batch, String trayCd, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), trayCd);
		DpsInspection inspection = this.findInspection(sql, params, exceptionWhenEmpty);

		if(inspection != null) {
			inspection.setBoxType("tray");
		}
		
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByBox(JobBatch batch, String boxId, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), boxId);
		DpsInspection inspection = this.findInspection(sql, params, exceptionWhenEmpty);

		if(inspection != null) {
			inspection.setBoxType("box");
		}
		
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByInvoice(JobBatch batch, String invoiceId, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId", batch.getDomainId(), batch.getId(), invoiceId);
		DpsInspection inspection = this.findInspection(sql, params, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}
	
	@Override
	public DpsInspection findInspectionByOrder(JobBatch batch, String orderNo, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,orderNo", batch.getDomainId(), batch.getId(), orderNo);
		DpsInspection inspection = this.findInspection(sql, params, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByBoxPack(BoxPack box) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId", box.getDomainId(), box.getBatchId(), box.getInvoiceId());
		DpsInspection inspection = this.findInspection(sql, params, true);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public void finishInspection(JobBatch batch, String orderNo, Float boxWeight, String printerId) {
		
		// 1. 박스 조회
		DpsInspection inspection = this.findInspectionByOrder(batch, orderNo, true);
		BoxPack box = ValueUtil.populate(inspection, new BoxPack());
		box.setDomainId(batch.getDomainId());
		box.setBoxTypeCd(inspection.getTrayCd());

		// 2. 검수 완료 처리 
		this.finishInspection(batch, box, boxWeight, printerId);
	}

	@Override
	public void finishInspection(JobBatch batch, BoxPack box, Float boxWeight, String printerId) {
		// 1. WMS로 박스 실적 전송
		String boxId = this.dpsBoxSendSvc.sendPackingToWms(batch, box.getOrderNo());
		
		// 2. 송장 발행 요청
		String invoiceId = this.dpsBoxSendSvc.requestInvoiceToWms(batch, boxId);
		
		// 3. 박스 내품 검수 항목 완료 처리
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId,status", box.getDomainId(), box.getBatchId(), invoiceId, BoxPack.BOX_STATUS_EXAMED);
		String sql = "update mhe_dr set status = :status where wh_cd = 'ICF' and work_unit = :batchId and waybill_no = :invoiceId";
		this.queryManager.executeBySql(sql, params);
		
		// 4. Tray 박스 상태 리셋
		String trayCd = box.getBoxTypeCd();
		TrayBox condition = new TrayBox();
		condition.setTrayCd(trayCd);
		TrayBox tray = this.queryManager.selectByCondition(TrayBox.class, condition);
		tray.setStatus(BoxPack.BOX_STATUS_WAIT);
		this.queryManager.update(tray, "status", "updaterId", "updatedAt");
		
		// 5. 송장 발행 - 별도 트랜잭션
		BeanUtil.get(DpsInspectionService.class).printInvoiceLabel(batch, box, printerId);
	}

	@Override
	public BoxPack splitBox(BoxPack sourceBox, List<DpsInspItem> inspectionItems, String printerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int printInvoiceLabel(JobBatch batch, BoxPack box, String printerId) {
		PrintEvent printEvent = this.createPrintEvent(batch.getDomainId(), box.getBoxId(), box.getInvoiceId(), printerId);
		this.printLabel(printEvent);
		return 1;
	}
	
	@Override
	public int printInvoiceLabel(JobBatch batch, DpsInspection inspection, String printerId) {
		PrintEvent printEvent = this.createPrintEvent(batch.getDomainId(), inspection.getBoxId(), inspection.getInvoiceId(), printerId);
		this.printLabel(printEvent);
		return 1;
	}

	@Override
	public int printTradeStatement(JobBatch batch, BoxPack box, String printerId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void inspectionAction(Long domainId, String boxPackId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inspectionAction(BoxPack box) {
		// TODO Auto-generated method stub
		
	}
	
	private PrintEvent createPrintEvent(Long domainId, String boxId, String invoiceId, String printerId) {
		// TODO 테스트 시 하드코딩 제거
		//invoiceId = "20200500080817";
		
		String labelTemplate = SettingUtil.getValue(domainId, "fnf.dps.invoice.template");
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsExpressWaybillPrint.class);
		Map<String, Object> waybillParams = ValueUtil.newMap("whCd,waybillNo,boxId", "ICF", invoiceId, boxId);
		WmsExpressWaybillPrint waybillPrint = wmsQueryMgr.selectByCondition(WmsExpressWaybillPrint.class, waybillParams);
		Map<String, Object> packinfoParams = ValueUtil.newMap("whCd,boxId", "ICF", boxId);
		List<WmsExpressWaybillPackinfo> packItems = wmsQueryMgr.selectList(WmsExpressWaybillPackinfo.class, packinfoParams);
		Map<String, Object> printParams = ValueUtil.newMap("box,items", waybillPrint, packItems);
		return new PrintEvent(domainId, printerId, labelTemplate, printParams);
	}

	/**
	 * 송장 라벨 인쇄 API
	 * 
	 * @param templateName
	 * @param printerId
	 * @param parameters
	 */
	private void printLabel(PrintEvent printEvent) {
		// 인쇄 옵션 정보 추출
		Printer printer = this.queryManager.select(Printer.class, printEvent.getPrinterId());
		String agentUrl = printer.getPrinterAgentUrl();
		String printerName = printer.getPrinterDriver();
		
		// 인쇄 요청
		this.printerCtrl.printLabelByLabelTemplate(agentUrl, printerName, printEvent.getPrintTemplate(), printEvent.getTemplateParams());
		
		//this.eventPublisher.publishEvent(printEvent);
	}

}
