package com.nslindia.procurezone.document.service;

import com.nslindia.procurezone.po.PurchaseOrder;
import com.nslindia.procurezone.po.PurchaseOrderDetail;
import com.nslindia.procurezone.po.PORepository;
import com.nslindia.procurezone.vendor.Vendor;
import com.nslindia.procurezone.vendor.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating Purchase Order PDF documents
 * Uses iText library for PDF generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderPdfService {

        private final PORepository poRepository;
        private final VendorRepository vendorRepository;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        /**
         * Generate PO PDF document
         * 
         * @param poId Purchase Order ID
         * @return PDF as byte array
         */
        public byte[] generatePOPdf(Long poId) {
                log.info("Generating PDF for PO ID: {}", poId);

                PurchaseOrder po = poRepository.findById(poId.intValue())
                                .orElseThrow(() -> new IllegalArgumentException("PO not found: " + poId));

                // Lookup vendor by vendorId
                Vendor vendor = vendorRepository.findById(po.getVendorId()).orElse(null);
                String vendorName = vendor != null ? vendor.getVendorName() : "N/A";

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf);

                        // Fonts
                        PdfFont boldFont = PdfFontFactory.createFont(
                                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
                        PdfFont regularFont = PdfFontFactory.createFont(
                                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

                        // Title
                        Paragraph title = new Paragraph("PURCHASE ORDER")
                                        .setFont(boldFont)
                                        .setFontSize(20)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setBold()
                                        .setMarginBottom(20);
                        document.add(title);

                        // PO Header Information
                        Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                                        .useAllAvailableWidth()
                                        .setMarginBottom(20);

                        addHeaderRow(headerTable, "PO Number:", po.getPoNumber(), boldFont, regularFont);
                        addHeaderRow(headerTable, "PO Date:",
                                        po.getPoDate() != null ? po.getPoDate().format(DATE_FORMATTER) : "N/A",
                                        boldFont, regularFont);
                        addHeaderRow(headerTable, "Vendor:", vendorName, boldFont, regularFont);
                        addHeaderRow(headerTable, "Delivery Date:",
                                        po.getDeliveryDate() != null ? po.getDeliveryDate().format(DATE_FORMATTER)
                                                        : "N/A",
                                        boldFont, regularFont);
                        addHeaderRow(headerTable, "Payment Terms:",
                                        po.getPaymentTerms() != null ? po.getPaymentTerms() : "N/A",
                                        boldFont, regularFont);
                        addHeaderRow(headerTable, "Status:", getStatusText(po.getPoStatus()), boldFont, regularFont);

                        document.add(headerTable);

                        // Line Items Table
                        Paragraph itemsTitle = new Paragraph("Line Items")
                                        .setFont(boldFont)
                                        .setFontSize(14)
                                        .setMarginTop(10)
                                        .setMarginBottom(10);
                        document.add(itemsTitle);

                        Table itemsTable = new Table(UnitValue.createPercentArray(
                                        new float[] { 5, 15, 35, 10, 10, 10, 15 }))
                                        .useAllAvailableWidth();

                        // Table header
                        String[] headers = { "S.No", "Material Code", "Material Name", "Qty", "UOM", "Rate", "Amount" };
                        for (String header : headers) {
                                Cell cell = new Cell()
                                                .add(new Paragraph(header).setFont(boldFont).setFontSize(10))
                                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setPadding(5);
                                itemsTable.addHeaderCell(cell);
                        }

                        // Table data
                        int sno = 1;
                        BigDecimal totalAmount = BigDecimal.ZERO;

                        for (PurchaseOrderDetail detail : po.getDetails()) {
                                itemsTable.addCell(
                                                createCell(String.valueOf(sno++), regularFont, TextAlignment.CENTER));
                                itemsTable.addCell(createCell(
                                                detail.getMaterialCode() != null ? detail.getMaterialCode() : "N/A",
                                                regularFont, TextAlignment.LEFT));
                                itemsTable.addCell(createCell(
                                                detail.getMaterialName() != null ? detail.getMaterialName() : "N/A",
                                                regularFont, TextAlignment.LEFT));
                                itemsTable.addCell(createCell(
                                                String.format("%.2f", detail.getQuantity()),
                                                regularFont, TextAlignment.RIGHT));
                                itemsTable.addCell(createCell(
                                                detail.getUnitOfMeasure() != null ? detail.getUnitOfMeasure() : "N/A",
                                                regularFont, TextAlignment.CENTER));
                                itemsTable.addCell(createCell(
                                                String.format("%.2f", detail.getUnitPrice()),
                                                regularFont, TextAlignment.RIGHT));

                                BigDecimal amount = detail.getLineTotal() != null ? detail.getLineTotal()
                                                : BigDecimal.ZERO;
                                totalAmount = totalAmount.add(amount);

                                itemsTable.addCell(createCell(
                                                String.format("%.2f", amount),
                                                regularFont, TextAlignment.RIGHT));
                        }

                        // Total row
                        Cell totalLabelCell = new Cell(1, 6)
                                        .add(new Paragraph("TOTAL").setFont(boldFont).setFontSize(10))
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setPadding(5)
                                        .setBackgroundColor(ColorConstants.LIGHT_GRAY);
                        itemsTable.addCell(totalLabelCell);

                        Cell totalAmountCell = new Cell()
                                        .add(new Paragraph(String.format("%.2f", totalAmount)).setFont(boldFont)
                                                        .setFontSize(10))
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setPadding(5)
                                        .setBackgroundColor(ColorConstants.LIGHT_GRAY);
                        itemsTable.addCell(totalAmountCell);

                        document.add(itemsTable);

                        // Terms and Conditions
                        if (po.getTermsConditions() != null && !po.getTermsConditions().isEmpty()) {
                                Paragraph termsTitle = new Paragraph("Terms & Conditions")
                                                .setFont(boldFont)
                                                .setFontSize(12)
                                                .setMarginTop(20)
                                                .setMarginBottom(10);
                                document.add(termsTitle);

                                Paragraph terms = new Paragraph(po.getTermsConditions())
                                                .setFont(regularFont)
                                                .setFontSize(10);
                                document.add(terms);
                        }

                        // Footer
                        Paragraph footer = new Paragraph(
                                        "\nThis is a computer-generated document. No signature required.")
                                        .setFont(regularFont)
                                        .setFontSize(8)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginTop(30)
                                        .setItalic();
                        document.add(footer);

                        document.close();

                        log.info("Successfully generated PDF for PO: {}", po.getPoNumber());
                        return baos.toByteArray();

                } catch (Exception e) {
                        log.error("Error generating PDF for PO ID: {}", poId, e);
                        throw new RuntimeException("Failed to generate PO PDF", e);
                }
        }

        private void addHeaderRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
                table.addCell(new Cell()
                                .add(new Paragraph(label).setFont(boldFont).setFontSize(10))
                                .setBorder(null)
                                .setPadding(3));
                table.addCell(new Cell()
                                .add(new Paragraph(value).setFont(regularFont).setFontSize(10))
                                .setBorder(null)
                                .setPadding(3));
        }

        private Cell createCell(String content, PdfFont font, TextAlignment alignment) {
                return new Cell()
                                .add(new Paragraph(content).setFont(font).setFontSize(9))
                                .setTextAlignment(alignment)
                                .setPadding(5);
        }

        private String getStatusText(Integer status) {
                if (status == null)
                        return "Unknown";
                return switch (status) {
                        case 1 -> "Created";
                        case 2 -> "Approved";
                        case 3 -> "Sent to Vendor";
                        case 4 -> "Partially Received";
                        case 5 -> "Fully Received";
                        case 6 -> "Closed";
                        case 7 -> "Cancelled";
                        default -> "Unknown";
                };
        }
}
