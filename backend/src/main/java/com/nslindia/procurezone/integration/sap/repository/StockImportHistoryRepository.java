package com.nslindia.procurezone.integration.sap.repository;

import com.nslindia.procurezone.integration.sap.entity.StockImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StockImportHistoryRepository extends JpaRepository<StockImportHistory, Integer> {

    Optional<StockImportHistory> findByFileName(String fileName);

    boolean existsByFileNameAndStatus(String fileName, String status);

    /** Latest successful import time — the "last stock import" marker for reconciliation. */
    @Query("SELECT MAX(h.importedAt) FROM StockImportHistory h WHERE h.status = 'SUCCESS'")
    Optional<LocalDateTime> findLastSuccessfulImportAt();
}
