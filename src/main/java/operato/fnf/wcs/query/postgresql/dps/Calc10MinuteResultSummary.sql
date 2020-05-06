select 
	COALESCE(sum(cmpt_qty), 0) as result 
from 
	mhe_box 
where 
	work_unit = :batchId 
	and work_date = :date 
	and mhe_datetime between to_timestamp(:timeFrom, 'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:timeTo, 'YYYY-MM-DD HH24:MI:SS')