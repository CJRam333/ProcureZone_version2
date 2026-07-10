package com.nslindia.procurezone.integration.sap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row per SAP stock CSV file the event-driven importer has processed, keyed by unique
 * file_name. Enables exactly-once import (idempotency) and — via MAX(imported_at) WHERE
 * status='SUCCESS' — the "last stock import" marker for reconciliation.
 */
@Entity
@Table(name = "tbl_stock_import_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockImportHistory {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    /** Some rows imported, some failed (e.g. pre-existing duplicates). Counts as processed. */
    public static final String STATUS_PARTIAL = "PARTIAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "file_name", nullable = false, unique = true, length = 255)
    private String fileName;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    @Column(name = "rows_updated")
    private Integer rowsUpdated;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "message", length = 500)
    private String message;
}
