package com.nslindia.procurezone.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new plant.
 */
public record CreatePlantRequest(
        @NotBlank(message = "Plant code is required") @Size(max = 20, message = "Plant code must not exceed 20 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Plant code must contain only uppercase letters, numbers, hyphens, and underscores") String code,

        @NotBlank(message = "Plant name is required") @Size(max = 100, message = "Plant name must not exceed 100 characters") String name,

        @NotNull(message = "Status is required") Integer status) {
}
