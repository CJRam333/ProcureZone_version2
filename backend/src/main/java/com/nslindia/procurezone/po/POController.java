package com.nslindia.procurezone.po;

import com.nslindia.procurezone.po.dto.*;
import com.nslindia.procurezone.security.UserPrincipal;
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
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Purchase Order operations
 * 
 * @author NSL India
 * @version 1.0
 */

@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
@Slf4j
// CORS handled by WebConfig - removed @CrossOrigin to avoid conflict with
// allowCredentials(true)
public class POController {

    private final POService poService;

    /**
     * 1. GET /api/v1/po/approved-indents
     * Get list of approved indents ready for PO creation
     */
    @GetMapping("/approved-indents")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<List<ApprovedIndentDTO>> getApprovedIndents(Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching approved indents", user.username());

        List<ApprovedIndentDTO> indents = poService.getApprovedIndents();
        return ResponseEntity.ok(indents);
    }

    /**
     * 2. POST /api/v1/po
     * Create new PO from approved indent
     */

    @PostMapping
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> createPO(
            @Valid @RequestBody CreatePORequest request,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} creating PO from indent {}", user.username(), request.indentId());

        PurchaseOrderResponse response = poService.createPOFromIndent(request, user.employeeNumber().intValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 3. GET /api/v1/po
     * Get all POs with pagination and filtering
     * Only for Roles with RolesName as 'PROCUREMENT', 'VIEWER', 'PLANTMANAGER',
     * 'ADMIN', 'SUPERADMIN'
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'VIEWER', 'PLANTMANAGER', 'STOREKEEPER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'EMPLOYEE', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getAllPOs(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "poDate,desc") String[] sort,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching POs - status: {}, search: {}", user.username(), status, search);

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<POSummaryResponse> pos = poService.getAllPOs(status, search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pos.getContent());
        response.put("currentPage", pos.getNumber());
        response.put("totalItems", pos.getTotalElements());
        response.put("totalPages", pos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 4. GET /api/v1/po/{id}
     * Get PO details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'VIEWER', 'PLANTMANAGER', 'STOREKEEPER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'EMPLOYEE', 'AUDITOR')")
    public ResponseEntity<PurchaseOrderResponse> getPOById(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching PO with ID: {}", user.username(), id);

        PurchaseOrderResponse response = poService.getPOById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 5. GET /api/v1/po/number/{poNumber}
     * Get PO details by PO number
     */
    @GetMapping("/number/{poNumber}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'VIEWER', 'PLANTMANAGER', 'STOREKEEPER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'EMPLOYEE', 'AUDITOR')")
    public ResponseEntity<PurchaseOrderResponse> getPOByNumber(
            @PathVariable String poNumber,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching PO with number: {}", user.username(), poNumber);

        PurchaseOrderResponse response = poService.getPOByNumber(poNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * 6. PUT /api/v1/po/{id}
     * Update PO (only DRAFT status POs can be updated)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> updatePO(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePORequest request,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} updating PO {}", user.username(), id);

        PurchaseOrderResponse response = poService.updatePO(id, request, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 7. POST /api/v1/po/{id}/submit
     * Submit PO for approval
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> submitPOForApproval(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} submitting PO {} for approval", user.username(), id);

        PurchaseOrderResponse response = poService.submitPOForApproval(id, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 7. POST /api/v1/po/{id}/approve
     * Approve PO
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> approvePO(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} approving PO {}", user.username(), id);

        PurchaseOrderResponse response = poService.approvePO(id, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 8. POST /api/v1/po/{id}/send-to-vendor
     * Send PO to vendor
     */
    @PostMapping("/{id}/send-to-vendor")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> sendPOToVendor(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} sending PO {} to vendor", user.username(), id);

        PurchaseOrderResponse response = poService.sendPOToVendor(id, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 9. POST /api/v1/po/{id}/receive-goods
     * Receive goods against PO
     */
    @PostMapping("/{id}/receive-goods")
    @PreAuthorize("hasAnyRole('STOREKEEPER', 'PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> receiveGoods(
            @PathVariable Integer id,
            @Valid @RequestBody List<ReceiveGoodsRequest> receiveRequests,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} receiving goods for PO {}", user.username(), id);

        PurchaseOrderResponse response = poService.receiveGoods(id, receiveRequests, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 10. POST /api/v1/po/{id}/cancel
     * Cancel PO
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> cancelPO(
            @PathVariable Integer id,
            @Valid @RequestBody CancelPORequest request,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} cancelling PO {}", user.username(), id);

        PurchaseOrderResponse response = poService.cancelPO(id, request, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 11. POST /api/v1/po/{id}/close
     * Close PO after completion
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> closePO(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} closing PO {}", user.username(), id);

        PurchaseOrderResponse response = poService.closePO(id, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 12. GET /api/v1/po/by-vendor/{vendorId}
     * Get POs by vendor
     */
    @GetMapping("/by-vendor/{vendorId}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'VIEWER', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getPOsByVendor(
            @PathVariable Integer vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "poDate,desc") String[] sort,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching POs for vendor {}", user.username(), vendorId);

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<POSummaryResponse> pos = poService.getPOsByVendor(vendorId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pos.getContent());
        response.put("currentPage", pos.getNumber());
        response.put("totalItems", pos.getTotalElements());
        response.put("totalPages", pos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 13. GET /api/v1/po/by-department/{departmentId}
     * Get POs by department
     */
    @GetMapping("/by-department/{departmentId}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'VIEWER', 'PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getPOsByDepartment(
            @PathVariable Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "poDate,desc") String[] sort,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching POs for department {}", user.username(), departmentId);

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<POSummaryResponse> pos = poService.getPOsByDepartment(departmentId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pos.getContent());
        response.put("currentPage", pos.getNumber());
        response.put("totalItems", pos.getTotalElements());
        response.put("totalPages", pos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 14. GET /api/v1/po/pending-approval
     * Get POs pending for approval
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getPendingForApproval(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching POs pending for approval", user.username());

        Pageable pageable = PageRequest.of(page, size);
        Page<POSummaryResponse> pos = poService.getPendingForApproval(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pos.getContent());
        response.put("currentPage", pos.getNumber());
        response.put("totalItems", pos.getTotalElements());
        response.put("totalPages", pos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 15. GET /api/v1/po/overdue
     * Get overdue POs
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getOverduePOs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching overdue POs", user.username());

        Pageable pageable = PageRequest.of(page, size);
        Page<POSummaryResponse> pos = poService.getOverduePOs(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pos.getContent());
        response.put("currentPage", pos.getNumber());
        response.put("totalItems", pos.getTotalElements());
        response.put("totalPages", pos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 16. GET /api/v1/po/dashboard/statistics
     * Get PO dashboard statistics
     */
    @GetMapping("/dashboard/statistics")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics(Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching PO dashboard statistics", user.username());

        Map<String, Object> statistics = poService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    // =====================================================
    // B.1 FIX: PO AMENDMENT ENDPOINTS
    // =====================================================

    /**
     * 17. POST /api/v1/po/{id}/amend
     * B.1 FIX: Amend PO after vendor confirmation
     * All amendments are tracked with full audit trail
     */
    @PostMapping("/{id}/amend")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PurchaseOrderResponse> amendPO(
            @PathVariable Integer id,
            @Valid @RequestBody AmendPORequest request,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} amending PO {}", user.username(), id);

        PurchaseOrderResponse response = poService.amendPO(id, request, user.employeeNumber().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * 18. GET /api/v1/po/{id}/amendments
     * B.1 FIX: Get amendment history for a PO
     */
    @GetMapping("/{id}/amendments")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'VIEWER', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getAmendmentHistory(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        log.info("User {} fetching amendment history for PO {}", user.username(), id);

        List<POAmendmentResponse> amendments = poService.getAmendmentHistory(id);
        Integer currentVersion = poService.getCurrentAmendmentVersion(id);

        Map<String, Object> response = new HashMap<>();
        response.put("poId", id);
        response.put("currentVersion", currentVersion);
        response.put("amendments", amendments);
        response.put("totalAmendments", amendments.size());

        return ResponseEntity.ok(response);
    }
}
