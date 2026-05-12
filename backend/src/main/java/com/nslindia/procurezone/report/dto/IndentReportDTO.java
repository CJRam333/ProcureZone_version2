package com.nslindia.procurezone.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs for Indent Reports
 */
public class IndentReportDTO {

    /**
     * Indent Summary Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String indentNo;
        private LocalDate indentDate;
        private String departmentName;
        private String companyName;
        private String createdBy;
        private String status;
        private Integer priority;
        private Long itemCount;
        private Double totalQuantity;
    }

    /**
     * Indent Detailed Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detailed {
        private String indentNo;
        private LocalDate indentDate;
        private String departmentName;
        private String materialName;
        private String materialCode;
        private Double quantity;
        private String uom;
        private BigDecimal rate;
        private LocalDate requiredDate;
        private String createdBy;
        private String status;
        private String remarks;
    }
}
