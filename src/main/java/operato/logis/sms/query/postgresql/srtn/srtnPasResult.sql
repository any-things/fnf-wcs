select
	batch_no, box_id, sku_cd, mhe_no
	, strr_id, new_yn, sum(qty) as qty
	, sum(new_qty) as new_qty, sum(dmg_qty) as dmg_qty
	, max(ins_datetime) as ins_datetime
from 
	mhe_pas_rlst 
where 
	batch_no = :batchNo and if_yn = :ifYn
group by 
	batch_no, box_id, sku_cd, mhe_no, strr_id, new_yn
order by 
	box_id, sku_cd