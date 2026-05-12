package com.nslindia.procurezone.report.controller;

import com.nslindia.procurezone.report.service.MaterialQuantityReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * REST API Controller for Material Reports
 * Provides endpoints for generating and downloading various Excel reports
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class MaterialReportController {

    private final MaterialQuantityReportService materialQuantityReportService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate Material Quantity Report for all companies
     * GET /api/v1/reports/material-quantity
     */
    @GetMapping("/material-quantity")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'SUPERADMIN', 'PLANTMANAGER')")
    public ResponseEntity<byte[]> generateMaterialQuantityReport() {
        try {
            log.info("Generating material quantity report for all companies");

            byte[] reportData = materialQuantityReportService.generateMaterialQuantityReport();

            String filename = String.format("Material_Quantity_Report_%s.xlsx",
                    LocalDate.now().format(DATE_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(reportData.length);

            log.info("Material quantity report generated successfully: {} bytes", reportData.length);
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating material quantity report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Generate Material Quantity Report for specific company
     * GET /api/v1/reports/material-quantity/company/{companyCode}
     */
    @GetMapping("/material-quantity/company/{companyCode}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'SUPERADMIN', 'PLANTMANAGER')")
    public ResponseEntity<byte[]> generateMaterialQuantityReportForCompany(
            @PathVariable String companyCode) {
        try {
            log.info("Generating material quantity report for company: {}", companyCode);

            byte[] reportData = materialQuantityReportService
                    .generateMaterialQuantityReportForCompany(companyCode);

            String filename = String.format("Material_Quantity_Report_%s_%s.xlsx",
                    companyCode, LocalDate.now().format(DATE_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(reportData.length);

            log.info("Material quantity report for company {} generated successfully", companyCode);
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Company not found: {}", companyCode, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
            log.error("Error generating material quantity report for company: {}", companyCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Generate Low Stock Alert Report
     * GET /api/v1/reports/low-stock-alert
     */
    @GetMapping("/low-stock-alert")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'SUPERADMIN', 'PLANTMANAGER')")
    public ResponseEntity<byte[]> generateLowStockAlertReport() {
        try {
            log.info("Generating low stock alert report");

            byte[] reportData = materialQuantityReportService.generateLowStockAlertReport();

            String filename = String.format("Low_Stock_Alert_Report_%s.xlsx",
                    LocalDate.now().format(DATE_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(reportData.length);

            log.info("Low stock alert report generated successfully: {} bytes", reportData.length);
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating low stock alert report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
