package com.nslindia.procurezone.report.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.nslindia.procurezone.report.dto.IndentPdfData;
import com.nslindia.procurezone.report.dto.PdfReportRequest;
import com.nslindia.procurezone.report.dto.PurchaseOrderPdfData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Professional PDF Generation Service using OpenPDF
 * Supports multiple document types: Indents, Purchase Orders, GRN, Issue Notes
 * 
 * Features:
 * - Professional styling with company branding
 * - Header/Footer with page numbers
 * - Multi-page table support
 * - Summary sections
 * - Configurable page sizes and orientations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    @Value("${company.logo.path:static/images/logo.png}")
    private String logoPath;

    @Value("${company.name:NSL INDIA}")
    private String defaultCompanyName;

    @Value("${company.address:NSL India Pvt Ltd}")
    private String defaultCompanyAddress;

    // Fonts
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font DATA_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.DARK_GRAY);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

    // Colors
    private static final Color HEADER_BG_COLOR = new Color(0, 51, 102);
    private static final Color ALTERNATE_ROW_COLOR = new Color(240, 240, 245);
    private static final Color BORDER_COLOR = new Color(180, 180, 180);

    // Formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.of("en", "IN"));

    // =====================================================
    // Generic PDF Report Generation
    // =====================================================

    /**
     * Generate a generic PDF report from request data
     */
    public byte[] generateReport(PdfReportRequest request) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Rectangle pageSize = getPageSize(request.getPageSize(), request.getOrientation());
        Document document = new Document(pageSize, 36, 36, 72, 50);

        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // Add page event handler for headers/footers
        if (request.isIncludePageNumbers()) {
            writer.setPageEvent(new PageEventHelper(
                    request.getTitle(),
                    request.isIncludeTimestamp()));
        }

        document.open();

        try {
            // Add header with logo
            if (request.isIncludeLogo()) {
                addHeader(document, request.getCompanyName(), request.getTitle(), request.getSubtitle());
            } else {
                addSimpleHeader(document, request.getTitle(), request.getSubtitle());
            }

            // Add data table
            if (request.getHeaders() != null && request.getData() != null) {
                addDataTable(document, request.getHeaders(), request.getData(), request.getColumnWidths());
            }

            // Add summary section
            if (request.getSummary() != null && !request.getSummary().isEmpty()) {
                addSummarySection(document, request.getSummary());
            }

        } finally {
            document.close();
        }

        log.info("Generated PDF report: {} - {} rows", request.getTitle(),
                request.getData() != null ? request.getData().size() : 0);

        return baos.toByteArray();
    }

    // =====================================================
    // Indent PDF Generation
    // =====================================================

    /**
     * Generate professional Indent PDF
     */
    public byte[] generateIndentPdf(IndentPdfData data) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 72, 50);

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageEventHelper("INDENT: " + data.getIndentNumber(), true));

        document.open();

        try {
            // Company Header
            addCompanyHeader(document, data.getCompanyName(), data.getCompanyAddress());

            // Document Title
            Paragraph title = new Paragraph("MATERIAL INDENT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Indent Information Section
            addIndentInfoSection(document, data);

            // Line Items Table
            addIndentItemsTable(document, data.getItems());

            // Totals Section
            addIndentTotals(document, data);

            // Comments Section
            if (data.getComments() != null && !data.getComments().isEmpty()) {
                addCommentsSection(document, "Comments/Remarks", data.getComments());
            }

            // Approval Section
            addApprovalSection(document, data);

        } finally {
            document.close();
        }

        log.info("Generated Indent PDF: {}", data.getIndentNumber());
        return baos.toByteArray();
    }

    // =====================================================
    // Purchase Order PDF Generation
    // =====================================================

    /**
     * Generate professional Purchase Order PDF
     */
    public byte[] generatePurchaseOrderPdf(PurchaseOrderPdfData data) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 72, 50);

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageEventHelper("PURCHASE ORDER: " + data.getPoNumber(), true));

        document.open();

        try {
            // Company Header
            addCompanyHeader(document, data.getCompanyName(), data.getCompanyAddress());

            // Document Title
            Paragraph title = new Paragraph("PURCHASE ORDER", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // PO Info and Vendor Info side by side
            addPoInfoSection(document, data);

            // Line Items Table
            addPoItemsTable(document, data.getItems());

            // Totals Section
            addPoTotals(document, data);

            // Terms & Conditions
            addTermsSection(document, data);

            // Signature Section
            addPoSignatureSection(document, data);

        } finally {
            document.close();
        }

        log.info("Generated PO PDF: {}", data.getPoNumber());
        return baos.toByteArray();
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private Rectangle getPageSize(PdfReportRequest.PageSize size, PdfReportRequest.PageOrientation orientation) {
        Rectangle pageSize = switch (size) {
            case A3 -> PageSize.A3;
            case LETTER -> PageSize.LETTER;
            case LEGAL -> PageSize.LEGAL;
            default -> PageSize.A4;
        };

        return orientation == PdfReportRequest.PageOrientation.LANDSCAPE ? pageSize.rotate() : pageSize;
    }

    private void addHeader(Document document, String companyName, String title, String subtitle)
            throws DocumentException {

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 30, 70 });

        // Logo placeholder (left cell)
        PdfPCell logoCell = new PdfPCell();
        try {
            Image logo = Image.getInstance(new ClassPathResource(logoPath).getURL());
            logo.scaleToFit(100, 50);
            logoCell.addElement(logo);
        } catch (Exception e) {
            // Logo not found, add company name instead
            logoCell.addElement(new Phrase(companyName != null ? companyName : defaultCompanyName, TITLE_FONT));
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(logoCell);

        // Title and subtitle (right cell)
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(titlePara);

        if (subtitle != null && !subtitle.isEmpty()) {
            Paragraph subtitlePara = new Paragraph(subtitle, SUBTITLE_FONT);
            subtitlePara.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(subtitlePara);
        }

        headerTable.addCell(titleCell);
        headerTable.setSpacingAfter(20);

        document.add(headerTable);
    }

    private void addSimpleHeader(Document document, String title, String subtitle) throws DocumentException {
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        if (subtitle != null && !subtitle.isEmpty()) {
            Paragraph subtitlePara = new Paragraph(subtitle, SUBTITLE_FONT);
            subtitlePara.setAlignment(Element.ALIGN_CENTER);
            subtitlePara.setSpacingAfter(20);
            document.add(subtitlePara);
        }
    }

    private void addCompanyHeader(Document document, String companyName, String address) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingBottom(10);

        Paragraph namePara = new Paragraph(companyName != null ? companyName : defaultCompanyName, TITLE_FONT);
        namePara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(namePara);

        if (address != null && !address.isEmpty()) {
            Paragraph addrPara = new Paragraph(address, SUBTITLE_FONT);
            addrPara.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(addrPara);
        }

        headerTable.addCell(cell);
        headerTable.setSpacingAfter(10);

        document.add(headerTable);

        // Add separator line
        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidthTop(2);
        lineCell.setBorderColorTop(HEADER_BG_COLOR);
        lineCell.setBorderWidthBottom(0);
        lineCell.setBorderWidthLeft(0);
        lineCell.setBorderWidthRight(0);
        lineCell.setFixedHeight(5);
        separator.addCell(lineCell);
        separator.setSpacingAfter(15);
        document.add(separator);
    }

    private void addDataTable(Document document, List<String> headers, List<Map<String, Object>> data,
            float[] columnWidths) throws DocumentException {

        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);

        if (columnWidths != null && columnWidths.length == headers.size()) {
            table.setWidths(columnWidths);
        }

        // Add headers
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Add data rows
        boolean alternate = false;
        for (Map<String, Object> row : data) {
            for (String header : headers) {
                Object value = row.get(header);
                String displayValue = formatValue(value);

                PdfPCell cell = new PdfPCell(new Phrase(displayValue, DATA_FONT));
                if (alternate) {
                    cell.setBackgroundColor(ALTERNATE_ROW_COLOR);
                }
                cell.setPadding(6);

                // Right-align numbers
                if (value instanceof Number) {
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }

                table.addCell(cell);
            }
            alternate = !alternate;
        }

        table.setSpacingAfter(20);
        document.add(table);
    }

    private void addSummarySection(Document document, Map<String, Object> summary) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("Summary", LABEL_FONT);
        summaryTitle.setSpacingBefore(10);
        document.add(summaryTitle);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setWidths(new float[] { 60, 40 });

        for (Map.Entry<String, Object> entry : summary.entrySet()) {
            PdfPCell labelCell = new PdfPCell(new Phrase(entry.getKey() + ":", LABEL_FONT));
            labelCell.setBorder(Rectangle.NO_BORDER);
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelCell.setPadding(4);
            summaryTable.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(formatValue(entry.getValue()), TOTAL_FONT));
            valueCell.setBorder(Rectangle.NO_BORDER);
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valueCell.setPadding(4);
            summaryTable.addCell(valueCell);
        }

        document.add(summaryTable);
    }

    private void addIndentInfoSection(Document document, IndentPdfData data) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 20, 30, 20, 30 });

        // Row 1
        addLabelValueCell(infoTable, "Indent No:", data.getIndentNumber());
        addLabelValueCell(infoTable, "Indent Date:", formatDateTime(data.getIndentDate()));

        // Row 2
        addLabelValueCell(infoTable, "Department:", data.getDepartmentName());
        addLabelValueCell(infoTable, "Plant:", data.getPlantName());

        // Row 3
        addLabelValueCell(infoTable, "Requestor:", data.getRequestorName());
        addLabelValueCell(infoTable, "Delivery Date:", formatDate(data.getDeliveryDate()));

        // Row 4
        addLabelValueCell(infoTable, "Status:", data.getStatus());
        addLabelValueCell(infoTable, "PO Number:", data.getPoNumber() != null ? data.getPoNumber() : "-");

        infoTable.setSpacingAfter(15);
        document.add(infoTable);
    }

    private void addIndentItemsTable(Document document, List<IndentPdfData.IndentItemPdfData> items)
            throws DocumentException {

        if (items == null || items.isEmpty()) {
            return;
        }

        Paragraph itemsTitle = new Paragraph("Line Items", LABEL_FONT);
        itemsTitle.setSpacingBefore(10);
        itemsTitle.setSpacingAfter(5);
        document.add(itemsTitle);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 5, 12, 30, 8, 10, 10, 10, 15 });

        // Headers
        String[] headers = { "#", "Code", "Material", "UOM", "Qty", "Stock", "Rate", "Amount" };
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Data rows
        boolean alternate = false;
        for (IndentPdfData.IndentItemPdfData item : items) {
            Color bgColor = alternate ? ALTERNATE_ROW_COLOR : Color.WHITE;

            addDataCell(table, String.valueOf(item.getSerialNumber()), bgColor, Element.ALIGN_CENTER);
            addDataCell(table, item.getMaterialCode(), bgColor, Element.ALIGN_LEFT);
            addDataCell(table, item.getMaterialName(), bgColor, Element.ALIGN_LEFT);
            addDataCell(table, item.getUomCode(), bgColor, Element.ALIGN_CENTER);
            addDataCell(table, formatNumber(item.getQuantity()), bgColor, Element.ALIGN_RIGHT);
            addDataCell(table, formatNumber(item.getStockAvailable()), bgColor, Element.ALIGN_RIGHT);
            addDataCell(table, formatCurrency(item.getPricing()), bgColor, Element.ALIGN_RIGHT);
            addDataCell(table, formatCurrency(item.getLineTotal()), bgColor, Element.ALIGN_RIGHT);

            alternate = !alternate;
        }

        table.setSpacingAfter(10);
        document.add(table);
    }

    private void addIndentTotals(Document document, IndentPdfData data) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[] { 60, 40 });

        addTotalRow(totalsTable, "Total Items:", String.valueOf(data.getTotalItems()));
        addTotalRow(totalsTable, "Total Quantity:", formatNumber(data.getTotalQuantity()));
        addTotalRow(totalsTable, "Total Amount:", formatCurrency(data.getTotalAmount()));

        totalsTable.setSpacingAfter(15);
        document.add(totalsTable);
    }

    private void addApprovalSection(Document document, IndentPdfData data) throws DocumentException {
        PdfPTable approvalTable = new PdfPTable(3);
        approvalTable.setWidthPercentage(100);
        approvalTable.setWidths(new float[] { 33, 34, 33 });
        approvalTable.setSpacingBefore(30);

        // Prepared By
        PdfPCell prepCell = createSignatureCell("Prepared By", data.getRequestorName());
        approvalTable.addCell(prepCell);

        // Approved By (Dept Head)
        PdfPCell approveCell = createSignatureCell("Approved By",
                data.getApprovedByName() != null ? data.getApprovedByName() : "");
        approvalTable.addCell(approveCell);

        // Final Approved By
        PdfPCell finalCell = createSignatureCell("Final Approval",
                data.getFinalApprovedByName() != null ? data.getFinalApprovedByName() : "");
        approvalTable.addCell(finalCell);

        document.add(approvalTable);
    }

    private void addPoInfoSection(Document document, PurchaseOrderPdfData data) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 50, 50 });

        // Left: PO Details
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.BOX);
        leftCell.setPadding(10);

        Paragraph poInfo = new Paragraph();
        poInfo.add(new Chunk("PO Number: ", LABEL_FONT));
        poInfo.add(new Chunk(data.getPoNumber() + "\n", VALUE_FONT));
        poInfo.add(new Chunk("PO Date: ", LABEL_FONT));
        poInfo.add(new Chunk(formatDateTime(data.getPoDate()) + "\n", VALUE_FONT));
        poInfo.add(new Chunk("Delivery Date: ", LABEL_FONT));
        poInfo.add(new Chunk(formatDate(data.getDeliveryDate()) + "\n", VALUE_FONT));
        poInfo.add(new Chunk("Indent Ref: ", LABEL_FONT));
        poInfo.add(new Chunk(data.getIndentNumber() != null ? data.getIndentNumber() : "-", VALUE_FONT));
        leftCell.addElement(poInfo);
        infoTable.addCell(leftCell);

        // Right: Vendor Details
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setPadding(10);

        Paragraph vendorInfo = new Paragraph();
        vendorInfo.add(new Chunk("VENDOR DETAILS\n\n", LABEL_FONT));
        vendorInfo.add(new Chunk(data.getVendorName() + "\n", VALUE_FONT));
        if (data.getVendorAddress() != null) {
            vendorInfo.add(new Chunk(data.getVendorAddress() + "\n", VALUE_FONT));
        }
        if (data.getVendorGstin() != null) {
            vendorInfo.add(new Chunk("GSTIN: " + data.getVendorGstin() + "\n", VALUE_FONT));
        }
        if (data.getVendorContact() != null) {
            vendorInfo.add(new Chunk("Contact: " + data.getVendorContact(), VALUE_FONT));
        }
        rightCell.addElement(vendorInfo);
        infoTable.addCell(rightCell);

        infoTable.setSpacingAfter(15);
        document.add(infoTable);
    }

    private void addPoItemsTable(Document document, List<PurchaseOrderPdfData.PoItemPdfData> items)
            throws DocumentException {

        if (items == null || items.isEmpty()) {
            return;
        }

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 5, 12, 30, 8, 12, 12, 15 });

        String[] headers = { "#", "Code", "Description", "UOM", "Qty", "Unit Price", "Amount" };
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        boolean alternate = false;
        for (PurchaseOrderPdfData.PoItemPdfData item : items) {
            Color bgColor = alternate ? ALTERNATE_ROW_COLOR : Color.WHITE;

            addDataCell(table, String.valueOf(item.getSerialNumber()), bgColor, Element.ALIGN_CENTER);
            addDataCell(table, item.getMaterialCode(), bgColor, Element.ALIGN_LEFT);
            addDataCell(table, item.getMaterialName() +
                    (item.getDescription() != null ? "\n" + item.getDescription() : ""), bgColor, Element.ALIGN_LEFT);
            addDataCell(table, item.getUomCode(), bgColor, Element.ALIGN_CENTER);
            addDataCell(table, formatNumber(item.getQuantity()), bgColor, Element.ALIGN_RIGHT);
            addDataCell(table, formatCurrency(item.getUnitPrice()), bgColor, Element.ALIGN_RIGHT);
            addDataCell(table, formatCurrency(item.getLineTotal()), bgColor, Element.ALIGN_RIGHT);

            alternate = !alternate;
        }

        table.setSpacingAfter(10);
        document.add(table);
    }

    private void addPoTotals(Document document, PurchaseOrderPdfData data) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[] { 60, 40 });

        addTotalRow(totalsTable, "Subtotal:", formatCurrency(data.getSubtotal()));
        addTotalRow(totalsTable, "Tax (" + formatNumber(data.getTaxPercentage()) + "%):",
                formatCurrency(data.getTaxAmount()));

        // Grand total with bold styling
        PdfPCell labelCell = new PdfPCell(new Phrase("GRAND TOTAL:", TOTAL_FONT));
        labelCell.setBorder(Rectangle.TOP);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingTop(8);
        totalsTable.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatCurrency(data.getTotalAmount()), TOTAL_FONT));
        valueCell.setBorder(Rectangle.TOP);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(8);
        totalsTable.addCell(valueCell);

        if (data.getTotalAmountInWords() != null) {
            PdfPCell wordsCell = new PdfPCell(new Phrase("(" + data.getTotalAmountInWords() + ")", VALUE_FONT));
            wordsCell.setColspan(2);
            wordsCell.setBorder(Rectangle.NO_BORDER);
            wordsCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            wordsCell.setPaddingTop(5);
            totalsTable.addCell(wordsCell);
        }

        totalsTable.setSpacingAfter(15);
        document.add(totalsTable);
    }

    private void addTermsSection(Document document, PurchaseOrderPdfData data) throws DocumentException {
        Paragraph termsTitle = new Paragraph("Terms & Conditions", LABEL_FONT);
        termsTitle.setSpacingBefore(10);
        document.add(termsTitle);

        PdfPTable termsTable = new PdfPTable(2);
        termsTable.setWidthPercentage(100);
        termsTable.setWidths(new float[] { 25, 75 });

        if (data.getPaymentTerms() != null) {
            addTermsRow(termsTable, "Payment Terms:", data.getPaymentTerms());
        }
        if (data.getDeliveryTerms() != null) {
            addTermsRow(termsTable, "Delivery Terms:", data.getDeliveryTerms());
        }
        if (data.getWarrantyTerms() != null) {
            addTermsRow(termsTable, "Warranty:", data.getWarrantyTerms());
        }
        if (data.getSpecialInstructions() != null) {
            addTermsRow(termsTable, "Special Instructions:", data.getSpecialInstructions());
        }

        termsTable.setSpacingAfter(15);
        document.add(termsTable);
    }

    private void addPoSignatureSection(Document document, PurchaseOrderPdfData data) throws DocumentException {
        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setWidths(new float[] { 50, 50 });
        sigTable.setSpacingBefore(40);

        PdfPCell prepCell = createSignatureCell("Prepared By", data.getPreparedBy());
        sigTable.addCell(prepCell);

        PdfPCell authCell = createSignatureCell("Authorized Signatory", data.getApprovedBy());
        sigTable.addCell(authCell);

        document.add(sigTable);
    }

    private void addCommentsSection(Document document, String title, String content) throws DocumentException {
        Paragraph commentsTitle = new Paragraph(title, LABEL_FONT);
        commentsTitle.setSpacingBefore(10);
        document.add(commentsTitle);

        PdfPTable commentsTable = new PdfPTable(1);
        commentsTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase(content, VALUE_FONT));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        cell.setBorderColor(BORDER_COLOR);
        commentsTable.addCell(cell);

        commentsTable.setSpacingAfter(10);
        document.add(commentsTable);
    }

    // =====================================================
    // Cell Helper Methods
    // =====================================================

    private void addLabelValueCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", VALUE_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addDataCell(PdfPTable table, String value, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", DATA_FONT));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addTermsRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private PdfPCell createSignatureCell(String title, String name) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Chunk("\n\n\n_________________________\n", VALUE_FONT));
        p.add(new Chunk(title + "\n", LABEL_FONT));
        if (name != null && !name.isEmpty()) {
            p.add(new Chunk(name, VALUE_FONT));
        }
        cell.addElement(p);

        return cell;
    }

    // =====================================================
    // Formatting Methods
    // =====================================================

    private String formatValue(Object value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof BigDecimal) {
            return NUMBER_FORMAT.format(((BigDecimal) value).doubleValue());
        }
        if (value instanceof Number) {
            return NUMBER_FORMAT.format(((Number) value).doubleValue());
        }
        if (value instanceof LocalDate) {
            return formatDate((LocalDate) value);
        }
        if (value instanceof LocalDateTime) {
            return formatDateTime((LocalDateTime) value);
        }
        return value.toString();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "-";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        return String.format("₹ %,.2f", amount);
    }

    private String formatNumber(BigDecimal number) {
        if (number == null) {
            return "-";
        }
        return NUMBER_FORMAT.format(number.doubleValue());
    }

    // =====================================================
    // Page Event Helper (Header/Footer)
    // =====================================================

    private static class PageEventHelper extends PdfPageEventHelper {
        private final String documentTitle;
        private final boolean includeTimestamp;
        private PdfTemplate totalPages;
        private BaseFont baseFont;

        public PageEventHelper(String documentTitle, boolean includeTimestamp) {
            this.documentTitle = documentTitle;
            this.includeTimestamp = includeTimestamp;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(30, 16);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new RuntimeException("Error creating font", e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Footer
            float footerY = document.bottom() - 20;

            // Page number on right
            String pageText = "Page " + writer.getPageNumber() + " of ";
            float textWidth = baseFont.getWidthPoint(pageText, 8);
            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setColorFill(Color.GRAY);
            cb.setTextMatrix(document.right() - textWidth - 30, footerY);
            cb.showText(pageText);
            cb.endText();
            cb.addTemplate(totalPages, document.right() - 30, footerY);

            // Generated date on left
            if (includeTimestamp) {
                String dateText = "Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER);
                cb.beginText();
                cb.setFontAndSize(baseFont, 8);
                cb.setColorFill(Color.GRAY);
                cb.setTextMatrix(document.left(), footerY);
                cb.showText(dateText);
                cb.endText();
            }

            // Document title in center
            if (documentTitle != null) {
                cb.beginText();
                cb.setFontAndSize(baseFont, 8);
                cb.setColorFill(Color.GRAY);
                float titleWidth = baseFont.getWidthPoint(documentTitle, 8);
                float center = (document.right() + document.left()) / 2 - titleWidth / 2;
                cb.setTextMatrix(center, footerY);
                cb.showText(documentTitle);
                cb.endText();
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPages.beginText();
            totalPages.setFontAndSize(baseFont, 8);
            totalPages.showText(String.valueOf(writer.getPageNumber()));
            totalPages.endText();
        }
    }
}
