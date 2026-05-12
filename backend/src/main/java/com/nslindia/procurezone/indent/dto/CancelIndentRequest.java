package com.nslindia.procurezone.indent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * B.3 FIX: Request DTO for indent cancellation with mandatory reason.
 * 
 * @author NSL India
 * @version 1.0
 */
public record CancelIndentRequest(
        @NotBlank(message = "Cancellation reason is required") @Size(min = 10, max = 500, message = "Cancellation reason must be between 10 and 500 characters") String reason) {
}
