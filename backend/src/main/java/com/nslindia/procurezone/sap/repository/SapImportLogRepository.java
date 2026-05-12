package com.nslindia.procurezone.sap.repository;

import com.nslindia.procurezone.sap.entity.SapImportLog;
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
 * Repository for SAP Import Log
 * Tracks import job history and statistics
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface SapImportLogRepository extends JpaRepository<SapImportLog, Integer> {

    /**
     * Find logs by status
     */
    List<SapImportLog> findByStatus(String status);

    /**
     * Find logs by import type
     */
    List<SapImportLog> findByImportType(String importType);

    /**
     * Find latest import log
     */
    Optional<SapImportLog> findFirstByOrderByStartedAtDesc();

    /**
     * Find latest successful import
     */
    Optional<SapImportLog> findFirstByStatusOrderByStartedAtDesc(String status);

    /**
     * Find logs within date range
     */
    @Query("SELECT l FROM SapImportLog l WHERE l.startedAt BETWEEN :startDate AND :endDate ORDER BY l.startedAt DESC")
    List<SapImportLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find logs by plant
     */
    List<SapImportLog> findByPlantIdOrderByStartedAtDesc(Integer plantId);

    /**
     * Find logs by triggered user
     */
    List<SapImportLog> findByTriggeredByOrderByStartedAtDesc(String triggeredBy);

    /**
     * Get paginated import history
     */
    Page<SapImportLog> findAllByOrderByStartedAtDesc(Pageable pageable);

    /**
     * Count imports by status
     */
    long countByStatus(String status);

    /**
     * Count imports today
     */
    @Query("SELECT COUNT(l) FROM SapImportLog l WHERE l.startedAt >= :startOfDay")
    long countImportsToday(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Get total records imported today
     */
    @Query("SELECT COALESCE(SUM(l.recordsInserted), 0) FROM SapImportLog l " +
            "WHERE l.status = 'COMPLETED' AND l.startedAt >= :startOfDay")
    long getTotalRecordsImportedToday(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Check if any import is currently running
     */
    @Query("SELECT COUNT(l) > 0 FROM SapImportLog l WHERE l.status = 'STARTED'")
    boolean isImportInProgress();

    /**
     * Get any currently running import
     */
    Optional<SapImportLog> findFirstByStatus(String status);
}
