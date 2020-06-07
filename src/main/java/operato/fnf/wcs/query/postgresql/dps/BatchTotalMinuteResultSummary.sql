select
	b.work_unit as batch_id, b.date as work_date, b.hour, b.minute, sum(b.picked_qty) as picked_qty
from (
	select 
		a.work_unit, a.date, a.hour, COALESCE(a.cmpt_qty, 0) as picked_qty,
		CASE
			WHEN (a.minute >= '00' and a.minute < '10') THEN '10'
			WHEN (a.minute >= '10' and a.minute < '20') THEN '20'
			WHEN (a.minute >= '20' and a.minute < '30') THEN '30'
			WHEN (a.minute >= '30' and a.minute < '40') THEN '40'
			WHEN (a.minute >= '40' and a.minute < '50') THEN '50'
			ELSE '60'
		END AS minute
	from (
		select
			work_unit,
			to_char(mhe_datetime, 'YYYY-MM-DD') as date,
			to_char(mhe_datetime, 'HH24') as hour,
			to_char(mhe_datetime, 'MI') as minute,
			cmpt_qty
		from 
			mhe_dr
		where
			wh_cd = 'ICF'
			and work_unit = :batchId
	) a
) b
group by
	b.work_unit, b.date, b.hour, b.minute
order by
	b.date, b.hour, b.minute