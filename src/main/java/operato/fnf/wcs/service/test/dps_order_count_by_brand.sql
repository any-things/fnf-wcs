-- 하나의 오더에 여러개 브랜드가 들어있는 오더 조회.

SELECT
	*
FROM
	(
	SELECT
		ref_no,
		COUNT(strr_id) AS brand_cnt
	FROM
		(
		SELECT
			DISTINCT ref_no,
			strr_id
		FROM
			DPS_JOB_INSTANCES
		WHERE
			work_date = '20200729'
			AND STATUS = 'E') aa
	GROUP BY
		ref_no) bb
WHERE
	brand_cnt > 1;