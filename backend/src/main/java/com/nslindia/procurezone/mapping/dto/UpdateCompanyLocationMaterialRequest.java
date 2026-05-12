package com.nslindia.procurezone.mapping.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing Company-Location-Material mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompanyLocationMaterialRequest {

    @DecimalMin(value = "0.0", inclusive = true, message = "Quantity cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Quantity must have at most 18 integer digits and 2 decimal places")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Reorder level cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Reorder level must have at most 18 integer digits and 2 decimal places")
    private BigDecimal reorderLevel;

    @DecimalMin(value = "0.0", inclusive = true, message = "Max level cannot be negative")
    @Digits(integer = 18, fraction = 2, message = "Max level must have at most 18 integer digits and 2 decimal places")
    private BigDecimal maxLevel;

    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status;
}
