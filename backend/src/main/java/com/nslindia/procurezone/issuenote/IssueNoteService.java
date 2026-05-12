package com.nslindia.procurezone.issuenote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.inventory.InventoryService;
import com.nslindia.procurezone.issuenote.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Issue Note operations
 * Handles material outflow from stores to departments/projects
 * 
 * @author NSL India
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IssueNoteService {

    private final IssueNoteRepository issueNoteRepository;
    private final AuditService auditService;
    private final InventoryService inventoryService;

    private static final String ENTITY_TYPE = "Issue Note";
    private static final String ERROR_NOT_FOUND = "Issue Note not found with ID: ";

    /**
     * Create a new issue note
     */
    @Transactional
    public IssueNoteResponse createIssueNote(CreateIssueNoteRequest request, Integer userId) {
        log.info("Creating new issue note for company: {}, department: {}",
                request.companyId(), request.departmentId());

        // Generate issue note number
        String issueNoteNumber = generateIssueNoteNumber();

        // Build issue note entity
        IssueNote issueNote = IssueNote.builder()
                .issueNoteNumber(issueNoteNumber)
                .issueDate(LocalDateTime.now())
                .companyId(request.companyId())
                .departmentId(request.departmentId())
                .sectionId(request.sectionId())
                .plantId(request.plantId())
                .issuedTo(request.issuedTo())
                .purpose(request.purpose())
                .comments(request.comments())
                .createdBy(userId)
                .status(1) // Created
                .lastModifiedDate(LocalDateTime.now())
                .lastModifiedBy(userId)
                .build();

        // Save issue note first to get ID
        issueNote = issueNoteRepository.save(issueNote);

        // Create line items
        for (var lineItem : request.lineItems()) {
            IssueNoteDetails detail = IssueNoteDetails.builder()
                    .issueNote(issueNote)
                    .materialId(lineItem.materialId())
                    .unitOfMeasureId(lineItem.unitOfMeasureId())
                    .quantity(lineItem.quantity())
                    .rate(lineItem.rate())
                    .amount(lineItem.rate() != null ? lineItem.rate().multiply(lineItem.quantity()) : null)
                    .purpose(lineItem.purpose())
                    .status(1)
                    .lastModifiedDate(LocalDateTime.now())
                    .lastModifiedBy(userId)
                    .build();

            issueNote.addDetail(detail);
        }

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        auditService.logEntityChange(
                "ISSUE_NOTE_CREATED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Created"));

        log.info("Created issue note: {}", issueNoteNumber);
        return mapToResponse(issueNote);
    }

    /**
     * Submit issue note for approval
     */
    @Transactional
    public IssueNoteResponse submitForApproval(Integer id, Integer userId) {
        log.info("Submitting issue note {} for approval", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeSubmitted()) {
            throw new IllegalStateException("Issue Note cannot be submitted in current state: " +
                    issueNote.getStatusDescription());
        }

        issueNote.setStatus(2); // Pending Approval
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        auditService.logEntityChange(
                "ISSUE_NOTE_SUBMITTED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Submitted for approval"));

        log.info("Issue note {} submitted for approval", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * Approve issue note (Manager)
     */
    @Transactional
    public IssueNoteResponse approve(Integer id, ApproveIssueNoteRequest request, Integer userId) {
        log.info("Approving issue note {}", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeApproved()) {
            throw new IllegalStateException("Issue Note cannot be approved in current state: " +
                    issueNote.getStatusDescription()
                    + ". It must be RM approved first, or have supervisor bypass enabled.");
        }

        issueNote.setStatus(4); // Approved
        issueNote.setApprovedBy(userId);
        issueNote.setApprovedByDate(LocalDateTime.now());
        issueNote.setApprovedStatus(1); // Approved
        issueNote.setApprovedRemarks(request.remarks());
        issueNote.setComments((issueNote.getComments() != null ? issueNote.getComments() : "") +
                "\nApproval remarks: " + request.remarks());
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("approver", userId);
        extraDetails.put("approvalRemarks", request.remarks());
        auditService.logEntityChange(
                "ISSUE_NOTE_APPROVED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Approved by manager", extraDetails));

        log.info("Issue note {} approved", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * RM (Reporting Manager) Approve issue note
     */
    @Transactional
    public IssueNoteResponse rmApprove(Integer id, ApproveIssueNoteRequest request, Integer userId) {
        log.info("RM approving issue note {}", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeRmApproved()) {
            throw new IllegalStateException("Issue Note cannot be RM approved in current state: " +
                    issueNote.getStatusDescription()
                    + ". Either not pending RM approval or supervisor bypass is enabled.");
        }

        issueNote.setStatus(3); // RM Approved
        issueNote.setRmApprovedBy(userId);
        issueNote.setRmApprovedByDate(LocalDateTime.now());
        issueNote.setRmApprovedStatus(1); // Approved
        issueNote.setRmApprovedRemarks(request.remarks());
        issueNote.setComments((issueNote.getComments() != null ? issueNote.getComments() : "") +
                "\nRM approval remarks: " + request.remarks());
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("rmApprover", userId);
        extraDetails.put("rmRemarks", request.remarks());
        auditService.logEntityChange(
                "ISSUE_NOTE_RM_APPROVED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "RM approved", extraDetails));

        log.info("Issue note {} RM approved", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * RM (Reporting Manager) Reject issue note
     */
    @Transactional
    public IssueNoteResponse rmReject(Integer id, RejectIssueNoteRequest request, Integer userId) {
        log.info("RM rejecting issue note {}", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeRmRejected()) {
            throw new IllegalStateException("Issue Note cannot be RM rejected in current state: " +
                    issueNote.getStatusDescription());
        }

        issueNote.setStatus(5); // Rejected by RM
        issueNote.setRmApprovedBy(userId);
        issueNote.setRmApprovedByDate(LocalDateTime.now());
        issueNote.setRmApprovedStatus(6); // Rejected
        issueNote.setRmApprovedRemarks(request.reason());
        issueNote.setComments((issueNote.getComments() != null ? issueNote.getComments() : "") +
                "\nRM rejection reason: " + request.reason());
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("rmRejectedBy", userId);
        extraDetails.put("rmRejectionReason", request.reason());
        auditService.logEntityChange(
                "ISSUE_NOTE_RM_REJECTED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "RM rejected", extraDetails));

        log.info("Issue note {} RM rejected", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * Reject issue note by manager
     */
    @Transactional
    public IssueNoteResponse rejectByManager(Integer id, RejectIssueNoteRequest request, Integer userId) {
        log.info("Rejecting issue note {} by manager", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeRejectedByManager()) {
            throw new IllegalStateException("Issue Note cannot be rejected in current state: " +
                    issueNote.getStatusDescription());
        }

        issueNote.setStatus(6); // Rejected by Manager
        issueNote.setApprovedBy(userId);
        issueNote.setApprovedByDate(LocalDateTime.now());
        issueNote.setApprovedStatus(6); // Rejected (from tbl_indent_status)
        issueNote.setApprovedRemarks(request.reason());
        issueNote.setComments((issueNote.getComments() != null ? issueNote.getComments() : "") +
                "\nManager rejection reason: " + request.reason());
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("rejectedBy", userId);
        extraDetails.put("rejectionReason", request.reason());
        auditService.logEntityChange(
                "ISSUE_NOTE_REJECTED_MANAGER",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Rejected by manager", extraDetails));

        log.info("Issue note {} rejected by manager", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * Issue goods from stores (critical - updates inventory)
     */
    @Transactional
    public IssueNoteResponse issueGoods(Integer id, IssueGoodsRequest request, Integer userId) {
        log.info("Issuing goods for issue note {}", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeIssued()) {
            throw new IllegalStateException("Issue Note cannot be issued in current state: " +
                    issueNote.getStatusDescription());
        }

        // Check stock availability for all materials
        for (IssueNoteDetails detail : issueNote.getDetails()) {
            if (!inventoryService.isSufficientStock(detail.getMaterialId(),
                    issueNote.getPlantId(), detail.getQuantity())) {
                BigDecimal available = inventoryService.getAvailableStock(
                        detail.getMaterialId(), issueNote.getPlantId());
                throw new IllegalStateException(
                        String.format("Insufficient stock for material %d. Required: %s, Available: %s",
                                detail.getMaterialId(), detail.getQuantity(), available));
            }
        }

        // Update inventory balances
        for (IssueNoteDetails detail : issueNote.getDetails()) {
            inventoryService.deductStock(
                    detail.getMaterialId(),
                    issueNote.getPlantId(),
                    issueNote.getCompanyId(),
                    detail.getUnitOfMeasureId(),
                    detail.getQuantity(),
                    "ISSUE_NOTE",
                    issueNote.getIssueNoteNumber(),
                    issueNote.getId(),
                    "Material issued from stores");
        }

        issueNote.setStatus(8); // Issued
        issueNote.setStoresBy(userId);
        issueNote.setStoresByDate(LocalDateTime.now());
        issueNote.setStoresByStatus(1); // Approved by stores
        issueNote.setComments(issueNote.getComments() + "\nIssue remarks: " + request.remarks());
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("issuedBy", userId);
        extraDetails.put("issueRemarks", request.remarks());
        auditService.logEntityChange(
                "ISSUE_NOTE_ISSUED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Goods issued", extraDetails));

        log.info("Issue note {} goods issued successfully", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * Reject by stores (insufficient stock)
     */
    @Transactional
    public IssueNoteResponse rejectByStores(Integer id, RejectIssueNoteRequest request, Integer userId) {
        log.info("Rejecting issue note {} by stores", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeRejectedByStores()) {
            throw new IllegalStateException("Issue Note cannot be rejected by stores in current state: " +
                    issueNote.getStatusDescription());
        }

        issueNote.setStatus(9); // Rejected by Stores
        issueNote.setStoresBy(userId);
        issueNote.setStoresByDate(LocalDateTime.now());
        issueNote.setStoresByStatus(6); // Rejected (from tbl_indent_status)
        issueNote.setComments(issueNote.getComments() + "\nStores rejection reason: " + request.reason());
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("rejectedBy", userId);
        extraDetails.put("rejectionReason", request.reason());
        auditService.logEntityChange(
                "ISSUE_NOTE_REJECTED_STORES",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Rejected by stores", extraDetails));

        log.info("Issue note {} rejected by stores", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * Cancel issue note (only if status = 1 or 2)
     */
    @Transactional
    public IssueNoteResponse cancelIssueNote(Integer id, Integer userId) {
        log.info("Cancelling issue note {}", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeCancelled()) {
            throw new IllegalStateException("Issue Note cannot be cancelled in current state: " +
                    issueNote.getStatusDescription());
        }

        // Mark as rejected
        issueNote.setStatus(4); // Rejected
        issueNote.setComments(issueNote.getComments() + "\nCancelled by user");
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        auditService.logEntityChange(
                "ISSUE_NOTE_CANCELLED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Cancelled"));

        log.info("Issue note {} cancelled", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }

    /**
     * Get all issue notes with pagination and filters
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getAll(int page, int size, Integer status,
            Integer companyId, Integer departmentId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate"));

        Page<IssueNote> issueNotesPage;

        if (status != null && companyId != null) {
            issueNotesPage = issueNoteRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else if (status != null && departmentId != null) {
            issueNotesPage = issueNoteRepository.findByDepartmentIdAndStatus(departmentId, status, pageable);
        } else if (status != null) {
            issueNotesPage = issueNoteRepository.findByStatus(status, pageable);
        } else if (companyId != null) {
            issueNotesPage = issueNoteRepository.findByCompanyId(companyId, pageable);
        } else if (departmentId != null) {
            issueNotesPage = issueNoteRepository.findByDepartmentId(departmentId, pageable);
        } else {
            issueNotesPage = issueNoteRepository.findAll(pageable);
        }

        return issueNotesPage.map(this::mapToSummaryResponse);
    }

    /**
     * Get issue note by ID
     */
    @Transactional(readOnly = true)
    public IssueNoteResponse getById(Integer id) {
        IssueNote issueNote = findById(id);
        return mapToResponse(issueNote);
    }

    /**
     * Get issue note by number
     */
    @Transactional(readOnly = true)
    public IssueNoteResponse getByIssueNoteNumber(String issueNoteNumber) {
        IssueNote issueNote = issueNoteRepository.findByIssueNoteNumber(issueNoteNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issue Note not found with number: " + issueNoteNumber));
        return mapToResponse(issueNote);
    }

    /**
     * Get pending RM approval queue (for supervisors/reporting managers)
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getPendingRmApproval(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "issueDate"));
        return issueNoteRepository.findPendingRmApproval(pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get pending manager approval queue
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getPendingApproval(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "issueDate"));
        return issueNoteRepository.findPendingApproval(pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get pending issue queue (for store keepers)
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getPendingIssue(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "issueDate"));
        return issueNoteRepository.findPendingIssue(pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get issue notes by department
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getByDepartment(Integer departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate"));
        return issueNoteRepository.findByDepartmentId(departmentId, pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get my issue notes (created by me)
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getByCreator(Integer empId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate"));
        return issueNoteRepository.findByCreatedBy(empId, pageable)
                .map(this::mapToSummaryResponse);
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public IssueNoteStatisticsResponse getStatistics() {
        Long totalCount = issueNoteRepository.count();
        Long createdCount = issueNoteRepository.countByStatus(1);
        Long pendingRmApprovalCount = issueNoteRepository.countByStatus(2);
        Long pendingApprovalCount = issueNoteRepository.countByStatus(3);
        Long approvedCount = issueNoteRepository.countByStatus(4);
        Long issuedCount = issueNoteRepository.countByStatus(8);
        Long rejectedCount = issueNoteRepository.countByStatus(5) + // Rejected by RM
                issueNoteRepository.countByStatus(6) + // Rejected by Manager
                issueNoteRepository.countByStatus(9); // Rejected by Stores

        BigDecimal totalIssuedValue = issueNoteRepository.getTotalIssuedValue();

        // Approximate current month value (could be more precise with a dedicated
        // query)
        BigDecimal currentMonthValue = BigDecimal.ZERO;

        return new IssueNoteStatisticsResponse(
                totalCount,
                createdCount,
                pendingRmApprovalCount,
                pendingApprovalCount,
                approvedCount,
                issuedCount,
                rejectedCount,
                totalIssuedValue != null ? totalIssuedValue : BigDecimal.ZERO,
                currentMonthValue);
    }

    // Helper methods

    private IssueNote findById(Integer id) {
        return issueNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_NOT_FOUND + id));
    }

    private String generateIssueNoteNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        IssueNote lastIssueNote = issueNoteRepository.findTopByOrderByIdDesc().orElse(null);

        int nextNumber = 1;
        if (lastIssueNote != null && lastIssueNote.getIssueNoteNumber() != null &&
                lastIssueNote.getIssueNoteNumber().startsWith("IN/" + year)) {
            try {
                String lastNumber = lastIssueNote.getIssueNoteNumber().split("/")[2];
                nextNumber = Integer.parseInt(lastNumber) + 1;
            } catch (Exception e) {
                log.warn("Error parsing last issue note number, starting from 1", e);
            }
        }

        return String.format("IN/%s/%05d", year, nextNumber);
    }

    private IssueNoteResponse mapToResponse(IssueNote issueNote) {
        List<IssueNoteResponse.IssueNoteDetailResponse> detailResponses = issueNote.getDetails().stream()
                .map(d -> new IssueNoteResponse.IssueNoteDetailResponse(
                        d.getId(),
                        d.getMaterialId(),
                        d.getUnitOfMeasureId(),
                        d.getQuantity(),
                        d.getRate(),
                        d.getAmount(),
                        d.getPurpose(),
                        d.getStatus()))
                .collect(Collectors.toList());

        BigDecimal totalAmount = issueNote.getDetails().stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new IssueNoteResponse(
                issueNote.getId(),
                issueNote.getIssueNoteNumber(),
                issueNote.getIssueDate(),
                issueNote.getCompanyId(),
                issueNote.getDepartmentId(),
                issueNote.getSectionId(),
                issueNote.getPlantId(),
                issueNote.getIssuedTo(),
                issueNote.getPurpose(),
                issueNote.getComments(),
                issueNote.getCreatedBy(),
                issueNote.getApprovedBy(),
                issueNote.getApprovedByDate(),
                issueNote.getStoresBy(),
                issueNote.getStoresByDate(),
                issueNote.getStatus(),
                issueNote.getStatusDescription(),
                issueNote.getApprovedStatus(),
                issueNote.getStoresByStatus(),
                issueNote.getLastModifiedDate(),
                issueNote.getLastModifiedBy(),
                detailResponses,
                totalAmount);
    }

    private IssueNoteSummaryResponse mapToSummaryResponse(IssueNote issueNote) {
        BigDecimal totalAmount = issueNote.getDetails().stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new IssueNoteSummaryResponse(
                issueNote.getId(),
                issueNote.getIssueNoteNumber(),
                issueNote.getIssueDate(),
                issueNote.getDepartmentId(),
                issueNote.getIssuedTo(),
                issueNote.getStatus(),
                issueNote.getStatusDescription(),
                totalAmount,
                issueNote.getDetails().size());
    }

    private String buildAuditDetails(IssueNote issueNote, String action) {
        return buildAuditDetails(issueNote, action, null);
    }

    private String buildAuditDetails(IssueNote issueNote, String action, Map<String, Object> extraDetails) {
        Map<String, Object> details = new HashMap<>();
        details.put("issueNoteNumber", issueNote.getIssueNoteNumber());
        details.put("status", issueNote.getStatus());
        details.put("statusDescription", issueNote.getStatusDescription());
        details.put("companyId", issueNote.getCompanyId());
        details.put("departmentId", issueNote.getDepartmentId());
        details.put("plantId", issueNote.getPlantId());
        details.put("issuedTo", issueNote.getIssuedTo());
        details.put("action", action);
        details.put("lineItemCount", issueNote.getDetails().size());

        if (extraDetails != null) {
            details.putAll(extraDetails);
        }

        try {
            return new ObjectMapper().writeValueAsString(details);
        } catch (Exception e) {
            log.error("Failed to convert audit details to JSON", e);
            return details.toString();
        }
    }

    // =====================================================
    // B.2 FIX: ISSUE NOTE RETURN WORKFLOW
    // Allows returning issued materials back to stores
    // =====================================================

    /**
     * B.2 FIX: Return issued materials back to stores
     * Reverses inventory transaction and updates status to RETURNED
     */
    @Transactional
    public IssueNoteResponse returnIssuedMaterials(Integer id, ReturnIssueNoteRequest request, Integer userId) {
        log.info("Processing return for issue note {}", id);

        IssueNote issueNote = findById(id);

        if (!issueNote.canBeReturned()) {
            throw new IllegalStateException("Issue Note cannot be returned in current state: " +
                    issueNote.getStatusDescription() + ". Only ISSUED notes can be returned.");
        }

        // Reverse inventory transaction - add stock back
        for (IssueNoteDetails detail : issueNote.getDetails()) {
            inventoryService.addStock(
                    detail.getMaterialId(),
                    issueNote.getPlantId(),
                    issueNote.getCompanyId(),
                    detail.getUnitOfMeasureId(),
                    detail.getQuantity(),
                    detail.getRate(), // Use original rate
                    "ISSUE_RETURN",
                    issueNote.getIssueNoteNumber() + "-RTN",
                    issueNote.getId(),
                    "Material returned to stores. Reason: " + request.returnReason());
        }

        // Update issue note status
        issueNote.setStatus(10); // Returned
        String existingComments = issueNote.getComments() != null ? issueNote.getComments() : "";
        issueNote.setComments(existingComments +
                "\n[RETURNED] " + LocalDateTime.now() +
                "\nReturn Reason: " + request.returnReason() +
                (request.remarks() != null ? "\nRemarks: " + request.remarks() : ""));
        issueNote.setLastModifiedDate(LocalDateTime.now());
        issueNote.setLastModifiedBy(userId);

        issueNote = issueNoteRepository.save(issueNote);

        // Audit log
        Map<String, Object> extraDetails = new HashMap<>();
        extraDetails.put("returnedBy", userId);
        extraDetails.put("returnReason", request.returnReason());
        extraDetails.put("returnRemarks", request.remarks());
        auditService.logEntityChange(
                "ISSUE_NOTE_RETURNED",
                ENTITY_TYPE,
                issueNote.getId(),
                userId,
                "User " + userId,
                buildAuditDetails(issueNote, "Materials returned to stores", extraDetails));

        log.info("Issue note {} materials returned successfully", issueNote.getIssueNoteNumber());
        return mapToResponse(issueNote);
    }
}
