package com.nslindia.procurezone.mapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Summary DTO for Company-Location-Material mapping.
 * Used for list views with minimal information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLocationMaterialSummary {

    private Integer id;
    private Integer companyId;
    private Integer locationId;
    private Integer materialId;
    private BigDecimal quantity;
    private BigDecimal reorderLevel;
    private Integer status;
    private boolean needsReorder;
}
