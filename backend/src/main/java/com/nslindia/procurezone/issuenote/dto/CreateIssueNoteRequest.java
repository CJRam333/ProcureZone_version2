package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new Issue Note
 * 
 * @author NSL India
 * @version 1.0
 */
public record CreateIssueNoteRequest(
        // company / department / plant are captured server-side from the creating employee's
        // record (see IssueNoteService.createIssueNote), so they are optional in the payload.
        // A value is still honoured as a fallback if the employee record can't supply it.
        Integer companyId,

        Integer departmentId,

        Integer sectionId,

        Integer plantId,

        // Optional — the legacy creation form has no "Issued To" field; the creator
        // (issue_note_createdby) is the requester of record. Column retained in DB.
        String issuedTo,

        String purpose,

        String comments,

        @NotEmpty(message = "At least one line item is required") @Valid List<IssueNoteLineItem> lineItems) {
    /**
     * Line item for issue note
     */
    public record IssueNoteLineItem(
            @NotNull(message = "Material ID is required") Integer materialId,

            @NotNull(message = "Unit of Measure ID is required") Integer unitOfMeasureId,

            @NotNull(message = "Quantity is required") @PositiveOrZero(message = "Quantity cannot be negative") @DecimalMax(value = "99999", message = "Quantity cannot exceed 99999") BigDecimal quantity,

            BigDecimal rate,

            String purpose,

            BigDecimal quantityStores) {
    }
}
