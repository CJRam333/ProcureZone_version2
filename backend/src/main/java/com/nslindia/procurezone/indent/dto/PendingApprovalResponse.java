package com.nslindia.procurezone.indent.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for pending approval list.
 */
public record PendingApprovalResponse(
        Integer indentId,
        String indentNumber,
        LocalDateTime indentDate,
        String companyName,
        String departmentName,
        String employeeName,
        LocalDate deliveryDate,
        String currentStatus,
        Integer itemCount,
        LocalDateTime submittedDate) {
}
