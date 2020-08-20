select 
	mpr.batch_no, mpr.job_type, mpr.chute_no, mpr.sku_cd, mpr.sku_bcd, mpr.strr_id
	, mpo.order_qty, mpr.qty, mdrbr.cmpt_qty, mpr.qty - mdrbr.cmpt_qty as diff_qty
from 
	mhe_pas_order mpo
left outer join 
	mhe_pas_rlst mpr
on
	mpo.batch_no = mpr.batch_no
and
	mpo.chute_no = mpr.chute_no
and
	mpo.sku_cd = mpr.sku_cd
left outer join 
	(
		select 
			batch_no, item_cd, strr_id, sum(cmpt_qty) cmpt_qty 
		from 
			mhe_das_rtn_box_rslt 
		where 
			batch_no in ( :batchList )
		group by 
			batch_no, item_cd, strr_id
	) mdrbr
on
	mpr.batch_no = mdrbr.batch_no
and
	mpr.strr_id = mdrbr.strr_id
and
	mpr.sku_cd = mdrbr.item_cd
where 
	mpr.batch_no in ( :batchList )
order by 
	mpr.chute_no, mpr.sku_cd