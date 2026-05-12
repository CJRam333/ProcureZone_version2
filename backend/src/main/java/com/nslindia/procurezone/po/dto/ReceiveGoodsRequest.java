package com.nslindia.procurezone.po.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for receiving goods against a PO
 */
public record ReceiveGoodsRequest(
        @NotNull(message = "PO detail ID is required") Integer poDetailId,

        @NotNull(message = "Received quantity is required") @Positive(message = "Received quantity must be positive") BigDecimal receivedQuantity,

        BigDecimal rejectedQuantity,

        String remarks) {
}
