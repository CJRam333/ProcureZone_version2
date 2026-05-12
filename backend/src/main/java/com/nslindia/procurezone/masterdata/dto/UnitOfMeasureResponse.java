package com.nslindia.procurezone.masterdata.dto;

import java.time.LocalDate;

import com.nslindia.procurezone.masterdata.UnitOfMeasure;

/**
 * Response DTO for Unit of Measure
 * 
 * @param id               Unit of Measure ID
 * @param code             Unique unit code (e.g., "KG", "LITRE", "PCS")
 * @param name             Unit name (e.g., "Kilogram", "Litre", "Pieces")
 * @param status           Status (1=Active, 0=Inactive)
 * @param lastModifiedDate Last modification date
 * @param lastModifiedBy   User ID who last modified
 */
public record UnitOfMeasureResponse(
        Integer id,
        String code,
        String name,
        Integer status,
        LocalDate lastModifiedDate,
        Integer lastModifiedBy) {

    /**
     * Create response from entity
     */
    public static UnitOfMeasureResponse fromEntity(UnitOfMeasure entity) {
        return new UnitOfMeasureResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getLastModifiedDate(),
                entity.getLastModifiedBy());
    }

    /**
     * Check if unit is active
     */
    public boolean isActive() {
        return status != null && status == 1;
    }
}
