package com.nslindia.procurezone.po.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Purchase Order with all details
 */
public record PurchaseOrderResponse(
        Integer id,
        String poNumber,
        LocalDate poDate,
        Integer indentId,
        String indentNumber,
        Integer vendorId,
        String vendorCode,
        String vendorName,
        Integer departmentId,
        String departmentName,
        Integer poStatus,
        String poStatusName,
        BigDecimal totalAmount,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        String currency,
        String paymentTerms,
        String deliveryAddress,
        LocalDate deliveryDate,
        LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        String termsConditions,
        String notes,
        String priority,
        Integer approvedBy,
        LocalDateTime approvedDate,
        Integer sentToVendorBy,
        LocalDateTime sentToVendorDate,
        Integer cancelledBy,
        LocalDateTime cancelledDate,
        String cancellationReason,
        Integer closedBy,
        LocalDateTime closedDate,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        List<PODetailResponse> details) {
}
