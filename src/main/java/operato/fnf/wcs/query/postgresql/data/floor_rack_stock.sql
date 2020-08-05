

-- diy_services
SELECT * FROM diy_services WHERE name in ('board_floor_rack_stock', 'board_floor_rack_sku_cnt', 'board_floor_rack_used_rate');
DELETE diy_services WHERE name in ('board_floor_rack_stock', 'board_floor_rack_sku_cnt', 'board_floor_rack_used_rate');

INSERT INTO diy_services (id,"name",description,category,lang_type,script_type,active_flag,atomic_flag,service_logic,domain_id,creator_id,updater_id,created_at,updated_at) VALUES 
('b189ecff-5676-4cdd-9fd7-b222edc717a4','board_floor_rack_used_rate',NULL,'SERVICE','','SQL',false,false,'SELECT 
  (box_qty / total_cap * 100) AS used_rate from
(SELECT
      wh_cd,
      SUM(space_cbm) AS total_cap,
      SUM(used_cbm) AS box_qty,
      SUM(invn_qty) AS pcs_qty
    FROM
      (
        SELECT
          vcl.wh_cd,
          vcl.zone_cd,
          vcl.wcell_no,
          vil.strr_id,
          vil.item_cd,
          nvl(vcl.space_cbm, 0) AS space_cbm,
          nvl(vil.used_cbm, 0) AS used_cbm,
          nvl(vil.invn_qty, 0) AS invn_qty
        FROM
          fnf_if.vms_cell_list        vcl
          LEFT JOIN fnf_if.vms_inventory_list   vil
          ON vcl.wh_cd = vil.wh_cd
             AND vcl.wcell_no = vil.wcell_no
        WHERE
          vcl.building_tcd = :buildingTcd
          AND vcl.floor_tcd = :floorCd
      ) CI
    GROUP BY
      wh_cd) TS',1,'admin','admin','2020-08-04 14:40:07.581','2020-08-05 13:15:46.755')
,('2984c707-0795-4f00-9f51-abdabd111f33','board_floor_rack_sku_cnt',NULL,'SERVICE','','SQL',false,false,'SELECT
  COUNT(DISTINCT item_cd) AS sku_count
FROM
  (
    SELECT
      vcl.wh_cd,
      vcl.zone_cd,
      vcl.wcell_no,
      vil.strr_id,
      vil.item_cd,
      nvl(vil.used_cbm, 0) AS used_cbm,
      nvl(vil.invn_qty, 0) AS invn_qty
    FROM
      fnf_if.vms_cell_list        vcl
      LEFT JOIN fnf_if.vms_inventory_list   vil
      ON vcl.wh_cd = vil.wh_cd
         AND vcl.wcell_no = vil.wcell_no
    WHERE
      vcl.building_tcd = :buildingTcd
      AND vcl.floor_tcd = :floorCd
  )',1,'admin','admin','2020-08-04 14:40:07.670','2020-08-05 13:15:09.218')
,('3f0499be-03cf-4494-b678-d2b1df502dcc','board_floor_rack_stock',NULL,'SERVICE','','SQL',false,false,'SELECT
  vcl.wh_cd,
  vcl.zone_cd,
  vcl.wcell_no,
  decode(vcl.zone_cd, ''DX-3FR'',(substr(vcl.wcell_no, 0, 8)
                                || ''-''
                                || to_char(substr(vcl.wcell_no, 10, 1), ''FM00'')), vcl.wcell_no || ''-01'') AS location,
  nvl(vil.item_cd, '' '') AS sku_cd,
  nvl(vsl.item_nm, '' '') AS sku_nm,
  nvl(wmi.brand, '' '') AS brand,
  nvl(wmi.item_season, '' '') AS season,
  nvl(wmi.item_color, '' '') AS color,
  nvl(wmi.item_style, '' '') AS style,
  nvl(wmi.item_size, '' '') AS "size",
  nvl(vcl.space_cbm, 0) AS space_cbm,
  ( nvl(vil.used_cbm, 0) / nvl(vcl.space_cbm, 1) * 100 ) AS used_rate,
  nvl(vil.used_cbm, 0) AS box_qty,
  nvl(vil.invn_qty, 0) AS pcs_qty,
  nvl(vsl.item_gcd, '' '') AS item_gcd,
  nvl(vig.itemgrp_snm, '' '') AS item_gnm
FROM
  vms_cell_list             vcl
  LEFT JOIN vms_inventory_list        vil
  ON vcl.wh_cd = vil.wh_cd
     AND vcl.wcell_no = vil.wcell_no
  LEFT JOIN fnf_wm.mhe_item_barcode   wmi
  ON vil.item_cd = wmi.item_cd
  LEFT JOIN vms_sku_list              vsl
  ON vil.strr_id = vsl.strr_id
     AND vil.item_cd = vsl.item_cd
  LEFT JOIN vms_item_group            vig
  ON vsl.item_gcd = vig.item_gcd
WHERE
  vcl.building_tcd = :buildingTcd
  AND vcl.floor_tcd = :floorCd
  AND vcl.wcell_no LIKE :wcellNo
ORDER BY
  vcl.wcell_no ASC',1,'admin','admin','2020-08-04 11:29:43.520','2020-08-05 13:14:37.824')
;;