select 
	COALESCE(sum(qty), 0) as result 
from 
	mhe_pas_rlst
where
	batch_no = :batchId
	and ins_datetime between to_timestamp(:timeFrom, 'YYYY-MM-DD HH24:MI:SS.MS') and to_timestamp(:timeTo, 'YYYY-MM-DD HH24:MI:SS.MS')