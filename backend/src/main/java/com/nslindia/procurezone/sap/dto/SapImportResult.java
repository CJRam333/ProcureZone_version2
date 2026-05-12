package com.nslindia.procurezone.sap.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for SAP Import Results
 * 
 * @author NSL India
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapImportResult {
    private boolean success;
    private String message;
    private Integer recordsProcessed;
    private Integer recordsInserted;
    private Integer recordsUpdated;
    private Integer recordsFailed;
    private String fileName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    private String errorDetails;
    private Integer logId;

    public static SapImportResult success(int processed, int inserted, String fileName) {
        return SapImportResult.builder()
                .success(true)
                .message("Import completed successfully")
                .recordsProcessed(processed)
                .recordsInserted(inserted)
                .recordsUpdated(0)
                .recordsFailed(0)
                .fileName(fileName)
                .build();
    }

    public static SapImportResult failure(String errorMessage, int recordsFailed) {
        return SapImportResult.builder()
                .success(false)
                .message("Import failed")
                .errorDetails(errorMessage)
                .recordsFailed(recordsFailed)
                .build();
    }
}
