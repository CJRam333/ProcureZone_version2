package com.nslindia.procurezone.indent.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for L2 (Department Head) approval request with quantity adjustment.
 * 
 * The Department Head can further adjust quantities approved by L1.
 * The deptQuantity (Department Head quantity) represents the final approved
 * quantity
 * for procurement, which may be less than or equal to the RM-approved quantity.
 */
public record L2ApprovalRequest(
        String remarks,

        @Valid List<LineItemAdjustment> adjustments) {
    /**
     * DTO for adjusting individual line items during L2 approval.
     */
    public record LineItemAdjustment(
            @NotNull(message = "Detail ID is required") Integer detailId,

            @NotNull(message = "Department quantity is required") @Positive(message = "Department quantity must be positive") BigDecimal deptQuantity,

            String deptRemarks) {
    }
}
