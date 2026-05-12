package com.nslindia.procurezone.integration.sap.controller;

import com.nslindia.procurezone.integration.sap.dto.MaterialCsvRow;
import com.nslindia.procurezone.integration.sap.dto.MaterialImportResult;
import com.nslindia.procurezone.integration.sap.service.CsvParserService;
import com.nslindia.procurezone.integration.sap.service.MaterialImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for SAP material CSV import operations
 * Provides manual upload endpoint for administrators
 */
@RestController
@RequestMapping("/api/v1/sap/materials")
@RequiredArgsConstructor
@Slf4j
public class SapMaterialImportController {

    private final CsvParserService csvParserService;
    private final MaterialImportService materialImportService;

    /**
     * Upload and import material CSV file
     * POST /api/v1/sap/materials/import
     * 
     * @param file CSV file with material data
     * @return Import result with statistics
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'PROCUREMENT')")
    public ResponseEntity<MaterialImportResult> importMaterialCsv(
            @RequestParam("file") MultipartFile file) {

        log.info("Received material CSV import request: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            // Validate file
            csvParserService.validateCsvFile(file);

            // Parse CSV
            List<MaterialCsvRow> rows = csvParserService.parseMaterialCsv(file);

            if (rows.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(MaterialImportResult.builder()
                                .success(false)
                                .message("No valid rows found in CSV file")
                                .filename(file.getOriginalFilename())
                                .build());
            }

            // Import materials
            MaterialImportResult result = materialImportService.importMaterials(
                    rows, file.getOriginalFilename());

            // Return appropriate status based on result
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(MaterialImportResult.builder()
                            .success(false)
                            .message("Invalid file: " + e.getMessage())
                            .filename(file.getOriginalFilename())
                            .build());
        } catch (Exception e) {
            log.error("Error importing material CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MaterialImportResult.builder()
                            .success(false)
                            .message("Import failed: " + e.getMessage())
                            .filename(file.getOriginalFilename())
                            .build());
        }
    }

    /**
     * Get import help/documentation
     * GET /api/v1/sap/materials/import/help
     */
    @GetMapping("/import/help")
    public ResponseEntity<ImportHelp> getImportHelp() {
        ImportHelp help = new ImportHelp();
        help.setFormat("CSV file with 5 columns");
        help.setColumns(new String[] {
                "Column 0: Company Code (e.g., NSL01)",
                "Column 1: Plant Code (e.g., PLT01)",
                "Column 2: Material Code (e.g., MAT12345)",
                "Column 3: Material Description (e.g., Steel Pipe 2 inch)",
                "Column 4: Quantity (e.g., 150.00)"
        });
        help.setExample("NSL01,PLT01,MAT12345,Steel Pipe 2 inch,150.00");
        help.setMaxFileSize("10 MB");
        help.setNotes(new String[] {
                "If material code doesn't exist, it will be created automatically",
                "Material name and description will be set to material code",
                "Quantity updates existing company-plant-material mapping",
                "All operations are logged for audit trail"
        });

        return ResponseEntity.ok(help);
    }

    /**
     * DTO for import help response
     */
    @lombok.Data
    static class ImportHelp {
        private String format;
        private String[] columns;
        private String example;
        private String maxFileSize;
        private String[] notes;
    }
}
