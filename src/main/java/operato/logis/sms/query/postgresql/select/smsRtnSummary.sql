select 
	jb.id as batch_id
	, jb.title
	, jb.instructed_at as from_date
	, jb.finished_at as to_date
	, jb.equip_runtime
	, COALESCE(total_workers, 0) as total_workers
	, jb.batch_order_qty as order_sku_cnt
	, jb.batch_pcs as order_pcs
	, mpr.pas_box_cnt
	, mdrbr.result_sku_cnt
	, jb.result_pcs
	, jb.result_box_qty
	, jb.batch_order_qty - mdrbr.result_sku_cnt as remain_sku
	, jb.batch_pcs - jb.result_pcs as remain_pcs
	, jb.progress_rate
from 
	job_batches jb 
left outer join
	(select 
		batch_no
		, count(distinct box_id) as pas_box_cnt 
	from 
		mhe_pas_rlst 
	group by 
		batch_no
	) as mpr
on
	jb.id = mpr.batch_no
left outer join
	(select 
		batch_no
		, count(distinct item_cd) as result_sku_cnt 
	from 
		mhe_das_rtn_box_rslt 
	group by 
		batch_no
	) as mdrbr
on
	jb.id = mdrbr.batch_no
where 
	jb.job_type = :job_type
and 
	jb.job_date between :from_date and :to_date