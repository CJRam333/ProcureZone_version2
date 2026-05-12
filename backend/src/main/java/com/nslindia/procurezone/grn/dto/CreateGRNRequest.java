package com.nslindia.procurezone.grn.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateGRNRequest(
        @NotNull(message = "Indent ID is required") Integer indentId,

        @NotNull(message = "Indent details ID is required") Integer indentDetailsId,

        @NotNull(message = "Received quantity is required") @DecimalMin(value = "0.01", message = "Received quantity must be greater than 0") BigDecimal receivedQuantity,

        @NotNull(message = "Rate is required") @DecimalMin(value = "0.0", message = "Rate cannot be negative") BigDecimal rate,

        @NotBlank(message = "Vendor name is required") String vendorName,

        BigDecimal openingQuantity,

        String comments) {
}
