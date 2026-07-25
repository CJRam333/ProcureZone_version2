package com.nslindia.procurezone.issuenote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for RM-approving an issue note.
 *
 * <p>Pass 3: {@code items} is OPTIONAL per-line RM quantity adjustments. If null/empty the note is
 * approved without any quantity change (and no audit rows are written). Each adjustment sets the
 * new {@code issue_note_details_rm_qty}; the requester's original quantity is never overwritten.
 *
 * @author NSL India
 * @version 1.0
 */
public record ApproveIssueNoteRequest(
        @NotBlank(message = "Remarks are required") String remarks,

        @Valid List<LineItemQtyAdjustment> items) {

    /**
     * Per-line RM quantity adjustment. rmQuantity must be >= 0 and (server-enforced) &lt;= the
     * requester's original quantity.
     */
    public record LineItemQtyAdjustment(
            @NotNull(message = "Detail ID is required") Integer detailId,

            @NotNull(message = "RM quantity is required") @PositiveOrZero(message = "RM quantity cannot be negative") BigDecimal rmQuantity) {
    }
}
