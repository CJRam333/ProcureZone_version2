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

/**
 * Repository for GRN QC Results operations.
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface GrnQcResultRepository extends JpaRepository<GrnQcResult, Integer> {

    /**
     * Find all QC results for a specific GRN
     */
    List<GrnQcResult> findByGrnIdOrderByInspectionDateDesc(Integer grnId);

    /**
     * Find the latest QC result for a GRN
     */
    Optional<GrnQcResult> findFirstByGrnIdOrderByInspectionDateDesc(Integer grnId);

    /**
     * Find QC results by status
     */
    Page<GrnQcResult> findByQcStatus(Integer qcStatus, Pageable pageable);

    /**
     * Find QC results by inspector
     */
    Page<GrnQcResult> findByInspectorId(Integer inspectorId, Pageable pageable);

    /**
     * Find QC results requiring re-inspection
     */
    @Query("SELECT q FROM GrnQcResult q WHERE q.reInspectionRequired = true " +
            "AND (q.reInspectionDate IS NULL OR q.reInspectionDate <= :date)")
    List<GrnQcResult> findPendingReInspections(@Param("date") LocalDateTime date);

    /**
     * Find failed QC results for a date range
     */
    @Query("SELECT q FROM GrnQcResult q WHERE q.qcStatus = 3 " +
            "AND q.inspectionDate BETWEEN :startDate AND :endDate")
    List<GrnQcResult> findFailedInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count by status for dashboard
     */
    long countByQcStatus(Integer qcStatus);

    /**
     * Check if GRN has passed QC
     */
    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END " +
            "FROM GrnQcResult q WHERE q.grnId = :grnId AND q.qcStatus = 2")
    boolean hasPassedQc(@Param("grnId") Integer grnId);

    /**
     * Check if GRN has any QC result
     */
    boolean existsByGrnId(Integer grnId);

    /**
     * Find QC results by GRN IDs (for bulk lookup)
     */
    @Query("SELECT q FROM GrnQcResult q WHERE q.grnId IN :grnIds " +
            "ORDER BY q.grnId, q.inspectionDate DESC")
    List<GrnQcResult> findByGrnIdIn(@Param("grnIds") List<Integer> grnIds);
}
