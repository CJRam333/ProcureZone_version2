package com.nslindia.procurezone.masterdata.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing Section.
 */
public record UpdateSectionRequest(
        @Size(max = 100, message = "Section code must be at most 100 characters") String code,

        @Size(max = 100, message = "Section name must be at most 100 characters") String name,

        Integer status) {

    /**
     * Validates that the status is either 0 (inactive) or 1 (active) if provided.
     */
    public boolean isValidStatus() {
        return status == null || status == 0 || status == 1;
    }
}
