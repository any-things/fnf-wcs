select 
	a.tower_lamp_cd, 
	sum(case when a.empty_yn = 'N' then 1 else 0 end) as fill_cell_cnt,
	sum(case when a.empty_yn = 'Y' then 1 else 0 end) as empty_cell_cnt,
	count(1) as tot_cell_cnt
from (
	select
		c.tower_lamp_cd,
		c.cell_cd,
		case when (trim(s.sku_cd) is null or trim(s.sku_cd) = '') then 'Y'
		 	 when (s.load_qty is null or s.load_qty = 0) and (s.alloc_qty is null or s.load_qty = 0) then 'Y'
		 	 else 'N'
		 	 end as empty_yn
	from
		cells c left outer join stocks s on c.equip_cd = s.equip_cd and c.cell_cd = s.cell_cd
	where
		c.domain_id = :domainId
		and c.active_flag = true
		and c.equip_type = :equipType
		and c.equip_cd = :equipCd
		and c.tower_lamp_cd is not null
	order by
		cell_cd) a	
group by
	a.tower_lamp_cd
order by
	a.tower_lamp_cd