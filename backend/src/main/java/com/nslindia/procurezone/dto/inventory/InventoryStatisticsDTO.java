package com.nslindia.procurezone.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Inventory Statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatisticsDTO {
    private Long totalInventoryItems;
    private Long activeInventoryItems;
    private Long zeroStockItems;
    private Long lowStockItems;
    private Long criticalStockItems;
    private Long overstockItems;
    private BigDecimal totalInventoryValue;
    private BigDecimal avgInventoryValue;
}
