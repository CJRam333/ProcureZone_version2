package com.nslindia.procurezone.po.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for individual line items in a Purchase Order
 */
public record POLineItemRequest(
        @NotNull(message = "Indent detail ID is required") Integer indentDetailId,

        @NotNull(message = "Material ID is required") Integer materialId,

        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity,

        @NotNull(message = "Unit price is required") @Positive(message = "Unit price must be positive") BigDecimal unitPrice,

        BigDecimal taxRate,

        BigDecimal discountRate,

        LocalDate expectedDeliveryDate,

        String notes) {
}
