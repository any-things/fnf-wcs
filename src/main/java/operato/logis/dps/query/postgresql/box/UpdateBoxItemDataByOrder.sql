UPDATE BOX_ITEMS X
   SET X.STATUS = :status
     , X.PICKED_QTY = (SELECT Y.PICKED_QTY FROM ORDERS Y 
                        WHERE Y.DOMAIN_ID = X.DOMAIN_ID
                          AND Y.ID = X.ORDER_ID)
     #if($updatePassFlag)
     , X.PASS_FLAG = CASE WHEN (:status = 'B') THEN true ELSE false END
     #end
 WHERE X.DOMAIN_ID = :domainId
   AND X.BOX_PACK_ID = :boxPackId
   AND X.ORDER_ID in (:orderIds)
 