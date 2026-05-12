package com.nslindia.procurezone.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTOs for Vendor Performance Reports
 */
public class VendorPerformanceDTO {

    /**
     * Vendor Performance Summary DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer vendorId;
        private String vendorName;
        private String vendorCode;
        private String vendorCategory;
        private Integer totalPOs;
        private Double totalPurchaseValue;
        private Integer fulfilledPOs;
        private Integer cancelledPOs;
        private Double avgDeliveryDays;
        private Integer totalGRNs;
        private Integer acceptedGRNs;
        private Integer rejectedGRNs;
        private Double fulfillmentRate; // percentage
        private Double qualityRate; // percentage
    }

    /**
     * Delivery Performance DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryPerformance {
        private String vendorName;
        private String poNumber;
        private LocalDate poDate;
        private LocalDate expectedDate;
        private LocalDate actualDate;
        private Integer delayDays;
        private String deliveryStatus; // On Time, Minor Delay, Major Delay
        private Double poValue;
    }
}
