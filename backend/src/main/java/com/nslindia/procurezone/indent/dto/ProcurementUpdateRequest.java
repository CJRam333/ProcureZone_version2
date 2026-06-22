package com.nslindia.procurezone.indent.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request body for POST /api/v1/indents/{id}/procurement-update.
 * Procurement sub-stage values (indent_procurement_status FK IDs):
 *   5 = Quotations Collected
 *   6 = Negotiation Done
 *   7 = PO Released  (requires poNumber + deliveryDate)
 *   8 = Hold         (requires remarks)
 *   9 = Cash Buy     (requires deliveryDate)
 */
public record ProcurementUpdateRequest(
        @NotNull(message = "Procurement sub-status is required") Integer procurementSubStatus,
        String poNumber,
        LocalDate deliveryDate,
        String remarks
) {}
