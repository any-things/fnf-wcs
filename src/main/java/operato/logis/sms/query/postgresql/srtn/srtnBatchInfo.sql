select 
	jb.job_type
	, jb.title
	, count(od.shop_cd) as shop_cnt
	, count(od.sku_cd) as sku_cnt
	, sum(od.order_qty) as pcs_cnt
from 
	job_batches jb
left outer join 
	orders od
on
	jb.id = od.batch_id
where 
	jb.id = :batchId
group by 
	jb.job_type, jb.title