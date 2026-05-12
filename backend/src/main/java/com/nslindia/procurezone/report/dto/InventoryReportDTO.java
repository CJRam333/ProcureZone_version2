package com.nslindia.procurezone.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTOs for Inventory Reports
 */
public class InventoryReportDTO {

    /**
     * Stock Status Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockStatus {
        private String materialCode;
        private String materialName;
        private String materialGroup;
        private String uom;
        private Double currentQuantity;
        private Double reservedQuantity;
        private Double availableQuantity;
        private Double minStockLevel;
        private Double maxStockLevel;
        private Double reorderLevel;
        private String stockStatus; // Critical, Low, Normal, Excess
        private String companyName;
        private String plantName;
        private String locationName;
        private LocalDate lastTransactionDate;
    }

    /**
     * Low Stock Alert DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStock {
        private String materialCode;
        private String materialName;
        private String uom;
        private Double availableQuantity;
        private Double reorderLevel;
        private Double minStockLevel;
        private Double shortfall;
        private String companyName;
        private String plantName;
        private String locationName;
    }

    /**
     * Material Usage Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialUsage {
        private String materialCode;
        private String materialName;
        private String uom;
        private String transactionType; // IN or OUT
        private Double totalIn;
        private Double totalOut;
        private Integer transactionCount;
        private String companyName;
    }
}
