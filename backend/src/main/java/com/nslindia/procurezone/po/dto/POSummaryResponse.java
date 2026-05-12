package com.nslindia.procurezone.po.dto;

import java.math.BigDecimal;

/**
 * Response DTO for PO summary/list view
 */
public record POSummaryResponse(
        Integer id,
        String poNumber,
        java.time.LocalDate poDate,
        String vendorName,
        String departmentName,
        Integer poStatus,
        String poStatusName,
        BigDecimal netAmount,
        String priority,
        java.time.LocalDate expectedDeliveryDate,
        Integer totalLineItems,
        BigDecimal deliveryPercentage) {
}
