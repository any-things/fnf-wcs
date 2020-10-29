SELECT 
	ORD.SKU_CD
	, ORD.SKU_BARCD
	, ORD.SKU_BARCD2
	, OPRE.chute_no as SUB_EQUIP_CD 
	, OPRE.cell_no as CLASS_CD 
FROM 
	ORDERS ORD
LEFT OUTER JOIN
	(select * from mhe_das_order where batch_no = :batchGroupId) OPRE
ON
	ORD.SKU_CD = OPRE.ITEM_CD
WHERE 
	ORD.BATCH_ID = :batchId 
AND 
	ORD.SKU_CD IN ( :skuCd )