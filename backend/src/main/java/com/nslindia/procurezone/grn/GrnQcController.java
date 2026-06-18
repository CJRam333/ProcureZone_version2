package com.nslindia.procurezone.grn;

import com.nslindia.procurezone.grn.dto.CreateQcResultRequest;
import com.nslindia.procurezone.grn.dto.QcResultResponse;
import com.nslindia.procurezone.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for GRN Quality Control operations.
 * 
 * @author NSL India
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/grn/qc")
@RequiredArgsConstructor
@Slf4j
public class GrnQcController {

    private final GrnQcService qcService;

    /**
     * Create a new QC inspection result
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GRNINCHARGE')")
    public ResponseEntity<QcResultResponse> createQcResult(
            @Valid @RequestBody CreateQcResultRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Creating QC result for GRN: {}", request.grnId());
        QcResultResponse response = qcService.createQcResult(request, currentUser.userId().intValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get QC result by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GRNINCHARGE', 'VIEWER')")
    public ResponseEntity<QcResultResponse> getQcResultById(@PathVariable Integer id) {
        log.info("Fetching QC result by ID: {}", id);
        QcResultResponse response = qcService.getQcResultById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all QC results for a GRN
     */
    @GetMapping("/grn/{grnId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GRNINCHARGE', 'VIEWER')")
    public ResponseEntity<List<QcResultResponse>> getQcResultsForGrn(@PathVariable Integer grnId) {
        log.info("Fetching QC results for GRN: {}", grnId);
        List<QcResultResponse> results = qcService.getQcResultsForGrn(grnId);
        return ResponseEntity.ok(results);
    }

    /**
     * Get latest QC result for a GRN
     */
    @GetMapping("/grn/{grnId}/latest")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GRNINCHARGE', 'VIEWER')")
    public ResponseEntity<QcResultResponse> getLatestQcResultForGrn(@PathVariable Integer grnId) {
        log.info("Fetching latest QC result for GRN: {}", grnId);
        return qcService.getLatestQcResultForGrn(grnId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get QC results by status (1=Pending, 2=Passed, 3=Failed, 4=Conditional)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GRNINCHARGE', 'VIEWER')")
    public ResponseEntity<Page<QcResultResponse>> getQcResultsByStatus(
            @PathVariable Integer status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching QC results by status: {}", status);
        Page<QcResultResponse> results = qcService.getQcResultsByStatus(status, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Update QC status (Pass/Fail)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER')")
    public ResponseEntity<QcResultResponse> updateQcStatus(
            @PathVariable Integer id,
            @RequestBody UpdateQcStatusRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Updating QC status: ID={}, newStatus={}", id, request.status());
        QcResultResponse response = qcService.updateQcStatus(
                id, request.status(), request.remarks(), currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Check if GRN has passed QC
     */
    @GetMapping("/grn/{grnId}/passed")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GRNINCHARGE', 'PROCUREMENT')")
    public ResponseEntity<Map<String, Boolean>> hasGrnPassedQc(@PathVariable Integer grnId) {
        log.info("Checking if GRN {} has passed QC", grnId);
        boolean passed = qcService.hasGrnPassedQc(grnId);
        return ResponseEntity.ok(Map.of("passed", passed));
    }

    /**
     * Get QC statistics for dashboard
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'PLANTMANAGER')")
    public ResponseEntity<Map<String, Long>> getQcStatistics() {
        log.info("Fetching QC statistics");
        Map<String, Long> stats = qcService.getQcStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get pending re-inspections
     */
    @GetMapping("/pending-reinspections")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'QUALITYMANAGER')")
    public ResponseEntity<List<QcResultResponse>> getPendingReInspections() {
        log.info("Fetching pending re-inspections");
        List<QcResultResponse> results = qcService.getPendingReInspections();
        return ResponseEntity.ok(results);
    }

    // ========== Inner DTO class for status update ==========

    public record UpdateQcStatusRequest(Integer status, String remarks) {
    }
}
