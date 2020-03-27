#if($jobType == 'RTN')
SELECT :comCd AS COM_CD
     , :areaCd AS AREA_CD
     , :stageCd AS STAGE_CD
     , :jobType AS JOB_TYPE
     , :jobSeq AS JOB_SEQ
     , COUNT(1) AS TOTAL_RECORDS
     , COUNT(1) AS TOTAL_ORDERS
     , SUM(INVN_QTY) AS TOTAL_PCS
     , 'order' AS ITEM_TYPE
     , 'W' AS STATUS
     , 'Rack' AS EQUIP_TYPE
     , 0 AS SKIP_FLAG
  FROM MHE_RTN_INVN
 WHERE WH_CD = :whCd
#elseif($jobType == 'DAS')
SELECT 1
  FROM MHE_RTN_INVN
 WHERE WH_CD = 'XXXX'
 #end


