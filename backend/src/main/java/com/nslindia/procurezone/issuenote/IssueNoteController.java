package com.nslindia.procurezone.issuenote;

import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.issuenote.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Issue Note operations
 * Handles material outflow tracking from stores
 * 
 * Endpoints:
 * - POST /api/v1/issue-notes - Create issue note
 * - GET /api/v1/issue-notes - List with filters
 * - GET /api/v1/issue-notes/{id} - Get by ID
 * - GET /api/v1/issue-notes/number/{issueNoteNumber} - Get by number
 * - POST /api/v1/issue-notes/{id}/submit - Submit for approval
 * - POST /api/v1/issue-notes/{id}/approve - Approve (Manager)
 * - POST /api/v1/issue-notes/{id}/reject - Reject (Manager)
 * - POST /api/v1/issue-notes/{id}/issue - Issue goods (Store keeper)
 * - POST /api/v1/issue-notes/{id}/reject-stores - Reject by stores
 * - POST /api/v1/issue-notes/{id}/cancel - Cancel
 * - GET /api/v1/issue-notes/pending-approval - Approval queue
 * - GET /api/v1/issue-notes/pending-issue - Store keeper queue
 * - GET /api/v1/issue-notes/by-department/{deptId} - By department
 * - GET /api/v1/issue-notes/my-issue-notes - My created issue notes
 * - GET /api/v1/issue-notes/statistics - Dashboard statistics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/issue-notes")
@RequiredArgsConstructor
public class IssueNoteController {

        private final IssueNoteService issueNoteService;

        /**
         * Create a new issue note
         * Authorized: REQUESTER, EMPLOYEE, ADMIN
         */
        @PostMapping
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> createIssueNote(
                        @Valid @RequestBody CreateIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Creating issue note for department: {} by user: {}",
                                request.departmentId(), userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.createIssueNote(
                                request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note created successfully");
                responseMap.put("data", response);

                return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        }

        /**
         * Get all issue notes with pagination and filters
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('USER', 'PLANTMANAGER', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'VIEWER')")
        public ResponseEntity<Map<String, Object>> getAllIssueNotes(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) Integer status,
                        @RequestParam(required = false) Integer companyId,
                        @RequestParam(required = false) Integer departmentId) {

                Page<IssueNoteSummaryResponse> issueNotePage = issueNoteService.getAll(
                                page, size, status, companyId, departmentId);

                Map<String, Object> response = new HashMap<>();
                response.put("content", issueNotePage.getContent());
                response.put("currentPage", issueNotePage.getNumber());
                response.put("totalItems", issueNotePage.getTotalElements());
                response.put("totalPages", issueNotePage.getTotalPages());

                return ResponseEntity.ok(response);
        }

        /**
         * Get issue note by ID
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('USER', 'PLANTMANAGER', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'VIEWER')")
        public ResponseEntity<Map<String, Object>> getIssueNoteById(@PathVariable Integer id) {
                IssueNoteResponse response = issueNoteService.getById(id);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Get issue note by issue note number
         */
        @GetMapping("/by-number")
        @PreAuthorize("hasAnyRole('USER', 'PLANTMANAGER', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'VIEWER')")
        public ResponseEntity<Map<String, Object>> getIssueNoteByNumber(
                        @RequestParam String issueNoteNumber) {

                IssueNoteResponse response = issueNoteService.getByIssueNoteNumber(issueNoteNumber);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Submit issue note for approval
         * Changes status from 1 (Created) to 2 (Pending Approval)
         */
        @PostMapping("/{id}/submit")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> submitForApproval(
                        @PathVariable Integer id,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Submitting issue note {} for approval by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.submitForApproval(
                                id, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note submitted for approval");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * RM Approve issue note (Reporting Manager)
         * Changes status from 2 (Pending RM Approval) to 3 (RM Approved)
         */
        @PostMapping("/{id}/rm-approve")
        @PreAuthorize("hasAnyRole('SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> rmApproveIssueNote(
                        @PathVariable Integer id,
                        @Valid @RequestBody ApproveIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("RM approving issue note {} by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.rmApprove(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note RM approved successfully");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * RM Reject issue note (Reporting Manager)
         * Changes status from 2 (Pending RM Approval) to 5 (Rejected by RM)
         */
        @PostMapping("/{id}/rm-reject")
        @PreAuthorize("hasAnyRole('SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> rmRejectIssueNote(
                        @PathVariable Integer id,
                        @Valid @RequestBody RejectIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("RM rejecting issue note {} by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.rmReject(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note RM rejected");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Approve issue note (Manager)
         * Changes status from 3 (RM Approved) to 4 (Approved)
         */
        @PostMapping("/{id}/approve")
        @PreAuthorize("hasAnyRole('PLANTMANAGER', 'PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> approveIssueNote(
                        @PathVariable Integer id,
                        @Valid @RequestBody ApproveIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Approving issue note {} by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.approve(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note approved successfully");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Reject issue note (Manager)
         * Changes status from 3 (RM Approved) to 6 (Rejected by Manager)
         */
        @PostMapping("/{id}/reject")
        @PreAuthorize("hasAnyRole('PLANTMANAGER', 'PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> rejectByManager(
                        @PathVariable Integer id,
                        @Valid @RequestBody RejectIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Rejecting issue note {} by manager, user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.rejectByManager(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note rejected");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Issue goods from stores (Store keeper)
         * Changes status from 4 (Approved) or 7 (Pending Store Issue) to 8 (Issued)
         * Updates inventory balances
         */
        @PostMapping("/{id}/issue")
        @PreAuthorize("hasAnyRole('ISSUECONFIRM', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> issueGoods(
                        @PathVariable Integer id,
                        @Valid @RequestBody IssueGoodsRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Issuing goods for issue note {} by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.issueGoods(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Goods issued successfully");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Reject by stores (insufficient stock or other reason)
         * Changes status from 7 (Pending Store Issue) to 9 (Rejected by Stores)
         */
        @PostMapping("/{id}/reject-stores")
        @PreAuthorize("hasAnyRole('ISSUECONFIRM', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> rejectByStores(
                        @PathVariable Integer id,
                        @Valid @RequestBody RejectIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Rejecting issue note {} by stores, user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.rejectByStores(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note rejected by stores");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Cancel issue note
         * Only allowed for status 1 (Created) or 2 (Pending Approval)
         */
        @PostMapping("/{id}/cancel")
        @PreAuthorize("hasAnyRole('USER', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> cancelIssueNote(
                        @PathVariable Integer id,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Cancelling issue note {} by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.cancelIssueNote(
                                id, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Issue note cancelled");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }

        /**
         * Get pending approval queue (for managers)
         * Status = 3 (RM Approved, pending manager approval)
         */
        @GetMapping("/pending-approval")
        @PreAuthorize("hasAnyRole('PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getPendingApproval(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<IssueNoteSummaryResponse> issueNotePage = issueNoteService.getPendingApproval(page, size);

                Map<String, Object> response = new HashMap<>();
                response.put("content", issueNotePage.getContent());
                response.put("currentPage", issueNotePage.getNumber());
                response.put("totalItems", issueNotePage.getTotalElements());
                response.put("totalPages", issueNotePage.getTotalPages());

                return ResponseEntity.ok(response);
        }

        /**
         * Get pending RM approval queue (for supervisors/reporting managers)
         * Status = 2 (Pending RM Approval)
         */
        @GetMapping("/pending-rm-approval")
        @PreAuthorize("hasAnyRole('SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getPendingRmApproval(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<IssueNoteSummaryResponse> issueNotePage = issueNoteService.getPendingRmApproval(page, size);

                Map<String, Object> response = new HashMap<>();
                response.put("content", issueNotePage.getContent());
                response.put("currentPage", issueNotePage.getNumber());
                response.put("totalItems", issueNotePage.getTotalElements());
                response.put("totalPages", issueNotePage.getTotalPages());

                return ResponseEntity.ok(response);
        }

        /**
         * Get pending issue queue (for store keepers)
         * Status = 4 (Approved) or 7 (Pending Store Issue)
         */
        @GetMapping("/pending-issue")
        @PreAuthorize("hasAnyRole('ISSUECONFIRM', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getPendingIssue(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<IssueNoteSummaryResponse> issueNotePage = issueNoteService.getPendingIssue(page, size);

                Map<String, Object> response = new HashMap<>();
                response.put("content", issueNotePage.getContent());
                response.put("currentPage", issueNotePage.getNumber());
                response.put("totalItems", issueNotePage.getTotalElements());
                response.put("totalPages", issueNotePage.getTotalPages());

                return ResponseEntity.ok(response);
        }

        /**
         * Get issue notes by department
         */
        @GetMapping("/by-department/{deptId}")
        @PreAuthorize("hasAnyRole('PLANTMANAGER', 'USER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getByDepartment(
                        @PathVariable Integer deptId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<IssueNoteSummaryResponse> issueNotePage = issueNoteService.getByDepartment(deptId, page, size);

                Map<String, Object> response = new HashMap<>();
                response.put("content", issueNotePage.getContent());
                response.put("currentPage", issueNotePage.getNumber());
                response.put("totalItems", issueNotePage.getTotalElements());
                response.put("totalPages", issueNotePage.getTotalPages());

                return ResponseEntity.ok(response);
        }

        /**
         * Get issue notes created by the current user
         */
        @GetMapping("/my-issue-notes")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getMyIssueNotes(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                Page<IssueNoteSummaryResponse> issueNotePage = issueNoteService.getByCreator(
                                userPrincipal.employeeNumber(),
                                page, size);

                Map<String, Object> response = new HashMap<>();
                response.put("content", issueNotePage.getContent());
                response.put("currentPage", issueNotePage.getNumber());
                response.put("totalItems", issueNotePage.getTotalElements());
                response.put("totalPages", issueNotePage.getTotalPages());

                return ResponseEntity.ok(response);
        }

        /**
         * Get dashboard statistics
         */
        @GetMapping("/statistics")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'PLANTMANAGER', 'ISSUECONFIRM')")
        public ResponseEntity<Map<String, Object>> getStatistics() {
                IssueNoteStatisticsResponse stats = issueNoteService.getStatistics();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", stats);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // B.2 FIX: ISSUE NOTE RETURN ENDPOINT
        // =====================================================

        /**
         * B.2 FIX: Return issued materials back to stores
         * Reverses inventory transaction
         * Changes status from 8 (Issued) to 10 (Returned)
         */
        @PostMapping("/{id}/return")
        @PreAuthorize("hasAnyRole('ISSUECONFIRM', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> returnMaterials(
                        @PathVariable Integer id,
                        @Valid @RequestBody ReturnIssueNoteRequest request,
                        Authentication authentication) {

                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

                log.info("Processing return for issue note {} by user: {}", id, userPrincipal.employeeNumber());

                IssueNoteResponse response = issueNoteService.returnIssuedMaterials(
                                id, request, userPrincipal.employeeNumber());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Materials returned to stores successfully");
                responseMap.put("data", response);

                return ResponseEntity.ok(responseMap);
        }
}
