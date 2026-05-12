package com.nslindia.procurezone.po.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * B.1 FIX: Request DTO for amending a PO after vendor confirmation
 * All changes will be tracked with amendment history
 */
public record AmendPORequest(
        LocalDate deliveryDate,
        String deliveryAddress,
        String paymentTerms,
        String termsConditions,
        String notes,
        String priority,

        @NotBlank(message = "Amendment reason is required") @Size(min = 10, max = 500, message = "Amendment reason must be between 10 and 500 characters") String amendmentReason) {
}
