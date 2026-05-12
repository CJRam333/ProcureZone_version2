package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for issuing goods from stores
 * 
 * @author NSL India
 * @version 1.0
 */
public record IssueGoodsRequest(
        @NotBlank(message = "Remarks are required") String remarks) {
}
