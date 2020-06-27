package operato.logis.dps.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.DpsJobInstance;
import operato.fnf.wcs.entity.WmsExpressWaybillPackinfo;
import operato.fnf.wcs.entity.WmsExpressWaybillPrint;
import operato.fnf.wcs.event.DpsResetBox;
import operato.fnf.wcs.service.send.DpsBoxSendService;
import operato.logis.dps.model.DpsInspItem;
import operato.logis.dps.model.DpsInspection;
import operato.logis.dps.query.store.DpsInspectionQueryStore;
import operato.logis.dps.service.api.IDpsInspectionService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Printer;
import xyz.anythings.base.rest.PrinterController;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
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
	 * 도메인 컨트롤러
	 */
	@Autowired
	private DomainController domainCtrl;
	
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
		
		params.put("onlyOne", true);
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
		
		// 검수 항목 정보 조회 
		String sql = this.dpsInspectionQueryStore.getSearchInspectionItemsQuery();
		List<DpsInspItem> items = this.queryManager.selectListBySql(sql, params, DpsInspItem.class, 0, 0);
		
		// 상태가 '박싱 완료' 상태면 
		if(ValueUtil.isEqualIgnoreCase(LogisConstants.JOB_STATUS_BOXED, inspection.getStatus())) {
			// WMS에 주문 상태를 조회하여
			List<DpsInspItem> wmsItems = this.dpsBoxSendSvc.checkInpectionItemsToWms(inspection);
			
			// WMS에 처리할 주문이 남아 있다면  검수 항목에 반영한다. 
			if(ValueUtil.isNotEmpty(wmsItems)) {
				for(DpsInspItem wmsItem : wmsItems) {
					for(DpsInspItem wcsItem : items) {
						if(ValueUtil.isEqualIgnoreCase(wmsItem.getSkuCd(), wcsItem.getSkuCd())) {
							wmsItem.setRfidItemYn(wcsItem.getRfidItemYn());
							wmsItem.setSkuBarcd(wcsItem.getSkuBarcd());
							wmsItem.setSkuBarcd2(wcsItem.getSkuBarcd2());
							wmsItem.setSkuNm(wcsItem.getSkuNm());
							wmsItem.setOutbEctDate(wcsItem.getOutbEctDate());
							wmsItem.setShopCd(wcsItem.getShopCd());
						}
					}
				}
				
				inspection.setItems(wmsItems);
				
			// 검수할 항목이 없고 상태가 '취소'이면 주문에 '취소'로 상태 업데이트 
			} else {
				if(ValueUtil.isEqualIgnoreCase(LogisConstants.JOB_STATUS_CANCEL, inspection.getStatus())) {
					// 주문에 상태 '주문 취소'로 변경
					Map<String, Object> boxParams = ValueUtil.newMap("batchId,orderNo,status", inspection.getBatchId(), inspection.getOrderNo(), LogisConstants.JOB_STATUS_CANCEL);
					sql = "update mhe_dr set status = :status where work_unit = :batchId and ref_no = :orderNo and (waybill_no is null or waybill_no = '')";
					this.queryManager.executeBySql(sql, boxParams);
					
					// 주문에 상태 '주문 취소'로 변경
					sql = "update dps_job_instances set status = :status where work_unit = :batchId and ref_no = :orderNo and (waybill_no is null or waybill_no = '')";
					this.queryManager.executeBySql(sql, boxParams);
				}
			}
			
		} else {
			inspection.setItems(items);
		}
		
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
	public void finishInspection(JobBatch batch, String orderNo, Float boxWeight, String printerId, Object ... params) {
		
		// 1. 박스 조회
		DpsInspection inspection = this.findInspectionByOrder(batch, orderNo, false, true);
		BoxPack box = ValueUtil.populate(inspection, new BoxPack());
		box.setDomainId(batch.getDomainId());
		box.setBoxTypeCd(inspection.getTrayCd());
		box.setBoxId(inspection.getBoxId());

		// 2. 검수 완료 처리 
		this.finishInspection(batch, box, boxWeight, printerId, params);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void finishInspection(JobBatch batch, BoxPack box, Float boxWeight, String printerId, Object ... params) {
		
		// 1. 기타 파라미터가 없다면 수동 스캔 모드의 전체 검수 완료 처리
		if(params == null || params.length == 0) {
			throw ThrowUtil.newValidationErrorWithNoLog("검수 항목이 없어서 검수를 진행할 수 없습니다.");
			
		// 2. 파라미터가 하나이면 검수 항목 리스트라고 판단하여 검수 항목 정보로 검수 완료 처리  
		} else if(params.length >= 1) {
			List<Map<String, Object>> inspItems = (List<Map<String, Object>>)params[0];
			this.finishInspectionByItems(batch, box, boxWeight, printerId, inspItems);
		}
	}
	
	/**
	 * 검수 항목으로 검수 완료 처리
	 * 
	 * @param batch
	 * @param box
	 * @param boxWeight
	 * @param printerId
	 * @param itemObjs
	 */
	private BoxPack finishInspectionByItems(JobBatch batch, BoxPack box, Float boxWeight, String printerId, List<Map<String, Object>> itemObjs) {
		
		// 1. 검수 항목 정보로 부터 검수 항목 리스트 추출 
		List<DpsInspItem> inspectionItems = new ArrayList<DpsInspItem>();
		
		for(Map<String, Object> item : itemObjs) {
			DpsInspItem scanItem = new DpsInspItem();
			scanItem.setRfidItemYn(ValueUtil.toString(item.get("rfid_item_yn")));
			scanItem.setBrandCd(ValueUtil.toString(item.get("brand_cd")));
			scanItem.setSkuCd(ValueUtil.toString(item.get("sku_cd")));
			scanItem.setSkuBarcd(ValueUtil.toString(item.get("sku_barcd")));
			scanItem.setSkuBarcd2(ValueUtil.toString(item.get("sku_barcd2")));
			scanItem.setPickedQty(ValueUtil.toInteger(item.get("picked_qty")));
			scanItem.setConfirmQty(ValueUtil.toInteger(item.get("confirm_qty")));
			scanItem.setOutbEctDate(ValueUtil.toString(item.get("outb_ect_date")));
			scanItem.setShopCd(ValueUtil.toString(item.get("shop_cd")));
			scanItem.setOrderQty(ValueUtil.toInteger(item.get("order_qty")));
			scanItem.setPickedQty(ValueUtil.toInteger(item.get("picked_qty")));
			
			if(ValueUtil.isEqualIgnoreCase(LogisConstants.Y_CAP_STRING, scanItem.getRfidItemYn())) {
				scanItem.setRfidId(ValueUtil.toString(item.get("rfid_id")));
			}
			
			inspectionItems.add(scanItem);
		}
		
		// 2. 검수 항목으로 검수 완료 처리
		return this.doFinishInspectionByItems(batch, box, inspectionItems, printerId);
	}
	
	/**
	 * 검수 항목으로 검수 완료 처리
	 * 
	 * @param batch
	 * @param sourceBox
	 * @param inspectionItems
	 * @param printerId
	 * @return
	 */
	public BoxPack doFinishInspectionByItems(JobBatch batch, BoxPack sourceBox, List<DpsInspItem> inspectionItems, String printerId) {
		
		// 1. 주문에 대해서 이미 송장이 발행이 되었는지 체크  
		String invoiceId = this.findInvoiceNoByOrderNoToWms(batch.getDomainId(), sourceBox.getOrderNo(), sourceBox.getBoxId());
		
		// 2. 송장이 발행이 되지 않았다면 WMS에 실적 전송 및 송장 발행 요청 
		if(ValueUtil.isEmpty(invoiceId)) {
			invoiceId = this.sendBoxResultAndGetInvoice(batch, sourceBox.getOrderNo(), sourceBox.getBoxId(), inspectionItems);
		}
		
		// 3. 송장 번호가 성공이면 
		if(ValueUtil.isNotEqual(invoiceId, FnFConstants.ORDER_CANCEL_ALL)) {
			// 3.1 박스에 송장 번호 설정
			sourceBox.setInvoiceId(invoiceId);
			
			// 3.2. 주문, 작업 정보에 검수 완료로 업데이트
			this.confirmDpsJobInstances(batch.getDomainId(), batch.getId(), sourceBox.getOrderNo(), sourceBox.getBoxId(), invoiceId, User.currentUser().getId(), inspectionItems);
			
			// 3.3 남은 검수 항목이 있으면 동일 주문의 처리 안 된 주문에 대해서 박스 ID 리셋
			this.resetBoxIdRemainJobs(batch.getDomainId(), batch.getId(), sourceBox.getOrderNo());
						
			// 3.4 송장 발행 
			BeanUtil.get(DpsInspectionService.class).printInvoiceLabel(batch, sourceBox, printerId);
			
		// 4. 송장 번호가 주문 전체 취소이면 리턴에 취소 설정
		} else {
			sourceBox.setStatus(LogisConstants.JOB_STATUS_CANCEL);
		}
		
		// 5. 리턴
		return sourceBox;
	}
	
	@Override
	public BoxPack splitBox(JobBatch batch, BoxPack sourceBox, List<DpsInspItem> inspectionItems, String printerId, Object ... params) {
		
		return this.doFinishInspectionByItems(batch, sourceBox, inspectionItems, printerId);
	}
	
	@Override
	public int printInvoiceLabel(JobBatch batch, BoxPack box, String printerId) {
		
		PrintEvent printEvent = this.createPrintEvent(batch.getDomainId(), box.getBoxId(), box.getInvoiceId(), printerId);
		this.eventPublisher.publishEvent(printEvent);
		return 1;
	}
	
	@Override
	public int printInvoiceLabel(JobBatch batch, DpsInspection inspection, String printerId) {
		
		PrintEvent printEvent = this.createPrintEvent(batch.getDomainId(), inspection.getBoxId(), inspection.getInvoiceId(), printerId);
		this.eventPublisher.publishEvent(printEvent);
		return 1;
	}

	@Override
	public int printTradeStatement(JobBatch batch, BoxPack box, String printerId) {
		// FnF에서는 거래명세서 없음 
		return 0;
	}

	@Override
	public void inspectionAction(Long domainId, String boxPackId) {
		// 구현 없음
		
	}

	@Override
	public void inspectionAction(BoxPack box) {
		// 구현 없음
	}
	
	/**
	 * 라벨 인쇄 이벤트 생성 
	 * 
	 * @param domainId
	 * @param boxId
	 * @param invoiceId
	 * @param printerId
	 * @return
	 */
	public PrintEvent createPrintEvent(Long domainId, String boxId, String invoiceId, String printerId) {
		
		String labelTemplate = SettingUtil.getValue(domainId, "fnf.dps.invoice.template");
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsExpressWaybillPrint.class);
		Map<String, Object> waybillParams = ValueUtil.newMap("whCd,waybillNo,boxId", FnFConstants.WH_CD_ICF, invoiceId, boxId);
		WmsExpressWaybillPrint waybillPrint = wmsQueryMgr.selectByCondition(WmsExpressWaybillPrint.class, waybillParams);
		Map<String, Object> packinfoParams = ValueUtil.newMap("whCd,boxId", FnFConstants.WH_CD_ICF, boxId);
		List<WmsExpressWaybillPackinfo> packItems = wmsQueryMgr.selectList(WmsExpressWaybillPackinfo.class, packinfoParams);
		Map<String, Object> printParams = ValueUtil.newMap("box,items", waybillPrint, packItems);
		return new PrintEvent(domainId, printerId, labelTemplate, printParams);
	}
	
	/**
	 * 송장 라벨 인쇄 API
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class)
	public void printLabel(PrintEvent printEvent) {
		
		// 현재 도메인 조회
		Domain domain = this.domainCtrl.findOne(printEvent.getDomainId(), null);
		// 현재 도메인 설정
		DomainContext.setCurrentDomain(domain);
		
		try {
			// 인쇄 옵션 정보 추출
			Printer printer = this.queryManager.select(Printer.class, printEvent.getPrinterId());
			String agentUrl = printer.getPrinterAgentUrl();
			String printerName = printer.getPrinterDriver();
			
			// 인쇄 요청
			this.printerCtrl.printLabelByLabelTemplate(agentUrl, printerName, printEvent.getPrintTemplate(), printEvent.getTemplateParams());
			
		} catch (Exception e) {
			// 예외 처리
			ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "PRINT_LABEL_ERROR", e, null, true, true);
			this.eventPublisher.publishEvent(errorEvent);
			
		} finally {
			// 스레드 로컬 변수에서 currentDomain 리셋 
			DomainContext.unsetAll();
		}
	}
	
	/**
	 * WMS에서 주문에 대해서 이미 송장이 발행이 되었는지 체크
	 * 
	 * @param domainId
	 * @param orderNo
	 * @param boxId
	 * @return
	 */
	private String findInvoiceNoByOrderNoToWms(Long domainId, String orderNo, String boxId) {
  
		Map<String, Object> condition = ValueUtil.newMap("whCd,orderNo,boxId", FnFConstants.WH_CD_ICF, orderNo, boxId);
		String sql = "select waybill_no from mps_express_waybill_print where wh_cd = :whCd and online_order_no = :orderNo and box_id = :boxId";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsExpressWaybillPrint.class);
		return wmsQueryMgr.selectBySql(sql, condition, String.class);
	}
	
	/**
	 * 송장 할당이 안 된 주문, 작업에 박스 ID 리셋
	 * 
	 * @param domainId
	 * @param batchId
	 * @param orderNo
	 */
	private void resetBoxIdRemainJobs(Long domainId, String batchId, String orderNo) {

		// 1. 남은 주문 정보의 BoxId를 null로 업데이트
		String sql = "update mhe_dr set box_id = null where work_unit = :batchId and ref_no = :orderNo and (waybill_no is null or waybill_no = '')";
		Map<String, Object> condition = ValueUtil.newMap("batchId,orderNo", batchId, orderNo);
		this.queryManager.executeBySql(sql, condition);
		
		// 2. 남은 작업 정보의 BoxId를 null로 업데이트
		sql = "update dps_job_instances set box_id = null where work_unit = :batchId and ref_no = :orderNo and (waybill_no is null or waybill_no = '')";
		this.queryManager.executeBySql(sql, condition);
	}
	
	/**
	 * 주문, 작업 정보에 송장 정보 및 검수 완료 정보 업데이트
	 * 
	 * @param domainId
	 * @param batchId
	 * @param orderNo
	 * @param invoiceId
	 * @param inspectorId
	 * @param inspectionItems
	 */
	private void confirmDpsJobInstances(Long domainId, String batchId, String orderNo, String boxId, String invoiceId, String inspectorId, List<DpsInspItem> inspectionItems) {
		
		// 1. 검수 항목 기준으로 상품 코드 : 검수 수량으로 추출한다.
		Map<String, Integer> skuInspectionQtyPair = new HashMap<String, Integer>();
		// 2. 상품 코드 리스트 추출
		List<String> skuCdList = new ArrayList<String>();
		
		for(DpsInspItem item : inspectionItems) {
			skuCdList.add(item.getSkuCd());
			int prevSkuConfirmQty = skuInspectionQtyPair.containsKey(item.getSkuCd()) ? skuInspectionQtyPair.get(item.getSkuCd()) : 0;
			skuInspectionQtyPair.put(item.getSkuCd(), item.getConfirmQty() + prevSkuConfirmQty);
		}
		
		// 3. 검수 안 된 작업 정보 조회 
		String sql = "select * from dps_job_instances where work_unit = :workUnit and ref_no = :refNo and box_id = :boxId and (waybill_no is null or waybill_no = '') and item_cd in (:itemCdList)";
		Map<String, Object> condition = ValueUtil.newMap("workUnit,refNo,boxId,itemCdList", batchId, orderNo, boxId, skuCdList);
		List<DpsJobInstance> jobList = this.queryManager.selectListBySql(sql, condition, DpsJobInstance.class, 0, 0);
		
		// 4. 검수 항목을 상품별 수량 기준으로 작업 정보를 조회, 작업 정보가 한 레코드로 수량이 맞지 않다면 Split 처리
		java.util.Iterator<String> keyIter = skuInspectionQtyPair.keySet().iterator();
		Date currentTime = new Date();
		List<DpsJobInstance> toInspectJobList = new ArrayList<DpsJobInstance>();
		List<String> orderIdList = new ArrayList<String>(); 
		
		while(keyIter.hasNext()) {
			String skuCd = keyIter.next();
			int totalConfirmQty = skuInspectionQtyPair.get(skuCd);
			
			// 검수 처리할 남은 수량 정보 
			int remainQty = totalConfirmQty;
			
			// 각 상품별로 순회하면서 검수 수량 만큼 검수 확정 처리한다.
			for(DpsJobInstance job : jobList) {
				if(remainQty <= 0) {
					break;
				}
				
				if(ValueUtil.isEqualIgnoreCase(job.getItemCd(), skuCd)) {
					int jobCmptQty = job.getCmptQty();
					// 검수 처리할 작업 : 검수 처리할 수량이 작업 확정 수량 보다 크거나 같다면 전부 검수 확정 처리 or 작업 확정 수량이 검수 처리할 수량 보다 크다면 분할 처리 후 검수 확정 처리
					DpsJobInstance toInspectJob = (remainQty >= jobCmptQty) ? job : this.splitJob(job, remainQty);
					toInspectJob.setWaybillNo(invoiceId);
					toInspectJob.setStatus(BoxPack.BOX_STATUS_EXAMED);
					toInspectJob.setInspectedAt(currentTime);
					toInspectJob.setInspectorId(inspectorId);
					toInspectJob.setBoxResultIfAt(currentTime);
					toInspectJobList.add(toInspectJob);
					orderIdList.add(toInspectJob.getMheDrId());
					
					// 남은 검수 확정 수량 계산
					remainQty = remainQty - jobCmptQty;
				}
			}
		}
		
		// 5. 마지막으로 작업, 주문에 대한 상태 업데이트
		if(ValueUtil.isNotEmpty(toInspectJobList)) {
			this.queryManager.updateBatch(toInspectJobList, "waybillNo", "status", "inspectedAt", "inspectorId", "boxResultIfAt");
			
			sql = "update mhe_dr set waybill_no = :invoiceId, status = :status, box_result_if_at = :boxResultIfAt where id in (:orderIdList)";
			Map<String, Object> params = ValueUtil.newMap("orderIdList,invoiceId,status,boxResultIfAt", orderIdList, invoiceId, BoxPack.BOX_STATUS_EXAMED, currentTime);
			this.queryManager.executeBySql(sql, params);
		}
	}
	
	/**
	 * 작업 분할 
	 * 
	 * @param originalJob 분할될 작업 정보
	 * @param inspectedQty 검수 처리할 수량 
	 * @return
	 */
	private DpsJobInstance splitJob(DpsJobInstance originalJob, int inspectedQty) {
		
		// 1. 작업 정보의 확정 수량이 검수 처리할 수량 보다 큰지 판단
		int splitQty = originalJob.getCmptQty() - inspectedQty;
		
		// 2. 작업 정보의 주문 수량이 검수 확인 수량보다 크다면 작업 분할 처리하고 그렇지 않다면 작업을 분할하지 않고 ...
		if(splitQty > 0) {			
			// 2.1 분할 작업 생성
			DpsJobInstance splittedJob = ValueUtil.populate(originalJob, new DpsJobInstance());
			splittedJob.setId(UUID.randomUUID().toString());
			splittedJob.setPickQty(splitQty);
			splittedJob.setCmptQty(splitQty);
			splittedJob.setBoxId(null);
			this.queryManager.insert(splittedJob);
			
			// 2.2 원래 작업 수량 업데이트 
			originalJob.setPickQty(inspectedQty);
			originalJob.setCmptQty(inspectedQty);
			this.queryManager.update(originalJob, "pickQty", "cmptQty");
		}
		
		// 3. 원래 작업 리턴
		return originalJob;
	}
	
	/**
	 * 송장 분할을 위한 WMS에 박스 실적 전송 및 송장 번호 발행
	 * 
	 * @param batch
	 * @param orderNo
	 * @param boxId
	 * @param inspectionItems
	 * @return
	 */
	public String sendBoxResultAndGetInvoice(JobBatch batch, String orderNo, String boxId, List<DpsInspItem> inspectionItems) {
		
		// WMS에서 발행한 송장 번호
		String invoiceId = null;
		
		try {
			// WMS로 박스 실적 전송 && 송장 발행 
			invoiceId = this.dpsBoxSendSvc.sendPackingToWms(batch, orderNo, boxId, inspectionItems);
			
		} catch(RuntimeException re) {
			// 오류 발생시 박스 리셋 이벤트를 던져 해당 주문의 boxId를 리셋한다. 
			this.eventPublisher.publishEvent(new DpsResetBox(batch.getDomainId(), batch.getId(), orderNo, boxId));
			throw re;
			
		} catch(Throwable th) {
			// 오류 발생시 박스 리셋 이벤트를 던져 해당 주문의 boxId를 리셋한다. 
			this.eventPublisher.publishEvent(new DpsResetBox(batch.getDomainId(), batch.getId(), orderNo, boxId));
			// throw
			String msg = th.getCause() == null ? th.getMessage() : th.getCause().getMessage();
			ElidomRuntimeException ere = new ElidomRuntimeException(msg, th);
			throw ere;
		}
	
		// 발행 송장 번호 리턴
		return invoiceId;
	}
	
	/**
	 * 주문에 대한 사은품 리스트 조회
	 * 
	 * @param batch
	 * @param boxId
	 * @return
	 */
	public List<DpsInspItem> searchGiftItems(JobBatch batch, String boxId) {
		
		String sql = "select item_cd as sku_cd, item_nm as sku_nm, qty as order_qty from mps_express_waybill_packinfo where wh_cd = :whCd and box_id = :boxId and item_season = 'X'";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsExpressWaybillPackinfo.class);
		Map<String, Object> params = ValueUtil.newMap("whCd,boxId", FnFConstants.WH_CD_ICF, boxId);
		return wmsQueryMgr.selectListBySql(sql, params, DpsInspItem.class, 0, 0);
	}
	
	/**
	 * 주문 정보의 박스 ID를 다시 리셋
	 * 
	 * @param event
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, classes = DpsResetBox.class)
	public void resetOrdersBoxId(DpsResetBox event) {
		
		if(ValueUtil.isNotEmpty(event.getOrderNo()) && ValueUtil.isNotEmpty(event.getBoxId())) {
			String sql = "update mhe_dr set box_id = null where ref_no = :orderNo and box_id = :boxId";
			Map<String, Object> params = ValueUtil.newMap("orderNo,boxId", event.getOrderNo(), event.getBoxId());
			this.queryManager.executeBySql(sql, params);
		
			sql = "update dps_job_instances set box_id = null where ref_no = :orderNo and box_id = :boxId";
			this.queryManager.executeBySql(sql, params);
		}
	}

}
