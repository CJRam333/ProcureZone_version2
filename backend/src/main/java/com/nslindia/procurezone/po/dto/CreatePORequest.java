package com.nslindia.procurezone.po.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a Purchase Order from an approved indent
 */
public record CreatePORequest(
                @NotNull(message = "Indent ID is required") Integer indentId,

                @NotNull(message = "Vendor ID is required") Integer vendorId,

                @Size(max = 100) String paymentTerms,

                @Size(max = 1000) String deliveryAddress,

                LocalDate expectedDeliveryDate,

                @Size(max = 20) String priority,

                String termsConditions,

                String notes,

                @NotNull(message = "Line items are required") List<POLineItemRequest> lineItems) {
}
