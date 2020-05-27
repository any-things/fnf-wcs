select
	stock.*
from (
	select
		s.equip_type,
		s.equip_cd,
		s.sku_cd,
		s.sku_nm,
		s.order_qty,
		s.alloc_qty,
		s.picked_qty,
		s.stock_qty,
		case 
			when (s.order_qty - s.alloc_qty - s.stock_qty) < 0 then 0
			else s.order_qty - s.alloc_qty - s.stock_qty
		end as input_qty
	from (
		select
			a.equip_type,
			a.equip_cd,
			a.sku_cd,
			a.sku_nm,
			COALESCE(a.order_qty, 0) as order_qty,
			COALESCE(a.picked_qty, 0) as picked_qty,
			COALESCE(a.alloc_qty, 0) as alloc_qty,
			COALESCE(b.stock_qty, 0) as stock_qty
		from
			(with fnf_orders as (
				select
					h.equip_type,
					h.equip_cd,
					d.item_cd as sku_cd,
					d.item_nm as sku_nm,
					d.pick_qty,
					d.cmpt_qty,
					case when d.dps_assign_yn = 'Y' then d.pick_qty 
						 else 0 end as alloc_qty
				from
					job_batches h inner join mhe_dr d on h.id = d.work_unit
				where
					h.domain_id = :domainId
					and h.job_type = 'DPS'
					and h.status = 'RUN'
					#if($equipType)
					and equip_type = :equipType
					#end
					#if($equipCd)
					and equip_cd = :equipCd
					#end
					#if($skuCd)
					and d.item_cd like '%' || :skuCd || '%'
					#end
				) select
					equip_type, equip_cd, sku_cd, sku_nm, 
					COALESCE(sum(pick_qty), 0) as order_qty, 
					COALESCE(sum(cmpt_qty), 0) as picked_qty, 
					COALESCE(sum(alloc_qty), 0) as alloc_qty
				from
					fnf_orders
				group by
					equip_type, equip_cd, sku_cd, sku_nm
			) a

			left outer join
			
			(select 
				i.equip_type, i.equip_cd, i.sku_cd, i.stock_qty 
			from (
				select
					equip_type, equip_cd, sku_cd,
					COALESCE(sum(load_qty), 0) as stock_qty
				from
					stocks
				where 
					domain_id = :domainId
					#if($equipType)
					and equip_type = :equipType
					#end
					#if($equipCd)
					and equip_cd = :equipCd
					#end
					#if($skuCd)
					and sku_cd like '%'||:skuCd||'%'
					#end
				group by
					equip_type, equip_cd, sku_cd
			) i 
			where
				i.stock_qty > 0) b

			on a.equip_type = b.equip_type and a.equip_cd = b.equip_cd and a.sku_cd = b.sku_cd
	) s
) stock
order by
	stock.equip_type,
	stock.equip_cd,
	stock.input_qty desc,
	stock.order_qty desc,
	stock.sku_cd