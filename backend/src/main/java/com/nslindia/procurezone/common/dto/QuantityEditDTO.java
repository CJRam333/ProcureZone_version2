package com.nslindia.procurezone.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One entry in a line item's quantity-edit history, resolved from the per-document quantity audit
 * tables (tbl_indent_details_qty_audit / tbl_issue_note_details_qty_audit). Ordered oldest-first
 * when returned in a detail response.
 *
 * @param stage        edit stage — "RM" or "DEPTHEAD"
 * @param oldQuantity  value before this edit (may be null)
 * @param newQuantity  value this stage set
 * @param editedByName resolved employee name of the editor
 * @param editedAt     when the edit happened
 */
public record QuantityEditDTO(
        String stage,
        BigDecimal oldQuantity,
        BigDecimal newQuantity,
        String editedByName,
        LocalDateTime editedAt) {
}
