package operato.fnf.wcs.service.rfid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.springframework.stereotype.Component;

import operato.fnf.wcs.FnFConstants;
import operato.fnf.wcs.FnfUtils;
import operato.fnf.wcs.entity.RfidBoxItem;
import operato.fnf.wcs.entity.WcsMheBox;
import operato.fnf.wcs.entity.WmsAssortItem;
import operato.fnf.wcs.service.send.DasBoxSendService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.ResponseObj;
import xyz.anythings.sys.service.AbstractQueryService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@Component
public class DasSendBoxInfoToRfid extends AbstractQueryService {

	private final String KEY_RFID_PROCESS_LIMIT = "wcs.rfid.process.limit";
	
	public ResponseObj dasSendBoxInfo(Map<String, Object> params) throws Exception {
		
		String batchId = String.valueOf(params.get("batch_id"));
		if (ValueUtil.isEmpty(batchId)) {
			return new ResponseObj();
		}
		
		JobBatch jobBatch = queryManager.select(JobBatch.class, batchId);
		if (ValueUtil.isEmpty(jobBatch)) {
			return new ResponseObj();
		}
		
		ResponseObj resp = new ResponseObj();
		return resp;
	}
	
	public ResponseObj dasSendBoxInfoToRfid(Long domainId) throws Exception {
		

		String scopeSql = FnfUtils.queryCustService("das_box_process_date");
		if (ValueUtil.isEmpty(scopeSql)) {
			throw new ValidationException("커스텀 서비스 [das_box_info_with_outb_date]가 존재하지 않습니다.");
		}
		
		Map<String, Object> params = new HashMap<>();
		params.put(SysConstants.ENTITY_FIELD_DOMAIN_ID, domainId);
		params.put("status", JobBatch.STATUS_RUNNING);
		params.put("jobType", ValueUtil.toList(LogisConstants.JOB_TYPE_DAS, LogisConstants.JOB_TYPE_DPS));	// FIXME DAS, DPS?
		List<String> runningBatchWorkDates = this.queryManager.selectListBySql(scopeSql, params, String.class, 0, 0);
		
		ResponseObj resp = new ResponseObj();
		if (ValueUtil.isEmpty(runningBatchWorkDates) || runningBatchWorkDates.size() == 0) {
			resp.setMsg("No Data1");
			return resp;
		}
		
		List<String> dates = new ArrayList<>();
		for (String date: runningBatchWorkDates) {
			dates.add(date.replaceAll("-", ""));
		}
		
		String serviceSql = FnfUtils.queryCustService("das_box_info_with_outb_date");
		if (ValueUtil.isEmpty(serviceSql)) {
			throw new ValidationException("커스텀 서비스 [das_box_info_with_outb_date]가 존재하지 않습니다.");
		}
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("whCd", FnFConstants.WH_CD_ICF);
		paramMap.put("workDates", dates);
		paramMap.put("delYn", LogisConstants.Y_CAP_STRING);
		paramMap.put("ifYn", LogisConstants.N_CAP_STRING);
		List<WcsMheBox> wcsMheBoxes = queryManager.selectListBySql(serviceSql, paramMap, WcsMheBox.class, 0, 1000);
		if (ValueUtil.isEmpty(wcsMheBoxes) || wcsMheBoxes.size() == 0) {
			resp.setMsg("No Data2");
			return resp;
		}
		
		IQueryManager rfidQueryMgr = this.getDataSourceQueryManager(RfidBoxItem.class);
		IQueryManager wmsQueryMgr = this.getDataSourceQueryManager(WmsAssortItem.class);
		DasBoxSendService boxSendSvc = BeanUtil.get(DasBoxSendService.class);

		int DEFAULT_PROCESS_LIMIT = 100;
		
		int processLimit = 0;
		try {
			processLimit = Integer.parseInt(SettingUtil.getValue(KEY_RFID_PROCESS_LIMIT));
		} catch(NumberFormatException e) {
			//e.printStackTrace();
			processLimit = DEFAULT_PROCESS_LIMIT;
		}
		
		int offset = 0;
		float size = (float)wcsMheBoxes.size();
		while(offset < Math.ceil(size/processLimit)) {
			int fromIndex = (int) (offset * processLimit);
			int toIndex = (int) (fromIndex + processLimit);
			toIndex = (int) (toIndex > size ? size : toIndex);
			List<WcsMheBox> boxes = new ArrayList<>(wcsMheBoxes.subList(fromIndex, toIndex));
			if (boxes.size() == 0) {
				break;
			}
			
			boxSendSvc.sendPackingsToRfid(domainId, rfidQueryMgr, wmsQueryMgr, boxes);
			
			offset++;
		}
		
		return new ResponseObj();
		
	}
}
