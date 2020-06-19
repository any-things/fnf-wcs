package operato.logis.dps.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.entity.DpsJobInstance;
import operato.fnf.wcs.entity.RfidResult;
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
import xyz.anythings.base.rest.PrinterController;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
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
			this.finishInspectionByAll(batch, box, boxWeight, printerId, null, null);
			
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
	private void finishInspectionByItems(JobBatch batch, BoxPack box, Float boxWeight, String printerId, List<Map<String, Object>> itemObjs) {
		// 1. 검수 항목으로 부터 총 검수 수량을 계산
		int totalConfirmPcs = 0;
		
		for(Map<String, Object> item : itemObjs) {
			if(item.containsKey("confirm_qty")) {
				totalConfirmPcs += ValueUtil.toInteger(item.get("confirm_qty"));
			}
		}
		
		// 2. 주문에 처리되지 않은 주문이 남아있는지 (즉 이 처리가 송장 분할인지) 여부 체크
		String sql = "select sum(pick_qty) as total_order_qty from dps_job_instances where work_unit = :workUnit and ref_no = :refNo and box_id = :boxId and (waybill_no is null or waybill_no = '')";
		Map<String, Object> condition = ValueUtil.newMap("workUnit,refNo,boxId", box.getBatchId(), box.getOrderNo(), box.getBoxId());
		int remainOrderPcs = this.queryManager.selectBySql(sql, condition, Integer.class);
		boolean isTotalMode = (totalConfirmPcs >= remainOrderPcs);
		
		// 3. 상품 스캔 검수 항목 & RFID 검수 항목 리스트 추출 
		List<DpsInspItem> scanInspItems = new ArrayList<DpsInspItem>();
		List<RfidResult> rfidInspItems = new ArrayList<RfidResult>();
		
		for(Map<String, Object> item : itemObjs) {
			boolean rfidItemFlag = ValueUtil.isNotEmpty(item.get("rfid_id"));
			
			if(rfidItemFlag) {
				RfidResult rfidItem = new RfidResult();
				rfidItem.setBatchId(batch.getId());
				rfidItem.setOrderNo(box.getOrderNo());
				rfidItem.setBoxId(box.getBoxId());
				rfidItem.setRfidId(ValueUtil.toString(item.get("rfid_id")));
				rfidItem.setSkuCd(ValueUtil.toString(item.get("sku_cd")));
				rfidItem.setBrandCd(ValueUtil.toString(item.get("brand_cd")));
				rfidItem.setJobDate(batch.getJobDate());
				rfidItem.setShopCd(ValueUtil.toString(item.get("shop_cd")));
				rfidItem.setOrderQty(ValueUtil.toInteger(item.get("order_qty")));
				rfidInspItems.add(rfidItem);
				
			} else {
				DpsInspItem scanItem = new DpsInspItem();
				scanItem.setSkuCd(ValueUtil.toString(item.get("sku_cd")));
				scanItem.setPickedQty(ValueUtil.toInteger(item.get("picked_qty")));
				scanItem.setConfirmQty(ValueUtil.toInteger(item.get("confirm_qty")));
				scanInspItems.add(scanItem);
			}
		}
		
		// 4. 분할 혹은 전체 모드로 검수 완료 처리 
		if(!isTotalMode) {
			this.splitBox(batch, box, scanInspItems, rfidInspItems, printerId);
		} else {
			this.finishInspectionByAll(batch, box, boxWeight, printerId, scanInspItems, rfidInspItems);
		}
	}
	
	/**
	 * 분할 없이 주문에 대한 검수 완료 처리
	 * 
	 * @param batch
	 * @param box
	 * @param boxWeight
	 * @param printerId
	 * @param scanInspItems
	 * @param rfidInspItems
	 */
	private void finishInspectionByAll(JobBatch batch, BoxPack box, Float boxWeight, String printerId, List<DpsInspItem> scanInspItems, List<RfidResult> rfidInspItems) {
		// 1. 주문에 대해서 이미 송장이 발행이 되었는지 체크  
		Map<String, Object> condition = ValueUtil.newMap("whCd,orderNo,boxId", FnFConstants.WH_CD_ICF, box.getOrderNo(), box.getBoxId());
		String sql = "select waybill_no from mps_express_waybill_print where wh_cd = :whCd and online_order_no = :orderNo and box_id = :boxId";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsExpressWaybillPrint.class);
		String invoiceId = wmsQueryMgr.selectBySql(sql, condition, String.class);

		// 2. 이미 송장이 발행되지 않았다면 
		if(ValueUtil.isEmpty(invoiceId)) {
			// 2.1 WMS로 박스 실적 전송
			this.dpsBoxSendSvc.sendPackingToWms(batch, box.getOrderNo(), box.getBoxId());
			// 2.2 송장 발행 요청
			invoiceId = this.dpsBoxSendSvc.requestInvoiceToWms(batch, box.getOrderNo(), box.getBoxId());
			
		// 3. 이미 송장이 발행되었다면 
		} else {
			condition.put("status", BoxPack.BOX_STATUS_EXAMED);
			condition.put("workUnit", batch.getId());
			condition.put("waybillNo", invoiceId);

			// 3.1 작업 정보 업데이트 - 송장, 상태, 실적 전송 시간 업데이트 
			sql = "update dps_job_instances set status = :status, box_result_if_at = now(), waybill_no = :waybillNo where wh_cd = :whCd and work_unit = :workUnit and ref_no = :orderNo and (waybill_no is null or waybill_no = '')";
			this.queryManager.executeBySql(sql, condition);
			
			// 3.2 주문  정보 업데이트 - 송장, 상태, 실적 전송 시간 업데이트 
			sql = "update mhe_dr set status = :status, box_result_if_at = now(), waybill_no = :waybillNo where wh_cd = :whCd and work_unit = :workUnit and ref_no = :orderNo and (waybill_no is null or waybill_no = '')";
			this.queryManager.executeBySql(sql, condition);		
		}
		
		// 4. 주문이 취소되었다면
		if(ValueUtil.isEqualIgnoreCase(invoiceId, FnFConstants.ORDER_CANCEL_ALL)) {
			// 주문 취소 처리
			box.setStatus(LogisConstants.JOB_STATUS_CANCEL);
			
		// 5. 주문에 대한 송장이 발행되었다면 
		} else {
			// 5.1 박스에 송장 번호 설정
			box.setInvoiceId(invoiceId);
			
			// 5.2 RFID 실적 전송
			if(ValueUtil.isNotEmpty(rfidInspItems)) {
				// 5.2.1 이미 RFID 실적이 전송되었는지 체크  
				sql = "select * from rfid_results where batch_id = :workUnit and invoice_id = :invoiceId";
				int count = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("workUnit,invoiceId", batch.getId(), invoiceId));
				
				// 5.2.2 이미 RFID 실적이 전송되지 않았다면 RFID 실적 전송 
				if(count == 0) {
					for(RfidResult rfid : rfidInspItems) {
						rfid.setInvoiceId(invoiceId);
					}
					// RFID 실적 저장
					this.queryManager.insertBatch(rfidInspItems);
					// RFID 실적 전송
					this.dpsBoxSendSvc.sendPackingToRfid(batch, invoiceId);
				}
			}
			
			// 5.3 송장 발행
			BeanUtil.get(DpsInspectionService.class).printInvoiceLabel(batch, box, printerId);
		}
	}

	@Override
	public BoxPack splitBox(JobBatch batch, BoxPack sourceBox, List<DpsInspItem> inspectionItems, String printerId, Object ... params) {
		return this.splitBox(batch, sourceBox, inspectionItems, null, printerId);
	}
	
	/**
	 * RFID로 송장 분할 처리 
	 * 
	 * @param batch
	 * @param sourceBox
	 * @param scanItemList
	 * @param rfidItemList
	 * @param printerId
	 * @return
	 */
	public BoxPack splitBox(JobBatch batch, BoxPack sourceBox, List<DpsInspItem> scanItemList, List<RfidResult> rfidItemList, String printerId) {
		
		// 1. 박스 정보로 검수 정보 조회 
		String sql = "select * from dps_job_instances where work_unit = :workUnit and ref_no = :refNo and box_id = :boxId and (waybill_no is null or waybill_no = '') and item_cd = :itemCd";
		Map<String, Object> condition = ValueUtil.newMap("workUnit,refNo,boxId", sourceBox.getBatchId(), sourceBox.getOrderNo(), sourceBox.getBoxId());
		List<DpsJobInstance> jobList = new ArrayList<DpsJobInstance>();
		
		// 2. inspectionItems 기준으로 DpsJobInstance 조회
		for(DpsInspItem inspItem : scanItemList) {
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
		
		// 4. 주문에 대해서 이미 송장이 발행이 되었는지 체크  
		Map<String, Object> waybillParams = ValueUtil.newMap("whCd,orderNo,boxId", FnFConstants.WH_CD_ICF, sourceBox.getOrderNo(), sourceBox.getBoxId());
		sql = "select waybill_no from mps_express_waybill_print where wh_cd = :whCd and online_order_no = :orderNo and box_id = :boxId";
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsExpressWaybillPrint.class);
		String invoiceId = wmsQueryMgr.selectBySql(sql, waybillParams, String.class);
		
		// 5. 송장이 발행이 되지 않았다면 WMS에 실적 전송 및 송장 발행 요청 
		if(ValueUtil.isEmpty(invoiceId)) {
			// 5.1 WMS로 조회한 DpsJobInstance 기준으로 박스 실적 전송
			this.dpsBoxSendSvc.sendPackingToWmsBySplit(batch, sourceBox.getOrderNo(), sourceBox.getBoxId(), jobList);
			// 5.2 WMS에 송장 발행 요청
			invoiceId = this.dpsBoxSendSvc.requestInvoiceToWmsBySplit(batch, sourceBox.getOrderNo(), sourceBox.getBoxId(), jobList);
			
		// 6. 작업 정보에 송장 업데이트  
		} else {
			String jobStatus = BoxPack.BOX_STATUS_EXAMED;
			
			// 송장, 상태, 실적 전송 시간 업데이트 
			if(ValueUtil.isNotEqual(jobStatus, jobList.get(0).getStatus())) {
				List<String> orderIdList = new ArrayList<String>(jobList.size());
				Date currentTime = new Date();
				
				// 6.1 작업 정보 업데이트
				for(DpsJobInstance job : jobList) {
					job.setStatus(jobStatus);
					job.setWaybillNo(invoiceId);
					job.setBoxResultIfAt(currentTime);
					orderIdList.add(job.getMheDrId());
				}
				this.queryManager.updateBatch(jobList, "status", "boxResultIfAt", "waybillNo");
				
				// 6.2 주문  정보 업데이트
				sql = "update mhe_dr set status = :status, box_result_if_at = :boxResultIfAt, waybill_no = :waybillNo where id in (:orderIdList)";
				Map<String, Object> params = ValueUtil.newMap("orderIdList,status,boxResultIfAt,waybillNo", orderIdList, jobStatus, currentTime, invoiceId);
				this.queryManager.executeBySql(sql, params);
			}
		}
		
		// 7. 송장 번호가 성공이면 
		if(ValueUtil.isNotEqual(invoiceId, FnFConstants.ORDER_CANCEL_ALL)) {
			// 7.1 박스에 송장 번호 설정
			sourceBox.setInvoiceId(invoiceId);
			
			// 7.2 RFID 실적 전송
			if(ValueUtil.isNotEmpty(rfidItemList)) {
				sql = "select * from rfid_results where batch_id = :workUnit and invoice_id = :invoiceId";
				condition.put("invoiceId", invoiceId);
				int count = this.queryManager.selectSizeBySql(sql, condition);
				
				if(count == 0) {
					// 7.2.1 RFID 실적 저장
					for(RfidResult rfid : rfidItemList) {
						rfid.setInvoiceId(invoiceId);
					}
					this.queryManager.insertBatch(rfidItemList);
					// 7.2.2 RFID 실적 전송
					this.dpsBoxSendSvc.sendPackingToRfid(batch, invoiceId);
				}
			}
			
			// 7.3 남은 검수 항목이 있으면 동일 주문의 처리 안 된 주문에 대해서 박스 ID 리셋
			sql = "select * from dps_job_instances where work_unit = :workUnit and ref_no = :refNo and (waybill_no is null or waybill_no = '')";
			if(this.queryManager.selectSizeBySql(sql, condition) > 0) {
				// 7.3.1 남은 주문 정보의 BoxId를 null로 업데이트
				sql = "update mhe_dr set box_id = null where work_unit = :workUnit and ref_no = :refNo and (waybill_no is null or waybill_no = '')";
				this.queryManager.executeBySql(sql, condition);
				
				// 7.3.2 남은 작업 정보의 BoxId를 null로 업데이트
				sql = "update dps_job_instances set box_id = null where work_unit = :workUnit and ref_no = :refNo and (waybill_no is null or waybill_no = '')";
				this.queryManager.executeBySql(sql, condition);
			}
			
			// 7.4 송장 발행 
			BeanUtil.get(DpsInspectionService.class).printInvoiceLabel(batch, sourceBox, printerId);
			
		// 8. 송장 번호가 주문 전체 취소이면 리턴에 취소 설정
		} else {
			sourceBox.setStatus(LogisConstants.JOB_STATUS_CANCEL);
		}
		
		// 9. 리턴
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
	 * @param templateName
	 * @param printerId
	 * @param parameters
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

}
