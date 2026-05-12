package com.nslindia.procurezone.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Stock Adjustment Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Material ID is required")
    private Integer materialId;

    @NotNull(message = "Plant ID is required")
    private Integer plantId;

    @NotNull(message = "Company ID is required")
    private Integer companyId;

    @NotNull(message = "UOM ID is required")
    private Integer uomId;

    @NotNull(message = "Adjustment type is required (ADD or DEDUCT)")
    @Pattern(regexp = "ADD|DEDUCT", message = "Adjustment type must be ADD or DEDUCT")
    private String adjustmentType; // ADD or DEDUCT

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "Rate must be non-negative")
    private BigDecimal rate;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
