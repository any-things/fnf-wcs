SELECT
	fmb.*,
	sbm.work_date
FROM
	mhe_box fmb,
	(
	SELECT
	  DISTINCT 
		shipto_id,
		box_no,
		MIN(outb_ect_date) AS work_date
	FROM
		(
		SELECT
			mb.shipto_id,
			mb.box_no,
			md.outb_ect_date
		FROM
			mhe_box mb,
			mhe_dr md
		WHERE
			mb.work_date IN (:workDates)
			AND mb.work_unit = md.work_unit
			AND mb.work_date = md.work_date
			AND mb.outb_no = md.outb_no
			AND mb.item_cd = md.item_cd) ts
	GROUP BY
		ts.shipto_id,
		ts.box_no) sbm
WHERE
	fmb.work_date = (:workDates)
	AND fmb.shipto_id = sbm.shipto_id
	AND fmb.box_no = sbm.box_no