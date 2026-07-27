package com.nslindia.procurezone.issuenote.dto;

/**
 * Request DTO for issuing goods from stores.
 * Remarks are OPTIONAL (the UI labels the field "Remarks (Optional)").
 *
 * @author NSL India
 * @version 1.0
 */
public record IssueGoodsRequest(
        String remarks) {
}
