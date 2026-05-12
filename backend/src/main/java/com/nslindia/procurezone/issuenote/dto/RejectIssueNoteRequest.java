package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for rejecting an issue note
 * 
 * @author NSL India
 * @version 1.0
 */
public record RejectIssueNoteRequest(
        @NotBlank(message = "Rejection reason is required") String reason) {
}
