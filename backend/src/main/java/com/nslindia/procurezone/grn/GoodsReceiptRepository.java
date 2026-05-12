package com.nslindia.procurezone.grn;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Integer> {

    Optional<GoodsReceipt> findByGrnNumber(String grnNumber);

    Page<GoodsReceipt> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT g FROM GoodsReceipt g WHERE g.status IN :statuses")
    Page<GoodsReceipt> findByStatusIn(@Param("statuses") List<Integer> statuses, Pageable pageable);

    @Query("SELECT g FROM GoodsReceipt g WHERE g.indentId = :indentId ORDER BY g.receiptDate DESC")
    List<GoodsReceipt> findByIndentId(@Param("indentId") Integer indentId);

    @Query("SELECT g FROM GoodsReceipt g WHERE g.vendorName LIKE %:vendorName% ORDER BY g.receiptDate DESC")
    Page<GoodsReceipt> findByVendorNameContaining(@Param("vendorName") String vendorName, Pageable pageable);

    @Query("SELECT g FROM GoodsReceipt g WHERE g.grnNumber LIKE %:search% OR g.vendorName LIKE %:search% ORDER BY g.receiptDate DESC")
    Page<GoodsReceipt> searchGRNs(@Param("search") String search, Pageable pageable);

    @Query("SELECT g FROM GoodsReceipt g WHERE g.receiptDate BETWEEN :startDate AND :endDate ORDER BY g.receiptDate DESC")
    Page<GoodsReceipt> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.status = :status")
    Long countByStatus(@Param("status") Integer status);

    @Query("SELECT SUM(g.amount) FROM GoodsReceipt g WHERE g.status IN (4, 5)") // Final Approved or Stored
    Double getTotalGRNValue();

    @Query("SELECT COUNT(g) FROM GoodsReceipt g WHERE g.receiptDate BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(g) FROM GoodsReceipt g")
    Long countAll();

    boolean existsByGrnNumber(String grnNumber);
}
