package com.nslindia.procurezone.mapping.dto;

import com.nslindia.procurezone.mapping.entity.CompanyPlantMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight summary DTO for Company-Plant-Material mappings
 * Used in list views and dropdowns where full details are not needed
 * Reduces payload size and improves performance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPlantMaterialSummary {

    private Integer id;
    private Integer companyId;
    private Integer plantId;
    private Integer materialId;
    private BigDecimal quantityStores;
    private BigDecimal reorderLevel;
    private Integer status;
    private boolean needsReorder;

    /**
     * Create summary from entity
     * 
     * @param entity The CompanyPlantMaterial entity
     * @return Lightweight summary DTO
     */
    public static CompanyPlantMaterialSummary from(CompanyPlantMaterial entity) {
        if (entity == null) {
            return null;
        }

        return CompanyPlantMaterialSummary.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .plantId(entity.getPlantId())
                .materialId(entity.getMaterialId())
                .quantityStores(entity.getQuantityStores())
                .reorderLevel(entity.getReorderLevel())
                .status(entity.getStatus())
                .needsReorder(entity.needsReorder())
                .build();
    }
}
