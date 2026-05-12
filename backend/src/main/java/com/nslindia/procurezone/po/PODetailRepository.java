package com.nslindia.procurezone.po;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Purchase Order Detail operations
 */
@Repository
public interface PODetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {

    /**
     * Find all details by PO ID
     */
    List<PurchaseOrderDetail> findByPurchaseOrderId(Integer poId);

    /**
     * Find detail by PO ID and line number
     */
    PurchaseOrderDetail findByPurchaseOrderIdAndLineNumber(Integer poId, Integer lineNumber);

    /**
     * Find details by material ID
     */
    List<PurchaseOrderDetail> findByMaterialId(Integer materialId);

    /**
     * Find details by indent detail ID
     */
    List<PurchaseOrderDetail> findByIndentDetailId(Integer indentDetailId);

    /**
     * Find pending delivery items
     */
    @Query("SELECT pod FROM PurchaseOrderDetail pod WHERE " +
            "pod.purchaseOrder.id = :poId AND pod.deliveryStatus IN (1, 2)")
    List<PurchaseOrderDetail> findPendingDeliveryItems(@Param("poId") Integer poId);

    /**
     * Check if all items in PO are fully delivered
     */
    @Query("SELECT CASE WHEN COUNT(pod) = 0 THEN true ELSE false END " +
            "FROM PurchaseOrderDetail pod WHERE " +
            "pod.purchaseOrder.id = :poId AND pod.deliveryStatus != 3")
    boolean areAllItemsFullyDelivered(@Param("poId") Integer poId);
}
