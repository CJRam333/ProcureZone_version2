package com.nslindia.procurezone.indent.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for creating or updating an indent detail (line item).
 */
public record IndentDetailRequest(
        @NotNull(message = "Material ID is required") Integer materialId,

        // The specific company selected with this material in the dropdown. Optional — legacy/absent
        // payloads leave it null and the display falls back to the multi-company resolver.
        Integer companyId,

        @NotNull(message = "Unit of measure ID is required") Integer unitOfMeasureId,

        @NotNull(message = "Quantity is required") @PositiveOrZero(message = "Quantity cannot be negative") @DecimalMax(value = "99999", message = "Quantity cannot exceed 99999") BigDecimal quantity,

        BigDecimal rmQuantity,

        BigDecimal deptQuantity,

        BigDecimal stockAvailable,

        BigDecimal pricing,

        String purpose,

        String vendor,

        Integer status) {
}
