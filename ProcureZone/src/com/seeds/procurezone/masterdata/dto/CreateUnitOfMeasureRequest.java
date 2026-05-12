package com.seeds.procurezone.masterdata.dto;

import com.seeds.procurezone.common.exception.BadRequestException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Unit of Measure.
 * Contains validation rules and business logic for unit of measure creation.
 * 
 * <p>
 * This record uses Java 21 features and Bean Validation annotations.
 * </p>
 * 
 * <p>
 * <b>Validation Rules:</b>
 * </p>
 * <ul>
 * <li>Code: Required, 2-100 characters, uppercase alphanumeric with
 * hyphens/underscores</li>
 * <li>Name: Required, 2-100 characters</li>
 * <li>Status: Required, must be 0 (inactive) or 1 (active)</li>
 * </ul>
 * 
 * @param code   The unique code for the unit of measure (e.g., "KG", "LITRE",
 *               "PCS")
 * @param name   The descriptive name of the unit of measure (e.g., "Kilogram",
 *               "Litre", "Pieces")
 * @param status The status of the unit of measure (0 = inactive, 1 = active)
 * 
 * @author ProcureZone Development Team
 * @version 1.0
 * @since 2025-10-13
 */
public record CreateUnitOfMeasureRequest(

        @NotBlank(message = "Unit of measure code is required") @Size(min = 2, max = 100, message = "Unit of measure code must be between 2 and 100 characters") @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Unit of measure code must contain only uppercase letters, numbers, underscores, and hyphens") String code,

        @NotBlank(message = "Unit of measure name is required") @Size(min = 2, max = 100, message = "Unit of measure name must be between 2 and 100 characters") String name,

        Integer status) {

    /**
     * Validates the status field.
     * Status must be either 0 (inactive) or 1 (active).
     * 
     * @throws BadRequestException if status is invalid
     */
    public void validateStatus() {
        if (status == null || (status != 0 && status != 1)) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }
    }

    /**
     * Checks if the status is valid without throwing an exception.
     * 
     * @return true if status is 0 or 1, false otherwise
     */
    public boolean isValidStatus() {
        return status != null && (status == 0 || status == 1);
    }
}
