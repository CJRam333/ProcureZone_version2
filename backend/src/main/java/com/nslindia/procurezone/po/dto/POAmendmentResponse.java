package com.nslindia.procurezone.po.dto;

import java.time.LocalDateTime;

/**
 * B.1 FIX: Response DTO for PO Amendment history
 */
public record POAmendmentResponse(
        Integer id,
        Integer poId,
        Integer amendmentVersion,
        String fieldName,
        String originalValue,
        String amendedValue,
        String amendmentReason,
        Integer amendedBy,
        String amendedByName,
        LocalDateTime amendedDate,
        Integer approvedBy,
        String approvedByName,
        LocalDateTime approvedDate,
        String status) {
}
