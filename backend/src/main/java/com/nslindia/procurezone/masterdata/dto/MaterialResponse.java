package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nslindia.procurezone.masterdata.Material;

/**
 * Response DTO for Material entity.
 * Uses Java 21 record for immutability and conciseness.
 */
public record MaterialResponse(
        @JsonProperty("id") Integer id,

        @JsonProperty("code") String code,

        @JsonProperty("name") String name,

        @JsonProperty("description") String description,

        @JsonProperty("status") Integer status,

        @JsonProperty("statusText") String statusText,

        @JsonProperty("lastModifiedDate") LocalDate lastModifiedDate,

        @JsonProperty("lastModifiedBy") Integer lastModifiedBy) {

    /**
     * Creates a MaterialResponse from a Material entity.
     * 
     * @param material the material entity
     * @return the response DTO
     */
    public static MaterialResponse from(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getCode(),
                material.getName(),
                material.getDescription(),
                material.getStatus(),
                Integer.valueOf(1).equals(material.getStatus()) ? "Active" : "Inactive",
                material.getLastModifiedDate(),
                material.getLastModifiedBy());
    }
}
