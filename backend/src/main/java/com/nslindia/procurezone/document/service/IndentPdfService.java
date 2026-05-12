package com.nslindia.procurezone.document.service;

import com.nslindia.procurezone.indent.Indent;
import com.nslindia.procurezone.indent.IndentDetail;
import com.nslindia.procurezone.indent.IndentRepository;
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
import java.time.format.DateTimeFormatter;

/**
 * Service for generating Indent PDF documents
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndentPdfService {

    private final IndentRepository indentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    /**
     * Generate Indent PDF document
     */
    public byte[] generateIndentPdf(Long indentId) {
        log.info("Generating PDF for Indent ID: {}", indentId);

        Indent indent = indentRepository.findById(indentId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Indent not found: " + indentId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont boldFont = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Title
            Paragraph title = new Paragraph("MATERIAL INDENT")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            // Header Information
            Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            addHeaderRow(headerTable, "Indent No:", indent.getIndentNumber(), boldFont, regularFont);
            addHeaderRow(headerTable, "Indent Date:",
                    indent.getIndentDate() != null ? indent.getIndentDate().format(DATE_FORMATTER) : "N/A",
                    boldFont, regularFont);
            addHeaderRow(headerTable, "Department:",
                    indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A",
                    boldFont, regularFont);
            addHeaderRow(headerTable, "Company:",
                    indent.getCompany() != null ? indent.getCompany().getName() : "N/A",
                    boldFont, regularFont);
            addHeaderRow(headerTable, "Created By:",
                    indent.getCreatedBy() != null ? indent.getCreatedBy().getEmpName() : "N/A",
                    boldFont, regularFont);
            addHeaderRow(headerTable, "Required By:",
                    indent.getDeliveryDate() != null ? indent.getDeliveryDate().toString() : "N/A",
                    boldFont, regularFont);
            addHeaderRow(headerTable, "Status:",
                    indent.getStatus() != null ? indent.getStatus().getName() : "N/A",
                    boldFont, regularFont);

            document.add(headerTable);

            // Line Items Table
            Paragraph itemsTitle = new Paragraph("Material Details")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(itemsTitle);

            Table itemsTable = new Table(UnitValue.createPercentArray(
                    new float[] { 8, 25, 37, 15, 15 }))
                    .useAllAvailableWidth();

            // Header row
            addTableHeader(itemsTable, "S.No", boldFont);
            addTableHeader(itemsTable, "Material Code", boldFont);
            addTableHeader(itemsTable, "Material Name", boldFont);
            addTableHeader(itemsTable, "Quantity", boldFont);
            addTableHeader(itemsTable, "UOM", boldFont);

            // Data rows
            int sno = 1;
            for (IndentDetail detail : indent.getDetails()) {
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(sno++))));
                itemsTable.addCell(new Cell().add(new Paragraph(
                        detail.getMaterial() != null ? detail.getMaterial().getCode() : "N/A")));
                itemsTable.addCell(new Cell().add(new Paragraph(
                        detail.getMaterial() != null ? detail.getMaterial().getName() : "N/A")));
                itemsTable.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", detail.getQuantity())))
                        .setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph(
                        detail.getUnitOfMeasure() != null
                                ? detail.getUnitOfMeasure().getName()
                                : "N/A")));
            }

            document.add(itemsTable);

            // Remarks
            if (indent.getRemarks() != null && !indent.getRemarks().trim().isEmpty()) {
                Paragraph remarksTitle = new Paragraph("Remarks:")
                        .setFont(boldFont)
                        .setMarginTop(15);
                document.add(remarksTitle);

                Paragraph remarks = new Paragraph(indent.getRemarks())
                        .setFont(regularFont)
                        .setMarginBottom(20);
                document.add(remarks);
            }

            // Footer
            addFooter(document, boldFont);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating Indent PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Indent PDF", e);
        }
    }

    private void addHeaderRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addCell(new Cell().add(new Paragraph(value).setFont(regularFont)));
    }

    private void addTableHeader(Table table, String text, PdfFont boldFont) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold());
    }

    private void addFooter(Document document, PdfFont boldFont) {
        Paragraph footer = new Paragraph("\n\nFor NSL India\n\n\n")
                .setFont(boldFont)
                .setMarginTop(30);
        document.add(footer);

        Table signatureTable = new Table(UnitValue.createPercentArray(new float[] { 33, 34, 33 }))
                .useAllAvailableWidth();

        signatureTable.addCell(new Cell().add(new Paragraph("Requested By\n\n\n_________________"))
                .setBorder(null));
        signatureTable.addCell(new Cell().add(new Paragraph("Approved By\n\n\n_________________"))
                .setBorder(null).setTextAlignment(TextAlignment.CENTER));
        signatureTable.addCell(new Cell().add(new Paragraph("Authorized By\n\n\n_________________"))
                .setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        document.add(signatureTable);
    }

    private String getStatusText(Integer status) {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case 0 -> "Draft";
            case 1 -> "Submitted";
            case 2 -> "L1 Approved";
            case 3 -> "L1 Rejected";
            case 4 -> "L2 Approved";
            case 5 -> "L2 Rejected";
            case 6 -> "PO Created";
            case 7 -> "Closed";
            default -> "Unknown";
        };
    }

    private String getPriorityText(Integer priority) {
        if (priority == null)
            return "Normal";
        return switch (priority) {
            case 1 -> "High";
            case 2 -> "Medium";
            default -> "Normal";
        };
    }
}
