package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new company-department mapping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyDepartmentRequest {

    @NotNull(message = "Company ID is required")
    private Integer companyId;

    @NotNull(message = "Department ID is required")
    private Integer departmentId;

    private Integer status = 1; // Default to active
}
