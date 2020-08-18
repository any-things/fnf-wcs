select 
	uuid_generate_v4() as id
	, jb.batch_group_id as batch_no
	, to_char(to_date(jb.job_date, 'YYYY-MM-DD'), 'YYYYMMDD') as job_date
	, '0' as job_type
	, '99999999' as box_id
	, op.sub_equip_cd as chute_no
	, op.cell_assgn_cd as sku_cd
	, s.sku_barcd as sku_bcd
	, total_pcs as order_qty
	, 'N' as if_yn
	, now() as ins_datetime
	, op.equip_cd as mhe_no
	, to_char(to_date(jb.job_date, 'YYYY-MM-DD'), 'YYYYMMDD') as input_date
	, s.brand_cd as strr_id
from 
	order_preprocesses op
left outer join 
	job_batches jb
on
	op.batch_id = jb.id
left outer join
	sku s
on
	op.cell_assgn_cd = s.sku_cd
where 
	batch_id = :batchId
order by 
	chute_no