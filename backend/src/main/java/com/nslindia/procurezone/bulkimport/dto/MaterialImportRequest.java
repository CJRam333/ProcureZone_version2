package com.nslindia.procurezone.bulkimport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO representing a single material record for bulk import.
 * Used for parsing CSV rows.
 */
public record MaterialImportRequest(
        @NotBlank(message = "Material code is required") @Size(max = 100, message = "Material code cannot exceed 100 characters") String code,

        @NotBlank(message = "Material name is required") @Size(max = 100, message = "Material name cannot exceed 100 characters") String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters") String description,

        @NotNull(message = "Status is required") Integer status) {

    /**
     * Validates if status is valid (0 or 1).
     */
    public boolean isValidStatus() {
        return status != null && (status == 0 || status == 1);
    }

    /**
     * Sanitizes the input by trimming whitespace.
     */
    public MaterialImportRequest sanitized() {
        return new MaterialImportRequest(
                code != null ? code.trim() : null,
                name != null ? name.trim() : null,
                description != null ? description.trim() : null,
                status);
    }
}
