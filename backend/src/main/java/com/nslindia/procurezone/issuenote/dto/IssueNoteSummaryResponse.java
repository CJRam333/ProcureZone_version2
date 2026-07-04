package com.nslindia.procurezone.issuenote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary DTO for Issue Note list view
 * 
 * @author NSL India
 * @version 1.0
 */
public record IssueNoteSummaryResponse(
        Integer id,
        String issueNoteNumber,
        LocalDateTime issueDate,
        Integer departmentId,
        String issuedTo,
        Integer status,
        String statusDescription,
        Integer approvedStatus,
        Integer storesByStatus,
        BigDecimal totalAmount,
        Integer lineItemCount) {
}
