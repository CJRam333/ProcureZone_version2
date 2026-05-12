package com.nslindia.procurezone.indent.dto;

import java.math.BigDecimal;

/**
 * DTO for indent detail response.
 */
public record IndentDetailResponse(
        Integer id,
        Integer materialId,
        String materialCode,
        String materialName,
        Integer unitOfMeasureId,
        String unitOfMeasureCode,
        String unitOfMeasureName,
        BigDecimal quantity,
        BigDecimal rmQuantity,
        BigDecimal deptQuantity,
        BigDecimal stockAvailable,
        BigDecimal pricing,
        String purpose,
        String vendor,
        Integer status) {
}
