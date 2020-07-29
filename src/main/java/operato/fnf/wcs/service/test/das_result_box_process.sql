-- DAS주문상태가 WAIT인데 실적이 올라온 케이스 조회.

SELECT
	DISTINCT work_unit
FROM
	mhe_box
WHERE
	work_date = '20200729'
	AND work_unit IN (
	SELECT
		wms_batch_no
	FROM
		job_batches
	WHERE
		job_date = '2020-07-29'
		AND status = 'WAIT');
