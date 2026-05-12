package com.nslindia.procurezone.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Section.
 */
public record CreateSectionRequest(
        @NotBlank(message = "Section code is required") @Size(max = 100, message = "Section code must be at most 100 characters") String code,

        @NotBlank(message = "Section name is required") @Size(max = 100, message = "Section name must be at most 100 characters") String name,

        Integer status) {

    /**
     * Returns the status value, defaulting to 1 (active) if not provided.
     */
    public Integer status() {
        return status != null ? status : 1;
    }

    /**
     * Validates that the status is either 0 (inactive) or 1 (active).
     */
    public boolean isValidStatus() {
        int statusValue = status();
        return statusValue == 0 || statusValue == 1;
    }
}
