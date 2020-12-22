SELECT
    JOB.DOMAIN_ID,
    JOB.ID,
    JOB.BATCH_ID,
    JOB.STAGE_CD,
    JOB.COM_CD,
    JOB.JOB_TYPE,
    JOB.INPUT_SEQ,
    JOB.EQUIP_TYPE,
    JOB.EQUIP_CD,
    JOB.EQUIP_NM,
    JOB.SUB_EQUIP_CD,
    COALESCE(JOB.COLOR_CD, 'R') AS COLOR_CD,
    COALESCE(JOB.PICK_QTY, 0) AS PICK_QTY,
    COALESCE(JOB.PICKING_QTY, 0) AS PICKING_QTY,
    COALESCE(JOB.PICKED_QTY, 0) AS PICKED_QTY,
    JOB.BOX_IN_QTY,
    CELL.SIDE_CD,
    CELL.STATION_CD,
    JOB.ORDER_NO,
    JOB.SKU_CD,
    SKU.SKU_BARCD,
    JOB.SKU_NM,
    JOB.STATUS,
    JOB.BOX_ID,
    JOB.BOX_TYPE_CD,
    JOB.INVOICE_ID,
    JOB.BOX_PACK_ID,
    JOB.ORDER_TYPE,
    JOB.CLASS_CD,
    JOB.BOX_CLASS_CD,
    JOB.PICK_STARTED_AT,
    JOB.PICK_ENDED_AT
FROM
	JOB_INSTANCES JOB
		INNER JOIN CELLS CELL 
			ON JOB.DOMAIN_ID = CELL.DOMAIN_ID AND JOB.EQUIP_TYPE = CELL.EQUIP_TYPE AND JOB.EQUIP_CD = CELL.EQUIP_CD AND JOB.SUB_EQUIP_CD = CELL.CELL_CD
		INNER JOIN SKU SKU 
			ON JOB.COM_CD = SKU.COM_CD AND SKU.SKU_CD = JOB.SKU_CD
WHERE
    JOB.DOMAIN_ID = :domainId
    #if($id)
    AND JOB.ID = :id
    #end
    #if($batchId)
    AND JOB.BATCH_ID = :batchId
    #end
    #if($inputSeq)
    AND JOB.INPUT_SEQ = :inputSeq
    #end
    #if($inputSeqIsBlank)
    AND (JOB.INPUT_SEQ IS NULL OR JOB.INPUT_SEQ = 0)
    #end
    #if($latestRunning)
    AND JOB.INPUT_SEQ = (SELECT MAX(INPUT_SEQ) AS INPUT_SEQ FROM JOB_INPUTS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND STATUS = :inputRunningStatus)
    #end
    #if($comCd)
    AND JOB.COM_CD = :comCd
    #end
    #if($status)
    AND JOB.STATUS = :status
    #end
    #if($statuses)
    AND JOB.STATUS IN (:statuses)
    #end
    #if($classCd)
    AND JOB.CLASS_CD = :classCd
    #end
    #if($boxClassCd)
    AND JOB.BOX_CLASS_CD = :boxClassCd
    #end
    #if($boxId)
    AND JOB.BOX_ID = :boxId
    #end
    #if($orderNo)
    AND JOB.ORDER_NO = :orderNo
    #end
    #if($skuCd)
    AND JOB.SKU_CD = :skuCd
    #end
    #if($equipType)
    AND CELL.EQUIP_TYPE = :equipType
    #end
    #if($equipCd)
    AND CELL.EQUIP_CD = :equipCd
    #end
    #if($excludeCellCd)
    AND CELL.CELL_CD <> :excludeCellCd
    #end
    #if($stationCd)
    AND CELL.STATION_CD = :stationCd
    #end
    #if($equipZoneCd)
    AND CELL.EQUIP_ZONE_CD = :equipZoneCd
    #end
    #if($sideCd)
    AND CELL.SIDE_CD = :sideCd
    #end
ORDER BY
    CELL.STATION_CD ASC, JOB.SUB_EQUIP_CD ASC
#if($onlyOne)
LIMIT 1
#end