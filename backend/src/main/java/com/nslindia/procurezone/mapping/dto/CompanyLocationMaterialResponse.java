package com.nslindia.procurezone.mapping.dto;

import com.nslindia.procurezone.mapping.CompanyLocationMaterialMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Triggering devtools rebuild for NoClassDefFoundError
@Builder
public class CompanyLocationMaterialResponse {

    private Integer id;
    private Integer companyId;
    private String companyName;
    private Integer locationId;
    private String locationName;
    private Integer materialId;
    private String materialCode;
    private String materialName;
    private Boolean isActive;
    private BigDecimal minStockLevel;
    private BigDecimal maxStockLevel;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQuantity;
    private String remarks;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    public static CompanyLocationMaterialResponse from(CompanyLocationMaterialMap entity) {
        if (entity == null) {
            return null;
        }
        return CompanyLocationMaterialResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompany() != null ? entity.getCompany().getId() : null)
                .companyName(entity.getCompany() != null ? entity.getCompany().getName() : null)
                .locationId(entity.getLocation() != null ? entity.getLocation().getId() : null)
                .locationName(entity.getLocation() != null ? entity.getLocation().getName() : null)
                .materialId(entity.getMaterial() != null ? entity.getMaterial().getId() : null)
                .materialCode(entity.getMaterial() != null ? entity.getMaterial().getCode() : null)
                .materialName(entity.getMaterial() != null ? entity.getMaterial().getName() : null)
                .isActive(entity.getIsActive())
                .minStockLevel(entity.getMinStockLevel())
                .maxStockLevel(entity.getMaxStockLevel())
                .reorderLevel(entity.getReorderLevel())
                .reorderQuantity(entity.getReorderQuantity())
                .remarks(entity.getRemarks())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }
}
