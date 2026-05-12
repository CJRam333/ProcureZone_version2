package com.nslindia.procurezone.integration.sap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of a material CSV import operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialImportResult {

    private LocalDateTime importDate;
    private String filename;
    private int totalRows;
    private int successfulInserts;
    private int successfulUpdates;
    private int failures;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    private boolean success;
    private String message;

    /**
     * Add an error message
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.failures++;
    }

    /**
     * Add a warning message
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * Get summary message
     */
    public String getSummary() {
        return String.format("Import completed: %d total rows, %d inserted, %d updated, %d failed",
                totalRows, successfulInserts, successfulUpdates, failures);
    }
}
