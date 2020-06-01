select
	COALESCE(sum(cmpt_qty), 0) as result
from
	mhe_dr
where
	wh_cd = 'ICF'
	and work_unit = :batchId 
	and mhe_datetime between to_timestamp(:timeFrom, 'YYYY-MM-DD HH24:MI:SS.MS') and to_timestamp(:timeTo, 'YYYY-MM-DD HH24:MI:SS.MS')