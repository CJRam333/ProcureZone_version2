package com.nslindia.procurezone.mapping.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating existing Company-Plant-Material mappings
 * All fields are optional to support partial updates
 * Same validation rules as Create when values are provided
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyPlantMaterialRequest {

    /**
     * Optional: Update plant assignment
     * If changing plant, ensure no duplicate (company+plant+material) exists
     */
    @Positive(message = "Plant ID must be positive")
    private Integer plantId;

    /**
     * Optional: Update material
     * If changing material, ensure no duplicate exists
     */
    @Positive(message = "Material ID must be positive")
    private Integer materialId;

    /**
     * Optional: Update current stock quantity in plant stores
     */
    @DecimalMin(value = "0.0", message = "Quantity in stores cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Quantity must have max 18 digits before decimal and 2 after")
    private BigDecimal quantityStores;

    /**
     * Optional: Update reorder threshold
     */
    @DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Reorder level must have max 18 digits before decimal and 2 after")
    private BigDecimal reorderLevel;

    /**
     * Optional: Update maximum storage capacity
     */
    @DecimalMin(value = "0.0", message = "Max level cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Max level must have max 18 digits before decimal and 2 after")
    private BigDecimal maxLevel;

    /**
     * Optional: Update status (activate/deactivate)
     */
    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status;
}
