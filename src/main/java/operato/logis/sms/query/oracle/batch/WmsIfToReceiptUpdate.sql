UPDATE MHE_HR
SET
	MHE_NO = :mheNo
	, STATUS = :status
	, RCV_DATETIME = :rcvDatetime
WHERE 
	WH_CD =:whCd
AND
  	WORK_UNIT = :workUnit