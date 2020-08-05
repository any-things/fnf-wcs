-- 생산성 조회
-- menu
SELECT * FROM menus WHERE name = 'DpsProductivity';
INSERT INTO menus (id,"name",description,parent_id,"template",menu_type,category,"rank",icon_path,hidden_flag,routing,routing_type,detail_form_id,detail_layout,resource_type,resource_name,resource_url,grid_save_url,id_field,title_field,desc_field,pagination,items_prop,total_prop,fixed_columns,domain_id,creator_id,updater_id,created_at,updated_at) VALUES 
('66eeb693-ea15-4e92-a93a-b96fe9eec9c8','DpsProductivity',NULL,'7AAB0D8A-30E3-1DC7-E053-8B010A803CD3',NULL,'SCREEN','STANDARD',2400,NULL,false,'dps_productivity','RESOURCE',NULL,NULL,'ENTITY',NULL,'/wcs/calc_dps_productivity',NULL,NULL,NULL,NULL,NULL,'items','total',0,1,'admin','admin','2020-07-31 13:44:07.349','2020-07-31 13:44:07.349')
SELECT * FROM menu_columns WHERE menu_id = '66eeb693-ea15-4e92-a93a-b96fe9eec9c8' ;
INSERT INTO menu_columns (id,menu_id,"name",description,"rank",term,col_type,col_size,"nullable",ref_type,ref_name,ref_url,ref_params,ref_related,search_rank,sort_rank,reverse_sort,virtual_field,ext_field,search_name,search_editor,search_oper,search_init_val,grid_rank,grid_editor,grid_format,grid_validator,grid_width,grid_align,uniq_rank,form_editor,form_validator,form_format,def_val,range_val,ignore_on_save,domain_id) VALUES 
('db538aae-512b-48be-ace4-1435e8f5f2e0','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','work_date',NULL,10,'작업일자','string',0,false,NULL,NULL,NULL,NULL,NULL,10,0,true,false,false,NULL,'date-from-to-picker',NULL,'today-30,today',10,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('71bc54d3-4630-44e3-9c28-e561568f95b1','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','work_type',NULL,20,'작업유형','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,20,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('32630d43-9774-420f-abe1-0000bdaf71a1','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','done_qty',NULL,40,'처리PCS수량','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,40,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('4fa3f9ea-5c1a-49d4-b208-da150ac6c5ab','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','strr_id',NULL,30,'브랜드','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('f1fc115f-f94b-4a46-aa74-eba0ddc7c45f','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','workers',NULL,70,'작업자수','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,70,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('2a4b55f0-a00c-4aba-aee8-cd2ed345f3da','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','work_hours',NULL,50,'작업시간','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,50,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('451e0b8f-8ae7-4e0f-b963-1e4a51f0d6a6','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','work_minutes',NULL,60,'작업시간','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('72e15400-ad6c-435f-ab57-aab2a3d0694e','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','ph',NULL,90,'생산성(PCS/h)','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,90,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
,('32275b7b-3430-43e7-899d-c06c0c40805b','66eeb693-ea15-4e92-a93a-b96fe9eec9c8','php',NULL,100,'생산성(PCS/h/p)','string',0,false,NULL,NULL,NULL,NULL,NULL,0,0,false,false,false,NULL,NULL,NULL,NULL,100,NULL,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,false,1)
;


-- diy services
SELECT * FROM diy_services WHERE name in ('dps_total_picking_ps_by_day', 'dps_load_stock_ps_by_day', 'dps_picking_ps_by_day', 'dps_inspection_ps_by_day');
DELETE FROM diy_services WHERE name in ('dps_total_picking_ps_by_day', 'dps_load_stock_ps_by_day', 'dps_picking_ps_by_day', 'dps_inspection_ps_by_day');
INSERT INTO diy_services (id,"name",description,category,lang_type,script_type,active_flag,atomic_flag,service_logic,domain_id,creator_id,updater_id,created_at,updated_at) VALUES 
('5351aa11-771c-43b0-b0f6-3e50e142a473','dps_picking_ps_by_day','','SERVICE','','SQL',false,false,'WITH ww AS (
SELECT
	work_date,
	pack_tcd,
	SUM(hour_min_cnt * workers) AS workers,
	SUM(hour_min_cnt) * 10 AS work_minutes,
	SUM(hour_min_cnt) * 10 / 60 AS work_hours
FROM
	(
	SELECT
		work_date,
		pack_tcd,
		COUNT(DISTINCT SUBSTRING(TO_CHAR(mhe_datetime, ''YYYYMMDDHH24MISS''), 0, 12)) AS hour_min_cnt,
		COUNT(DISTINCT inspector_id) AS workers
	FROM
		dps_job_instances AA
	WHERE
		work_date > :fromDate
		AND work_date <= :toDate
	GROUP BY
		work_date,
		SUBSTRING(TO_CHAR(mhe_datetime, ''YYYYMMDDHH24MISS''), 0, 12),
		pack_tcd) BB
GROUP BY
	work_date,
	pack_tcd),
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
	ww.work_hours,
	3 AS seq
FROM
	ss,
	ww
WHERE
	ss.work_date = ww.work_date
	AND ss.pack_tcd = ww.pack_tcd',1,'admin','admin','2020-08-03 14:45:48.795','2020-08-05 13:18:57.537')
,('6710f7c4-c86a-4e0c-a0fc-d5f5f5f2155a','dps_load_stock_ps_by_day','','SERVICE','','SQL',false,false,'WITH ww AS (
SELECT
	work_date,
	SUM(hour_min_cnt * workers) AS workers,
	SUM(hour_min_cnt) * 10 AS work_minutes,
	SUM(hour_min_cnt) * 10 / 60 AS work_hours
FROM
	(
	SELECT
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 8) AS work_date,
		COUNT(DISTINCT SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 11)) AS hour_min_cnt,
		COUNT(DISTINCT creator_id ) AS workers
	FROM
		stock_hists sh
	WHERE
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 11) > :fromDate
		AND SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 11) <= :toDate
		AND in_qty > 0
		AND tran_cd = ''in''
	GROUP BY
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 8),
		SUBSTRING(TO_CHAR(created_at, ''YYYYMMDDHH24MISS''), 1, 11)) BB
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
		SUBSTRING(TO_CHAR(sh.created_at, ''YYYYMMDDHH24MISS''), 1, 8) AS work_date,
		sh.sku_cd,
		sh.in_qty,
		sh.creator_id
	FROM
		stock_hists sh
	WHERE
		sh.in_qty > 0
		AND tran_cd = ''in''
		AND SUBSTRING(TO_CHAR(sh.created_at, ''YYYYMMDDHH24MISS''), 1, 8) > :fromDate
		AND SUBSTRING(TO_CHAR(sh.created_at, ''YYYYMMDDHH24MISS''), 1, 8) <= :toDate) AA
GROUP BY
	work_date )
SELECT
	''LOAD'' AS work_type,
	ss.work_date,
	ss.item_cd_cnt,
	ss.done_qty,
	ww.workers,
	ww.work_minutes,
	ww.work_hours,
	2 AS seq
FROM
	ss,
	ww
WHERE
	ss.work_date = ww.work_date',1,'admin','admin','2020-08-03 14:45:48.758','2020-08-05 13:18:43.519')
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
	bpw.workers,
	5 AS seq
FROM
	bpcnt,
	bpw
WHERE
	bpcnt.work_date = bpw.work_date',1,'admin','admin','2020-08-03 14:45:48.721','2020-08-05 13:18:30.581')
,('377dd39a-651d-4cb7-989f-cc01aa0358bc','dps_total_picking_ps_by_day','','SERVICE','','SQL',false,false,'WITH ww AS (
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
  count(worker_id) as workers,
  count(distinct item_cd) as item_cd_cnt,
  SUM(qty) AS done_qty
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
  work_date)
SELECT
	''TPCK'' AS work_type,
	ss.work_date,
	ss.item_cd_cnt,
	ss.done_qty,
	ww.workers,
	ww.work_minutes,
	ww.work_hours,
	1 AS seq
FROM
	ss,
	ww
WHERE
	ss.work_date = ww.work_date',1,'admin','admin','2020-08-03 14:45:48.911','2020-08-05 13:19:22.768')
;