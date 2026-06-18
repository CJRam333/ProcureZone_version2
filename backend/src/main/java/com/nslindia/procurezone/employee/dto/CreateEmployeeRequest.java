package com.nslindia.procurezone.employee.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new employee.
 *
 * @JsonProperty maps the frontend's emp-prefixed field names to the backend
 * Java field names so both layers can use their own idiomatic naming without
 * any runtime transformation.
 *
 * Authentication type is inferred at login from emp_email:
 *   contains '@' → LDAP user
 *   no '@'       → Non-LDAP (local) user
 *
 * password is optional:
 *   Non-LDAP: provide a temporary password (stored as MD5 in emp_password)
 *   LDAP: omit — authentication is handled externally
 */
public record CreateEmployeeRequest(

        @JsonProperty("empId")
        @NotBlank(message = "Employee ID is required")
        @Size(max = 100, message = "Employee ID must not exceed 100 characters")
        String employeeId,

        @JsonProperty("empName")
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @JsonProperty("empEmail")
        @NotBlank(message = "Email or username is required")
        @Size(max = 100, message = "Email/username must not exceed 100 characters")
        String email,

        // No @JsonProperty needed — key is already "password" on both sides
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password,

        @JsonProperty("empJoinDate")
        LocalDate joinDate,

        @JsonProperty("empDesignation")
        @Size(max = 100, message = "Designation must not exceed 100 characters")
        String designation,

        @JsonProperty("empCostCenter")
        @Size(max = 500, message = "Cost center must not exceed 500 characters")
        String costCenter,

        // departmentId, locationId, companyId: same key on both sides
        Integer departmentId,

        Integer locationId,

        Integer companyId,

        @JsonProperty("plantName")
        @Size(max = 100, message = "Plant must not exceed 100 characters")
        String plant,

        // roleIds, reportingManagerId: same key on both sides
        List<Integer> roleIds,

        Integer reportingManagerId) {
}
