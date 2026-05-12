package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nslindia.procurezone.masterdata.Section;

/**
 * Response DTO for Section entity.
 * Uses Java 21 record for immutability and conciseness.
 */
public record SectionResponse(
        @JsonProperty("id") Integer id,

        @JsonProperty("code") String code,

        @JsonProperty("name") String name,

        @JsonProperty("status") Integer status,

        @JsonProperty("statusText") String statusText,

        @JsonProperty("lastModifiedDate") LocalDate lastModifiedDate,

        @JsonProperty("lastModifiedBy") Integer lastModifiedBy) {

    /**
     * Creates a SectionResponse from a Section entity.
     * 
     * @param section the section entity
     * @return the response DTO
     */
    public static SectionResponse from(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getCode(),
                section.getName(),
                section.getStatus(),
                section.getStatus() == 1 ? "Active" : "Inactive",
                section.getLastModifiedDate(),
                section.getLastModifiedBy());
    }
}
