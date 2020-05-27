select 
	id, domain_id, equip_type, equip_cd, cell_cd, com_cd, sku_cd, sku_barcd, sku_nm, 
	load_qty, alloc_qty, picked_qty, min_stock_qty, max_stock_qty, 
	fixed_flag, active_flag, (COALESCE(load_qty, 0) + COALESCE(alloc_qty, 0)) as stock_qty
from 
	stocks 
where 
	domain_id = :domainId
	#if($cellCd)
	and cell_cd = :cellCd
	#end
	#if($equipType)
	and equip_type = :equipType
	#end
	#if($equipCd) 
	and equip_cd = :equipCd
	#end
	#if($comCd)
	and com_cd = :comCd
	#end
	#if($skuCd)
	and sku_cd = :skuCd
	#end
	#if($fixedFlag)
	and fixed_flag = :fixedFlag
	#end
	#if($activeFlag)
	and active_flag = :activeFlag
	#end
	#if($status)
	and status = :status
	#end
	#if($minStockQty)
	and min_stock_qty < :minStockQty
	#end
	#if($maxStockQty)
	and max_stock_qty < :maxStockQty
	#end