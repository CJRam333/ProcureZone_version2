package com.nslindia.procurezone.issuenote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Issue Note with full details.
 * Enriched with resolved master-data names (employee, department, company,
 * section, plant, issued-by) so the frontend never has to work from raw IDs —
 * same standard as IndentResponse.
 *
 * @author NSL India
 * @version 1.0
 */
public record IssueNoteResponse(
        Integer id,
        String issueNoteNumber,
        LocalDateTime issueDate,
        Integer companyId,
        String companyName,
        Integer departmentId,
        String departmentName,
        Integer sectionId,
        String sectionName,
        Integer plantId,
        String plantName,
        String issuedTo,
        String purpose,
        String comments,
        Integer createdBy,
        Integer employeeNumber,
        String employeeName,
        Integer approvedBy,
        LocalDateTime approvedByDate,
        Integer storesBy,
        LocalDateTime storesByDate,
        String issuedByName,
        Integer status,
        String statusDescription,
        String displayStatus,
        Integer approvedStatus,
        Integer storesByStatus,
        LocalDateTime lastModifiedDate,
        Integer lastModifiedBy,
        List<IssueNoteDetailResponse> details,
        BigDecimal totalAmount) {
    /**
     * Detail line item response — enriched with material/UOM codes.
     */
    public record IssueNoteDetailResponse(
            Integer id,
            Integer materialId,
            String materialCode,
            String materialName,
            Integer unitOfMeasureId,
            String uomCode,
            BigDecimal quantity,
            BigDecimal rate,
            BigDecimal amount,
            String purpose,
            Integer status) {
    }
}
