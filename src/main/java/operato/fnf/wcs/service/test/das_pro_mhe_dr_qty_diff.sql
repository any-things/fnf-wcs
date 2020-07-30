SELECT
	*
FROM
	(
	SELECT
		DR.*,
		br.cmpt_qty
	FROM
		(
		SELECT
			shipto_id,
			item_cd,
			SUM(pick_qty) AS pick_qty
		FROM
			mhe_dr
		WHERE
			work_unit = '1001532793'
		GROUP BY
			shipto_id,
			item_cd) DR,
		(
		SELECT
			shipto_id,
			item_cd,
			SUM(cmpt_qty) AS cmpt_qty
		FROM
			mhe_box
		WHERE
			work_unit = '1001532793'
			AND del_yn != 'Y'
		GROUP BY
			shipto_id,
			item_cd) br
	WHERE
		dr.shipto_id = br.shipto_id
		AND dr.item_cd = br.item_cd) aa
WHERE
	pick_qty != cmpt_qty