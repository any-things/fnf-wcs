select 
	mpo.batch_no as batch_id, mpo.job_type, mpo.chute_no, mpo.sku_cd, mpo.sku_bcd, mpo.strr_id
	, mpo.order_qty, coalesce(mpr.qty, 0) as pas_qty, coalesce(das.cmpt_qty, 0) as cmpt_qty
	, coalesce(mpr.qty, 0) - coalesce(das.cmpt_qty, 0) as diff_qty
from 
	(
		select 
			batch_no, job_type, chute_no, sku_cd, sku_bcd, strr_id, sum(order_qty) as order_qty
	  	from 
	  		mhe_pas_order 
	  	where 
	  		batch_no in ( :batchList )
	  	group by 
	  		batch_no, job_type, chute_no, sku_cd, sku_bcd, strr_id
	) as mpo
left outer join
	(
		select 
			batch_no, job_type, chute_no, sku_cd, sku_bcd, strr_id, sum(qty) as qty 
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
and 
	mpo.sku_bcd = mpr.sku_bcd
left outer join
	(
		select 
			mdo.batch_no, mdo.job_type, mdo.chute_no, mdo.item_cd, mdo.barcode
			, mdo.strr_id, sum(mdo.order_qty) as das_order_qty, sum(md.cmpt_qty) as cmpt_qty
		from 
			mhe_das_order mdo
		left outer join
			mhe_dr md
		on
			mdo.batch_no = md.work_unit
		and 
			mdo.item_cd = md.item_cd
		and 
			(mdo.shop_cd = md.shipto_id or mdo.shop_cd = md.ref_no)
		where 
			md.work_unit in ( :batchList )
		and 
			mdo.batch_no in ( :batchList )
		group by 
			mdo.batch_no, mdo.job_type, mdo.chute_no, mdo.item_cd, mdo.barcode, mdo.strr_id order by mdo.batch_no, mdo.chute_no, mdo.item_cd
 	) as das
on
	mpo.batch_no = das.batch_no
and 
	mpo.chute_no = das.chute_no
and 
	mpo.sku_cd = das.item_cd