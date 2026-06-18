package com.nslindia.procurezone.employee.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing employee.
 *
 * @JsonProperty maps the frontend's emp-prefixed keys to the Java field names.
 *
 * roleIds: null = leave unchanged; [] = remove all; [1,2] = replace all (Option A)
 * reportingManagerId: null = unchanged; 0 = remove; positive = set/replace
 */
public record UpdateEmployeeRequest(

        @JsonProperty("empName")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @JsonProperty("empEmail")
        @Size(max = 100, message = "Email/username must not exceed 100 characters")
        String email,

        @JsonProperty("empJoinDate")
        LocalDate joinDate,

        @JsonProperty("empDesignation")
        @Size(max = 100, message = "Designation must not exceed 100 characters")
        String designation,

        @JsonProperty("empCostCenter")
        @Size(max = 500, message = "Cost center must not exceed 500 characters")
        String costCenter,

        // Same key on both sides — no @JsonProperty needed
        Integer departmentId,

        Integer locationId,

        Integer companyId,

        @JsonProperty("plantName")
        @Size(max = 100, message = "Plant must not exceed 100 characters")
        String plant,

        List<Integer> roleIds,

        Integer reportingManagerId) {
}
