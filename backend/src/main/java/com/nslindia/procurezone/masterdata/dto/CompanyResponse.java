package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nslindia.procurezone.masterdata.Company;

/**
 * Response DTO for Company entity.
 * Uses Java 21 record for immutability and conciseness.
 */
public record CompanyResponse(
        @JsonProperty("id") Integer id,

        @JsonProperty("code") String code,

        @JsonProperty("name") String name,

        @JsonProperty("status") Integer status,

        @JsonProperty("statusText") String statusText,

        @JsonProperty("lastModifiedDate") LocalDate lastModifiedDate,

        @JsonProperty("lastModifiedBy") Integer lastModifiedBy) {

    /**
     * Creates a CompanyResponse from a Company entity.
     * 
     * @param company the company entity
     * @return the response DTO
     */
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                company.getStatus(),
                company.getStatus() == 1 ? "Active" : "Inactive",
                company.getLastModifiedDate(),
                company.getLastModifiedBy());
    }
}
