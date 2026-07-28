package com.nslindia.procurezone.indent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nslindia.procurezone.security.UserPrincipal;

import com.nslindia.procurezone.indent.dto.CancelIndentRequest;
import com.nslindia.procurezone.indent.dto.CreateIndentRequest;
import com.nslindia.procurezone.indent.dto.IndentFormMetaResponse;
import com.nslindia.procurezone.indent.dto.IndentListResponse;
import com.nslindia.procurezone.indent.dto.IndentResponse;
import com.nslindia.procurezone.indent.dto.L1ApprovalRequest;
import com.nslindia.procurezone.indent.dto.L2ApprovalRequest;
import com.nslindia.procurezone.indent.dto.PendingApprovalResponse;
import com.nslindia.procurezone.indent.dto.UpdateIndentRequest;

import jakarta.validation.Valid;

/**
 * REST controller for indent management.
 * Provides endpoints for CRUD operations and workflow management.
 */
@RestController
@RequestMapping("/api/v1/indents")
@Validated
public class IndentController {

    private static final Logger log = LoggerFactory.getLogger(IndentController.class);

    private final IndentService indentService;

    @Autowired
    private IndentFormMetaService indentFormMetaService;

    public IndentController(IndentService indentService) {
        this.indentService = indentService;
    }

    /**
     * GET /api/v1/indents/meta
     * Returns pre-populated form metadata for the indent creation form.
     */
    @GetMapping("/meta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentFormMetaResponse> getFormMeta(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(indentFormMetaService.getIndentMeta(principal));
    }

    /**
     * Create a new indent
     * POST /api/v1/indents
     */

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentResponse> createIndent(
            @Valid @RequestBody CreateIndentRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Creating indent for user: {} ({})", principal.displayName(), principal.email());
        IndentResponse response = indentService.createIndent(request, principal.email());
        log.info("Indent created successfully with ID: {} by user: {}", response.id(), principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get indent by ID
     * GET /api/v1/indents/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentResponse> getIndentById(@PathVariable Integer id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.debug("Fetching indent ID: {} for user: {}", id, principal.email());
        IndentResponse response = indentService.getIndentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List all indents with pagination
     * GET /api/v1/indents?page=0&size=10&sort=indentDate,desc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'FLOORINCHARGE', 'GOODSINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'USER', 'SUPERVISOR')")
    public ResponseEntity<Page<IndentListResponse>> listIndents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "indentDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer plantId,
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer approvedStatus,
            @RequestParam(required = false) Integer finalStatus,
            @RequestParam(required = false) Integer procurementStatus) {

        Pageable pageable = createPageable(page, size, sort);
        LocalDateTime fromDt = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDt = toDate != null ? toDate.atTime(23, 59, 59) : null;
        Page<IndentListResponse> response = indentService.filterIndents(
                search, status, departmentId, plantId, companyId, fromDt, toDt,
                approvedStatus, finalStatus, procurementStatus, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Export indents as Excel or CSV, respecting the same filters AND role-visibility as the
     * list endpoint (the service routes through the scoped filterIndents()). Available to every
     * role that can view the list — you can only export what you can already see.
     * GET /api/v1/indents/export?format=xlsx|csv&search=...&status=...&...
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'FLOORINCHARGE', 'GOODSINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'USER', 'SUPERVISOR')")
    public ResponseEntity<byte[]> exportIndents(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer plantId,
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer approvedStatus,
            @RequestParam(required = false) Integer finalStatus,
            @RequestParam(required = false) Integer procurementStatus) throws java.io.IOException {

        LocalDateTime fromDt = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDt   = toDate   != null ? toDate.atTime(23, 59, 59) : null;

        java.util.List<com.nslindia.procurezone.indent.dto.IndentListResponse> rows =
                indentService.exportIndents(search, status, departmentId, plantId, companyId, fromDt, toDt,
                                            approvedStatus, finalStatus, procurementStatus);

        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Indent No.,Date,Company,Department,Requested By,Delivery Date,Status,Items\n");
            for (var r : rows) {
                sb.append(csv(r.indentNumber())).append(',')
                  .append(csv(r.indentDate() != null ? r.indentDate().toLocalDate().toString() : "")).append(',')
                  .append(csv(r.companyName())).append(',')
                  .append(csv(r.departmentName())).append(',')
                  .append(csv(r.employeeName())).append(',')
                  .append(csv(r.deliveryDate() != null ? r.deliveryDate().toString() : "")).append(',')
                  .append(csv(r.displayStatus() != null ? r.displayStatus() : r.statusName())).append(',')
                  .append(r.detailsCount() != null ? r.detailsCount() : 0).append('\n');
            }
            byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"indents-" + java.time.LocalDate.now() + ".csv\"")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .body(bytes);
        }

        // Excel (default)
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Indents");

            // Header style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Indent No.", "Date", "Company", "Department", "Requested By", "Delivery Date", "Status", "Items"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 18 * 256);
            }

            int rowNum = 1;
            for (var r : rows) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.indentNumber() != null ? r.indentNumber() : "");
                row.createCell(1).setCellValue(r.indentDate() != null ? r.indentDate().toLocalDate().toString() : "");
                row.createCell(2).setCellValue(r.companyName() != null ? r.companyName() : "");
                row.createCell(3).setCellValue(r.departmentName() != null ? r.departmentName() : "");
                row.createCell(4).setCellValue(r.employeeName() != null ? r.employeeName() : "");
                row.createCell(5).setCellValue(r.deliveryDate() != null ? r.deliveryDate().toString() : "");
                row.createCell(6).setCellValue(r.displayStatus() != null ? r.displayStatus() : (r.statusName() != null ? r.statusName() : ""));
                row.createCell(7).setCellValue(r.detailsCount() != null ? r.detailsCount() : 0);
            }

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            wb.write(bos);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"indents-" + java.time.LocalDate.now() + ".xlsx\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(bos.toByteArray());
        }
    }

    private static String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * List indents by status
     * GET /api/v1/indents/status/{statusId}?page=0&size=10
     */
    @GetMapping("/status/{statusId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<IndentListResponse>> listIndentsByStatus(
            @PathVariable Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "indentDate,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<IndentListResponse> response = indentService.listIndentsByStatus(statusId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * List indents by employee
     * GET /api/v1/indents/employee/{empNumber}?page=0&size=10
     */
    @GetMapping("/employee/{empNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<IndentListResponse>> listIndentsByEmployee(
            @PathVariable Integer empNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "indentDate,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<IndentListResponse> response = indentService.listIndentsByEmployee(empNumber, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Search indents
     * GET /api/v1/indents/search?q=searchTerm&page=0&size=10
     */

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'FLOORINCHARGE', 'GOODSINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'USER', 'SUPERVISOR')")
    public ResponseEntity<Page<IndentListResponse>> searchIndents(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "indentDate,desc") String[] sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<IndentListResponse> response = indentService.searchIndents(q, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update indent (only in Draft status)
     * PUT /api/v1/indents/{id}
     */

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentResponse> updateIndent(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateIndentRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        IndentResponse response = indentService.updateIndent(id, request, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete/Cancel indent (only in Draft status)
     * DELETE /api/v1/indents/{id}
     * 
     * @deprecated Use POST /api/v1/indents/{id}/cancel with reason instead
     */

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteIndent(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        indentService.deleteIndent(id, principal.email());
        return ResponseEntity.noContent().build();
    }

    /**
     * B.3 FIX: Cancel indent with mandatory reason (only in Draft status)
     * POST /api/v1/indents/{id}/cancel
     * Sends email notification to indent creator
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelIndent(
            @PathVariable Integer id,
            @Valid @RequestBody CancelIndentRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Cancel request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        indentService.cancelIndent(id, request.reason(), principal.email());
        log.info("Indent ID: {} cancelled successfully by: {}", id, principal.email());
        return ResponseEntity.noContent().build();
    }

    /**
     * Submit indent for approval
     * POST /api/v1/indents/{id}/submit
     */

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentResponse> submitIndent(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        IndentResponse response = indentService.submitIndent(id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Approve indent
     * POST /api/v1/indents/{id}/approve
     */

    @PostMapping("/{id}/approve")
    // Workflow ACTION (2026-07-27): requires the actual workflow role (DeptHead/PlantManager).
    // ADMIN/SUPERADMIN removed — a global admin who is not a workflow approver cannot approve.
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER')")
    public ResponseEntity<IndentResponse> approveIndent(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Approve request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        log.debug("User roles: {}", principal.roles());
        IndentResponse response = indentService.approveIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} approved successfully by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Reject indent
     * POST /api/v1/indents/{id}/reject
     */

    @PostMapping("/{id}/reject")
    // Workflow ACTION (2026-07-27): requires the actual workflow role; ADMIN/SUPERADMIN removed.
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER')")
    public ResponseEntity<IndentResponse> rejectIndent(
            @PathVariable Integer id,
            @RequestParam String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Reject request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        log.debug("User roles: {}, Reject remarks: {}", principal.roles(), remarks);
        IndentResponse response = indentService.rejectIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} rejected successfully by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    // ==================== L1/L2 APPROVAL ENDPOINTS ====================

    /**
     * L1 Approve indent (RM/Section Head level) with quantity adjustment
     * POST /api/v1/indents/{id}/l1-approve
     * 
     * The Reporting Manager can approve indents from their direct reports and
     * optionally adjust quantities (rmQuantity) for each line item.
     */
    @PostMapping("/{id}/l1-approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentResponse> l1ApproveIndent(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) L1ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("L1 Approve request for indent ID: {} by user: {} ({})", id, principal.displayName(),
                principal.email());

        // Default request if none provided
        L1ApprovalRequest req = request != null ? request : new L1ApprovalRequest(null, null);
        IndentResponse response = indentService.l1Approve(id, req, principal.email());

        log.info("Indent ID: {} L1 approved by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * L1 Reject indent (RM/Section Head level)
     * POST /api/v1/indents/{id}/l1-reject
     */
    @PostMapping("/{id}/l1-reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IndentResponse> l1RejectIndent(
            @PathVariable Integer id,
            @RequestParam String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("L1 Reject request for indent ID: {} by user: {} ({})", id, principal.displayName(),
                principal.email());
        IndentResponse response = indentService.l1Reject(id, principal.email(), remarks);
        log.info("Indent ID: {} L1 rejected by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * L2 Approve indent (Department Head level) with quantity adjustment
     * POST /api/v1/indents/{id}/l2-approve
     * 
     * The Department Head can approve indents after L1 approval and
     * optionally further adjust quantities (deptQuantity) for each line item.
     */
    @PostMapping("/{id}/l2-approve")
    // Workflow ACTION (2026-07-27): L2 requires the actual DeptHead/PlantManager role; ADMIN/SUPERADMIN removed.
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER')")
    public ResponseEntity<IndentResponse> l2ApproveIndent(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) L2ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("L2 Approve request for indent ID: {} by user: {} ({})", id, principal.displayName(),
                principal.email());

        // Default request if none provided
        L2ApprovalRequest req = request != null ? request : new L2ApprovalRequest(null, null);
        IndentResponse response = indentService.l2Approve(id, req, principal.email());

        log.info("Indent ID: {} L2 approved by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * L2 Reject indent (Department Head level)
     * POST /api/v1/indents/{id}/l2-reject
     */
    @PostMapping("/{id}/l2-reject")
    // Workflow ACTION (2026-07-27): L2 requires the actual DeptHead/PlantManager role; ADMIN/SUPERADMIN removed.
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER')")
    public ResponseEntity<IndentResponse> l2RejectIndent(
            @PathVariable Integer id,
            @RequestParam String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("L2 Reject request for indent ID: {} by user: {} ({})", id, principal.displayName(),
                principal.email());
        IndentResponse response = indentService.l2Reject(id, principal.email(), remarks);
        log.info("Indent ID: {} L2 rejected by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending L1 approvals for current user (approvals from their direct
     * reports)
     * GET /api/v1/indents/pending/l1
     */
    @GetMapping("/pending/l1")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<PendingApprovalResponse>> getPendingL1Approvals(
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Fetching pending L1 approvals for user: {}", principal.email());
        java.util.List<PendingApprovalResponse> response = indentService.getPendingL1Approvals(principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending L2 approvals for current user's department (from JWT).
     * GET /api/v1/indents/pending/l2
     * SUPERADMIN/ADMIN see all departments; DEPTHEAD/PLANTMANAGER see their own.
     */
    @GetMapping("/pending/l2")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<java.util.List<PendingApprovalResponse>> getPendingL2Approvals(
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Fetching pending L2 approvals for user: {}", principal.email());
        java.util.List<PendingApprovalResponse> response = indentService.getPendingL2Approvals(principal.email());
        return ResponseEntity.ok(response);
    }

    // ==================== END L1/L2 APPROVAL ENDPOINTS ====================

    /**
     * Final Approve indent (Finance/Plant Manager level)
     * POST /api/v1/indents/{id}/final-approve
     * Transitions from Status 3 (Dept Head Approved) → Status 4 (Finance Approved)
     */
    @PostMapping("/{id}/final-approve")
    @PreAuthorize("hasRole('PLANTMANAGER') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<IndentResponse> finalApproveIndent(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Final approve request for indent ID: {} by user: {} ({})", id, principal.displayName(),
                principal.email());
        IndentResponse response = indentService.finalApproveIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} final approved successfully by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Procurement Approve indent (Procurement level)
     * POST /api/v1/indents/{id}/procurement-approve
     * Transitions from Status 4 (Finance Approved) → Status 5 (Procurement
     * Approved)
     */
    @PostMapping("/{id}/procurement-approve")
    @PreAuthorize("hasRole('PROCUREMENT') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<IndentResponse> procurementApproveIndent(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Procurement approve request for indent ID: {} by user: {} ({})", id, principal.displayName(),
                principal.email());
        IndentResponse response = indentService.procurementApproveIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} procurement approved by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Update procurement sub-stage.
     * POST /api/v1/indents/{id}/procurement-update
     * Sets indent_procurement_status to one of: 5=Quotations, 6=Negotiation, 7=PO Released, 8=Hold, 9=Cash Buy.
     */
    @PostMapping("/{id}/procurement-update")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<IndentResponse> updateProcurementStatus(
            @PathVariable Integer id,
            @Valid @RequestBody com.nslindia.procurezone.indent.dto.ProcurementUpdateRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Procurement update for indent {} sub-status {} by {}", id, request.procurementSubStatus(), principal.email());
        IndentResponse response = indentService.updateProcurementStatus(
                id, principal.email(),
                request.procurementSubStatus(), request.poNumber(),
                request.deliveryDate(), request.remarks());
        return ResponseEntity.ok(response);
    }

    /**
     * Put indent on hold
     * POST /api/v1/indents/{id}/hold
     * Transitions any approved status → Status 7 (On Hold)
     */
    @PostMapping("/{id}/hold")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('PROCUREMENT') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<IndentResponse> holdIndent(
            @PathVariable Integer id,
            @RequestParam String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Hold request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        IndentResponse response = indentService.holdIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} put on hold by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Complete indent
     * POST /api/v1/indents/{id}/complete
     * Transitions from Status 5 (Procurement Approved) → Status 8 (Completed)
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'FLOORINCHARGE', 'GOODSINCHARGE', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<IndentResponse> completeIndent(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Complete request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        IndentResponse response = indentService.completeIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} completed by: {}", id, principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Resume indent from hold status
     * POST /api/v1/indents/{id}/resume
     * Transitions from Status 7 (On Hold) → Previous status or Status 3
     */
    @PostMapping("/{id}/resume")
    @PreAuthorize("hasRole('DEPTHEAD') or hasRole('PLANTMANAGER') or hasRole('PROCUREMENT') or hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<IndentResponse> resumeIndent(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Resume request for indent ID: {} by user: {} ({})", id, principal.displayName(), principal.email());
        IndentResponse response = indentService.resumeIndent(id, principal.email(), remarks);
        log.info("Indent ID: {} resumed by: {}", id, principal.email());
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
