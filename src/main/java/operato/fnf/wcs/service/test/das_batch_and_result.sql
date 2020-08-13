select * from job_batches where id = 'M-S-U-1';
select * from mhe_pas_order where batch_no = 'M-S-U-1';
select * from mhe_das_order where batch_no = 'M-S-U-1';

-- pas, das주문 전송
select sum(order_qty) from mhe_pas_order where batch_no = 'M-S-U-1';
select sum(order_qty) from mhe_das_order where batch_no = 'M-S-U-1';

-- pas, das실적조회
select sum(qty), sum(dmg_qty), sum(new_qty) from mhe_pas_rlst where batch_no = 'M-S-U-1';
select count(distinct box_no), sum(cmpt_qty) from mhe_das_rtn_box_rslt where batch_no = 'M-S-U-1';


-- pas, das실적이 다른거.
SELECT
	mpr.chute_no,
	mpr.sku_cd,
	mpr.qty,
	AB.item_cd,
	AB.qty,
	mpr.qty - AB.qty
FROM
	mhe_pas_rlst mpr
LEFT OUTER JOIN (
	SELECT
		batch_no,
		item_cd,
		COALESCE(SUM(cmpt_qty), 0) AS qty
	FROM
		mhe_das_rtn_box_rslt
	WHERE
		batch_no = 'M-S-U-1'
	GROUP BY
		batch_no,
		item_cd) AS AB ON
	mpr.batch_no = AB.batch_no
	AND mpr.sku_cd = AB.item_cd
WHERE
	mpr.batch_no = 'M-S-U-1'
	AND mpr.qty <> AB.qty
ORDER BY
	mpr.chute_no;
