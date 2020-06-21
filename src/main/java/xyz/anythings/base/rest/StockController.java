package xyz.anythings.base.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.api.IStockService;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/stocks")
@ServiceDesc(description = "Stock Service API")
public class StockController extends AbstractRestService {
	
	/**
	 * 재고 서비스
	 */
	@Autowired
	private IStockService stockService;

	@Override
	protected Class<?> entityClass() {
		return Stock.class;
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
	public Stock findOne(@PathVariable("id") String id) {
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
	public Stock create(@RequestBody Stock input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Stock update(@PathVariable("id") String id, @RequestBody Stock input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Stock> list) {
		for(Stock stock : list) {
			stock.setLastTranCd(Stock.TRX_UPDATE);
		}
		
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/find_by_cell/{equip_type}/{equip_cd}/{cell_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "DPS Find Stock By Cell Code")
	public Stock findStock(@PathVariable("equip_type") String equipType, @PathVariable("equip_cd") String equipCd, @PathVariable("cell_cd") String cellCd) {
		
		Stock stock = this.stockService.findOrCreateStock(Domain.currentDomainId(), cellCd);
		
		if(ValueUtil.isNotEqual(stock.getEquipType(), equipType) || ValueUtil.isNotEqual(stock.getEquipCd(), equipCd)) {
			// 현재 호기의 로케이션이 아닙니다. 
			throw ThrowUtil.newValidationErrorWithNoLog(true, "IS_NOT_LOCATION_OF_CURRENT_REGION");
		}
		
		return stock;
	}
	
	@RequestMapping(value = "/search_by_sku/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "DPS Search Stocks By SKU")
	public List<Stock> searchStocksBySku(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd) {
		
		return this.stockService.searchStocksBySku(Domain.currentDomainId(), equipType, equipCd, comCd, skuCd);
	}
	
	@RequestMapping(value = "/recommend_cells/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "DPS Search Recommendation Cells")
	public List<Stock> recommendCells(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name = "fixed_flag", required = false) Boolean fixedFlag) {
		
		return this.stockService.searchRecommendCells(Domain.currentDomainId(), equipType, null, comCd, skuCd, fixedFlag);
	}
	
	@RequestMapping(value = "/find_order_stock/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "DPS Find Order Stock By SKU")
	public Stock findOrderStock(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd) {

		Long domainId = Domain.currentDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		Stock stock = this.stockService.calculateSkuOrderStock(domainId, batch.getId(), equipType, null, comCd, skuCd);
		
		if(stock != null && stock.getOrderQty() > 0) {
			stock.setEquipType(equipType);
			stock.setEquipCd(equipCd);
			stock.setOrderQty(stock.getOrderQty() - stock.getAllocQty() - stock.getPickedQty());
		} else {
			stock = new Stock();
			stock.setOrderQty(0);
			stock.setStockQty(0);
			stock.setInputQty(0);
		}
		
		return stock;
	}
	
	@RequestMapping(value = "/calc_order_stock/{equip_type}/{equip_cd}/{cell_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Calculate Order Stock By Cell and SKU")
	public Stock findOrderStock(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("cell_cd") String cellCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@RequestParam(name = "fixed_flag", required = false) Boolean fixedFlag) {
		
		Long domainId = Domain.currentDomainId();
		SKU sku = AnyEntityUtil.findEntityBy(domainId, true, SKU.class, "id,com_cd,sku_cd,sku_barcd,sku_nm", "comCd,skuCd", comCd, skuCd);
		Stock stock = this.stockService.findOrCreateStock(domainId, cellCd, sku);
		return this.stockService.calcuateOrderStock(stock);
	}
	
	@RequestMapping(value = "/load_stock/{rack_cd}/{cell_cd}/{com_cd}/{sku_cd}/{qty_unit}/{load_qty}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Load Stock")
	public Stock loadStock(
			@PathVariable("rack_cd") String rackCd,
			@PathVariable("cell_cd") String cellCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd,
			@PathVariable("qty_unit") String qtyUnit,
			@PathVariable("load_qty") Integer loadQty) {
		
		// 1. Validation
		Long domainId = Domain.currentDomainId();
		
		// 2. SKU 조회
		SKU sku = AnyEntityUtil.findEntityBy(domainId, true, SKU.class, "id,box_in_qty,com_cd,sku_cd,sku_barcd,sku_nm", "comCd,skuCd", comCd, skuCd);

		// 3. 수량 단위가 박스 단위이면 박스 수량과 적치 수량을 곱해서 처리 
		if(ValueUtil.isEqualIgnoreCase("B", qtyUnit)) {
			loadQty = sku.getBoxInQty() * loadQty;
		}

		// 4. 재고 조회
		Stock stock = this.stockService.findOrCreateStock(domainId, cellCd, sku);
		
		// 5. 재고 보충
		stock = this.stockService.addStock(stock, Stock.TRX_IN, loadQty);

		// 6. 재고 리턴
		return stock;
	}
	
}