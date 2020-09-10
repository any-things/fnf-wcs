SELECT
	SUM(cmpt_qty) AS cmpt_qty
FROM
	(
	SELECT
		item_cd,
		SUM(cmpt_qty) AS cmpt_qty
	FROM
		dps_job_instances dji
	WHERE
		work_date = '20200820'
		AND status = 'E'
	GROUP BY
		item_cd) aa;







SELECT
	SUM(cmpt_qty)
FROM
	(
	SELECT
		dji.item_cd,
		cmpt_qty AS cmpt_qty
	FROM
		(
		SELECT
			item_cd,
			SUM(cmpt_qty) AS cmpt_qty
		FROM
			dps_job_instances dji
		WHERE
			work_date = '20200820'
			AND status = 'E'
		GROUP BY
			item_cd) dji,
		sku ss
	WHERE
		dji.item_cd = ss.sku_cd) aa;