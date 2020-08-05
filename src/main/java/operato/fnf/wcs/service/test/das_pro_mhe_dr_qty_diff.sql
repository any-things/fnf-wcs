
-- 20200731: 1001537503, 1001536778, 1001536928

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
			work_unit,
			shipto_id,
			item_cd,
			SUM(pick_qty) AS pick_qty
		FROM
			mhe_dr
		WHERE
			work_unit IN ('1001537503', '1001536778', '1001536928')
		GROUP BY
			work_unit,
			shipto_id,
			item_cd) DR,
		(
		SELECT
			work_unit,
			shipto_id,
			item_cd,
			SUM(cmpt_qty) AS cmpt_qty
		FROM
			mhe_box
		WHERE
			work_unit IN ('1001537503', '1001536778', '1001536928')
			AND del_yn != 'Y'
		GROUP BY
			work_unit,
			shipto_id,
			item_cd) br
	WHERE
		dr.work_unit = br.work_unit
		AND dr.shipto_id = br.shipto_id
		AND dr.item_cd = br.item_cd) aa
WHERE
	pick_qty != cmpt_qty
ORDER BY
	work_unit;
  







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
			work_date,
			work_unit,
			shipto_id,
			item_cd,
			SUM(pick_qty) AS pick_qty
		FROM
			mhe_dr
		WHERE
			work_date = '20200731'
		GROUP BY
			work_date,
			work_unit,
			shipto_id,
			item_cd) DR,
		(
		SELECT
			work_date,
			work_unit,
			shipto_id,
			item_cd,
			SUM(cmpt_qty) AS cmpt_qty
		FROM
			mhe_box
		WHERE
			work_date = '20200731'
			AND del_yn != 'Y'
		GROUP BY
			work_date,
			work_unit,
			shipto_id,
			item_cd) br
	WHERE
		dr.work_date = br.work_date
		AND dr.work_unit = br.work_unit
		AND dr.shipto_id = br.shipto_id
		AND dr.item_cd = br.item_cd) aa
WHERE
	pick_qty != cmpt_qty
ORDER BY
	work_unit;