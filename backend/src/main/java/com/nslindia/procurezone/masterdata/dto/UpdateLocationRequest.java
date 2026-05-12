package com.nslindia.procurezone.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing location.
 */
public record UpdateLocationRequest(
        @NotBlank(message = "Location name is required") @Size(max = 100, message = "Location name must not exceed 100 characters") String name,

        @NotNull(message = "Status is required") Integer status) {
}
