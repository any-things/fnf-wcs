package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 재고 관리
 * 
 * @author shortstop
 */
@Table(name = "stocks", idStrategy = GenerationRule.UUID, uniqueFields="domainId,equipType,equipCd,cellCd", indexes = {
	@Index(name = "ix_stocks_0", columnList = "domain_id,equip_type,equip_cd,cell_cd", unique = true),
	@Index(name = "ix_stocks_1", columnList = "domain_id,cell_cd"),
	@Index(name = "ix_stocks_2", columnList = "domain_id,equip_type,equip_cd"),
	@Index(name = "ix_stocks_3", columnList = "domain_id,com_cd,sku_cd")
})
public class Stock extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 196960417590117264L;
	
	/**
	 * 트랜잭션 - 재고 생성 (create) 
	 */
	public static final String TRX_CREATE = "create";
	/**
	 * 트랜잭션 - 재고 투입 (in) 
	 */
	public static final String TRX_IN = "in";
	/**
	 * 트랜잭션 - 재고 빼기 (out) 
	 */
	public static final String TRX_OUT = "out";
	/**
	 * 트랜잭션 - 재고 조정 (adjust) 
	 */
	public static final String TRX_ADJUST = "adjust";
	/**
	 * 트랜잭션 - 재고 보충 (supply) 
	 */
	public static final String TRX_SUPPLEMENT = "supply";
	/**
	 * 트랜잭션 - 피킹 (pick) 
	 */
	public static final String TRX_PICK = "pick";
	/**
	 * 트랜잭션 - 작업 할당 (assign) 
	 */
	public static final String TRX_ASSIGN = "assign";
	/**
	 * 트랜잭션 - 단순 재고 업데이트 (update) 
	 */
	public static final String TRX_UPDATE = "update";	

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column(name = "equip_type", nullable = false, length = 20)
	private String equipType;

	@Column(name = "equip_cd", nullable = false, length = 30)
	private String equipCd;

	@Column (name = "cell_cd", nullable = false, length = 30)
	private String cellCd;
	
	@Column (name = "com_cd", length = 30)
	private String comCd;

	@Column (name = "sku_cd", length = 30)
	private String skuCd;
	
	@Column (name = "sku_barcd", length = 30)
	private String skuBarcd;

	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	@Column (name = "stock_qty", length = 12)
	private Integer stockQty;

	@Column (name = "load_qty", length = 12)
	private Integer loadQty;

	@Column (name = "alloc_qty", length = 12)
	private Integer allocQty;

	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	@Column (name = "min_stock_qty", length = 12)
	private Integer minStockQty;

	@Column (name = "max_stock_qty", length = 12)
	private Integer maxStockQty;

	@Column (name = "fixed_flag", length = 1)
	private Boolean fixedFlag;

	@Column (name = "active_flag", length = 1)
	private Boolean activeFlag;
	
	@Column (name = "expired_at", length = 20)
	private String expiredAt;
	
	@Column (name = "last_tran_cd", length = 30)
	private String lastTranCd;

	@Column (name = "status", length = 10)
	private String status;

	/**
	 * 총 주문 수량
	 */
	@Ignore
	private Integer orderQty;
	/**
	 * 재고 투입 수량
	 */
	@Ignore
	private Integer inputQty;
	/**
	 * 이전 재고 수량 
	 */
	@Ignore
	private Integer prevStockQty;
	/**
	 * 이전 적치 수량
	 */
	@Ignore
	private Integer prevLoadQty;
	/**
	 * 이전 할당 수량
	 */
	@Ignore
	private Integer prevAllocQty;	
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getCellCd() {
		return cellCd;
	}

	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}
	
	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuBarcd() {
		return skuBarcd;
	}

	public void setSkuBarcd(String skuBarcd) {
		this.skuBarcd = skuBarcd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Integer getStockQty() {
		return stockQty;
	}

	public void setStockQty(Integer stockQty) {
		this.stockQty = stockQty;
	}

	public Integer getLoadQty() {
		return loadQty;
	}

	public void setLoadQty(Integer loadQty) {
		this.prevLoadQty = this.loadQty;
		this.loadQty = loadQty;
	}

	public Integer getAllocQty() {
		return allocQty;
	}

	public void setAllocQty(Integer allocQty) {
		this.prevAllocQty = this.allocQty;
		this.allocQty = allocQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public Integer getMinStockQty() {
		return minStockQty;
	}

	public void setMinStockQty(Integer minStockQty) {
		this.minStockQty = minStockQty;
	}

	public Integer getMaxStockQty() {
		return maxStockQty;
	}

	public void setMaxStockQty(Integer maxStockQty) {
		this.maxStockQty = maxStockQty;
	}

	public Boolean getFixedFlag() {
		return fixedFlag;
	}

	public void setFixedFlag(Boolean fixedFlag) {
		this.fixedFlag = fixedFlag;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public String getLastTranCd() {
		return lastTranCd;
	}

	public void setLastTranCd(String lastTranCd) {
		this.lastTranCd = lastTranCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}

	public Integer getInputQty() {
		return inputQty;
	}

	public void setInputQty(Integer inputQty) {
		this.inputQty = inputQty;
	}
	
	/**
	 * DPS용 작업 할당 트랜잭션
	 * 
	 * @param assignQty
	 */
	public Stock assignJob(Integer assignQty) {
		this.setLastTranCd(Stock.TRX_ASSIGN);
		
		int allocQty = ValueUtil.toInteger(this.getAllocQty(), 0) + assignQty;
		allocQty = allocQty >= 0 ? allocQty : 0;
		this.setAllocQty(allocQty);
		
		int loadQty = ValueUtil.toInteger(this.getLoadQty(), 0) - assignQty;
		loadQty = loadQty >= 0 ? loadQty : 0;
		this.setLoadQty(loadQty);
		
		BeanUtil.get(IQueryManager.class).update(this, "lastTranCd", "stockQty", "allocQty", "loadQty", "updatedAt");
		return this;
	}
	
	/**
	 * 피킹 처리
	 * 
	 * @param pickQty
	 * @return
	 */
	public Stock pickJob(Integer pickQty) {
		this.setLastTranCd(Stock.TRX_PICK);

		int allocQty = ValueUtil.toInteger(this.getAllocQty(), 0) - pickQty;
		allocQty = allocQty >= 0 ? allocQty : 0;
		this.setAllocQty(allocQty);
		
		if(ValueUtil.isEmpty(id)) {
			BeanUtil.get(IQueryManager.class).insert(this);
		} else {
			BeanUtil.get(IQueryManager.class).update(this, "lastTranCd", "allocQty", "stockQty", "updaterId", "updatedAt");
		}
		
		return this;		
	}
	
	/**
	 * 재고 보충
	 * 
	 * @param addQty
	 */
	public Stock addStock(Integer addQty) {
		this.setLastTranCd(Stock.TRX_IN);
		
		int loadQty = ValueUtil.toInteger(this.getLoadQty(), 0) + addQty;
		loadQty = loadQty >= 0 ? loadQty : 0;
		this.setLoadQty(loadQty);
		
		if(ValueUtil.isEmpty(id)) {
			BeanUtil.get(IQueryManager.class).insert(this);
		} else {
			BeanUtil.get(IQueryManager.class).update(this, "comCd", "skuCd", "skuBarcd", "skuNm", "lastTranCd", "loadQty", "stockQty", "updaterId", "updatedAt");
		}
		
		return this;
	}
	
	/**
	 * 재고 마이너스 
	 * 
	 * @param removeQty
	 * @return
	 */
	public Stock removeStock(Integer removeQty) {
		this.setLastTranCd(Stock.TRX_OUT);

		int loadQty = ValueUtil.toInteger(this.getLoadQty(), 0) - removeQty;
		loadQty = loadQty >= 0 ? loadQty : 0;
		this.setLoadQty(loadQty);
		
		if(ValueUtil.isEmpty(id)) {
			BeanUtil.get(IQueryManager.class).insert(this);
		} else {
			BeanUtil.get(IQueryManager.class).update(this, "lastTranCd", "loadQty", "stockQty", "updaterId", "updatedAt");
		}
		
		return this;
	}
	
	/**
	 * 재고 조정
	 * 
	 * @param adjustQty
	 * @return
	 */
	public Stock adjustStock(int adjustQty) {
		this.setLastTranCd(Stock.TRX_ADJUST);

		int loadQty = ValueUtil.toInteger(this.getLoadQty(), 0) + adjustQty;
		loadQty = loadQty >= 0 ? loadQty : 0;
		this.setLoadQty(loadQty);

		BeanUtil.get(IQueryManager.class).update(this, "comCd", "skuCd", "skuBarcd", "skuNm", "lastTranCd", "loadQty", "stockQty", "updaterId", "updatedAt");
		return this;
	}
	
	@Override
	public void beforeUpdate() {
		super.beforeUpdate();
		
		this.stockQty = (this.stockQty == null) ? 0 : this.stockQty;
		this.prevStockQty = this.stockQty;
		this.loadQty = (this.loadQty == null) ? 0 : this.loadQty;
		this.allocQty = (this.allocQty == null) ? 0 : this.allocQty;
		this.pickedQty = (this.pickedQty == null) ? 0 : this.pickedQty;
		
		// 재고 자동 계산
		this.stockQty = this.loadQty + this.allocQty;
	}

	@Override
	public void afterUpdate() {
		super.afterUpdate();
		
		if(ValueUtil.isEmpty(this.lastTranCd) || ValueUtil.isEqualIgnoreCase(this.lastTranCd, Stock.TRX_ASSIGN) || ValueUtil.isEmpty(this.skuCd)) {
			return;
		}
		
		StockHist hist = new StockHist();
		hist.setCellCd(this.cellCd);
		hist.setComCd(this.comCd);
		hist.setSkuCd(this.skuCd);
		hist.setTranCd(this.lastTranCd);
		hist.setPrevStockQty(this.prevStockQty);
		hist.setStockQty(this.stockQty);
		int inOutQty = 0;
		
		// 피킹인 경우 
		if(ValueUtil.isEqualIgnoreCase(this.lastTranCd, Stock.TRX_PICK)) {
			inOutQty = -1 * (this.prevAllocQty - this.allocQty);
			
		// 나머지는 재고 수량으로 판단
		} else {
			inOutQty = this.stockQty - this.prevStockQty;
		}

		if(inOutQty > 0) {
			hist.setInQty(inOutQty);
			
		} else if(inOutQty < 0) {
			hist.setOutQty(Math.abs(inOutQty));
		}
		
		BeanUtil.get(IQueryManager.class).insert(hist);
	}

}
