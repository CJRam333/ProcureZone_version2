package com.nslindia.procurezone.grn;

import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.grn.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/grn")
@RequiredArgsConstructor
@Slf4j
// CORS handled by WebConfig - removed @CrossOrigin to avoid conflict with
// allowCredentials
public class GRNController {

    private final GRNService grnService;

    /**
     * Create new GRN
     * POST /api/v1/grn
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> createGRN(
            @Valid @RequestBody CreateGRNRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Creating GRN for indent {} by user {}", request.indentId(), userPrincipal.username());

        GRNResponse response = grnService.createGRN(request, userPrincipal.employeeNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all GRNs with pagination and filtering
     * GET /api/v1/grn?page=0&size=10&status=1&search=vendor
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER', 'QUALITYMANAGER', 'USER')")
    public ResponseEntity<Map<String, Object>> getAllGRNs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").descending());
        Page<GRNSummaryResponse> grnPage = grnService.getAllGRNs(status, search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grnPage.getContent());
        response.put("currentPage", grnPage.getNumber());
        response.put("totalItems", grnPage.getTotalElements());
        response.put("totalPages", grnPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get GRN by ID
     * GET /api/v1/grn/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER', 'USER')")
    public ResponseEntity<GRNResponse> getGRNById(@PathVariable Integer id) {
        log.info("Fetching GRN with ID: {}", id);
        GRNResponse response = grnService.getGRNById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get GRN by number
     * GET /api/v1/grn/number/{grnNumber}
     */
    @GetMapping("/number/{grnNumber}")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER')")
    public ResponseEntity<GRNResponse> getGRNByNumber(@PathVariable String grnNumber) {
        log.info("Fetching GRN with number: {}", grnNumber);
        GRNResponse response = grnService.getGRNByNumber(grnNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Quality inspection
     * POST /api/v1/grn/{id}/inspect
     */
    @PostMapping("/{id}/inspect")
    @PreAuthorize("hasAnyRole('QUALITYMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> qualityInspection(
            @PathVariable Integer id,
            @Valid @RequestBody InspectionRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Quality inspection for GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.qualityInspection(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * RM Approve GRN (Reporting Manager approval)
     * POST /api/v1/grn/{id}/rm-approve
     */
    @PostMapping("/{id}/rm-approve")
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> rmApproveGRN(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("RM approving GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.rmApproveGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * RM Reject GRN (Reporting Manager rejection)
     * POST /api/v1/grn/{id}/rm-reject
     */
    @PostMapping("/{id}/rm-reject")
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> rmRejectGRN(
            @PathVariable Integer id,
            @Valid @RequestBody RejectionRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("RM rejecting GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.rmRejectGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Approve GRN (First level approval)
     * POST /api/v1/grn/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> approveGRN(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Approving GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.approveGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Final approval (Second level approval)
     * POST /api/v1/grn/{id}/final-approve
     */
    @PostMapping("/{id}/final-approve")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> finalApproveGRN(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Final approving GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.finalApproveGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Store goods in inventory
     * POST /api/v1/grn/{id}/store
     */
    @PostMapping("/{id}/store")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> storeGoods(
            @PathVariable Integer id,
            @Valid @RequestBody StoreRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Storing goods for GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.storeGoods(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Reject GRN
     * POST /api/v1/grn/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('QUALITYMANAGER', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> rejectGRN(
            @PathVariable Integer id,
            @Valid @RequestBody RejectionRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Rejecting GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.rejectGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Get GRNs pending inspection
     * GET /api/v1/grn/pending-inspection
     */
    @GetMapping("/pending-inspection")
    @PreAuthorize("hasAnyRole('QUALITYMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingInspection(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").ascending());
        Page<GRNSummaryResponse> grnPage = grnService.getPendingInspection(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grnPage.getContent());
        response.put("currentPage", grnPage.getNumber());
        response.put("totalItems", grnPage.getTotalElements());
        response.put("totalPages", grnPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get GRNs pending approval
     * GET /api/v1/grn/pending-approval
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingApproval(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").ascending());
        Page<GRNSummaryResponse> grnPage = grnService.getPendingApproval(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grnPage.getContent());
        response.put("currentPage", grnPage.getNumber());
        response.put("totalItems", grnPage.getTotalElements());
        response.put("totalPages", grnPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get dashboard statistics
     * GET /api/v1/grn/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        log.info("Fetching GRN dashboard statistics");
        Map<String, Object> stats = grnService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }
}
