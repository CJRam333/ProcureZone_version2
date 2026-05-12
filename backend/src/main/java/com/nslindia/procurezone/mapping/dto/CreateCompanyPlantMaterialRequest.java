package com.nslindia.procurezone.mapping.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating new Company-Plant-Material mappings
 * Includes comprehensive validation rules to ensure data integrity
 * 
 * Business Rules:
 * - Company, Plant, and Material IDs are required and must be positive
 * - Quantities must be non-negative with max precision 20,2
 * - Status must be binary (0 or 1)
 * - Max level should be >= Reorder level (enforced in service layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyPlantMaterialRequest {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer companyId;

    @NotNull(message = "Plant ID is required")
    @Positive(message = "Plant ID must be positive")
    private Integer plantId;

    @NotNull(message = "Material ID is required")
    @Positive(message = "Material ID must be positive")
    private Integer materialId;

    /**
     * Current quantity in plant stores
     * Note: Different from location quantity - represents production inventory
     */
    @DecimalMin(value = "0.0", message = "Quantity in stores cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Quantity must have max 18 digits before decimal and 2 after")
    private BigDecimal quantityStores;

    /**
     * Minimum stock level before reordering
     * When stock falls to or below this, procurement should be triggered
     */
    @DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Reorder level must have max 18 digits before decimal and 2 after")
    private BigDecimal reorderLevel;

    /**
     * Maximum storage capacity at this plant
     * Prevents over-purchasing beyond plant storage limits
     */
    @DecimalMin(value = "0.0", message = "Max level cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Max level must have max 18 digits before decimal and 2 after")
    private BigDecimal maxLevel;

    /**
     * Status: 1 = Active, 0 = Inactive
     */
    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status;
}
