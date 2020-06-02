package operato.logis.dps.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import operato.logis.dps.model.DpsInspItem;
import operato.logis.dps.model.DpsInspection;
import operato.logis.dps.query.store.DpsInspectionQueryStore;
import operato.logis.dps.service.api.IDpsInspectionService;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * DPS 출고 검수 서비스
 * 
 * @author shortstop
 */
public class DpsInspectionService extends AbstractInstructionService implements IDpsInspectionService {

	/**
	 * DPS 출고 검수 처리용 쿼리 스토어
	 */	
	@Autowired
	private DpsInspectionQueryStore dpsInspectionQueryStore;
	
	/**
	 * 박스 조회
	 * 
	 * @param domainId
	 * @param batchId
	 * @param invoiceId
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private BoxPack findBoxByInvoiceId(Long domainId, String batchId, String invoiceId, boolean exceptionWhenEmpty) {
		BoxPack box = AnyEntityUtil.findEntityBy(domainId, false, BoxPack.class, null, "batchId,invoiceId", batchId, invoiceId);
		
		if(exceptionWhenEmpty) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.BoxPack", invoiceId);
		}
		
		return box;
	}
	
	/**
	 * 검수 항목 조회 처리 ...
	 * 
	 * @param inspection
	 * @param params
	 * @return
	 */
	private DpsInspection searchInpsectionItems(DpsInspection inspection, Map<String, Object> params) {
		if(!params.containsKey("invoiceId")) {
			params.put("invoiceId", inspection.getInvoiceId());
		}
		
		String sql = this.dpsInspectionQueryStore.getSearchInspectionItemsQuery();
		List<DpsInspItem> items = this.queryManager.selectListBySql(sql, params, DpsInspItem.class, 0, 0);
		inspection.setItems(items);
		return inspection;
	}
	
	@Override
	public DpsInspection findInspectionByInput(JobBatch batch, String inputType, String inputId) {
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		
		if(ValueUtil.isEqualIgnoreCase(inputType, "box") || ValueUtil.isEqualIgnoreCase(inputType, "tray")) {
			params.put("boxId", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "orderNo")) {
			params.put("orderNo", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "invoiceId")) {
			params.put("invoiceId", inputId);
		}

		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		if(inspection == null) {
			return null;
		}

		if(ValueUtil.isEqualIgnoreCase(inputType, "box") || ValueUtil.isEqualIgnoreCase(inputType, "tray")) {
			inspection.setBoxType(inputType);
		}

		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByTray(JobBatch batch, String trayCd) {
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), trayCd);
		
		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		if(inspection == null) {
			return null;
		}

		inspection.setBoxType("tray");
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByBox(JobBatch batch, String boxId) {
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), boxId);
		
		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		if(inspection == null) {
			return null;
		}

		inspection.setBoxType("box");
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByInvoice(JobBatch batch, String invoiceId) {
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId", batch.getDomainId(), batch.getId(), invoiceId);
		
		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		if(inspection == null) {
			return null;
		}

		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public DpsInspection findInspectionByBoxPack(BoxPack box) {
		String sql = this.dpsInspectionQueryStore.getFindInspectionQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId", box.getDomainId(), box.getBatchId(), box.getInvoiceId());
		
		DpsInspection inspection = this.queryManager.selectBySql(sql, params, DpsInspection.class);
		if(inspection == null) {
			return null;
		}

		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public void finishInspection(JobBatch batch, String invoiceId, Float boxWeight, String printerId) {
		// 박스 조회
		BoxPack box = this.findBoxByInvoiceId(batch.getDomainId(), batch.getId(), invoiceId, true);
		// 검수 완료 처리 
		this.finishInspection(box, boxWeight, printerId);
	}

	@Override
	public void finishInspection(BoxPack box, Float boxWeight, String printerId) {
		// 1. 박스 내품 검수 항목 완료 처리, TODO mhe_dr에 검수 시간 필요 ...
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId,status", box.getDomainId(), box.getBatchId(), box.getInvoiceId(), BoxPack.BOX_STATUS_EXAMED);
		String sql = "update mhe_dr set status = :status where wh_cd = 'ICF' and work_unit = :batchId and waybill_no = :invoiceId";
		this.queryManager.executeBySql(sql, params);
	}

	@Override
	public BoxPack splitBox(BoxPack sourceBox, List<DpsInspItem> inspectionItems, String printerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int printInvoiceLabel(JobBatch batch, BoxPack box, String printerId) {
		String labelTemplate = BatchJobConfigUtil.getInvoiceLabelTemplate(batch);
		PrintEvent printEvent = new PrintEvent(batch.getDomainId(), printerId, labelTemplate, ValueUtil.newMap("box", box));
		BeanUtil.get(EventPublisher.class).publishEvent(printEvent);
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

}
