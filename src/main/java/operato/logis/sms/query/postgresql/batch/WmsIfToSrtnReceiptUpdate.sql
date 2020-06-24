UPDATE WCS_RTN_CHASU
SET
	STATUS = :status
WHERE 
	STRR_ID =:strrId
AND
  	SEASON = :season
AND
  	TYPE = :type
AND
  	seq = :seq