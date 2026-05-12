package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new company-location mapping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyLocationRequest {

    @NotNull(message = "Company ID is required")
    private Integer companyId;

    @NotNull(message = "Location ID is required")
    private Integer locationId;

    private Integer status = 1; // Default to active
}
