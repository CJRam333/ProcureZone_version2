package com.nslindia.procurezone.report.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;

import com.nslindia.procurezone.grn.GoodsReceiptRepository;
import com.nslindia.procurezone.indent.IndentRepository;
import com.nslindia.procurezone.po.PORepository;
import com.nslindia.procurezone.repository.inventory.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API Controller for JSON Report Data
 * Provides endpoints for dashboard statistics and report summaries
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportApiController {

    private final IndentRepository indentRepository;
    private final PORepository poRepository;
    private final GoodsReceiptRepository grnRepository;
    private final InventoryRepository inventoryRepository;

    // Status Constants aligned with legacy system tbl_indent_status and tbl_po_master
    private static final int INDENT_STATUS_PENDING = 2; // SUBMITTED
    private static final int INDENT_STATUS_APPROVED = 3; // DEPARTMENT HEAD APPROVED
    private static final int INDENT_STATUS_FINANCE_APPROVED = 4; // FINANCE APPROVED
    private static final int INDENT_STATUS_PROCUREMENT = 5; // PROCUREMENT APPROVED (PO_CREATED)
    private static final int INDENT_STATUS_REJECTED = 6; // REJECTED
    
    private static final int PO_STATUS_PENDING = 2; // PENDING APPROVAL
    private static final int PO_STATUS_CONFIRMED = 3; // CONFIRMED/APPROVED
    private static final int PO_STATUS_RECEIVED = 6; // FULLY RECEIVED
    private static final int GRN_STATUS_PENDING_QC = 2;

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Fetching dashboard statistics");

        Map<String, Object> stats = new HashMap<>();

        // Indent stats
        Map<String, Object> indentStats = new HashMap<>();
        indentStats.put("total", indentRepository.count());
        indentStats.put("pending", indentRepository.countByStatusId(INDENT_STATUS_PENDING));
        // Approved = Status 3 (Dept Head) + Status 4 (Finance) + Status 5 (Procurement)
        long approvedIndents = indentRepository.countByStatusId(INDENT_STATUS_APPROVED) + 
                               indentRepository.countByStatusId(INDENT_STATUS_FINANCE_APPROVED) +
                               indentRepository.countByStatusId(INDENT_STATUS_PROCUREMENT);
        indentStats.put("approved", approvedIndents);
        indentStats.put("rejected", indentRepository.countByStatusId(INDENT_STATUS_REJECTED));
        stats.put("indentStats", indentStats);

        // PO stats
        Map<String, Object> poStats = new HashMap<>();
        poStats.put("total", poRepository.count());
        
        // Use the simple count queries to avoid mapping exceptions from complex Map projections
        try {
            poStats.put("pending", poRepository.countByPoStatus(PO_STATUS_PENDING));
            poStats.put("delivered", poRepository.countByPoStatus(PO_STATUS_RECEIVED));
        } catch (Exception e) {
            log.error("Error fetching PO stats", e);
            poStats.put("pending", 0);
            poStats.put("delivered", 0);
        }
        stats.put("poStats", poStats);

        // GRN stats
        Map<String, Object> grnStats = new HashMap<>();
        grnStats.put("total", grnRepository.count());
        grnStats.put("pending", grnRepository.countByStatus(GRN_STATUS_PENDING_QC));
        stats.put("grnStats", grnStats);

        // Inventory stats
        Map<String, Object> inventoryStats = new HashMap<>();
        inventoryStats.put("totalItems", inventoryRepository.count());
        inventoryStats.put("lowStock", inventoryRepository.findLowStockItems().size());
        inventoryStats.put("outOfStock", inventoryRepository.findZeroStockItems().size());
        stats.put("inventoryStats", inventoryStats);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get indent summary report
     */
    @GetMapping("/indent-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<Map<String, Object>> getIndentSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer departmentId) {

        log.info("Fetching indent summary report");

        Map<String, Object> summary = new HashMap<>();

        summary.put("totalCount", indentRepository.count());
        summary.put("totalValue", 0);
        summary.put("pendingCount", indentRepository.countByStatusId(INDENT_STATUS_PENDING));
        summary.put("approvedCount", indentRepository.countByStatusId(INDENT_STATUS_APPROVED));
        summary.put("rejectedCount", indentRepository.countByStatusId(INDENT_STATUS_REJECTED));

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("PENDING_APPROVAL", indentRepository.countByStatusId(INDENT_STATUS_PENDING));
        byStatus.put("FULLY_APPROVED", indentRepository.countByStatusId(INDENT_STATUS_APPROVED));
        summary.put("byStatus", byStatus);

        summary.put("byDepartment", new HashMap<String, Long>());
        summary.put("byMonth", List.of());

        return ResponseEntity.ok(summary);
    }

    /**
     * Get indent details report
     */
    @GetMapping("/indent-details")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<List<Map<String, Object>>> getIndentDetails(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching indent details report");

        var indents = indentRepository.findAll();

        List<Map<String, Object>> details = indents.stream()
                .limit(100)
                .map(indent -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("indentId", indent.getId());
                    item.put("indentNo", indent.getIndentNumber());
                    item.put("indentDate", indent.getIndentDate().toString());
                    item.put("departmentName", indent.getDepartment() != null ? indent.getDepartment().getName() : "");
                    item.put("employeeName", indent.getEmployee() != null ? indent.getEmployee().getFullName() : "");
                    item.put("totalItems", indent.getDetails() != null ? indent.getDetails().size() : 0);
                    java.math.BigDecimal totalValue = indent.getDetails() != null ? indent.getDetails().stream()
                            .map(d -> d.getQuantity()
                                    .multiply(d.getPricing() != null ? d.getPricing() : java.math.BigDecimal.ZERO))
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add) : java.math.BigDecimal.ZERO;
                    item.put("totalValue", totalValue);
                    item.put("status", indent.getStatus() != null ? indent.getStatus().getName() : "UNKNOWN");
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(details);
    }

    /**
     * Get PO summary report
     */
    @GetMapping("/po-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<Map<String, Object>> getPOSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching PO summary report");

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", poRepository.count());
        summary.put("totalValue", 0);

        List<Map<String, Object>> counts = poRepository.getPOCountByStatus();
        // Convert list of maps to logic logic
        long pending = 0;
        long approved = 0;

        for (Map<String, Object> row : counts) {
            Integer status = (Integer) row.get("status");
            Long count = (Long) row.get("count");
            if (status == PO_STATUS_PENDING)
                pending += count;
            if (status == PO_STATUS_CONFIRMED)
                approved += count;
        }

        summary.put("pendingCount", pending);
        summary.put("approvedCount", approved);
        summary.put("rejectedCount", 0);

        Map<String, Long> byStatus = new HashMap<>();
        summary.put("byStatus", byStatus);
        summary.put("byDepartment", new HashMap<>());
        summary.put("byMonth", List.of());

        return ResponseEntity.ok(summary);
    }

    /**
     * Get PO details report
     */
    @GetMapping("/po-details")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<List<Map<String, Object>>> getPODetails(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching PO details report");

        var orders = poRepository.findAll();

        List<Map<String, Object>> details = orders.stream()
                .limit(100)
                .map(po -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("poId", po.getId());
                    item.put("poNumber", po.getPoNumber());
                    item.put("poDate", po.getPoDate().toString());
                    item.put("vendorName", "Vendor ID: " + po.getVendorId());
                    item.put("totalAmount", po.getTotalAmount());
                    item.put("status", String.valueOf(po.getPoStatus()));
                    item.put("deliveryDate", po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : null);
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(details);
    }

    /**
     * Get inventory summary report
     */
    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'STOREKEEPER', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        log.info("Fetching inventory summary report");

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", inventoryRepository.count());
        summary.put("totalValue", 0);
        summary.put("pendingCount", 0);
        summary.put("approvedCount", inventoryRepository.count());
        summary.put("rejectedCount", 0);

        Map<String, Long> byStatus = new HashMap<>();
        long lowStock = inventoryRepository.findLowStockItems().size();
        long zeroStock = inventoryRepository.findZeroStockItems().size();
        byStatus.put("IN_STOCK", inventoryRepository.count() - lowStock - zeroStock);
        byStatus.put("LOW_STOCK", lowStock);
        byStatus.put("OUT_OF_STOCK", zeroStock);
        summary.put("byStatus", byStatus);
        summary.put("byDepartment", new HashMap<>());
        summary.put("byMonth", List.of());

        return ResponseEntity.ok(summary);
    }

    /**
     * Get inventory details report
     */
    @GetMapping("/inventory-details")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'STOREKEEPER', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<List<Map<String, Object>>> getInventoryDetails() {
        log.info("Fetching inventory details report");

        var stocks = inventoryRepository.findAll();

        List<Map<String, Object>> details = stocks.stream()
                .limit(100)
                .map(stock -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("materialId", stock.getMaterial() != null ? stock.getMaterial().getId() : null);
                    item.put("materialCode", stock.getMaterial() != null ? stock.getMaterial().getCode() : "");
                    item.put("materialName", stock.getMaterial() != null ? stock.getMaterial().getDescription() : "");
                    item.put("uomName", stock.getUnitOfMeasure() != null
                            ? stock.getUnitOfMeasure().getCode()
                            : "");
                    item.put("currentStock", stock.getCurrentBalance());
                    item.put("reservedQty", stock.getReservedQuantity());
                    item.put("availableQty", stock.getAvailableQuantity());
                    item.put("reorderLevel", stock.getReorderLevel());
                    boolean isLow = stock.getReorderLevel() != null
                            && stock.getCurrentBalance().compareTo(stock.getReorderLevel()) <= 0;
                    item.put("status", isLow ? "LOW" : "OK");
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(details);
    }

    /**
     * Get low stock items
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'STOREKEEPER', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<List<Map<String, Object>>> getLowStockItems() {
        log.info("Fetching low stock items report");

        var lowStockItems = inventoryRepository.findLowStockItems();

        List<Map<String, Object>> details = lowStockItems.stream()
                .map(stock -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("materialId", stock.getMaterial() != null ? stock.getMaterial().getId() : null);
                    item.put("materialCode", stock.getMaterial() != null ? stock.getMaterial().getCode() : "");
                    item.put("materialName", stock.getMaterial() != null ? stock.getMaterial().getDescription() : "");
                    item.put("uomName", stock.getUnitOfMeasure() != null
                            ? stock.getUnitOfMeasure().getCode()
                            : "");
                    item.put("currentStock", stock.getCurrentBalance());
                    item.put("reorderLevel", stock.getReorderLevel());
                    item.put("status", "LOW");
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(details);
    }

    /**
     * Export report as Excel, PDF, or CSV
     * GET
     * /api/v1/reports/export/{reportType}?startDate=&endDate=&format=excel|pdf|csv
     */
    @GetMapping("/export/{reportType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'STOREKEEPER', 'DEPTHEAD', 'AUDITOR', 'VIEWER')")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "excel") String format) {

        log.info("Exporting report: type={}, format={}, startDate={}, endDate={}",
                reportType, format, startDate, endDate);

        try {
            List<Map<String, Object>> data;
            List<String> headers;
            String title;

            // Get data based on report type
            switch (reportType.toLowerCase()) {
                case "indent-summary":
                case "indent-details":
                    data = getIndentExportData();
                    headers = List.of("Indent No", "Date", "Department", "Employee", "Items", "Status");
                    title = "Indent Report";
                    break;
                case "po-summary":
                case "po-details":
                    data = getPOExportData();
                    headers = List.of("PO Number", "Date", "Vendor", "Amount", "Status", "Delivery Date");
                    title = "Purchase Order Report";
                    break;
                case "inventory-summary":
                case "inventory-details":
                    data = getInventoryExportData();
                    headers = List.of("Material Code", "Material Name", "UOM", "Current Stock", "Reorder Level",
                            "Status");
                    title = "Inventory Report";
                    break;
                case "low-stock":
                    data = getLowStockExportData();
                    headers = List.of("Material Code", "Material Name", "UOM", "Current Stock", "Reorder Level");
                    title = "Low Stock Report";
                    break;
                case "vendor-performance":
                    data = getVendorPerformanceExportData();
                    headers = List.of("Vendor Code", "Vendor Name", "Total POs", "Total Value", "On-Time %");
                    title = "Vendor Performance Report";
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            // Generate based on format
            byte[] bytes;
            String filename;
            String contentType;

            switch (format.toLowerCase()) {
                case "pdf":
                    bytes = generatePdfReport(title, headers, data);
                    filename = reportType + "_" + LocalDate.now() + ".pdf";
                    contentType = "application/pdf";
                    break;
                case "excel":
                case "xlsx":
                    bytes = generateExcelReport(title, headers, data);
                    filename = reportType + "_" + LocalDate.now() + ".xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "csv":
                default:
                    bytes = generateCsvReport(headers, data);
                    filename = reportType + "_" + LocalDate.now() + ".csv";
                    contentType = "text/csv";
                    break;
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", contentType)
                    .body(bytes);

        } catch (Exception e) {
            log.error("Error exporting report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate PDF report using iText
     */
    private byte[] generatePdfReport(String title, List<String> headers, List<Map<String, Object>> data)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        Paragraph titlePara = new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titlePara);

        // Date range
        Paragraph datePara = new Paragraph("Generated on: " + LocalDate.now())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10);
        document.add(datePara);

        // Create table with equal column widths
        float[] columnWidths = new float[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            columnWidths[i] = 1;
        }
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth();

        // Add header row
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
            table.addHeaderCell(cell);
        }

        // Add data rows
        for (Map<String, Object> row : data) {
            for (String header : headers) {
                Object val = row.get(header);
                String cellValue = val != null ? val.toString() : "";
                Cell cell = new Cell()
                        .add(new Paragraph(cellValue))
                        .setPadding(5);
                table.addCell(cell);
            }
        }

        document.add(table);

        // Footer with total count
        Paragraph footer = new Paragraph("Total Records: " + data.size())
                .setFontSize(10)
                .setMarginTop(20);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Generate Excel report using Apache POI
     */
    private byte[] generateExcelReport(String title, List<String> headers, List<Map<String, Object>> data)
            throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(title);

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Title row
            Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title + " - Generated: " + LocalDate.now());
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Header row
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.size(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 3;
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.size(); i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                    Object val = rowData.get(headers.get(i));
                    if (val != null) {
                        if (val instanceof Number) {
                            cell.setCellValue(((Number) val).doubleValue());
                        } else {
                            cell.setCellValue(val.toString());
                        }
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate CSV report
     */
    private byte[] generateCsvReport(List<String> headers, List<Map<String, Object>> data) {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", headers)).append("\n");

        for (Map<String, Object> row : data) {
            List<String> values = headers.stream()
                    .map(h -> {
                        Object val = row.get(h);
                        String strVal = val != null ? val.toString() : "";
                        // Escape quotes and wrap in quotes if contains comma
                        if (strVal.contains(",") || strVal.contains("\"")) {
                            strVal = "\"" + strVal.replace("\"", "\"\"") + "\"";
                        }
                        return strVal;
                    })
                    .collect(Collectors.toList());
            csv.append(String.join(",", values)).append("\n");
        }

        return csv.toString().getBytes();
    }

    private List<Map<String, Object>> getIndentExportData() {
        return indentRepository.findAll().stream()
                .limit(500)
                .map(indent -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("Indent No", indent.getIndentNumber());
                    row.put("Date", indent.getIndentDate() != null ? indent.getIndentDate().toString() : "");
                    row.put("Department", indent.getDepartment() != null ? indent.getDepartment().getName() : "");
                    row.put("Employee", indent.getEmployee() != null ? indent.getEmployee().getFullName() : "");
                    row.put("Items", indent.getDetails() != null ? indent.getDetails().size() : 0);
                    row.put("Status", indent.getStatus() != null ? indent.getStatus().getName() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPOExportData() {
        return poRepository.findAll().stream()
                .limit(500)
                .map(po -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("PO Number", po.getPoNumber());
                    row.put("Date", po.getPoDate() != null ? po.getPoDate().toString() : "");
                    row.put("Vendor", "Vendor #" + po.getVendorId());
                    row.put("Amount", po.getTotalAmount());
                    row.put("Status", po.getPoStatus());
                    row.put("Delivery Date", po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : "");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getInventoryExportData() {
        return inventoryRepository.findAll().stream()
                .limit(500)
                .map(stock -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("Material Code", stock.getMaterial() != null ? stock.getMaterial().getCode() : "");
                    row.put("Material Name", stock.getMaterial() != null ? stock.getMaterial().getDescription() : "");
                    row.put("UOM", stock.getUnitOfMeasure() != null ? stock.getUnitOfMeasure().getCode() : "");
                    row.put("Current Stock", stock.getCurrentBalance());
                    row.put("Reorder Level", stock.getReorderLevel());
                    boolean isLow = stock.getReorderLevel() != null
                            && stock.getCurrentBalance().compareTo(stock.getReorderLevel()) <= 0;
                    row.put("Status", isLow ? "LOW" : "OK");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getLowStockExportData() {
        return inventoryRepository.findLowStockItems().stream()
                .map(stock -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("Material Code", stock.getMaterial() != null ? stock.getMaterial().getCode() : "");
                    row.put("Material Name", stock.getMaterial() != null ? stock.getMaterial().getDescription() : "");
                    row.put("UOM", stock.getUnitOfMeasure() != null ? stock.getUnitOfMeasure().getCode() : "");
                    row.put("Current Stock", stock.getCurrentBalance());
                    row.put("Reorder Level", stock.getReorderLevel());
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getVendorPerformanceExportData() {
        // Simplified vendor performance data
        List<Map<String, Object>> data = new ArrayList<>();
        // Return empty list for now - can be enhanced later
        return data;
    }

    // NOTE: /vendor-performance endpoint is provided by
    // ComprehensiveReportController
    // with full implementation including date filtering and vendor ID parameters
}
