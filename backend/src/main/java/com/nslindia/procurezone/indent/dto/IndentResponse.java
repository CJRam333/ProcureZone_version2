package com.nslindia.procurezone.indent.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for full indent response with all details.
 */
public record IndentResponse(
        Integer id,
        String indentNumber,
        String indentYear,
        LocalDateTime indentDate,
        Integer companyId,
        String companyName,
        Integer departmentId,
        String departmentName,
        Integer sectionId,
        String sectionName,
        Integer plantId,
        String plantName,
        Integer employeeId,
        String employeeName,
        String comments,
        LocalDate deliveryDate,
        String poNumber,
        Integer createdById,
        String createdByName,
        Integer approvedById,
        String approvedByName,
        LocalDateTime approvedByDate,
        Integer finalApprovedById,
        String finalApprovedByName,
        LocalDateTime finalApprovedDate,
        Integer procurementById,
        String procurementByName,
        String remarks,
        String finalRemarks,
        Integer statusId,
        String statusName,
        Integer approvedStatusId,
        String approvedStatusName,
        Integer finalStatusId,
        String finalStatusName,
        Integer procurementStatusId,
        String procurementStatusName,
        LocalDateTime lastModifiedDate,
        Integer lastModifiedBy,
        String displayStatus,
        List<IndentDetailResponse> details) {
}
