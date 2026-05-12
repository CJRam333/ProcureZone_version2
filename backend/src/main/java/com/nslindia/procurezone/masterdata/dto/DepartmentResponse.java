package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nslindia.procurezone.masterdata.Department;

/**
 * Response DTO for Department entity.
 * Uses Java 21 record for immutability and conciseness.
 */
public record DepartmentResponse(
        @JsonProperty("id") Integer id,

        @JsonProperty("code") String code,

        @JsonProperty("name") String name,

        @JsonProperty("status") Integer status,

        @JsonProperty("statusText") String statusText,

        @JsonProperty("lastModifiedDate") LocalDate lastModifiedDate,

        @JsonProperty("lastModifiedBy") Integer lastModifiedBy) {

    /**
     * Creates a DepartmentResponse from a Department entity.
     * 
     * @param department the department entity
     * @return the response DTO
     */
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getStatus(),
                department.getStatus() == 1 ? "Active" : "Inactive",
                department.getLastModifiedDate(),
                department.getLastModifiedBy());
    }
}
