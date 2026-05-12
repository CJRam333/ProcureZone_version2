package com.seeds.procurezone.masterdata.dto;

import com.seeds.procurezone.masterdata.UnitOfMeasure;
import java.time.LocalDate;

/**
 * Response DTO for Unit of Measure entity.
 * Represents the data returned to clients when querying unit of measure
 * information.
 * 
 * <p>
 * This record uses Java 21 features for immutability and conciseness.
 * </p>
 * 
 * @param id               The unique identifier of the unit of measure
 * @param code             The unique code for the unit of measure (e.g., "KG",
 *                         "LITRE", "PCS")
 * @param name             The descriptive name of the unit of measure (e.g.,
 *                         "Kilogram", "Litre", "Pieces")
 * @param status           The status of the unit of measure (0 = inactive, 1 =
 *                         active)
 * @param statusText       Human-readable status text
 * @param lastModifiedDate The date when the unit of measure was last modified
 * @param lastModifiedBy   The ID of the user who last modified the unit of
 *                         measure
 * 
 * @author ProcureZone Development Team
 * @version 1.0
 * @since 2025-10-13
 */
public record UnitOfMeasureResponse(
        Integer id,
        String code,
        String name,
        Integer status,
        String statusText,
        LocalDate lastModifiedDate,
        Integer lastModifiedBy) {

    /**
     * Factory method to create a UnitOfMeasureResponse from a UnitOfMeasure entity.
     * 
     * @param unitOfMeasure The unit of measure entity to convert
     * @return A new UnitOfMeasureResponse instance
     * @throws IllegalArgumentException if unitOfMeasure is null
     */
    public static UnitOfMeasureResponse from(UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasure == null) {
            throw new IllegalArgumentException("UnitOfMeasure cannot be null");
        }

        return new UnitOfMeasureResponse(
                unitOfMeasure.getId(),
                unitOfMeasure.getCode(),
                unitOfMeasure.getName(),
                unitOfMeasure.getStatus(),
                unitOfMeasure.getStatus() == 1 ? "Active" : "Inactive",
                unitOfMeasure.getLastModifiedDate(),
                unitOfMeasure.getLastModifiedBy());
    }
}
