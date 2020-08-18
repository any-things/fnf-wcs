select 
	jb.id as batch_id
	, jb.title
	, jb.instructed_at as from_date
	, jb.finished_at as to_date
	, jb.equip_runtime
	, COALESCE(total_workers, 0) as total_workers
	, jb.batch_order_qty as order_shop_cnt
	, jb.batch_pcs as order_pcs
	, mpr.sku_cnt
	, mb.result_sku_cnt
	, jb.result_pcs
	, jb.result_box_qty
	, jb.batch_order_qty - mb.result_sku_cnt as remain_sku
	, jb.batch_pcs - jb.result_pcs as remain_pcs
	, jb.progress_rate
from 
	job_batches jb 
left outer join
	(select 
		batch_no
	 	, count(distinct sku_cd) as sku_cnt
	from 
		mhe_pas_rlst 
	group by 
		batch_no
	) as mpr
on
	jb.id = mpr.batch_no
left outer join
	(select 
		work_unit
	 	, count(distinct item_cd) as result_sku_cnt
	from 
		mhe_box 
	group by 
		work_unit
	) as mb
on
	jb.id = mb.work_unit
where 
	jb.job_type IN ( :job_type )
and 
	jb.job_date between :from_date and :to_date