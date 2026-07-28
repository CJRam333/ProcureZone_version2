package com.nslindia.procurezone.issuenote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Summary DTO for Issue Note list view
 *
 * <p>{@code items} carries a COMPACT per-line summary (material code/name, UOM, quantity) so the
 * list page can show an Items hover-preview without a second per-row detail fetch.
 *
 * @author NSL India
 * @version 1.0
 */
public record IssueNoteSummaryResponse(
        Integer id,
        String issueNoteNumber,
        LocalDateTime issueDate,
        Integer departmentId,
        String issuedTo,
        Integer createdBy,
        String employeeName,
        Integer status,
        String statusDescription,
        Integer approvedStatus,
        Integer storesByStatus,
        BigDecimal totalAmount,
        Integer lineItemCount,
        LocalDateTime lastModifiedDate,
        List<ItemSummary> items) {

    /**
     * Compact line-item summary for the list-page Items hover-preview.
     * {@code companies} is the comma-separated list of companies stocking the material, resolved the
     * SAME way the detail page does (Map repo, tbl_map_company_plant_material).
     */
    public record ItemSummary(
            String materialName,
            String materialDescription,
            String companies,
            String uomCode,
            BigDecimal quantity) {
    }
}
