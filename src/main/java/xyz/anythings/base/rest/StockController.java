package xyz.anythings.base.rest;

import java.util.ArrayList;
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

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.Stock;
import xyz.anythings.base.service.api.ISkuSearchService;
import xyz.anythings.base.service.api.IStockService;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
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
	
	/**
	 * 상품 조회 서비스
	 */
	@Autowired
	private ISkuSearchService skuSearchService;

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
	
	@RequestMapping(value = "/sku/search/{equip_type}/{equip_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search by SKU")
	public List<SKU> searchBySkuCd(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("sku_cd") String skuCd) {
		
		Long domainId = Domain.currentDomainId();
		
		// 1. 배치 진행 여부 확인 
		boolean runningBatchExist = this.isRunningBatchExist(domainId, equipType, equipCd);
		
		// 2. 배치 진행 상태에 따라 검색 가능한 대상 SKU 가 달라짐.
		if(runningBatchExist) {
			// 2.1. 작업 진행중일 때눈 배치에 포함된 상품 리스트 검색 
			JobBatch batch = this.checkRunningBatch(domainId, equipType, equipCd);
			return this.skuSearchService.searchList(batch, skuCd);
			
		} else {
			// 2.2. 작업 진행중이 아닐 때는, 고정식에 지정된 상품 리스트 에서 검색 	
			String itemMasterTb = SettingUtil.getValue(domainId, "fnf.item_barcode.table.name", "mhe_item_barcode");
			
			// 2.2.1 상품 마스터에서 상품 검색 
			String qry = "select brand as brand_cd, item_color as color_cd, 'FnF' as com_cd, item_size as size_cd, barcode as sku_barcd , item_cd as sku_cd , '' as sku_nm, item_style as style_cd from " + itemMasterTb + " where (item_cd = :skuCd or barcode = :skuCd or barcode2 = :skuCd)";
			List<SKU> skuList = this.queryManager.selectListBySql(qry, ValueUtil.newMap("skuCd", skuCd), SKU.class, 0, 0);
			
			if(ValueUtil.isEmpty(skuList)) {
				List<String> terms = ValueUtil.toList(MessageUtil.getTerm("terms.label.sku", "SKU"), skuCd);
				throw ThrowUtil.newValidationErrorWithNoLog(true, SysMessageConstants.NOT_FOUND, terms);
			}
			
			// 2.2.2 상품별 고정 로케이션 상품인지 판별
			List<SKU> retSkuList = new ArrayList<SKU>();
			for(SKU sku : skuList) {
				List<Stock> fixStocks = this.stockService.searchStocksBySku(domainId, equipType, null, true, sku.getComCd(), sku.getSkuCd());
				if(ValueUtil.isNotEmpty(fixStocks)) {
					retSkuList.add(sku);
				}
			}
			
			// 2.2.3. 고정 로케이션에 포함된 상품이 아닌 경우에는 error 
			if(ValueUtil.isEmpty(retSkuList)) {
				throw ThrowUtil.newValidationErrorWithNoLog("지금은 고정 셀 보충만 가능합니다.고정 셀 상품을 선택해주세요.");
			}
			
			return retSkuList;
		}
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
		
		Long domainId = Domain.currentDomainId();
		
		// 1. 배치 진행 여부 확인 
		boolean runningBatchExist = this.isRunningBatchExist(domainId, equipType, equipCd);
		
		// 2. 배치 진행 여부에 따라 추천셀 리스트 달라짐.
		if(runningBatchExist) {
			// 2.1. 진행 중이면 고정/ 자유 모두 보임 
			return this.stockService.searchRecommendCells(domainId, equipType, null, comCd, skuCd, null);
		} else {
			// 2.2. 진행 중이 아니면 고정 로케이션만 사용 
			return this.stockService.searchRecommendCells(domainId, equipType, null, comCd, skuCd, true);
		}
	}
	
	@RequestMapping(value = "/find_order_stock/{equip_type}/{equip_cd}/{com_cd}/{sku_cd}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "DPS Find Order Stock By SKU")
	public Stock findOrderStock(
			@PathVariable("equip_type") String equipType,
			@PathVariable("equip_cd") String equipCd,
			@PathVariable("com_cd") String comCd,
			@PathVariable("sku_cd") String skuCd) {

		Long domainId = Domain.currentDomainId();
		
		// 1. 배치 진행 여부 확인 
		boolean runningBatchExist = this.isRunningBatchExist(domainId, equipType, equipCd);
		
		if(runningBatchExist) {
			JobBatch batch = this.checkRunningBatch(domainId, equipType, equipCd);
			Stock stock = this.stockService.calculateSkuOrderStock(domainId, batch.getId(), equipType, null, comCd, skuCd);
			
			if(stock != null && stock.getOrderQty() > 0) {
				stock.setEquipType(equipType);
				stock.setEquipCd(equipCd);
				stock.setOrderQty(stock.getOrderQty() - stock.getAllocQty() - stock.getStockQty());
			} else {
				stock = new Stock();
				stock.setOrderQty(0);
				stock.setStockQty(0);
				stock.setInputQty(0);
			}
			
			return stock;
		} else {
			throw ThrowUtil.newValidationErrorWithNoLog("지금은 고정 셀 보충만 가능합니다.고정 셀 상품을 선택해주세요.");
		}
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
		
		// 셀 상품 주문의 재고 계산 .....
		Long domainId = Domain.currentDomainId();
		
		// 1. 배치 진행 여부 확인 
		boolean runningBatchExist = this.isRunningBatchExist(domainId, equipType, equipCd);
		
		// 2. WMS에서 상품 정보 조회
		SKU sku = this.skuSearchService.findSku(domainId, comCd, skuCd, true);
		
		if(runningBatchExist) {
			// 3. 배치가 진행중일 때는 모든 셀을 자유식으로 계산 ....  
			// 재고 조회 - 없으면 생성
			Stock stock = this.stockService.findOrCreateStock(domainId, cellCd, sku);
			stock.setFixedFlag(false);
			// 3.1. 주문 처리에 필요 수량 조회 
			return this.stockService.calcuateOrderStock(stock);
			
		} else {
			// 4. 배치가 진행중이 아닐때는 재고에 등록 된 고정 로케이션의 상품에 대한 적치 만 가능 
			Stock stock = this.stockService.findStock(domainId, cellCd, comCd, skuCd, true);
			// 4.1. 최대 적치 가능 수량 조회 
			return this.stockService.calcuateOrderStock(stock);
		}
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
		
		Long domainId = Domain.currentDomainId();
		
		// 1. 배치 진행 여부 확인 
		boolean runningBatchExist = this.isRunningBatchExist(domainId, LogisConstants.EQUIP_TYPE_RACK, rackCd);
		
		// 2. SKU 조회
		SKU sku = this.skuSearchService.findSku(domainId, comCd, skuCd, true);

		// 3. 재고 조회시 Lock을 걸고 조회
		String sql = "select * from stocks where domain_id = :domainId and cell_cd = :cellCd for update";
		Stock stock = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,cellCd", domainId, cellCd), Stock.class);
		
		// 4. 작업이 진행중이면 
		if(runningBatchExist) {
			// 4.1. 재고가 없다면 생성 
			if(stock == null) {
				stock = this.stockService.createStock(domainId, cellCd, sku.getComCd(), sku.getSkuCd(), sku.getSkuNm());
				
			// 4.2. 재고가 있다면 
			} else {
				// 재고에 상품 정보가 없다면 sku의 정보를 설정 
				if(ValueUtil.isEmpty(stock.getSkuCd())) {
					stock.setComCd(sku.getComCd());
					stock.setSkuCd(sku.getSkuCd());
					stock.setSkuBarcd(sku.getSkuBarcd());
					stock.setSkuNm(sku.getSkuNm());
					
				} else if(ValueUtil.isNotEqual(stock.getSkuCd(), sku.getSkuCd())) {
					// 재고의 상품 정보와 sku의 상품 정보가 다른 경우 재고 수량이 존재하지 않으면 sku 정보로 재고 설정 
					if(ValueUtil.toInteger(stock.getAllocQty(), 0) == 0 && ValueUtil.toInteger(stock.getLoadQty(), 0) == 0) {
						stock.setComCd(sku.getComCd());
						stock.setSkuCd(sku.getSkuCd());
						stock.setSkuBarcd(sku.getSkuBarcd());
						stock.setSkuNm(sku.getSkuNm());
						
					} else {
						throw ThrowUtil.newValidationErrorWithNoLog("해당 재고에 다른 상품 재고가 존재합니다.");
					}
				}
			}
			
		// 5. 작업이 진행중이 아니면 
		} else {
			if(stock == null || stock.getFixedFlag() == null || !stock.getFixedFlag()) {
				// 5.1 재고가 없다면 에러 
				throw ThrowUtil.newValidationErrorWithNoLog("지금은 고정 셀 보충만 가능합니다.고정 셀을 선택해주세요.");
			} else {
				// 5.2. 상품 정보와 Stock 정보가 동일하지 않으면 에러 
				if(ValueUtil.isNotEqual(stock.getSkuCd(), sku.getSkuCd())) {
					throw ThrowUtil.newValidationErrorWithNoLog("해당 셀에 다른 상품 재고가 존재합니다.");
				} else {
					// 5.3. 최대 적치 수량을 초과 하면 에러 
					if((ValueUtil.toInteger(stock.getLoadQty(), 0) + loadQty) > stock.getMaxStockQty()) {
						throw ThrowUtil.newValidationErrorWithNoLog("최대 적치 수량을 초과해서 적치 할 수 없습니다.");
					} else {
						stock.setComCd(sku.getComCd());
						stock.setSkuCd(sku.getSkuCd());
						stock.setSkuBarcd(sku.getSkuBarcd());
						stock.setSkuNm(sku.getSkuNm());
					}
				}
			}
		}
		
		// 6. 재고 보충
		this.stockService.addStock(stock, Stock.TRX_IN, loadQty);

		// 7. 재고 리턴
		return stock;
	}
	
	/**
	 * 진행 중인 배치가 존재하는 지 판단만 한다. 존재하지 않아도 에러가 발생하지 않는다.
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	private boolean isRunningBatchExist(Long domainId, String equipType, String equipCd) {
		String sql = "select id from job_batches where domain_id = :domainId and status = :status and equip_type = :equipType and equip_cd = :equipCd";
		int runBatchCount = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("domainId,status,equipType,equipCd", domainId, JobBatch.STATUS_RUNNING, equipType, equipCd));
		return runBatchCount > 0;
	}

	/**
	 * 현재 DPS 배치가 진행중인 여부를 판단 한다.존재하지 않아도 에러가 발생한다.
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	private JobBatch checkRunningBatch(Long domainId, String equipType, String equipCd) {
		
		if(ValueUtil.isEqualIgnoreCase(LogisConstants.EQUIP_TYPE_RACK, equipType)) {
			Rack rack = LogisServiceUtil.checkValidRack(domainId, equipCd);
			JobBatch batch = LogisServiceUtil.findBatch(domainId, rack.getBatchId(), false, false);
			if(ValueUtil.isEqual(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
				return batch;
			} 
			
			return null;
			
		} else {
			List<String> terms = ValueUtil.toList(MessageUtil.getTerm("terms.label.equip_type", "EquipType"), equipType);
			throw ThrowUtil.newValidationErrorWithNoLog(true, SysMessageConstants.INVALID_PARAM, terms);
		}		
	}
	
}