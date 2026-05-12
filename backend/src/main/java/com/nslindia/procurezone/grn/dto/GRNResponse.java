package com.nslindia.procurezone.grn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record GRNResponse(
        Integer id,
        String grnNumber,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime receiptDate,

        Integer indentId,
        Integer indentDetailsId,

        BigDecimal receivedQuantity,
        BigDecimal issuedQuantity,
        BigDecimal balanceInventory,
        BigDecimal openingQuantity,
        BigDecimal requestedQuantity,
        BigDecimal balanceQuantityStores,

        BigDecimal rate,
        BigDecimal amount,

        String vendorName,
        String comments,

        Integer status,
        String statusName,

        Integer createdBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdDate,
        String createdRemarks,

        Integer approvedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime approvedDate,
        String approvedRemarks,

        Integer finalApprovedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime finalApprovedDate,
        String finalApprovedRemarks,

        Integer storedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime storedDate,
        String storedRemarks) {
    public static String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "Created";
            case 2 -> "Inspected";
            case 3 -> "Approved";
            case 4 -> "Final Approved";
            case 5 -> "Stored";
            case 6 -> "Rejected";
            default -> "Unknown";
        };
    }
}
