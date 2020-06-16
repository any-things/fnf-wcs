package operato.logis.dps.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.DpsJobInstance;
import operato.fnf.wcs.entity.WmsExpressWaybillPackinfo;
import operato.fnf.wcs.entity.WmsExpressWaybillPrint;
import operato.fnf.wcs.service.send.DpsBoxSendService;
import operato.logis.dps.model.DpsInspItem;
import operato.logis.dps.model.DpsInspection;
import operato.logis.dps.query.store.DpsInspectionQueryStore;
import operato.logis.dps.service.api.IDpsInspectionService;
import xyz.anythings.base.LogisConstants;
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
	 * @param domainId
	 * @param batchId
	 * @param equipGroupCd
	 * @param sql
	 * @param params
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private DpsInspection findInspection(Long domainId, String batchId, String equipGroupCd, String sql, Map<String, Object> params, boolean exceptionWhenEmpty) {
		String status = ValueUtil.toString(params.get("status"));
		
		if(ValueUtil.isNotEmpty(status) && ValueUtil.isEqualIgnoreCase(status, BoxPack.BOX_STATUS_EXAMED)) {
			params.put("onlyOne", true);
		}
		
		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		
		if(inspection == null) {
			if(exceptionWhenEmpty) {
				Object data = (params == null) ? null : (params.containsKey("boxId") ? params.get("boxId") : (params.containsKey("orderNo") ? params.get("orderNo") : (params.containsKey("invoiceId") ? params.get("invoiceId") : null)));
				throw ThrowUtil.newNotFoundRecord("terms.label.box", ValueUtil.toString(data));
				
			} else {
				return null;
			}
			
		} else {
			if(ValueUtil.isEmpty(inspection.getBoxId())) {
				String orderNo = inspection.getOrderNo();
				String boxId = this.dpsBoxSendSvc.generateBoxIdByOrderNo(domainId, batchId, equipGroupCd, orderNo);
				inspection.setBoxId(boxId);
			}
			
			return inspection;
		}
	}
	
	/**
	 * 검수 정보 조회
	 * 
	 * @param batch
	 * @param sql
	 * @param params
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private DpsInspection findInspection(JobBatch batch, String sql, Map<String, Object> params, boolean exceptionWhenEmpty) {
		
		return this.findInspection(batch.getDomainId(), batch.getId(), batch.getEquipGroupCd(), sql, params, exceptionWhenEmpty);
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
	public DpsInspection findInspectionByInput(JobBatch batch, String inputType, String inputId, boolean reprintMode, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		
		if(ValueUtil.isEqualIgnoreCase(inputType, "box") || ValueUtil.isEqualIgnoreCase(inputType, "tray")) {
			params.put("boxId", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "orderNo")) {
			params.put("orderNo", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "invoiceId")) {
			params.put("invoiceId", inputId);
		}

		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		
		DpsInspection inspection = this.findInspection(batch, sql, params, exceptionWhenEmpty);
		if(inspection != null && (ValueUtil.isEqualIgnoreCase(inputType, "box") || ValueUtil.isEqualIgnoreCase(inputType, "tray"))) {
			inspection.setBoxType(inputType);
		}
		
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByTray(JobBatch batch, String trayCd, boolean reprintMode, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), trayCd);
		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		DpsInspection inspection = this.findInspection(batch, sql, params, exceptionWhenEmpty);

		if(inspection != null) {
			inspection.setBoxType("tray");
		}
		
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByBox(JobBatch batch, String boxId, boolean reprintMode, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), boxId);
		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		DpsInspection inspection = this.findInspection(batch, sql, params, exceptionWhenEmpty);

		if(inspection != null) {
			inspection.setBoxType("box");
		}
		
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByInvoice(JobBatch batch, String invoiceId, boolean reprintMode, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId", batch.getDomainId(), batch.getId(), invoiceId);
		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		DpsInspection inspection = this.findInspection(batch, sql, params, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}
	
	@Override
	public DpsInspection findInspectionByOrder(JobBatch batch, String orderNo, boolean reprintMode, boolean exceptionWhenEmpty) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,orderNo", batch.getDomainId(), batch.getId(), orderNo);
		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		DpsInspection inspection = this.findInspection(batch, sql, params, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByBoxPack(BoxPack box, boolean reprintMode) {
		
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", box.getDomainId(), box.getBatchId());
		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		
		if(ValueUtil.isNotEmpty(box.getInvoiceId())) {
			params.put("invoiceId", box.getInvoiceId());
			
		} else if(ValueUtil.isNotEmpty(box.getOrderNo())) {
			params.put("orderNo", box.getOrderNo());
			
		} else if(ValueUtil.isNotEmpty(box.getBoxId())) {
			params.put("boxId", box.getBoxId());
			
		} else if(ValueUtil.isNotEmpty(box.getBoxTypeCd())) {
			params.put("boxId", box.getBoxTypeCd());
		}
		
		DpsInspection inspection = this.findInspection(box.getDomainId(), box.getBatchId(), box.getEquipGroupCd(), sql, params, true);
		return this.searchInpsectionItems(inspection, params);
	}
	
	@Override
	public List<DpsInspection> searchInspectionList(JobBatch batch, String orderNo, boolean reprintMode, boolean exceptionWhenEmpty) {
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,orderNo", batch.getDomainId(), batch.getId(), orderNo);
		params.put("status", reprintMode ? BoxPack.BOX_STATUS_EXAMED : BoxPack.BOX_STATUS_BOXED);
		return this.queryManager.selectListBySql(sql, params, DpsInspection.class, 0, 0);
	}

	@Override
	public void finishInspection(JobBatch batch, String orderNo, Float boxWeight, String printerId) {
		
		// 1. 박스 조회
		DpsInspection inspection = this.findInspectionByOrder(batch, orderNo, false, true);
		BoxPack box = ValueUtil.populate(inspection, new BoxPack());
		box.setDomainId(batch.getDomainId());
		box.setBoxTypeCd(inspection.getTrayCd());
		box.setBoxId(inspection.getBoxId());

		// 2. 검수 완료 처리 
		this.finishInspection(batch, box, boxWeight, printerId);
	}

	@Override
	public void finishInspection(JobBatch batch, BoxPack box, Float boxWeight, String printerId) {
		// 1. WMS로 박스 실적 전송
		this.dpsBoxSendSvc.sendPackingToWms(batch, box.getOrderNo(), box.getBoxId());
		
		// 2. 송장 발행 요청
		String invoiceId = this.dpsBoxSendSvc.requestInvoiceToWms(batch, box.getOrderNo(), box.getBoxId());
		box.setInvoiceId(invoiceId);
		
		// 3. Tray 박스 상태 리셋
		this.resetTrayBox(box.getBoxTypeCd());
		
		// 4. 송장 발행 - 별도 트랜잭션
		if(ValueUtil.isEqualIgnoreCase(invoiceId, FnFConstants.ORDER_CANCEL_ALL)) {
			box.setStatus(LogisConstants.JOB_STATUS_CANCEL);
		} else {
			BeanUtil.get(DpsInspectionService.class).printInvoiceLabel(batch, box, printerId);
		}
	}

	@Override
	public BoxPack splitBox(JobBatch batch, BoxPack sourceBox, List<DpsInspItem> inspectionItems, String printerId) {
		
		// 1. 박스 정보로 검수 정보 조회 
		String sql = "select * from dps_job_instances where work_unit = :workUnit and ref_no = :refNo and box_id = :boxId and (waybill_no is null or waybill_no = '') and item_cd = :itemCd";
		Map<String, Object> condition = ValueUtil.newMap("workUnit,refNo,boxId", sourceBox.getBatchId(), sourceBox.getOrderNo(), sourceBox.getBoxId());
		List<DpsJobInstance> jobList = new ArrayList<DpsJobInstance>();
		
		// 2. inspectionItems 기준으로 DpsJobInstance 조회
		for(DpsInspItem inspItem : inspectionItems) {
			condition.put("itemCd", inspItem.getSkuCd());
			
			// 검수 항목 기준으로 DpsJobInstance 조회 - 검수 항목이 작업 수량보다 적으면 작업 분할 
			DpsJobInstance originalJob = this.queryManager.selectBySql(sql, condition, DpsJobInstance.class);
			originalJob = this.splitJob(originalJob, inspItem.getConfirmQty());
			jobList.add(originalJob);
		}
		
		// 3. 작업 대상이 없다면 리턴 
		if(ValueUtil.isEmpty(jobList)) {
			return null;
		}
						
		// 4. WMS로 조회한 DpsJobInstance 기준으로 박스 실적 전송
		this.dpsBoxSendSvc.sendPackingToWmsBySplit(batch, sourceBox.getOrderNo(), sourceBox.getBoxId(), jobList);
		
		// 5. WMS에 송장 발행 요청
		String invoiceId = this.dpsBoxSendSvc.requestInvoiceToWmsBySplit(batch, sourceBox.getOrderNo(), sourceBox.getBoxId(), jobList);
		sourceBox.setInvoiceId(invoiceId);
		
		// 6. 송장 번호가 성공이면 
		if(ValueUtil.isNotEqual(invoiceId, FnFConstants.ORDER_CANCEL_ALL)) {
			// 6.1 해당 주문으로 남은 검수 항목이 있는지 체크
			condition.remove("itemCd");
			
			// 6.2 없다면 Tray 박스 상태 리셋 
			if(this.queryManager.selectSize(DpsJobInstance.class, condition) == 0) {
				this.resetTrayBox(sourceBox.getBoxTypeCd());
				
			// 6.3 있으면 분할 이외 주문에 대해서 박스 ID 리셋
			} else {
				condition.remove("boxId");
				
				// 6.3.1 남은 주문 정보의 BoxId를 null로 업데이트
				sql = "update mhe_dr set box_id = null where work_unit = :workUnit and ref_no = :refNo and (waybill_no is null or waybill_no = '')";
				this.queryManager.executeBySql(sql, condition);
				
				// 6.3.2 남은 작업 정보의 BoxId를 null로 업데이트
				sql = "update dps_job_instances set box_id = null where work_unit = :workUnit and ref_no = :refNo and (waybill_no is null or waybill_no = '')";
				this.queryManager.executeBySql(sql, condition);
			}
			
			// 6.4 송장 발행 
			BeanUtil.get(DpsInspectionService.class).printInvoiceLabel(batch, sourceBox, printerId);
			
		// 7. 송장 번호가 주문 전체 취소이면 리턴에 취소 설정
		} else {
			sourceBox.setStatus(LogisConstants.JOB_STATUS_CANCEL);
		}
		
		// 8. 리턴
		return sourceBox;
	}
	
	/**
	 * 작업 분할 
	 * 
	 * @param originalJob
	 * @param splitQty
	 * @return
	 */
	private DpsJobInstance splitJob(DpsJobInstance originalJob, int splitQty) {
		
		if(originalJob != null) { 
			// 작업 정보의 주문 수량이 검수 확인 수량보다 크다면 작업 분할 처리 
			if(originalJob.getPickQty() > splitQty) {
				int inspectedQty = originalJob.getPickQty() - splitQty;
				
				// 1. SplitJob 생성
				DpsJobInstance splittedJob = ValueUtil.populate(originalJob, new DpsJobInstance());
				splittedJob.setId(null);
				splittedJob.setPickQty(originalJob.getPickQty() - inspectedQty);
				splittedJob.setCmptQty(splittedJob.getPickQty());
				splittedJob.setBoxId(null);
				this.queryManager.insert(splittedJob);
				
				// 2. 원래 작업 수량 업데이트 
				originalJob.setPickQty(inspectedQty);
				originalJob.setCmptQty(inspectedQty);
				this.queryManager.update(originalJob, "pickQty", "cmptQty");
			}
		}
		
		return originalJob;
	}
	
	/**
	 * tray 박스 상태 리셋
	 * 
	 * @param trayCd
	 */
	private void resetTrayBox(String trayCd) {
		TrayBox condition = new TrayBox();
		condition.setTrayCd(trayCd);
		TrayBox tray = this.queryManager.selectByCondition(TrayBox.class, condition);
		tray.setStatus(BoxPack.BOX_STATUS_WAIT);
		this.queryManager.update(tray, "status", "updaterId", "updatedAt");		
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
	
	public PrintEvent createPrintEvent(Long domainId, String boxId, String invoiceId, String printerId) {
		
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
	public void printLabel(PrintEvent printEvent) {
		// 인쇄 옵션 정보 추출
		Printer printer = this.queryManager.select(Printer.class, printEvent.getPrinterId());
		String agentUrl = printer.getPrinterAgentUrl();
		String printerName = printer.getPrinterDriver();
		
		// 인쇄 요청
		this.printerCtrl.printLabelByLabelTemplate(agentUrl, printerName, printEvent.getPrintTemplate(), printEvent.getTemplateParams());
		//this.eventPublisher.publishEvent(printEvent);
	}

}
