SELECT
	rr.*,
	pick_qty
FROM
	(
	SELECT
		shipto_id,
		item_cd,
		SUM(pick_qty) AS pick_qty
	FROM
		mhe_dr
	WHERE
		work_unit = '1001501210'
	GROUP BY
		shipto_id,
		item_cd) bb,
	(
	SELECT
		shipto_id,
		item_cd,
		SUM(cmpt_qty) AS cmpt_qty
	FROM
		mhe_box
	WHERE
		work_unit = '1001501210'
		AND del_yn != 'Y'
	GROUP BY
		shipto_id,
		item_cd) rr
WHERE
	bb.shipto_id = rr.shipto_id
	AND bb.item_cd = rr.item_cd
	AND bb.pick_qty < cmpt_qty