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
 * DTO for Purchase Order PDF generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderPdfData {

    // PO Header
    private String poNumber;
    private LocalDateTime poDate;
    private String poType;
    private LocalDate deliveryDate;

    // Company Info
    private String companyCode;
    private String companyName;
    private String companyAddress;
    private String companyGstin;
    private String plantName;
    private String plantAddress;

    // Vendor Info
    private String vendorCode;
    private String vendorName;
    private String vendorAddress;
    private String vendorGstin;
    private String vendorContact;
    private String vendorEmail;

    // Reference
    private String indentNumber;
    private String quotationNumber;

    // Line Items
    private List<PoItemPdfData> items;

    // Totals
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxPercentage;
    private BigDecimal totalAmount;
    private String totalAmountInWords;

    // Terms
    private String paymentTerms;
    private String deliveryTerms;
    private String warrantyTerms;
    private String specialInstructions;

    // Approval
    private String preparedBy;
    private String approvedBy;
    private LocalDateTime approvedDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PoItemPdfData {
        private Integer serialNumber;
        private String materialCode;
        private String materialName;
        private String description;
        private String hsnCode;
        private String uomCode;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal lineTotal;
        private LocalDate expectedDelivery;
    }
}
