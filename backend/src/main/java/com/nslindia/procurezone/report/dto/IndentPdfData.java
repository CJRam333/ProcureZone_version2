package com.nslindia.procurezone.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Indent PDF generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndentPdfData {

    // Header Information
    private String indentNumber;
    private LocalDateTime indentDate;
    private String indentYear;
    private LocalDate deliveryDate;

    // Company/Org Info
    private String companyCode;
    private String companyName;
    private String companyAddress;
    private String departmentName;
    private String sectionName;
    private String plantName;

    // Requester Info
    private String requestorName;
    private String requestorDesignation;
    private String requestorDepartment;

    // Status & Approval
    private String status;
    private String approvedByName;
    private LocalDateTime approvedDate;
    private String approvalRemarks;
    private String finalApprovedByName;
    private LocalDateTime finalApprovedDate;
    private String finalRemarks;

    // Comments
    private String comments;
    private String poNumber;

    // Line Items
    private List<IndentItemPdfData> items;

    // Totals
    private BigDecimal totalAmount;
    private Integer totalItems;
    private BigDecimal totalQuantity;

    /**
     * Individual indent line item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndentItemPdfData {
        private Integer serialNumber;
        private String materialCode;
        private String materialName;
        private String uomCode;
        private String uomName;
        private BigDecimal quantity;
        private BigDecimal rmQuantity;
        private BigDecimal deptQuantity;
        private BigDecimal stockAvailable;
        private BigDecimal pricing;
        private BigDecimal lineTotal;
        private String purpose;
        private String vendor;
        private String itemStatus;
    }
}
