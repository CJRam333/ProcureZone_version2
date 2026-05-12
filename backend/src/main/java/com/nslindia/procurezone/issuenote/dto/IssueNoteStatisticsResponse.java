package com.nslindia.procurezone.issuenote.dto;

import java.math.BigDecimal;

/**
 * Response DTO for Issue Note dashboard statistics
 * 
 * @author NSL India
 * @version 1.0
 */
public record IssueNoteStatisticsResponse(
                Long totalIssueNotes,
                Long createdCount,
                Long pendingRmApprovalCount,
                Long pendingApprovalCount,
                Long approvedCount,
                Long issuedCount,
                Long rejectedCount,
                BigDecimal totalIssuedValue,
                BigDecimal currentMonthValue) {
}
