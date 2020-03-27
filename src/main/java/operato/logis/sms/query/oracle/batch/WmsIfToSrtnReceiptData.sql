SELECT 
	HR.SORT_DATE
	, HR.SORT_SEQ  AS JOB_SEQ
	, HR.MHE_NO || '-' || HR.SORT_DATE || '-' || HR.SORT_SEQ AS WMS_BATCH_NO
	, 'WCS-' || HR.MHE_NO || '-' || HR.SORT_DATE || '-' || HR.SORT_SEQ AS WCS_BATCH_NO
	, 'FnF' AS COM_CD
	, 'A' AS AREA_CD
	, 'AB1' AS STAGE_CD
	, 'SRTN' AS JOB_TYPE
	, 'SORTER' AS EQUIP_TYPE
	, HR.MHE_NO AS EQUIP_CD
	, COUNT(DISTINCT ITEM_CD) AS TOTAL_ORDERS
	, 0 AS TOTAL_PCS
	, SUM(1) AS TOTAL_RECORDS
	, 'order' AS ITEM_TYPE
	, 'W' AS STATUS
	, 0 AS SKIP_FLAG
FROM 
	RTN_SORT_HR HR
LEFT OUTER JOIN 
	RTN_SORT_DR DR
ON
	HR.WH_CD = DR.WH_CD
AND
	HR.MHE_NO = DR.MHE_NO
AND
	HR.SORT_DATE = DR.SORT_DATE
AND
	HR.SORT_SEQ = DR.SORT_SEQ
WHERE 
	HR.STATUS = :status
AND
	HR.WH_CD = :whCd
GROUP BY 
	HR.SORT_DATE, HR.SORT_SEQ, HR.MHE_NO