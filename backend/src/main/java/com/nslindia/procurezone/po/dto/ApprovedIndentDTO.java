package com.nslindia.procurezone.po.dto;

import java.time.LocalDate;

/**
 * DTO for approved indent that can be converted to PO
 */
public record ApprovedIndentDTO(
        Integer indentId,
        String indentNumber,
        LocalDate indentDate,
        Integer departmentId,
        String departmentName,
        Integer requestorId,
        String requestorName,
        String purpose,
        String priority,
        Integer totalLineItems,
        LocalDate requiredByDate) {
}
