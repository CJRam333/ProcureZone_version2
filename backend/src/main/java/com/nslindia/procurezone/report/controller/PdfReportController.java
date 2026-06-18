package com.nslindia.procurezone.report.controller;

import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.indent.Indent;
import com.nslindia.procurezone.indent.IndentDetail;
import com.nslindia.procurezone.indent.IndentRepository;
import com.nslindia.procurezone.po.PORepository;
import com.nslindia.procurezone.po.PurchaseOrder;
import com.nslindia.procurezone.po.PurchaseOrderDetail;
import com.nslindia.procurezone.report.dto.IndentPdfData;
import com.nslindia.procurezone.report.dto.PdfReportRequest;
import com.nslindia.procurezone.report.dto.PurchaseOrderPdfData;
import com.nslindia.procurezone.report.service.PdfReportService;
import com.nslindia.procurezone.vendor.Vendor;
import com.nslindia.procurezone.vendor.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST Controller for PDF Report Generation
 * Provides endpoints to generate and download PDF reports
 */
@RestController
@RequestMapping("/api/v1/reports/pdf")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PdfReportController {

    private final PdfReportService pdfReportService;
    private final IndentRepository indentRepository;
    private final PORepository poRepository;
    private final VendorRepository vendorRepository;

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Generate generic PDF report
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'VIEWER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT')")
    public ResponseEntity<byte[]> generateReport(@RequestBody PdfReportRequest request) {
        try {
            log.info("Generating PDF report: {}", request.getTitle());

            byte[] pdfContent = pdfReportService.generateReport(request);

            String filename = sanitizeFilename(request.getTitle()) + "_" +
                    LocalDateTime.now().format(FILE_DATE_FORMAT) + ".pdf";

            return createPdfResponse(pdfContent, filename);

        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Indent PDF by ID
     */
    @GetMapping("/indent/{indentId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'VIEWER', 'DEPTHEAD', 'PROCUREMENT', 'FLOORINCHARGE')")
    public ResponseEntity<byte[]> generateIndentPdf(@PathVariable Integer indentId) {
        try {
            log.info("Generating PDF for Indent ID: {}", indentId);

            // Fetch indent data and convert to PDF data
            IndentPdfData pdfData = fetchIndentData(indentId);

            byte[] pdfContent = pdfReportService.generateIndentPdf(pdfData);

            String filename = "Indent_" + pdfData.getIndentNumber().replace("/", "-") + ".pdf";

            return createPdfResponse(pdfContent, filename);

        } catch (ResourceNotFoundException e) {
            log.warn("Indent not found for PDF generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error generating Indent PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Indent PDF from provided data (for preview)
     */
    @PostMapping("/indent/preview")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'VIEWER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT')")
    public ResponseEntity<byte[]> generateIndentPdfPreview(@RequestBody IndentPdfData pdfData) {
        try {
            log.info("Generating Indent PDF preview: {}", pdfData.getIndentNumber());

            // Validate required fields
            if (pdfData.getIndentNumber() == null || pdfData.getIndentNumber().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] pdfContent = pdfReportService.generateIndentPdf(pdfData);

            String filename = "Indent_" + pdfData.getIndentNumber().replace("/", "-") + "_preview.pdf";

            return createPdfResponse(pdfContent, filename);

        } catch (Exception e) {
            log.error("Error generating Indent PDF preview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Purchase Order PDF by ID
     */
    @GetMapping("/purchase-order/{poId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'PROCUREMENT')")
    public ResponseEntity<byte[]> generatePurchaseOrderPdf(@PathVariable Integer poId) {
        try {
            log.info("Generating PDF for Purchase Order ID: {}", poId);

            // Fetch PO data and convert to PDF data
            PurchaseOrderPdfData pdfData = fetchPurchaseOrderData(poId);

            byte[] pdfContent = pdfReportService.generatePurchaseOrderPdf(pdfData);

            String filename = "PO_" + pdfData.getPoNumber().replace("/", "-") + ".pdf";

            return createPdfResponse(pdfContent, filename);

        } catch (ResourceNotFoundException e) {
            log.warn("Purchase Order not found for PDF generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error generating PO PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Purchase Order PDF from provided data (for preview)
     */
    @PostMapping("/purchase-order/preview")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'PROCUREMENT')")
    public ResponseEntity<byte[]> generatePoPdfPreview(@RequestBody PurchaseOrderPdfData pdfData) {
        try {
            log.info("Generating PO PDF preview: {}", pdfData.getPoNumber());

            // Validate required fields
            if (pdfData.getPoNumber() == null || pdfData.getPoNumber().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] pdfContent = pdfReportService.generatePurchaseOrderPdf(pdfData);

            String filename = "PO_" + pdfData.getPoNumber().replace("/", "-") + "_preview.pdf";

            return createPdfResponse(pdfContent, filename);

        } catch (Exception e) {
            log.error("Error generating PO PDF preview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfContent, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(PDF_CONTENT_TYPE));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfContent.length);

        // Enable inline viewing in browser
        headers.add("Content-Disposition", "inline; filename=\"" + filename + "\"");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isEmpty()) {
            return "report";
        }
        return name.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    /**
     * Fetch indent data from database and convert to PDF DTO
     */
    private IndentPdfData fetchIndentData(Integer indentId) {
        Indent indent = indentRepository.findById(indentId)
                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + indentId));

        // Map indent details to PDF line items
        List<IndentPdfData.IndentItemPdfData> items = new ArrayList<>();
        AtomicInteger serialNo = new AtomicInteger(1);
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        if (indent.getDetails() != null) {
            for (IndentDetail detail : indent.getDetails()) {
                BigDecimal lineTotal = detail.getPricing() != null && detail.getQuantity() != null
                        ? detail.getPricing().multiply(detail.getQuantity())
                        : BigDecimal.ZERO;

                items.add(IndentPdfData.IndentItemPdfData.builder()
                        .serialNumber(serialNo.getAndIncrement())
                        .materialCode(detail.getMaterial() != null ? detail.getMaterial().getCode() : "")
                        .materialName(detail.getMaterial() != null ? detail.getMaterial().getName() : "")
                        .uomCode(detail.getUnitOfMeasure() != null ? detail.getUnitOfMeasure().getCode() : "")
                        .uomName(detail.getUnitOfMeasure() != null ? detail.getUnitOfMeasure().getName() : "")
                        .quantity(detail.getQuantity())
                        .rmQuantity(detail.getRmQuantity())
                        .deptQuantity(detail.getDeptQuantity())
                        .stockAvailable(detail.getStockAvailable())
                        .pricing(detail.getPricing())
                        .lineTotal(lineTotal)
                        .purpose(detail.getPurpose())
                        .vendor(detail.getVendor())
                        .itemStatus(detail.getStatus() != null ? getIndentDetailStatusName(detail.getStatus()) : "")
                        .build());

                totalAmount = totalAmount.add(lineTotal);
                if (detail.getQuantity() != null) {
                    totalQuantity = totalQuantity.add(detail.getQuantity());
                }
            }
        }

        return IndentPdfData.builder()
                .indentNumber(indent.getIndentNumber())
                .indentDate(indent.getIndentDate())
                .indentYear(indent.getIndentYear())
                .deliveryDate(indent.getDeliveryDate())
                .companyCode(indent.getCompany() != null ? indent.getCompany().getCode() : "")
                .companyName(indent.getCompany() != null ? indent.getCompany().getName() : "")
                .companyAddress(indent.getCompany() != null ? indent.getCompany().getName() : "") // Company entity
                                                                                                  // doesn't have
                                                                                                  // address
                .departmentName(indent.getDepartment() != null ? indent.getDepartment().getName() : "")
                .sectionName(indent.getSection() != null ? indent.getSection().getName() : "")
                .plantName(indent.getPlant() != null ? indent.getPlant().getName() : "")
                .requestorName(indent.getEmployee() != null ? indent.getEmployee().getFullName() : "")
                .requestorDesignation(indent.getEmployee() != null ? indent.getEmployee().getDesignation() : "")
                .requestorDepartment(indent.getDepartment() != null ? indent.getDepartment().getName() : "")
                .status(indent.getStatus() != null ? indent.getStatus().getName() : "")
                .approvedByName(indent.getApprovedBy() != null ? indent.getApprovedBy().getFullName() : "")
                .approvedDate(indent.getApprovedByDate())
                .approvalRemarks(indent.getRemarks())
                .finalApprovedByName(
                        indent.getFinalApprovedBy() != null ? indent.getFinalApprovedBy().getFullName() : "")
                .finalApprovedDate(indent.getFinalApprovedDate())
                .comments(indent.getComments())
                .poNumber(indent.getPoNumber())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(items.size())
                .totalQuantity(totalQuantity)
                .build();
    }

    /**
     * Fetch PO data from database and convert to PDF DTO
     */
    private PurchaseOrderPdfData fetchPurchaseOrderData(Integer poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + poId));

        Vendor vendor = vendorRepository.findById(po.getVendorId()).orElse(null);

        // Map PO details to PDF line items
        List<PurchaseOrderPdfData.PoItemPdfData> items = new ArrayList<>();
        AtomicInteger serialNo = new AtomicInteger(1);

        if (po.getDetails() != null) {
            for (PurchaseOrderDetail detail : po.getDetails()) {
                items.add(PurchaseOrderPdfData.PoItemPdfData.builder()
                        .serialNumber(serialNo.getAndIncrement())
                        .materialCode(detail.getMaterialCode())
                        .materialName(detail.getMaterialName())
                        .description(detail.getMaterialDescription())
                        .hsnCode("") // HSN code not stored in detail
                        .uomCode(detail.getUnitOfMeasure())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .taxRate(detail.getTaxRate())
                        .taxAmount(detail.getTaxAmount())
                        .lineTotal(detail.getLineTotal())
                        .expectedDelivery(detail.getExpectedDeliveryDate())
                        .build());
            }
        }

        return PurchaseOrderPdfData.builder()
                .poNumber(po.getPoNumber())
                .poDate(po.getPoDate() != null ? po.getPoDate().atStartOfDay() : null)
                .poType(po.getPriority())
                .deliveryDate(po.getExpectedDeliveryDate())
                .companyName("NSL India Pvt Ltd")
                .companyAddress("Hyderabad, Telangana, India")
                .vendorCode(vendor != null ? vendor.getVendorCode() : "")
                .vendorName(vendor != null ? vendor.getVendorName() : "")
                .vendorAddress(vendor != null ? buildVendorAddress(vendor) : "")
                .vendorGstin(vendor != null ? vendor.getGstNumber() : "")
                .vendorContact(vendor != null ? vendor.getContactPhone() : "")
                .vendorEmail(vendor != null ? vendor.getContactEmail() : "")
                .items(items)
                .subtotal(po.getTotalAmount())
                .taxAmount(po.getTaxAmount())
                .totalAmount(po.getNetAmount())
                .totalAmountInWords(convertAmountToWords(po.getNetAmount()))
                .paymentTerms(po.getPaymentTerms())
                .deliveryTerms(po.getDeliveryAddress())
                .specialInstructions(po.getNotes())
                .build();
    }

    // Helper methods
    private String buildCompanyAddress(com.nslindia.procurezone.masterdata.Company company) {
        // Company entity only has code, name, status - no address fields
        // Return company name as placeholder
        return company.getName() != null ? company.getName() : "";
    }

    private String buildVendorAddress(Vendor vendor) {
        StringBuilder addr = new StringBuilder();
        if (vendor.getAddressLine1() != null)
            addr.append(vendor.getAddressLine1());
        if (vendor.getAddressLine2() != null && !vendor.getAddressLine2().isBlank()) {
            if (addr.length() > 0)
                addr.append(", ");
            addr.append(vendor.getAddressLine2());
        }
        if (vendor.getCity() != null) {
            if (addr.length() > 0)
                addr.append(", ");
            addr.append(vendor.getCity());
        }
        if (vendor.getState() != null) {
            if (addr.length() > 0)
                addr.append(", ");
            addr.append(vendor.getState());
        }
        if (vendor.getPincode() != null) {
            if (addr.length() > 0)
                addr.append(" - ");
            addr.append(vendor.getPincode());
        }
        return addr.toString();
    }

    private String getIndentDetailStatusName(Integer status) {
        return switch (status) {
            case 1 -> "Pending";
            case 2 -> "Submitted";
            case 3 -> "Approved";
            case 4 -> "Rejected";
            case 5 -> "Ordered";
            case 6 -> "Received";
            default -> "Unknown";
        };
    }

    private String convertAmountToWords(BigDecimal amount) {
        if (amount == null)
            return "";
        // Simple implementation - in production use a library like NumberToWords
        long rupees = amount.longValue();
        return "Rupees " + rupees + " Only";
    }
}
