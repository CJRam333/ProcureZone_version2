package com.nslindia.procurezone.sap.controller;

import com.nslindia.procurezone.sap.dto.SapImportConfig;
import com.nslindia.procurezone.sap.dto.SapImportResult;
import com.nslindia.procurezone.sap.entity.SapImportLog;
import com.nslindia.procurezone.sap.entity.SapScheduleMaterial;
import com.nslindia.procurezone.sap.service.SapImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for SAP Material Import
 * Provides endpoints for:
 * - Manual file upload import
 * - Trigger scheduled import
 * - View import history and status
 * - Query imported materials
 * 
 * @author NSL India
 * @version 1.0
 */
@RestController
@RequestMapping("/api/sap")
@RequiredArgsConstructor
@Slf4j
public class SapImportController {

    private final SapImportService sapImportService;

    // ==================== IMPORT ENDPOINTS ====================

    /**
     * Upload and import SAP CSV file
     */
    @PostMapping(value = "/import/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STOREKEEPER')")
    public ResponseEntity<SapImportResult> uploadAndImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "truncate", defaultValue = "true") boolean truncate,
            @RequestParam(value = "plantId", required = false) Integer plantId,
            Authentication authentication) {

        log.info("SAP import upload initiated by user: {}", authentication.getName());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(SapImportResult.failure("No file uploaded", 0));
        }

        SapImportConfig config = SapImportConfig.builder()
                .truncateBeforeImport(truncate)
                .plantId(plantId)
                .importType("MANUAL")
                .triggeredBy(authentication.getName())
                .build();

        SapImportResult result = sapImportService.importFromFile(file, config);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Import from server file path
     */
    @PostMapping("/import/path")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<SapImportResult> importFromPath(
            @RequestParam("path") String filePath,
            @RequestParam(value = "truncate", defaultValue = "true") boolean truncate,
            Authentication authentication) {

        log.info("SAP import from path initiated by user: {} - path: {}",
                authentication.getName(), filePath);

        SapImportConfig config = SapImportConfig.builder()
                .truncateBeforeImport(truncate)
                .importType("MANUAL")
                .triggeredBy(authentication.getName())
                .build();

        SapImportResult result = sapImportService.importFromPath(filePath, config);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Trigger the scheduled import manually
     */
    @PostMapping("/import/trigger")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<SapImportResult> triggerScheduledImport(Authentication authentication) {
        log.info("Manual SAP import triggered by user: {}", authentication.getName());

        SapImportResult result = sapImportService.runScheduledImport();

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // ==================== STATUS & HISTORY ENDPOINTS ====================

    /**
     * Get current import status
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'PROCUREMENT')")
    public ResponseEntity<Map<String, Object>> getImportStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("isImportInProgress", sapImportService.isImportInProgress());
        status.put("totalMaterials", sapImportService.getMaterialCount());
        status.put("plantCodes", sapImportService.getDistinctPlantCodes());

        Optional<SapImportLog> latestImport = sapImportService.getLatestImportLog();
        if (latestImport.isPresent()) {
            SapImportLog log = latestImport.get();
            status.put("lastImport", Map.of(
                    "id", log.getId(),
                    "status", log.getStatus(),
                    "startedAt", log.getStartedAt(),
                    "completedAt", log.getCompletedAt() != null ? log.getCompletedAt() : "",
                    "recordsInserted", log.getRecordsInserted() != null ? log.getRecordsInserted() : 0));
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Get import history
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'STOREKEEPER')")
    public ResponseEntity<List<SapImportLog>> getImportHistory(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(sapImportService.getImportHistory(limit));
    }

    // ==================== MATERIAL QUERY ENDPOINTS ====================

    /**
     * Search materials
     */
    @GetMapping("/materials/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SapScheduleMaterial>> searchMaterials(
            @RequestParam("q") String query) {
        return ResponseEntity.ok(sapImportService.searchMaterials(query));
    }

    /**
     * Get materials by plant
     */
    @GetMapping("/materials/plant/{plantCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SapScheduleMaterial>> getMaterialsByPlant(
            @PathVariable Integer plantCode) {
        return ResponseEntity.ok(sapImportService.getMaterialsByPlant(plantCode));
    }

    /**
     * Get available quantity for a material
     */
    @GetMapping("/materials/quantity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAvailableQuantity(
            @RequestParam("plantCode") Integer plantCode,
            @RequestParam("materialCode") String materialCode,
            @RequestParam("storageLocation") String storageLocation) {

        BigDecimal quantity = sapImportService.getAvailableQuantity(
                plantCode, materialCode, storageLocation);

        return ResponseEntity.ok(Map.of(
                "plantCode", plantCode,
                "materialCode", materialCode,
                "storageLocation", storageLocation,
                "availableQuantity", quantity));
    }

    /**
     * Get material count by plant
     */
    @GetMapping("/materials/count/{plantCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMaterialCountByPlant(
            @PathVariable Integer plantCode) {
        long count = sapImportService.getMaterialCountByPlant(plantCode);
        return ResponseEntity.ok(Map.of(
                "plantCode", plantCode,
                "materialCount", count));
    }

    /**
     * Get all distinct plant codes
     */
    @GetMapping("/plants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Integer>> getPlantCodes() {
        return ResponseEntity.ok(sapImportService.getDistinctPlantCodes());
    }
}
