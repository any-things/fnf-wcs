-- DPS작업배치는 피킹기준으로 실적처리 PCS를 집계함.

SELECT
	work_date,
	SUM(cmpt_qty) AS MULTI_DONE_PCS_QTY,
	SUM(pick_qty) AS pick_qty
FROM
	dps_job_instances
WHERE
	work_date = '20200724'
	AND STATUS = 'E'
	AND pack_tcd = 'H'
	AND mhe_datetime is not null
GROUP BY
	WORK_DATE;


SELECT
	*
FROM
	(
	SELECT
		*
	FROM
		(
		SELECT
			work_date,
			item_cd,
			SUM(cmpt_qty) AS MULTI_DONE_PCS_QTY,
			SUM(pick_qty) AS pick_qty
		FROM
			dps_job_instances
		WHERE
			work_date = '20200724'
			AND STATUS = 'E'
			AND pack_tcd = 'H'
			AND mhe_datetime is not null
		GROUP BY
			WORK_DATE,
			item_cd) jb,
		(
		SELECT
			item_cd,
			SUM(cmpt_qty) AS cmpt_qty
		FROM
			mhe_dr
		WHERE
			work_date = '20200724'
			AND work_unit = '1001521978'
		GROUP BY
			item_cd) md
	WHERE
		jb.item_cd = md.item_cd) aa
WHERE
	MULTI_DONE_PCS_QTY != cmpt_qty;