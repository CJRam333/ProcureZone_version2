package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for approving an issue note
 * 
 * @author NSL India
 * @version 1.0
 */
public record ApproveIssueNoteRequest(
        @NotBlank(message = "Remarks are required") String remarks) {
}
