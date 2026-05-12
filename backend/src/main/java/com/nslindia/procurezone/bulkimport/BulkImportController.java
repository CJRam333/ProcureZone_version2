package com.nslindia.procurezone.bulkimport;

import com.nslindia.procurezone.bulkimport.dto.ImportResultResponse;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.security.UserPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for bulk import operations.
 * Provides endpoints for uploading CSV files, downloading templates, and error
 * reports.
 */
@RestController
@RequestMapping("/api/v1/bulk-import")
public class BulkImportController {

    private static final Logger logger = LoggerFactory.getLogger(BulkImportController.class);

    private final MaterialBulkImportService materialBulkImportService;
    private final ImportResultCache importResultCache;

    public BulkImportController(MaterialBulkImportService materialBulkImportService,
            ImportResultCache importResultCache) {
        this.materialBulkImportService = materialBulkImportService;
        this.importResultCache = importResultCache;
    }

    /**
     * Upload and import materials from a CSV file.
     * Endpoint: POST /api/v1/bulk-import/materials
     * Roles: SUPERADMIN, ADMIN, MANAGER
     * 
     * @param file      the CSV file to import
     * @param principal the authenticated user
     * @return import result with statistics and errors (includes importId for error
     *         download)
     */
    @PostMapping(value = "/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<ImportResultWithId> importMaterials(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {

        logger.info("POST /api/v1/bulk-import/materials - Import request from user: {}, file: {}",
                principal.email(), file.getOriginalFilename());

        ImportResultResponse result = materialBulkImportService.importMaterials(file, principal.email());

        // I.1 FIX: Store result in cache for error download
        String importId = importResultCache.store(result);

        // Return 201 for full success, 200 for partial success
        HttpStatus status = "SUCCESS".equals(result.status()) ? HttpStatus.CREATED : HttpStatus.OK;

        ImportResultWithId responseWithId = new ImportResultWithId(result, importId);

        return ResponseEntity.status(status).body(responseWithId);
    }

    /**
     * I.1 FIX: Response wrapper that includes importId for error download.
     */
    public record ImportResultWithId(
            int totalRecords,
            int successCount,
            int failureCount,
            int skippedCount,
            java.util.List<ImportResultResponse.ImportError> errors,
            String status,
            java.time.LocalDateTime importedAt,
            long processingTimeMs,
            String importId,
            String errorDownloadUrl) {
        public ImportResultWithId(ImportResultResponse result, String importId) {
            this(
                    result.totalRecords(),
                    result.successCount(),
                    result.failureCount(),
                    result.skippedCount(),
                    result.errors(),
                    result.status(),
                    result.importedAt(),
                    result.processingTimeMs(),
                    importId,
                    result.failureCount() > 0 ? "/api/v1/bulk-import/materials/errors/" + importId : null);
        }
    }

    /**
     * Download a CSV template for material import.
     * Endpoint: GET /api/v1/bulk-import/materials/template
     * Roles: All authenticated users
     * 
     * @return CSV template file
     */
    @GetMapping("/materials/template")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadMaterialTemplate() {

        logger.info("GET /api/v1/bulk-import/materials/template - Template download request");

        String template = materialBulkImportService.generateTemplate();
        byte[] content = template.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "material_import_template.csv");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * I.1 FIX: Download import errors as CSV file.
     * Endpoint: GET /api/v1/bulk-import/materials/errors/{importId}
     * Roles: SUPERADMIN, ADMIN, MANAGER
     * 
     * @param importId the import ID returned from the import request
     * @return CSV file containing all import errors
     */
    @GetMapping("/materials/errors/{importId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<byte[]> downloadImportErrors(@PathVariable String importId) {

        logger.info("GET /api/v1/bulk-import/materials/errors/{} - Error download request", importId);

        ImportResultResponse result = importResultCache.get(importId);
        if (result == null) {
            throw new ResourceNotFoundException(
                    "Import result not found or expired. Import results are available for 24 hours.");
        }

        if (result.errors() == null || result.errors().isEmpty()) {
            throw new ResourceNotFoundException("No errors found for this import.");
        }

        String csvContent = generateErrorsCsv(result);
        byte[] content = csvContent.getBytes(StandardCharsets.UTF_8);

        String filename = String.format("import_errors_%s_%s.csv",
                importId.substring(0, 8),
                result.importedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * I.1 FIX: Get import result summary by ID (without downloading).
     * Endpoint: GET /api/v1/bulk-import/materials/result/{importId}
     * Roles: SUPERADMIN, ADMIN, MANAGER
     */
    @GetMapping("/materials/result/{importId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<ImportResultResponse> getImportResult(@PathVariable String importId) {

        logger.info("GET /api/v1/bulk-import/materials/result/{} - Result lookup request", importId);

        ImportResultResponse result = importResultCache.get(importId);
        if (result == null) {
            throw new ResourceNotFoundException(
                    "Import result not found or expired. Import results are available for 24 hours.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Generate CSV content from import errors.
     */
    private String generateErrorsCsv(ImportResultResponse result) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Row Number,Material Code,Material Name,Error Type,Error Message\n");

        // Add byte order mark for Excel compatibility
        String bom = "\uFEFF";

        // Data rows
        for (ImportResultResponse.ImportError error : result.errors()) {
            csv.append(error.rowNumber()).append(",");
            csv.append(escapeCsvField(error.code())).append(",");
            csv.append(escapeCsvField(error.name())).append(",");
            csv.append(error.errorType()).append(",");
            csv.append(escapeCsvField(error.errorMessage())).append("\n");
        }

        // Add summary at the end
        csv.append("\n");
        csv.append("=== Import Summary ===\n");
        csv.append("Total Records,").append(result.totalRecords()).append("\n");
        csv.append("Successful,").append(result.successCount()).append("\n");
        csv.append("Failed,").append(result.failureCount()).append("\n");
        csv.append("Import Time,").append(result.importedAt()).append("\n");

        return bom + csv.toString();
    }

    /**
     * Escape CSV field to handle commas and quotes.
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If field contains comma, quote, or newline, wrap in quotes and escape inner
        // quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
