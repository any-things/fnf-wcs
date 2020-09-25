select 
	mpo.job_date
	, mpr.batch_no as batch_id
	, jb.title
	, mpo.chute_no
	, mdo.cell_no
	, mpo.sku_cd
	, sku.sku_barcd as sku_barcd
	, sku.brand_cd
	, sku.season_cd
	, sku.style_cd
	, sku.color_cd
	, sku.size_cd
	, mpo.order_qty
	, coalesce(mpr.qty, 0) as pas_qty
	, coalesce(mdrbr.cmpt_qty, 0) as das_qty
	, coalesce(mpr.qty, 0) - coalesce(mdrbr.cmpt_qty, 0) as diff_qty
from 
	(
		select 
			strr_id, job_date, batch_no
			, chute_no, sku_cd, sum(order_qty) order_qty 
		from 
			mhe_pas_order 
		where 
			batch_no in ( :batchList ) 
		group by 
			strr_id, job_date, batch_no, chute_no, sku_cd
	) as mpo
left outer join 
	(
		select 
			batch_no, job_type, chute_no
			, sku_cd, sku_bcd, strr_id, sum(qty) qty 
		from 
			mhe_pas_rlst 
		where 
			batch_no in ( :batchList ) 
		group by 
			batch_no, job_type, chute_no, sku_cd, sku_bcd, strr_id
	) as mpr
on
	mpo.batch_no = mpr.batch_no
and
	mpo.chute_no = mpr.chute_no
and
	mpo.sku_cd = mpr.sku_cd
left outer join
	(
		select 
			batch_no, item_cd, strr_id, cell_no 
		from 
			mhe_das_order 
		where 
			batch_no in ( :batchList )
		group by
			batch_no, item_cd, strr_id, cell_no 
	) mdo
on
	mpo.batch_no = mdo.batch_no
and
	mpo.sku_cd = mdo.item_cd
and
	mpo.strr_id = mdo.strr_id
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
left outer join
	job_batches jb
on
	mpo.batch_no = jb.id
left outer join
	sku sku
on
	mpo.sku_cd = sku.sku_cd
where 
	mpo.batch_no in ( :batchList )
#if($cell_no)
and mdo.cell_no like :cell_no
#end
#if($sku_cd)
and mpr.sku_cd like :sku_cd
#end
order by 
	mpr.chute_no, mdo.cell_no, mpr.sku_cd