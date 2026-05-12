package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.nslindia.procurezone.masterdata.Plant;

/**
 * Response DTO for Plant entity.
 */
public record PlantResponse(
        Integer id,
        String code,
        String name,
        Integer status,
        String statusName,
        LocalDate lastModifiedDate,
        Integer lastModifiedBy) {

    /**
     * Maps Plant entity to PlantResponse DTO.
     */
    public static PlantResponse from(Plant plant) {
        return new PlantResponse(
                plant.getId(),
                plant.getCode(),
                plant.getName(),
                plant.getStatus(),
                plant.getStatus() == 1 ? "Active" : "Inactive",
                plant.getLastModifiedDate(),
                plant.getLastModifiedBy());
    }
}
