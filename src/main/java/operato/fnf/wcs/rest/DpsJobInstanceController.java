package operato.fnf.wcs.rest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import operato.fnf.wcs.entity.DpsJobInstance;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/dps_job_instances")
@ServiceDesc(description = "DpsJobInstance Service API")
public class DpsJobInstanceController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return DpsJobInstance.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public DpsJobInstance findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public DpsJobInstance create(@RequestBody DpsJobInstance input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public DpsJobInstance update(@PathVariable("id") String id, @RequestBody DpsJobInstance input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<DpsJobInstance> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/pick/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Picking")
	public BaseResponse processJobInstance(@PathVariable("id") String id) {
		
		// 1. 작업 조회 && 피킹 확정 처리
		DpsJobInstance job = this.queryManager.select(DpsJobInstance.class, id);
		if(job == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.DpsJobInstance", id);
		} else {
			if(job.getCmptQty() >= job.getPickQty()) {
				throw ThrowUtil.newValidationErrorWithNoLog("이미 처리된 작업입니다.");
			}			
		}
		
		job.setCmptQty(job.getPickQty());
		job.setMheDatetime(new Date());
		this.queryManager.update(job, "cmptQty", "mheDatetime");
		
		// 2. 해당 주문이 모두 완료되었는지 체크
		String sql = "SELECT id FROM DPS_JOB_INSTANCES WHERE WORK_UNIT = :batchId AND BOX_NO = :trayCd AND REF_NO = :orderNo AND PICK_QTY > CMPT_QTY";
		Map<String, Object> params = ValueUtil.newMap("batchId,trayCd,orderNo", job.getWorkUnit(), job.getBoxNo(), job.getRefNo());
		int count = this.queryManager.selectSizeBySql(sql, params);
		
		// 3. 완료되었다면 박싱 완료 처리
		if(count == 0) {
			params.put("jobStatus", LogisConstants.JOB_STATUS_BOXED);
			params.put("trayStatus", LogisConstants.JOB_STATUS_WAIT);
			
			// 작업 정보 박싱 완료 처리
			sql = "UPDATE DPS_JOB_INSTANCES SET STATUS = :jobStatus WHERE WORK_UNIT = :batchId AND BOX_NO = :trayCd AND REF_NO = :orderNo";
			this.queryManager.executeBySql(sql, params);
			
			// 주문 정보 박싱 완료 처리
			sql = "UPDATE MHE_DR SET STATUS = :jobStatus, CMPT_QTY = PICK_QTY, MHE_DATETIME = now() WHERE WORK_UNIT = :batchId AND BOX_NO = :boxNo AND REF_NO = :orderNo";
			
			// TrayBox 상태를 대기 상태로 업데이트 
			sql = "UPDATE TRAY_BOXES SET STATUS = :trayStatus WHERE TRAY_CD = :trayCd";

		}
		
		// 4. 재고 차감
		sql = "select * from stocks where domain_id = :domainId and cell_cd = :cellCd for update";
		Stock stock = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,cellCd", Domain.currentDomainId(), job.getCellCd()), Stock.class);
		stock.removeStock(job.getPickQty());
		
		// 5. 결과 리턴
		return new BaseResponse(true, SysConstants.OK_STRING, job);
	}

}