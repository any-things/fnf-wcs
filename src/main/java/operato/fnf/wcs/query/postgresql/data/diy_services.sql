-- 생산성 조회
SELECT * FROM diy_services WHERE name in ('dps_total_picking_ps_by_day', 'dps_load_stock_ps_by_day', 'dps_picking_ps_by_day', 'dps_inspection_ps_by_day');
DELETE FROM diy_services WHERE name in ('dps_total_picking_ps_by_day', 'dps_load_stock_ps_by_day', 'dps_picking_ps_by_day', 'dps_inspection_ps_by_day');
INSERT INTO diy_services (id,"name",description,category,lang_type,script_type,active_flag,atomic_flag,service_logic,domain_id,creator_id,updater_id,created_at,updated_at) VALUES 
('377dd39a-651d-4cb7-989f-cc01aa0358bc','dps_total_picking_ps_by_day','','SERVICE','','SQL',false,false,'WITH ww AS (
SELECT
	work_date,
	SUM(hour_min_cnt * workers) AS workers,
	SUM(hour_min_cnt) * 10 AS work_minutes,
	SUM(hour_min_cnt) * 10 / 60 AS work_hours
FROM
	(
	SELECT
		to_char(ins_datetime, ''YYYYMMDD'') AS work_date,
		COUNT(DISTINCT substr(to_char(working_time, ''YYYYMMDDHH24MISS''), 1, 11)) AS hour_min_cnt,
		COUNT(DISTINCT worker_id) AS workers
	FROM
		dps_picking
	WHERE
		to_char(ins_datetime, ''YYYYMMDD'') > :fromDate
		AND to_char(ins_datetime, ''YYYYMMDD'') <= :toDate
	GROUP BY
		to_char(ins_datetime, ''YYYYMMDD''),
		substr(to_char(working_time, ''YYYYMMDDHH24MISS''), 1, 11)) BB
GROUP BY
	work_date),
ss AS (
SELECT
  work_date,
  hour_min,
  count(worker_id) as workers,
  count(item_cd) as item_cd_cnt,
  SUM(qty) AS done_qty,
  ''TPCK'' AS work_type
FROM
  (
    SELECT
      to_char(ins_datetime, ''YYYYMMDD'') AS work_date,
      to_char(working_time, ''YYYYMMDDHH24MISS'') AS work_time,
      substr(to_char(working_time, ''YYYYMMDDHH24MISS''), 1, 11) AS hour_min,
      item_cd,
      strr_id,
      pack_tcd,
      worker_id,
      qty
    FROM
      dps_picking
    WHERE
      wh_cd = ''ICF''
      AND to_char(ins_datetime, ''YYYYMMDD'') > :fromDate
      AND to_char(ins_datetime, ''YYYYMMDD'') <= :toDate
  )
GROUP BY
  work_date,
  hour_min)
SELECT
	ss.work_date,
	ss.item_cd_cnt,
	ss.done_qty,
	ww.workers,
	ww.work_minutes,
	ww.work_hours
FROM
	ss,
	ww
WHERE
	ss.work_date = ww.work_date',1,'admin','admin','2020-08-03 14:45:48.911','2020-08-03 20:53:42.286')
,('6710f7c4-c86a-4e0c-a0fc-d5f5f5f2155a','dps_load_stock_ps_by_day','','SERVICE','','SQL',false,false,'WITH ww AS (
SELECT
	work_date,
	SUM(hour_min_cnt * workers) AS workers,
	SUM(hour_min_cnt) * 10 AS work_minutes,
	SUM(hour_min_cnt) * 10 / 60 AS work_hours
FROM
	(
	SELECT
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 9) AS work_date,
		COUNT(DISTINCT SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 0, 12)) AS hour_min_cnt,
		COUNT(DISTINCT creator_id ) AS workers
	FROM
		stock_hists sh
	WHERE
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 0, 12) > :fromDate
		AND SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 0, 12) <= :toDate
		AND in_qty > 0
		AND tran_cd = ''in''
	GROUP BY
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 9),
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 0, 12)) BB
GROUP BY
	work_date),
ss AS (
SELECT
	work_date,
	COUNT(DISTINCT sku_cd) AS item_cd_cnt,
	SUM(in_qty) AS done_qty
FROM
	(
	SELECT
		SUBSTRING(TO_CHAR(sh.created_at, ''YYYYMMDDHH24MISS''), 0, 9) AS work_date,
		sh.sku_cd,
		sh.in_qty,
		sh.creator_id
	FROM
		stock_hists sh
	WHERE
		sh.in_qty > 0
		AND tran_cd = ''in''
		AND SUBSTRING(TO_CHAR(sh.created_at, ''YYYYMMDDHH24MISS''), 0, 8) > :fromDate
		AND SUBSTRING(TO_CHAR(sh.created_at, ''YYYYMMDDHH24MISS''), 0, 8) <= :toDate) AA
GROUP BY
	work_date )
SELECT
	''LOAD'' AS work_type,
	ss.work_date,
	ss.item_cd_cnt,
	ss.done_qty,
	ww.workers,
	ww.work_minutes,
	ww.work_hours
FROM
	ss,
	ww
WHERE
	ss.work_date = ww.work_date',1,'admin','admin','2020-08-03 14:45:48.758','2020-08-03 20:48:15.031')
,('5351aa11-771c-43b0-b0f6-3e50e142a473','dps_picking_ps_by_day','','SERVICE','','SQL',false,false,'WITH ww AS (
SELECT
	work_date,
	SUM(hour_min_cnt * workers) AS workers,
	SUM(hour_min_cnt) * 10 AS work_minutes,
	SUM(hour_min_cnt) * 10 / 60 AS work_hours
FROM
	(
	SELECT
		work_date,
		COUNT(DISTINCT SUBSTRING(TO_CHAR(mhe_datetime, ''YYYYMMDDHH24MISS''), 0, 12)) AS hour_min_cnt,
		COUNT(DISTINCT inspector_id) AS workers
	FROM
		dps_job_instances AA
	WHERE
		work_date > :fromDate
		AND work_date <= :toDate
	GROUP BY
		work_date,
		SUBSTRING(TO_CHAR(mhe_datetime, ''YYYYMMDDHH24MISS''), 0, 12)) BB
GROUP BY
	work_date),
ss AS (
SELECT
	work_date,
	pack_tcd,
	COUNT(DISTINCT ref_no) AS ref_no_cnt,
	COUNT(DISTINCT item_cd) AS item_cd_cnt,
	SUM(cmpt_qty) AS done_qty
FROM
	dps_job_instances
WHERE
	work_date > :fromDate
	AND work_date <= :toDate
	AND status = ''E''
GROUP BY
	work_date,
	pack_tcd)
SELECT
	''PICK'' AS work_type,
	ss.work_date,
	ss.pack_tcd,
	ss.ref_no_cnt,
	ss.item_cd_cnt,
	ss.done_qty,
	ww.workers,
	ww.work_minutes,
	ww.work_hours
FROM
	ss,
	ww
WHERE
	ss.work_date = ww.work_date',1,'admin','admin','2020-08-03 14:45:48.795','2020-08-03 17:29:56.868')
,('58c27914-8182-4e69-bacd-fb0f564d9f3a','dps_inspection_ps_by_day','','SERVICE','','SQL',false,false,'WITH bpw AS (
SELECT
	work_date,
	SUM(hour_min_cnt * workers) AS workers,
	SUM(hour_min_cnt) * 10 AS work_minutes,
	SUM(hour_min_cnt) * 10/60 AS work_hours
FROM
	(
	SELECT
		REPLACE (job_date, ''-'', '''') AS work_date,
		SUBSTRING(insp_ended_at, 0, 11) AS work_hour,
		COUNT(DISTINCT SUBSTRING(insp_ended_at, 0, 12)) AS hour_min_cnt,
		COUNT(DISTINCT inspector_id) AS workers
	FROM
		box_packs AA
		WHERE
		job_date > :fromDate
		AND job_date <= :toDate
	GROUP BY
		work_date,
		SUBSTRING(insp_ended_at, 0, 11),
		SUBSTRING(insp_ended_at, 0, 12)) BB
GROUP BY
	work_date),
bpbs AS (
SELECT
	bp.job_date AS work_date,
	bp.order_no,
	bp.box_id,
	bp.inspector_id,
	SUBSTRING(bp.insp_ended_at, 0, 12) AS hour_min,
	md.item_cd,
	md.cmpt_qty AS done_qty
FROM
	box_packs bp,
	(
	SELECT
		work_date,
		box_id,
		item_cd,
		SUM(cmpt_qty) AS cmpt_qty
	FROM
		mhe_dr
	WHERE
		work_date > :fromDate
		AND work_date <= :toDate
	GROUP BY
		box_id,
		work_date,
		item_cd) md
WHERE
	bp.job_date > :fromDate
	AND bp.job_date <= :toDate
	AND bp.job_date = md.work_date
	AND bp.box_id = md.box_id),
bpcnt AS (
SELECT
	work_date,
	SUM(done_qty) AS done_qty,
	COUNT(DISTINCT order_no) AS ref_no_cnt,
	COUNT(DISTINCT item_cd) AS item_cd_cnt
FROM
	bpbs AA
GROUP BY
	work_date)
SELECT
	''INSP'' AS work_type,
	bpcnt.work_date,
	bpcnt.done_qty,
	bpcnt.ref_no_cnt,
	bpcnt.item_cd_cnt,
	bpw.work_minutes,
	bpw.work_hours,
	bpw.workers
FROM
	bpcnt,
	bpw
WHERE
	bpcnt.work_date = bpw.work_date',1,'admin','admin','2020-08-03 14:45:48.721','2020-08-04 13:51:03.513')
;