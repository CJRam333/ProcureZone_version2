package com.nslindia.procurezone.indent.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for L1 (RM/Section Head) approval request with quantity adjustment.
 * 
 * In the legacy system, the RM can adjust the requested quantity before
 * approving.
 * This DTO captures both the approval decision and any quantity adjustments.
 * 
 * The rmQuantity (Reporting Manager quantity) represents what the RM approves,
 * which may be less than or equal to the original requested quantity.
 */
public record L1ApprovalRequest(
                String remarks,

                @Valid List<LineItemAdjustment> adjustments) {
        /**
         * DTO for adjusting individual line items during L1 approval.
         */
        public record LineItemAdjustment(
                        @NotNull(message = "Detail ID is required") Integer detailId,

                        @NotNull(message = "RM quantity is required") @Positive(message = "RM quantity must be positive") BigDecimal rmQuantity,

                        String rmRemarks) {
        }
}
