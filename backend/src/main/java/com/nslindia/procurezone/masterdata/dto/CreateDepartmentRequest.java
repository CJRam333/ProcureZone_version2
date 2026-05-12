package com.nslindia.procurezone.masterdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Department.
 * Uses Java 21 record with validation annotations.
 */
public record CreateDepartmentRequest(
        @JsonProperty("code") @NotBlank(message = "Department code is required") @Size(min = 2, max = 100, message = "Department code must be between 2 and 100 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Department code must contain only uppercase letters, numbers, underscores, and hyphens") String code,

        @JsonProperty("name") @NotBlank(message = "Department name is required") @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters") String name,

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
