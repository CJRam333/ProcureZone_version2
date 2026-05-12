package com.nslindia.procurezone.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Inventory Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Integer id;
    private Integer materialId;
    private String materialCode;
    private String materialName;
    private Integer plantId;
    private String plantCode;
    private String plantName;
    private Integer companyId;
    private String companyCode;
    private String companyName;
    private Integer uomId;
    private String uomCode;
    private String uomName;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal reorderLevel;
    private BigDecimal maxLevel;
    private BigDecimal minLevel;
    private BigDecimal avgRate;
    private BigDecimal totalValue;
    private LocalDateTime lastReceiptDate;
    private LocalDateTime lastIssueDate;
    private LocalDateTime lastUpdated;
    private Boolean isLowStock;
    private Boolean isCriticalStock;
    private Boolean isOverstock;
}
