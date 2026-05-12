package com.nslindia.procurezone.employee.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing employee.
 */
public record UpdateEmployeeRequest(
                @Size(max = 100, message = "Full name must not exceed 100 characters") String fullName,

                @Email(message = "Email must be valid") @Size(max = 100, message = "Email must not exceed 100 characters") String email,

                LocalDate joinDate,

                @Size(max = 100, message = "Designation must not exceed 100 characters") String designation,

                @Size(max = 500, message = "Cost center must not exceed 500 characters") String costCenter,

                Integer departmentId,

                Integer locationId,

                Integer companyId,

                @Size(max = 100, message = "Plant must not exceed 100 characters") String plant) {
}
