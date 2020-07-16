WITH cs AS (
  SELECT
    vcl.wh_cd,
    vcl.zone_cd,
    vcl.wcell_no,
    decode(vcl.zone_cd, 'DX-3FR',(substr(vcl.wcell_no, 0, 8)
                                  || '-'
                                  || to_char(substr(vcl.wcell_no, 10, 1), 'FM00')), vcl.wcell_no || '-01') AS location,
    nvl(vil.item_cd, ' ') AS sku_cd,
    nvl(vsl.item_nm, ' ') AS sku_nm,
    nvl(wmi.brand, ' ') AS brand,
    nvl(wmi.item_season, ' ') AS season,
    nvl(wmi.item_color, ' ') AS color,
    nvl(wmi.item_style, ' ') AS style,
    nvl(wmi.item_size, ' ') AS "size",
    nvl(vcl.space_cbm, 0) AS space_cbm,
    ( nvl(vil.used_cbm, 0) / nvl(vcl.space_cbm, 1) * 100 ) AS used_rate,
    nvl(vil.used_cbm, 0) AS box_qty,
    nvl(vil.invn_qty, 0) AS pcs_qty,
    nvl(vsl.item_gcd, ' ') AS item_gcd,
    nvl(vig.itemgrp_snm, ' ') AS item_gnm
  FROM
    fnf_if.vms_cell_list        vcl
    LEFT JOIN fnf_if.vms_inventory_list   vil
    ON vcl.wh_cd = vil.wh_cd
       AND vcl.wcell_no = vil.wcell_no
    LEFT JOIN mhe_item_barcode            wmi
    ON vil.item_cd = wmi.item_cd
    LEFT JOIN fnf_if.vms_sku_list         vsl
    ON vil.strr_id = vsl.strr_id
       AND vil.item_cd = vsl.item_cd
    LEFT JOIN fnf_if.vms_item_group       vig
    ON vsl.item_gcd = vig.item_gcd
  WHERE
    vcl.building_tcd = 'B'
    AND vcl.floor_tcd = '3F'
    AND vcl.wcell_no LIKE '3F%'
--  ORDER BY
--    vcl.wcell_no ASC
)
SELECT
  wh_cd,
  zone_cd,
  location,
  SUM(used_rate) as cell_used_rate
FROM
  cs
GROUP BY
  wh_cd,
  zone_cd,
  location