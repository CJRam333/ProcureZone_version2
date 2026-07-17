package com.nslindia.procurezone.indent.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for indent list summary (without details).
 */
public record IndentListResponse(
        Integer id,
        String indentNumber,
        String indentYear,
        LocalDateTime indentDate,
        String companyName,
        String departmentName,
        String employeeName,
        LocalDate deliveryDate,
        String statusName,
        Integer statusId,
        Integer approvedStatusId,
        Integer finalStatusId,
        Integer procurementStatusId,
        Integer detailsCount,
        String displayStatus) {
}
