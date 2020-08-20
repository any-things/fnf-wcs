package xyz.anythings.base.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

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

import operato.fnf.wcs.FnFConstants;
import operato.logis.sms.SmsConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.OrderLabel;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/orders")
@ServiceDesc(description = "Order Service API")
public class OrderController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Order.class;
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
	public Order findOne(@PathVariable("id") String id) {
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
	public Order create(@RequestBody Order input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Order update(@PathVariable("id") String id, @RequestBody Order input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Order> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/{id}/include_details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find One included all details by ID")
	public Map<String, Object> findDetails(@PathVariable("id") String id) {
		return this.findOneIncludedDetails(id);
	}

	@RequestMapping(value = "/order_labels", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find detail by master ID")
	public OrderLabel selectOrderLabel(@PathVariable("id") String id) {
		xyz.elidom.dbist.dml.Query query = new xyz.elidom.dbist.dml.Query();
		query.addFilter(new Filter("orderId", id));
		return this.queryManager.selectByCondition(OrderLabel.class, query);
	}

	@RequestMapping(value = "/{id}/upsert", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Insert or Update")
	public OrderLabel upsertOrderLabel(@PathVariable("id") String id, @RequestBody OrderLabel input) {
		if (ValueUtil.isEmpty(input.getId())) {
			input.setOrderId(id);
			return this.createOne(input);
		} else {
			OrderLabel detail = this.selectOrderLabel(id);
			ValueUtil.cloneObject(input, detail, "");
			return this.updateOne(detail);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/update_multiple_excel", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdateExcel(@RequestBody List<Map> list) {
		List<Order> orderList = new ArrayList<Order>(list.size());
		
		Map<String, Object> seqParams = ValueUtil.newMap("jobType,batchType", SmsConstants.JOB_TYPE_SRTN, FnFConstants.ORDER_RECEIVE_UPLOAD);
		String seqSql = "select cast(COALESCE(max(job_seq), '0') as integer) + 1 as job_seq from job_batches where job_type = :jobType and batch_type = :batchType";
		Map<String, Object> seqMap = this.queryManager.selectBySql(seqSql, seqParams, Map.class);
		String tempJobSeq = "1";
		if(ValueUtil.isNotEmpty(seqMap)) {
			tempJobSeq = ValueUtil.toString(seqMap.get("job_seq"));
		}
		
		String title = "";
		for (Map order : list) {
			if (ValueUtil.isEmpty(order.get("brand_cd")) || ValueUtil.isEmpty(order.get("sku_cd")) || ValueUtil.isEmpty(order.get("order_qty"))) {
				String msg = MessageUtil.getMessage("Empty", "빈칸이 있습니다.");
				throw ThrowUtil.newValidationErrorWithNoLog(msg);
			}
			Order orderInfo = new Order();
			
			String batchId = ValueUtil.toString(order.get("brand_cd")) + "-" + FnFConstants.UPLOAD_ORDER_SEASON + "-" + FnFConstants.ORDER_RECEIVE_UPLOAD + "-" + tempJobSeq;
			String jobDate = DateUtil.dateStr(new Date(), "yyyy-MM-dd");
			
			orderInfo.setId(UUID.randomUUID().toString());
			orderInfo.setBatchId(batchId);
			orderInfo.setOrderNo(batchId);
			orderInfo.setWmsBatchNo(batchId);
			orderInfo.setWcsBatchNo(batchId);
			orderInfo.setJobDate(jobDate);
			orderInfo.setJobSeq(tempJobSeq);
			orderInfo.setJobType(SmsConstants.JOB_TYPE_SRTN);
			orderInfo.setOrderDate(jobDate);
			orderInfo.setComCd(FnFConstants.FNF_COM_CD);
			orderInfo.setAreaCd(ValueUtil.toString(order.get("area_cd")));
			orderInfo.setStageCd(ValueUtil.toString(order.get("stage_cd")));
			orderInfo.setEquipType("SORTER");
			orderInfo.setSkuCd(ValueUtil.toString(order.get("sku_cd")));
			orderInfo.setOrderQty(ValueUtil.toInteger(order.get("order_qty")));
			
			orderList.add(orderInfo);
			
			title = ValueUtil.toString(order.get("title"));
		}
		
		if(ValueUtil.isNotEmpty(orderList)) {
			AnyOrmUtil.insertBatch(orderList, 100);
		}
		
		List<String> batchIdList = AnyValueUtil.filterValueListBy(orderList, "batchId");
		Map<String, Object> updateParams = ValueUtil.newMap("batchList", batchIdList);
		String updateSql = "update orders set sku_type = a.sku_type, sku_barcd = a.sku_barcd, sku_barcd2 = a.sku_barcd2 from (select * from sku) as a where orders.sku_cd = a.sku_cd and orders.batch_id in ( :batchList )";
		this.queryManager.executeBySql(updateSql, updateParams);
		
		StringJoiner selectSql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		selectSql.add("select");
		selectSql.add("	od.batch_id, od.job_date, od.area_cd, od.stage_cd, so.equip_group_cd");
		selectSql.add("	, count(distinct od.sku_cd) as order_qty, sum(od.order_qty) as pcs");
		selectSql.add("from");
		selectSql.add("	orders od");
		selectSql.add("left outer join");
		selectSql.add("	sorters so");
		selectSql.add("on");
		selectSql.add("	od.stage_cd = so.stage_cd");
		selectSql.add("where");
		selectSql.add("	od.batch_id in ( :batchList )");
		selectSql.add("group by");
		selectSql.add("	od.batch_id, od.job_date, od.area_cd, od.stage_cd, so.equip_group_cd");
		
		List<Map> uploadBatchList = this.queryManager.selectListBySql(selectSql.toString(), updateParams, Map.class, 0, 0);
		
		for (Map batch : uploadBatchList) {
			String[] batchInfo = ValueUtil.toString(batch.get("batch_id")).split(SysConstants.DASH);
			String brandCd = batchInfo[0];
			String seasonCd = batchInfo[1];
			String rtnType = batchInfo[2];
			String jobSeq = batchInfo[3];
			
			JobBatch jobBatch = new JobBatch();
			jobBatch.setId(ValueUtil.toString(batch.get("batch_id")));
			jobBatch.setBatchGroupId(ValueUtil.toString(batch.get("batch_id")));
			jobBatch.setWmsBatchNo(ValueUtil.toString(batch.get("batch_id")));
			jobBatch.setWcsBatchNo(ValueUtil.toString(batch.get("batch_id")));
			jobBatch.setTitle(title);
			jobBatch.setComCd(FnFConstants.FNF_COM_CD);
			jobBatch.setJobType(SmsConstants.JOB_TYPE_SRTN);
			jobBatch.setBatchType(rtnType);
			jobBatch.setJobDate(ValueUtil.toString(batch.get("job_date")));
			jobBatch.setJobSeq(jobSeq);
			jobBatch.setAreaCd(ValueUtil.toString(batch.get("area_cd")));
			jobBatch.setStageCd(ValueUtil.toString(batch.get("stage_cd")));
			jobBatch.setEquipType("SORTER");
			jobBatch.setEquipCd(ValueUtil.toString(batch.get("equip_group_cd")));
			jobBatch.setEquipGroupCd(ValueUtil.toString(batch.get("equip_group_cd")));
			jobBatch.setEquipNm(LogisConstants.EMPTY_STRING);
			jobBatch.setBrandCd(brandCd);
			jobBatch.setSeasonCd(seasonCd);
			jobBatch.setParentOrderQty(ValueUtil.toInteger(batch.get("order_qty")));
			jobBatch.setParentPcs(ValueUtil.toInteger(batch.get("pcs")));
			jobBatch.setBatchOrderQty(ValueUtil.toInteger(batch.get("order_qty")));
			jobBatch.setBatchPcs(ValueUtil.toInteger(batch.get("pcs")));
			jobBatch.setStatus(JobBatch.STATUS_WAIT);
			jobBatch.setClosedFlag(false);
			jobBatch.setRfidYn(LogisConstants.N_CAP_STRING);
			this.queryManager.insert(jobBatch);
		}
		
		return true;
	}
}