package com.nslindia.procurezone.po.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for cancelling a Purchase Order
 */
public record CancelPORequest(
        @NotBlank(message = "Cancellation reason is required") @Size(max = 500, message = "Cancellation reason must not exceed 500 characters") String cancellationReason) {
}
