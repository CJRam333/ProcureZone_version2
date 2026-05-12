package com.nslindia.procurezone.po.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Purchase Order line item details
 */
public record PODetailResponse(
        Integer id,
        Integer lineNumber,
        Integer indentDetailId,
        Integer materialId,
        String materialCode,
        String materialName,
        String materialDescription,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal discountRate,
        BigDecimal discountAmount,
        BigDecimal lineTotal,
        BigDecimal receivedQuantity,
        BigDecimal pendingQuantity,
        BigDecimal rejectedQuantity,
        LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        Integer deliveryStatus,
        String deliveryStatusName,
        BigDecimal deliveryPercentage,
        String notes) {
}
