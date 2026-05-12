package com.nslindia.procurezone.grn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record GRNSummaryResponse(
        Integer id,
        String grnNumber,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime receiptDate,

        String vendorName,
        BigDecimal receivedQuantity,
        BigDecimal amount,

        Integer status,
        String statusName,

        BigDecimal balanceInventory) {
}
