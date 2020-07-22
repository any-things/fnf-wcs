WITH T_CHECK_ORDER AS (
    SELECT 
        WORK_UNIT AS BATCH_ID, REF_NO AS ORDER_NO, 'FnF' AS COM_CD, ITEM_CD AS SKU_CD, MAX(ITEM_NM) AS SKU_NM, SUM(PICK_QTY) AS ORDER_QTY
    FROM 
        MHE_DR
    WHERE
        WORK_UNIT = :batchId
        AND REF_NO = :orderNo
        AND (DPS_ASSIGN_YN IS NULL OR DPS_ASSIGN_YN = 'N')
    GROUP BY
        WORK_UNIT, REF_NO, ITEM_CD
),
T_STOCKS AS (
    SELECT 
        X.BATCH_ID, X.ORDER_NO, X.COM_CD, X.SKU_CD, X.SKU_NM, X.ORDER_QTY, Y.CELL_CD, Y.STOCK_ID, COALESCE(Y.LOAD_QTY, 0) AS LOAD_QTY
    FROM 
        T_CHECK_ORDER X LEFT OUTER JOIN 
        (
            SELECT 
                Y.CELL_CD, Y.IND_CD, Z.ID AS STOCK_ID, Z.COM_CD, Z.SKU_CD, Z.LOAD_QTY
            FROM 
                CELLS Y LEFT OUTER JOIN STOCKS Z 
                ON Y.DOMAIN_ID = Z.DOMAIN_ID AND Y.EQUIP_TYPE = Z.EQUIP_TYPE AND Y.EQUIP_CD = Z.EQUIP_CD AND Y.CELL_CD = Z.CELL_CD AND Y.ACTIVE_FLAG = Z.ACTIVE_FLAG
            WHERE
                Y.DOMAIN_ID = :domainId 
                AND Y.EQUIP_TYPE = :equipType
                AND Y.EQUIP_CD = :equipCd
                AND Y.ACTIVE_FLAG = true
                AND Z.LOAD_QTY > 0
        ) Y ON X.COM_CD = Y.COM_CD AND X.SKU_CD = Y.SKU_CD
),
T_ASSIGN_CHECK AS (
    SELECT SUM(V.CHECK_STOCK) AS CHECK_ASSIGNABLE
        FROM (
            SELECT 
                CASE WHEN SUM(LOAD_QTY) - MAX(ORDER_QTY) < 0 THEN 1
                     ELSE 0
                END AS CHECK_STOCK
            FROM 
                T_STOCKS
            GROUP BY 
                COM_CD, SKU_CD
        ) V
),
T_ORDER_STOCKS AS (
    SELECT 
        X.*, 
        ROW_NUMBER() OVER (PARTITION BY COM_CD, SKU_CD ORDER BY LOAD_SUM_BY_CELL) AS CHECK_ROW, 
        SUM(CASE WHEN LOAD_SUM_BY_CELL < ORDER_QTY THEN 0
                 ELSE 1
            END) OVER (PARTITION BY COM_CD, SKU_CD ORDER BY LOAD_SUM_BY_CELL) AS ROW_POINT
    FROM (
        SELECT 
            X.*, 
            SUM(LOAD_QTY) OVER (PARTITION BY COM_CD, SKU_CD ORDER BY LOAD_QTY ASC, CELL_CD) AS LOAD_SUM_BY_CELL
        FROM 
            T_STOCKS X
    ) X
)
SELECT 
    A.*
FROM (
    SELECT 
        X.*, Z.*, ROW_NUMBER() OVER (PARTITION BY X.COM_CD, X.SKU_CD ORDER BY X.LOAD_SUM_BY_CELL) AS RANKING
    FROM 
        T_ORDER_STOCKS X, T_ASSIGN_CHECK Z
    WHERE 
        EXISTS (SELECT 
                    1 
                FROM 
                    T_ORDER_STOCKS Y
                WHERE 
                    Y.ROW_POINT = 1
                    AND Y.COM_CD = X.COM_CD
                    AND Y.SKU_CD = X.SKU_CD 
                    AND Y.CHECK_ROW >= X.CHECK_ROW)
) A
ORDER BY 
    A.COM_CD, A.SKU_CD, A.RANKING