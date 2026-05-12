package com.nslindia.procurezone.masterdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing Material.
 * Uses Java 21 record with validation annotations.
 */
public record UpdateMaterialRequest(
        @JsonProperty("code") @NotBlank(message = "Material code is required") @Size(min = 2, max = 100, message = "Material code must be between 2 and 100 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Material code must contain only uppercase letters, numbers, underscores, and hyphens") String code,

        @JsonProperty("name") @NotBlank(message = "Material name is required") @Size(min = 2, max = 100, message = "Material name must be between 2 and 100 characters") String name,

        @JsonProperty("description") @Size(max = 5000, message = "Material description must not exceed 5000 characters") String description,

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
