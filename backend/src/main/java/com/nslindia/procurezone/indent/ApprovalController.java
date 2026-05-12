package com.nslindia.procurezone.indent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nslindia.procurezone.indent.dto.ApprovalWorkflowResponse;
import com.nslindia.procurezone.indent.dto.IndentListResponse;
import com.nslindia.procurezone.indent.dto.IndentResponse;
import com.nslindia.procurezone.indent.dto.PendingApprovalResponse;
import com.nslindia.procurezone.indent.dto.RequestInfoRequest;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;

/**
 * REST controller for approval workflow management.
 * Provides endpoints for approval operations by Department Heads and Approvers.
 */
@RestController
@RequestMapping("/api/v1/approvals")
@Validated
public class ApprovalController {

    private static final Logger log = LoggerFactory.getLogger(ApprovalController.class);

    private final IndentService indentService;

    public ApprovalController(IndentService indentService) {
        this.indentService = indentService;
    }

    /**
     * Get pending approvals for the logged-in user
     * GET /api/v1/approvals/pending
     * Alias for pending-for-me without requiring departmentId parameter
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<PendingApprovalResponse>> getPendingApprovalsSimple(
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Fetching pending approvals for user: {} ({})",
                principal.displayName(), principal.email());

        List<PendingApprovalResponse> response = indentService.getPendingApprovals(
                principal.email(), null);

        log.info("Found {} pending approvals for user: {}", response.size(), principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending approvals for the logged-in user
     * GET /api/v1/approvals/pending-for-me?departmentId={deptId}
     * If departmentId is not provided, fetches approvals from all departments the
     * user has access to.
     */
    @GetMapping("/pending-for-me")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<PendingApprovalResponse>> getPendingApprovals(
            @RequestParam(required = false) Integer departmentId,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Fetching pending approvals for user: {} ({}) in department: {}",
                principal.displayName(), principal.email(), departmentId);

        List<PendingApprovalResponse> response = indentService.getPendingApprovals(
                principal.email(), departmentId);

        log.info("Found {} pending approvals for user: {}", response.size(), principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Approve an indent - SMART ROUTING based on current status.
     * POST /api/v1/approvals/indents/{id}/approve?remarks=...
     * 
     * Automatically routes to the correct approval method:
     * - Status 2 (Submitted) → L1 Dept Head Approval (→ status 3)
     * - Status 3 (Dept Head Approved) → L2 Final Approval (→ status 4)
     * - Status 4 (Finance Approved) → L3 Procurement Approval (→ status 5)
     */
    @PostMapping("/indents/{id}/approve")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('PROCUREMENT') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<?> approveIndent(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Smart-approve request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        log.debug("User roles: {}", principal.roles());

        try {
            // Get the indent's current status to decide which approval method to call
            IndentResponse current = indentService.getIndentById(id);
            int statusId = current.statusId() != null ? current.statusId() : 0;

            IndentResponse response;
            switch (statusId) {
                case 2: // Submitted → Dept Head Approved
                    log.info("Routing to L1 (Dept Head) approval for indent ID: {}", id);
                    response = indentService.approveIndent(id, principal.email(), remarks);
                    break;
                case 3: // Dept Head Approved → Finance Approved
                    log.info("Routing to L2 (Final) approval for indent ID: {}", id);
                    response = indentService.finalApproveIndent(id, principal.email(), remarks);
                    break;
                case 4: // Finance Approved → Procurement Approved
                    log.info("Routing to L3 (Procurement) approval for indent ID: {}", id);
                    response = indentService.procurementApproveIndent(id, principal.email(), remarks);
                    break;
                default:
                    throw new IllegalStateException(
                            "Indent cannot be approved in current status: " + (current.statusName() != null ? current.statusName() : "Unknown (ID=" + statusId + ")"));
            }

            log.info("Indent ID: {} approved successfully by: {} (status {} → next)", id, principal.email(), statusId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("Cannot approve indent ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "code", "INVALID_STATE",
                    "message", e.getMessage()));
        }
    }

    /**
     * Reject an indent - SMART ROUTING based on current status.
     * POST /api/v1/approvals/indents/{id}/reject?remarks=...
     */
    @PostMapping("/indents/{id}/reject")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('PROCUREMENT') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<?> rejectIndent(
            @PathVariable Integer id,
            @RequestParam String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Smart-reject request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        log.debug("User roles: {}, Reject remarks: {}", principal.roles(), remarks);

        try {
            IndentResponse response = indentService.rejectIndent(id, principal.email(), remarks);
            log.info("Indent ID: {} rejected successfully by: {}", id, principal.email());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("Cannot reject indent ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "code", "INVALID_STATE",
                    "message", e.getMessage()));
        }
    }

    /**
     * Request additional information on an indent
     * POST /api/v1/approvals/indents/{id}/request-info
     */
    @PostMapping("/indents/{id}/request-info")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<IndentResponse> requestInfo(
            @PathVariable Integer id,
            @Valid @RequestBody RequestInfoRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Request info for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        log.debug("Info requested: {}", request.infoRequested());

        IndentResponse response = indentService.requestInfo(id, principal.email(), request.infoRequested());

        log.info("Info requested for indent ID: {} by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Get approval workflow history for an indent
     * GET /api/v1/indents/{id}/approval-workflow
     */
    @GetMapping("/indents/{id}/approval-workflow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ApprovalWorkflowResponse>> getApprovalHistory(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.debug("Fetching approval workflow for indent ID: {} by user: {}", id, principal.email());

        List<ApprovalWorkflowResponse> response = indentService.getApprovalHistory(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get indents for a department (for Department Head)
     * GET /api/v1/indents/department-indents?departmentId={deptId}&page=0&size=10
     */
    @GetMapping("/department-indents")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Page<IndentListResponse>> getDepartmentIndents(
            @RequestParam Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "indentDate,desc") String[] sort,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Fetching indents for department: {} by user: {}", departmentId, principal.email());

        Pageable pageable = createPageable(page, size, sort);
        Page<IndentListResponse> response = indentService.getIndentsByDepartment(departmentId, pageable);

        log.debug("Found {} indents for department: {}", response.getTotalElements(), departmentId);
        return ResponseEntity.ok(response);
    }

    // Helper method to create Pageable with sorting
    private Pageable createPageable(int page, int size, String[] sort) {
        String property = sort[0];
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
