package com.nslindia.procurezone.po;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for Purchase Order operations
 */
@Repository
public interface PORepository extends JpaRepository<PurchaseOrder, Integer> {

    /**
     * Find PO by unique PO number
     */
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    /**
     * Check if PO number exists
     */
    boolean existsByPoNumber(String poNumber);

    /**
     * Find POs by status with pagination
     */
    Page<PurchaseOrder> findByPoStatus(Integer status, Pageable pageable);

    /**
     * Find POs by indent ID
     */
    List<PurchaseOrder> findByIndentId(Integer indentId);

    /**
     * Find POs by vendor with pagination
     */
    Page<PurchaseOrder> findByVendorId(Integer vendorId, Pageable pageable);

    /**
     * Find POs by department with pagination
     */
    Page<PurchaseOrder> findByDepartmentId(Integer departmentId, Pageable pageable);

    /**
     * Search POs by PO number or vendor name
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "po.vendorId IN (SELECT v.id FROM Vendor v WHERE LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<PurchaseOrder> searchPOs(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find POs pending for approval (status = SUBMITTED)
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.poStatus = 2 ORDER BY po.createdDate ASC")
    Page<PurchaseOrder> findPendingForApproval(Pageable pageable);

    /**
     * Find overdue POs (expected delivery date passed but not fully received)
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "po.expectedDeliveryDate < :currentDate AND " +
            "po.poStatus IN (4, 5) ORDER BY po.expectedDeliveryDate ASC")
    Page<PurchaseOrder> findOverduePOs(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    /**
     * Find POs by status and date range
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "po.poStatus = :status AND " +
            "po.poDate BETWEEN :startDate AND :endDate " +
            "ORDER BY po.poDate DESC")
    Page<PurchaseOrder> findByStatusAndDateRange(
            @Param("status") Integer status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Find POs by vendor and status
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "po.vendorId = :vendorId AND po.poStatus = :status " +
            "ORDER BY po.poDate DESC")
    Page<PurchaseOrder> findByVendorAndStatus(
            @Param("vendorId") Integer vendorId,
            @Param("status") Integer status,
            Pageable pageable);

    /**
     * Get PO count by status
     */
    @Query("SELECT po.poStatus AS status, COUNT(po) AS count " +
            "FROM PurchaseOrder po " +
            "GROUP BY po.poStatus")
    List<Map<String, Object>> getPOCountByStatus();

    /**
     * Get total PO value by status
     */
    @Query("SELECT po.poStatus AS status, SUM(po.netAmount) AS totalValue " +
            "FROM PurchaseOrder po " +
            "GROUP BY po.poStatus")
    List<Map<String, Object>> getPOValueByStatus();

    /**
     * Find POs created by user
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.createdBy = :employeeNumber " +
            "ORDER BY po.createdDate DESC")
    Page<PurchaseOrder> findByCreatedBy(@Param("employeeNumber") Integer employeeNumber, Pageable pageable);

    /**
     * Find POs approved by user
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.approvedBy = :employeeNumber " +
            "ORDER BY po.approvedDate DESC")
    Page<PurchaseOrder> findByApprovedBy(@Param("employeeNumber") Integer employeeNumber, Pageable pageable);

    /**
     * Count POs by status
     */
    long countByPoStatus(Integer status);

    /**
     * Find POs with pending deliveries
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.poStatus IN (4, 5) " +
            "ORDER BY po.expectedDeliveryDate ASC")
    Page<PurchaseOrder> findPendingDeliveries(Pageable pageable);
}
