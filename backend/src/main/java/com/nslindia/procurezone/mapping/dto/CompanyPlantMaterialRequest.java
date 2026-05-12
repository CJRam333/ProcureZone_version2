package com.nslindia.procurezone.mapping.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO for Company-Plant-Material Mapping Request
 * 
 * @author NSL India
 * @version 1.0
 */
@Builder
public record CompanyPlantMaterialRequest(
                @NotNull(message = "Company ID is required") @Positive(message = "Company ID must be positive") Integer companyId,

                @NotNull(message = "Plant ID is required") @Positive(message = "Plant ID must be positive") Integer plantId,

                @NotNull(message = "Material ID is required") @Positive(message = "Material ID must be positive") Integer materialId,

                BigDecimal quantity,
                BigDecimal reorderLevel,
                BigDecimal maxLevel,
                Integer status) {
}
