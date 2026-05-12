package com.nslindia.procurezone.indent.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for creating or updating an indent detail (line item).
 */
public record IndentDetailRequest(
        @NotNull(message = "Material ID is required") Integer materialId,

        @NotNull(message = "Unit of measure ID is required") Integer unitOfMeasureId,

        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity,

        BigDecimal rmQuantity,

        BigDecimal deptQuantity,

        BigDecimal stockAvailable,

        BigDecimal pricing,

        String purpose,

        String vendor,

        Integer status) {
}
