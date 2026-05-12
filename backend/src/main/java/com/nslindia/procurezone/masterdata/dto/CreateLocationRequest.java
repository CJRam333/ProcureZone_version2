package com.nslindia.procurezone.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new location.
 */
public record CreateLocationRequest(
        @NotBlank(message = "Location code is required") @Size(max = 20, message = "Location code must not exceed 20 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Location code must contain only uppercase letters, numbers, hyphens, and underscores") String code,

        @NotBlank(message = "Location name is required") @Size(max = 100, message = "Location name must not exceed 100 characters") String name,

        @NotNull(message = "Status is required") Integer status) {
}
