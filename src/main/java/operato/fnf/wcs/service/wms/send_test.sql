SELECT
  bs.*
FROM
  (
    SELECT
      ds_batch_no,
      cd_brand,
      no_box,
      MAX(result_st) AS result_st
    FROM
      rfid_if.if_pasdelivery_send
    WHERE
      dt_delivery = '20200713'
    GROUP BY
      ds_batch_no,
      cd_brand,
      no_box
  ) bb,
  rfid_if.if_pasdelivery_send bs
WHERE
  bb.ds_batch_no = bs.ds_batch_no
  AND bb.cd_brand = bs.cd_brand
  AND bb.no_box = bs.no_box
  AND bb.result_st = bs.result_st