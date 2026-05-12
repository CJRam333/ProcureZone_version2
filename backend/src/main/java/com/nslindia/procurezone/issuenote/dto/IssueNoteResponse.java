package com.nslindia.procurezone.issuenote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Issue Note with full details
 * 
 * @author NSL India
 * @version 1.0
 */
public record IssueNoteResponse(
        Integer id,
        String issueNoteNumber,
        LocalDateTime issueDate,
        Integer companyId,
        Integer departmentId,
        Integer sectionId,
        Integer plantId,
        String issuedTo,
        String purpose,
        String comments,
        Integer createdBy,
        Integer approvedBy,
        LocalDateTime approvedByDate,
        Integer storesBy,
        LocalDateTime storesByDate,
        Integer status,
        String statusDescription,
        Integer approvedStatus,
        Integer storesByStatus,
        LocalDateTime lastModifiedDate,
        Integer lastModifiedBy,
        List<IssueNoteDetailResponse> details,
        BigDecimal totalAmount) {
    /**
     * Detail line item response
     */
    public record IssueNoteDetailResponse(
            Integer id,
            Integer materialId,
            Integer unitOfMeasureId,
            BigDecimal quantity,
            BigDecimal rate,
            BigDecimal amount,
            String purpose,
            Integer status) {
    }
}
