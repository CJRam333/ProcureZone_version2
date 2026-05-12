package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * B.2 FIX: Request DTO for returning issued materials back to stores
 */
public record ReturnIssueNoteRequest(
        @NotBlank(message = "Return reason is required") @Size(min = 10, max = 500, message = "Return reason must be between 10 and 500 characters") String returnReason,

        String remarks) {
}
