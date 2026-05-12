package com.nslindia.procurezone.mapping.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO for Company-Location-Material Mapping Request
 * 
 * @author NSL India
 * @version 1.0
 */
@Builder
public record CompanyLocationMaterialRequest(
        @NotNull(message = "Company ID is required") @Positive(message = "Company ID must be positive") Integer companyId,

        @NotNull(message = "Location ID is required") @Positive(message = "Location ID must be positive") Integer locationId,

        @NotNull(message = "Material ID is required") @Positive(message = "Material ID must be positive") Integer materialId,

        Boolean isActive,
        BigDecimal minStockLevel,
        BigDecimal maxStockLevel,
        BigDecimal reorderLevel,
        BigDecimal reorderQuantity,
        String remarks) {
}
