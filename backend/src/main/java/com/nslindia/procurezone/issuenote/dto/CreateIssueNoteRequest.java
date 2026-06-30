package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new Issue Note
 * 
 * @author NSL India
 * @version 1.0
 */
public record CreateIssueNoteRequest(
        @NotNull(message = "Company ID is required") Integer companyId,

        @NotNull(message = "Department ID is required") Integer departmentId,

        Integer sectionId,

        @NotNull(message = "Plant ID is required") Integer plantId,

        @NotBlank(message = "Issued to is required") String issuedTo,

        String purpose,

        String comments,

        @NotEmpty(message = "At least one line item is required") @Valid List<IssueNoteLineItem> lineItems) {
    /**
     * Line item for issue note
     */
    public record IssueNoteLineItem(
            @NotNull(message = "Material ID is required") Integer materialId,

            @NotNull(message = "Unit of Measure ID is required") Integer unitOfMeasureId,

            @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity,

            BigDecimal rate,

            String purpose,

            BigDecimal quantityStores) {
    }
}
