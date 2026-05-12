package com.nslindia.procurezone.sap.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking SAP import job history
 * Records each import execution with stats and status
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "sap_import_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer id;

    @Column(name = "import_type", length = 50, nullable = false)
    private String importType; // SCHEDULED, MANUAL, API

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "records_inserted")
    private Integer recordsInserted;

    @Column(name = "records_updated")
    private Integer recordsUpdated;

    @Column(name = "records_failed")
    private Integer recordsFailed;

    @Column(name = "status", length = 20)
    private String status; // STARTED, COMPLETED, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy; // username or SYSTEM

    @Column(name = "plant_id")
    private Integer plantId;

    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "STARTED";
        }
    }

    public void markCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.durationSeconds = java.time.Duration.between(startedAt, completedAt).getSeconds();
        }
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.durationSeconds = java.time.Duration.between(startedAt, completedAt).getSeconds();
        }
    }
}
