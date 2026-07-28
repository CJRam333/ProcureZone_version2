package com.nslindia.procurezone.indent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for indent list summary (without full details).
 *
 * <p>{@code items} carries a COMPACT per-line summary (material code/name, UOM, quantity) so the
 * list page can show an Items hover-preview without a second per-row detail fetch.
 */
public record IndentListResponse(
        Integer id,
        String indentNumber,
        String indentYear,
        LocalDateTime indentDate,
        String companyName,
        String departmentName,
        String employeeName,
        LocalDate deliveryDate,
        String statusName,
        Integer statusId,
        Integer approvedStatusId,
        Integer finalStatusId,
        Integer procurementStatusId,
        Integer detailsCount,
        String displayStatus,
        LocalDateTime lastModifiedDate,
        List<ItemSummary> items) {

    /** Compact line-item summary for the list-page Items hover-preview. */
    public record ItemSummary(
            String materialCode,
            String materialName,
            String uomCode,
            BigDecimal quantity) {
    }
}
