INSERT INTO JOB_INSTANCES (
       ID,
       BATCH_ID,
       JOB_DATE,
       JOB_SEQ,
       JOB_TYPE,
       COM_CD,
       EQUIP_TYPE,
       EQUIP_CD,
       EQUIP_NM,
       SUB_EQUIP_CD,
       SHOP_CD,
       SHOP_NM,
       ORDER_NO,
       SKU_CD,
       SKU_NM,
       BOX_TYPE_CD,
       INVOICE_ID,
       BOX_IN_QTY,
       PICK_QTY,
       PICKING_QTY,
       PICKED_QTY,
       ORDER_TYPE,
       DOMAIN_ID,
       STAGE_CD,
       STATUS,
       CREATOR_ID,
       UPDATER_ID,
       CREATED_AT,
       UPDATED_AT
)
SELECT
	F_GET_GENERATE_UUID() ID,
	BATCH_ID,
	JOB_DATE,
	JOB_SEQ,
	JOB_TYPE,
	COM_CD,
	EQUIP_TYPE,
	:equipCd,
	:equipNm,
	:subEquipCd,
	SHOP_CD,
	:shopNm,
	ORDER_NO,
	SKU_CD,
	SKU_NM,
	BOX_TYPE_CD,
	INVOICE_ID,
	BOX_IN_QTY,
	ORDER_QTY AS PICK_QTY,
	0 AS PICKING_QTY,
	0 AS PICKED_QTY,
	ORDER_TYPE,
	DOMAIN_ID,
	STAGE_CD,
	'W',
	'system',
	'system',
	SYSDATE,
	SYSDATE
FROM
	ORDERS
WHERE
	DOMAIN_ID = :domainId
AND 
	BATCH_ID  = :batchId
AND 
	SHOP_CD  = :shopCd