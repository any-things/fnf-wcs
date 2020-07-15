SELECT DISTINCT
  recva.cd_warehouse,
  recvb.ds_batch_no,
  recva.dt_delivery,
  recva.tp_machine,
  recva.cd_brand,
  recva.no_box
FROM
  rfid_if.if_pasdelivery_recv recva,
  (
    SELECT
      no_box,
      MAX(ds_batch_no) AS ds_batch_no
    FROM
      rfid_if.if_pasdelivery_recv
    WHERE
      dt_delivery = :date
    GROUP BY
      no_box
  ) recvb
WHERE
  1 = 1
  AND recva.dt_delivery = :date
  AND recva.ds_batch_no = recvb.ds_batch_no
  AND recva.no_box = recvb.no_box