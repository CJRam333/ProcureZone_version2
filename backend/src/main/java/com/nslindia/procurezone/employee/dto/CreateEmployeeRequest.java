package com.nslindia.procurezone.employee.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new employee.
 */
public record CreateEmployeeRequest(
        @NotBlank(message = "Employee ID is required") @Size(max = 100, message = "Employee ID must not exceed 100 characters") String employeeId,

        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name must not exceed 100 characters") String fullName,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 100, message = "Email must not exceed 100 characters") String email,

        @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password,

        @NotNull(message = "Join date is required") LocalDate joinDate,

        @NotBlank(message = "Designation is required") @Size(max = 100, message = "Designation must not exceed 100 characters") String designation,

        @Size(max = 500, message = "Cost center must not exceed 500 characters") String costCenter,

        @NotNull(message = "Department ID is required") Integer departmentId,

        @NotNull(message = "Location ID is required") Integer locationId,

        @NotNull(message = "Company ID is required") Integer companyId,

        @Size(max = 100, message = "Plant must not exceed 100 characters") String plant,

        List<Integer> roleIds) {
}
