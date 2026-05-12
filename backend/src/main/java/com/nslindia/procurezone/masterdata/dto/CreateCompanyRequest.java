package com.nslindia.procurezone.masterdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Company.
 * Uses Java 21 record with validation annotations.
 */
public record CreateCompanyRequest(
        @JsonProperty("code") @NotBlank(message = "Company code is required") @Size(min = 2, max = 100, message = "Company code must be between 2 and 100 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Company code must contain only uppercase letters, numbers, underscores, and hyphens") String code,

        @JsonProperty("name") @NotBlank(message = "Company name is required") @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters") String name,

        @JsonProperty("status") @NotNull(message = "Status is required") Integer status) {

    /**
     * Validates that status is either 0 (inactive) or 1 (active).
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValidStatus() {
        return status != null && (status == 0 || status == 1);
    }
}
