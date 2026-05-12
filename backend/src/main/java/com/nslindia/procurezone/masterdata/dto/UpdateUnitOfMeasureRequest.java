package com.nslindia.procurezone.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing Unit of Measure
 * 
 * @param code   Unique unit code (uppercase, alphanumeric, max 20 chars)
 * @param name   Unit name (max 100 chars)
 * @param status Status (1=Active, 0=Inactive)
 */
public record UpdateUnitOfMeasureRequest(
        @NotBlank(message = "Unit code is required") @Size(max = 20, message = "Unit code must not exceed 20 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Unit code must be uppercase alphanumeric with hyphens/underscores only") String code,

        @NotBlank(message = "Unit name is required") @Size(max = 100, message = "Unit name must not exceed 100 characters") String name,

        @NotNull(message = "Status is required") Integer status) {

    /**
     * Validate status value
     */
    public boolean isValidStatus() {
        return status != null && (status == 0 || status == 1);
    }

    /**
     * Trim and normalize fields
     */
    public UpdateUnitOfMeasureRequest normalized() {
        return new UpdateUnitOfMeasureRequest(
                code != null ? code.trim().toUpperCase() : null,
                name != null ? name.trim() : null,
                status);
    }
}
