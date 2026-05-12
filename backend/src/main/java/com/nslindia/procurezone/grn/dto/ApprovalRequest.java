package com.nslindia.procurezone.grn.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalRequest(
        @NotBlank(message = "Approval remarks are required") String remarks) {
}
