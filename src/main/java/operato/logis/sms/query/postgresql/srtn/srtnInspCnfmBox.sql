SELECT 
	B.REF_NO
	, B.ITEM_CD
	, B.INB_CMPT_QTY
	, B.SUPPR_ID
	, B.SUPPR_NM
	, B.inb__date
	, B.STRR_ID
	, B.interface_crt_dt
	, B.interface_no
FROM (
	SELECT 
		REF_NO
		, ITEM_CD
		, max(INTERFACE_NO) AS INTERFACE_NO 
	FROM 
		WMT_UIF_WCS_INB_RTN_CNFM
	WHERE 
		STRR_ID = :strrId 
	AND 
		REF_SEASON = :season 
	AND 
		SHOP_RTN_TYPE = :rtnType 
	AND 
		SHOP_RTN_SEQ = :jobSeq
	AND 
		WCS_IF_CHK = :wcsIfChk 
	AND
		length(ref_no) = 8
	GROUP BY 
		REF_NO, ITEM_CD ) A
LEFT OUTER JOIN 
	WMT_UIF_WCS_INB_RTN_CNFM B
ON
	A.REF_NO = B.REF_NO 
AND 
	A.ITEM_CD = B.ITEM_CD 
AND 
	A.INTERFACE_NO = B.INTERFACE_NO
AND 
	B.STRR_ID = :strrId
AND 
	B.REF_SEASON = :season 
AND 
	B.SHOP_RTN_TYPE = :rtnType 
AND 
	B.SHOP_RTN_SEQ = :jobSeq
AND 
	B.WCS_IF_CHK = :wcsIfChk 
AND
	length(B.ref_no) = 8
ORDER BY 
	B.REF_NO, B.ITEM_CD