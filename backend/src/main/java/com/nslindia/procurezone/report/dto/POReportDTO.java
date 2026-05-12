package com.nslindia.procurezone.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs for Purchase Order Reports
 */
public class POReportDTO {

    /**
     * PO Summary Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String poNumber;
        private LocalDate poDate;
        private String vendorName;
        private String vendorCode;
        private String status;
        private BigDecimal totalAmount;
        private LocalDate deliveryDate;
        private String paymentTerms;
        private Long itemCount;
        private Double totalQuantity;
    }

    /**
     * Vendor Performance Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorPerformance {
        private String vendorName;
        private String vendorCode;
        private Long totalPOs;
        private BigDecimal totalValue;
        private Long completedPOs;
        private Long delayedPOs;
        private Double avgDeliveryDays;
        private Long cancelledPOs;
        private Double onTimeDeliveryRate; // Percentage
    }
}
