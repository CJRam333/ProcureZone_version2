package com.nslindia.procurezone.report.controller;

import com.nslindia.procurezone.report.dto.IndentReportDTO;
import com.nslindia.procurezone.report.dto.InventoryReportDTO;
import com.nslindia.procurezone.report.dto.POReportDTO;
import com.nslindia.procurezone.report.dto.VendorPerformanceDTO;
import com.nslindia.procurezone.report.service.ExcelReportService;
import com.nslindia.procurezone.report.service.IndentReportService;
import com.nslindia.procurezone.report.service.InventoryReportService;
import com.nslindia.procurezone.report.service.POReportService;
import com.nslindia.procurezone.report.service.VendorPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Report Controller
 * Provides all report endpoints with JSON and Excel export functionality
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComprehensiveReportController {

        private final IndentReportService indentReportService;
        private final POReportService poReportService;
        private final InventoryReportService inventoryReportService;
        private final VendorPerformanceService vendorPerformanceService;
        private final ExcelReportService excelReportService;

        // ============================================================================
        // INDENT REPORTS
        // ============================================================================

        /**
         * Get indent summary report
         * GET /api/v1/reports/indent/summary?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/indent/summary")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<IndentReportDTO.Summary>> getIndentSummary(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer statusId,
                        @RequestParam(required = false) Integer departmentId,
                        @RequestParam(required = false) Integer companyId) {

                log.info("Indent summary report request: {} to {}, status={}, dept={}",
                                startDate, endDate, statusId, departmentId);

                List<IndentReportDTO.Summary> report = indentReportService.getIndentSummary(
                                startDate, endDate, statusId, departmentId, companyId);

                return ResponseEntity.ok(report);
        }

        /**
         * Get detailed indent report with line items
         * GET /api/v1/reports/indent/detailed?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/indent/detailed")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<IndentReportDTO.Detailed>> getIndentDetailed(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer statusId,
                        @RequestParam(required = false) Integer departmentId) {

                log.info("Indent detailed report request: {} to {}", startDate, endDate);

                List<IndentReportDTO.Detailed> report = indentReportService.getIndentDetailed(
                                startDate, endDate, statusId, departmentId);

                return ResponseEntity.ok(report);
        }

        /**
         * Get indent statistics by department
         * GET
         * /api/v1/reports/indent/stats-by-department?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/indent/stats-by-department")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<Map<String, Object>>> getIndentStatsByDepartment(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                List<Map<String, Object>> stats = indentReportService.getIndentStatsByDepartment(startDate, endDate);
                return ResponseEntity.ok(stats);
        }

        /**
         * Export indent summary to Excel
         * GET
         * /api/v1/reports/indent/export-summary?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/indent/export-summary")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'AUDITOR')")
        public ResponseEntity<byte[]> exportIndentSummary(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer statusId,
                        @RequestParam(required = false) Integer departmentId,
                        @RequestParam(required = false) Integer companyId) throws IOException {

                log.info("Exporting indent summary to Excel: {} to {}", startDate, endDate);

                List<IndentReportDTO.Summary> data = indentReportService.getIndentSummary(
                                startDate, endDate, statusId, departmentId, companyId);

                // Convert to Map format for Excel service
                List<Map<String, Object>> excelData = new ArrayList<>();
                for (IndentReportDTO.Summary item : data) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("Indent No", item.getIndentNo());
                        row.put("Date", item.getIndentDate());
                        row.put("Department", item.getDepartmentName());
                        row.put("Company", item.getCompanyName());
                        row.put("Created By", item.getCreatedBy());
                        row.put("Status", item.getStatus());
                        row.put("Priority", item.getPriority());
                        row.put("Items", item.getItemCount());
                        row.put("Total Qty", item.getTotalQuantity());
                        excelData.add(row);
                }

                List<String> headers = List.of("Indent No", "Date", "Department", "Company",
                                "Created By", "Status", "Priority", "Items", "Total Qty");

                String title = String.format("Indent Summary Report (%s to %s)",
                                startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")),
                                endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));

                byte[] excelBytes = excelReportService.generateExcelReport(
                                headers, excelData, "Indent Summary", title);

                String filename = String.format("Indent_Summary_%s_to_%s.xlsx",
                                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(excelBytes);
        }

        // ============================================================================
        // PURCHASE ORDER REPORTS
        // ============================================================================

        /**
         * Get PO summary report
         * GET /api/v1/reports/po/summary?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/po/summary")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<POReportDTO.Summary>> getPOSummary(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer vendorId,
                        @RequestParam(required = false) Integer statusId) {

                log.info("PO summary report request: {} to {}, vendor={}", startDate, endDate, vendorId);

                List<POReportDTO.Summary> report = poReportService.getPOSummary(
                                startDate, endDate, vendorId, statusId);

                return ResponseEntity.ok(report);
        }

        /**
         * Get vendor performance report
         * GET
         * /api/v1/reports/po/vendor-performance?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/po/vendor-performance")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<POReportDTO.VendorPerformance>> getVendorPerformance(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("Vendor performance report request: {} to {}", startDate, endDate);

                List<POReportDTO.VendorPerformance> report = poReportService.getVendorPerformance(
                                startDate, endDate);

                return ResponseEntity.ok(report);
        }

        /**
         * Export vendor performance to Excel
         * GET
         * /api/v1/reports/po/export-vendor-performance?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/po/export-vendor-performance")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'AUDITOR')")
        public ResponseEntity<byte[]> exportVendorPerformance(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
                        throws IOException {

                log.info("Exporting vendor performance to Excel: {} to {}", startDate, endDate);

                List<POReportDTO.VendorPerformance> data = poReportService.getVendorPerformance(startDate, endDate);

                List<Map<String, Object>> excelData = new ArrayList<>();
                for (POReportDTO.VendorPerformance item : data) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("Vendor Name", item.getVendorName());
                        row.put("Vendor Code", item.getVendorCode());
                        row.put("Total POs", item.getTotalPOs());
                        row.put("Total Value", item.getTotalValue());
                        row.put("Completed", item.getCompletedPOs());
                        row.put("Delayed", item.getDelayedPOs());
                        row.put("Avg Delivery Days", item.getAvgDeliveryDays());
                        row.put("Cancelled", item.getCancelledPOs());
                        row.put("On-Time %", String.format("%.2f%%", item.getOnTimeDeliveryRate()));
                        excelData.add(row);
                }

                List<String> headers = List.of("Vendor Name", "Vendor Code", "Total POs", "Total Value",
                                "Completed", "Delayed", "Avg Delivery Days", "Cancelled", "On-Time %");

                String title = String.format("Vendor Performance Report (%s to %s)",
                                startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")),
                                endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));

                byte[] excelBytes = excelReportService.generateExcelReport(
                                headers, excelData, "Vendor Performance", title);

                String filename = String.format("Vendor_Performance_%s_to_%s.xlsx",
                                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(excelBytes);
        }

        // ============================================================================
        // INVENTORY REPORTS
        // ============================================================================

        /**
         * Get inventory stock status
         * GET /api/v1/reports/inventory/stock-status?companyId=1&plantId=1&locationId=1
         */
        @GetMapping("/inventory/stock-status")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'PLANTMANAGER', 'VIEWER')")
        public ResponseEntity<List<InventoryReportDTO.StockStatus>> getInventoryStockStatus(
                        @RequestParam(required = false) Integer companyId,
                        @RequestParam(required = false) Integer plantId,
                        @RequestParam(required = false) Integer locationId) {

                log.info("Stock status report request: company={}, plant={}, location={}",
                                companyId, plantId, locationId);

                List<InventoryReportDTO.StockStatus> report = inventoryReportService.getStockStatus(
                                companyId, plantId, locationId);

                return ResponseEntity.ok(report);
        }

        /**
         * Get low stock alerts
         * GET /api/v1/reports/inventory/low-stock?companyId=1
         */
        @GetMapping("/inventory/low-stock")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER')")
        public ResponseEntity<List<InventoryReportDTO.LowStock>> getLowStockAlerts(
                        @RequestParam(required = false) Integer companyId) {

                log.info("Low stock alerts request for company={}", companyId);

                List<InventoryReportDTO.LowStock> alerts = inventoryReportService.getLowStockAlerts(companyId);

                return ResponseEntity.ok(alerts);
        }

        /**
         * Get material usage report
         * GET
         * /api/v1/reports/inventory/material-usage?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/inventory/material-usage")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'PLANTMANAGER', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<InventoryReportDTO.MaterialUsage>> getMaterialUsage(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer materialId) {

                log.info("Material usage report request: {} to {}, material={}",
                                startDate, endDate, materialId);

                List<InventoryReportDTO.MaterialUsage> report = inventoryReportService.getMaterialUsage(
                                startDate, endDate, materialId);

                return ResponseEntity.ok(report);
        }

        /**
         * Get inventory value report
         * GET /api/v1/reports/inventory/value?companyId=1
         */
        @GetMapping("/inventory/value")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'FINANCE', 'PLANTMANAGER', 'VIEWER')")
        public ResponseEntity<List<Map<String, Object>>> getInventoryValue(
                        @RequestParam(required = false) Integer companyId) {

                log.info("Inventory value report request for company={}", companyId);

                List<Map<String, Object>> report = inventoryReportService.getInventoryValue(companyId);

                return ResponseEntity.ok(report);
        }

        // ============================================================================
        // VENDOR PERFORMANCE REPORTS
        // ============================================================================

        /**
         * Get vendor performance summary
         * GET
         * /api/v1/reports/vendor-performance?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/vendor-performance")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<VendorPerformanceDTO.Summary>> getVendorPerformance(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Integer vendorId) {

                log.info("Vendor performance report request: {} to {}, vendor={}",
                                startDate, endDate, vendorId);

                List<VendorPerformanceDTO.Summary> report = vendorPerformanceService.getVendorPerformance(
                                startDate, endDate, vendorId);

                return ResponseEntity.ok(report);
        }

        /**
         * Get vendor delivery performance
         * GET
         * /api/v1/reports/vendor-performance/delivery?startDate=2025-01-01&endDate=2025-12-31
         */
        @GetMapping("/vendor-performance/delivery")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'AUDITOR', 'VIEWER')")
        public ResponseEntity<List<VendorPerformanceDTO.DeliveryPerformance>> getDeliveryPerformance(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("Vendor delivery performance request: {} to {}", startDate, endDate);

                List<VendorPerformanceDTO.DeliveryPerformance> report = vendorPerformanceService
                                .getDeliveryPerformance(startDate, endDate);

                return ResponseEntity.ok(report);
        }

        /**
         * Get vendor ranking
         * GET
         * /api/v1/reports/vendor-performance/ranking?startDate=2025-01-01&endDate=2025-12-31&rankBy=value
         */
        @GetMapping("/vendor-performance/ranking")
        @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'PLANTMANAGER', 'VIEWER')")
        public ResponseEntity<List<Map<String, Object>>> getVendorRanking(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false, defaultValue = "value") String rankBy) {

                log.info("Vendor ranking request: {} to {}, rankBy={}", startDate, endDate, rankBy);

                List<Map<String, Object>> ranking = vendorPerformanceService.getVendorRanking(
                                startDate, endDate, rankBy);

                return ResponseEntity.ok(ranking);
        }

        // ============================================================================
        // HEALTH CHECK
        // ============================================================================

        /**
         * Health check endpoint
         */
        @GetMapping("/health")
        public ResponseEntity<Map<String, String>> health() {
                Map<String, String> status = new HashMap<>();
                status.put("status", "UP");
                status.put("service", "Comprehensive Report Service");
                status.put("version", "1.0.0");
                return ResponseEntity.ok(status);
        }
}
