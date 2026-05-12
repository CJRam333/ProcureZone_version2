package com.nslindia.procurezone.mapping.dto;

import com.nslindia.procurezone.mapping.CompanyPlantMaterialMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Company-Plant-Material mappings
 * Includes all entity data plus related names for display
 * Used in API responses for full detail views
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPlantMaterialResponse {

    private Integer id;
    private Integer companyId;
    private String companyName;
    private Integer plantId;
    private String plantName;
    private Integer materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private BigDecimal reorderLevel;
    private BigDecimal maxLevel;
    private Integer status;
    private Integer lastModifiedBy;
    private LocalDate lastModifiedDate;

    /**
     * Static factory method to create response from entity
     * Includes related entity names for display
     * 
     * @param entity The CompanyPlantMaterialMap entity
     * @return Populated response DTO
     */
    public static CompanyPlantMaterialResponse from(CompanyPlantMaterialMap entity) {
        if (entity == null) {
            return null;
        }

        return CompanyPlantMaterialResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompany() != null ? entity.getCompany().getId() : null)
                .companyName(entity.getCompany() != null ? entity.getCompany().getName() : null)
                .plantId(entity.getPlant() != null ? entity.getPlant().getId() : null)
                .plantName(entity.getPlant() != null ? entity.getPlant().getName() : null)
                .materialId(entity.getMaterial() != null ? entity.getMaterial().getId() : null)
                .materialCode(entity.getMaterial() != null ? entity.getMaterial().getCode() : null)
                .materialName(entity.getMaterial() != null ? entity.getMaterial().getName() : null)
                .quantity(entity.getQuantity())
                .reorderLevel(entity.getReorderLevel())
                .maxLevel(entity.getMaxLevel())
                .status(entity.getStatus())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}
