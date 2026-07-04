package com.nslindia.procurezone.issuenote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.inventory.InventoryService;
import com.nslindia.procurezone.issuenote.dto.*;
import com.nslindia.procurezone.masterdata.PlantRepository;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;
import com.nslindia.procurezone.masterdata.repository.SectionRepository;
import com.nslindia.procurezone.masterdata.repository.UnitOfMeasureRepository;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.repository.EmployeeReportingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;

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
    private final EmailService emailService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeReportingRepository employeeReportingRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private final PlantRepository plantRepository;
    private final MaterialRepository materialRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    private static final String ENTITY_TYPE = "Issue Note";
    private static final String ERROR_NOT_FOUND = "Issue Note not found with ID: ";

    /**
     * Derives user-visible display status from the two-column issue note workflow:
     * approvedStatus = RM approval (1=pending, 2=rejected, 3=approved)
     * storesByStatus = Stores action  (1=pending, 2=rejected, 11=issued)
     * Covers every combination in production data: (3,11) (3,2) (2,1) (3,1) (1,1).
     * "In Progress" is a true fallback and should be extremely rare.
     * Package-private for DeriveIssueNoteDisplayStatusTest.
     */
    static String deriveIssueNoteDisplayStatus(Integer approvedStatusId, Integer storesByStatusId) {
        if (approvedStatusId == null || approvedStatusId == 1) return "Pending RM Approval";
        if (approvedStatusId == 2) return "RM Rejected";
        if (approvedStatusId == 3) {
            if (storesByStatusId == null || storesByStatusId == 1) return "RM Approved"; // awaiting stores
            if (storesByStatusId == 2)  return "Stores Rejected";
            if (storesByStatusId == 11) return "Goods Issued";
        }
        return "In Progress";
    }

    /**
     * Create a new issue note
     */
    @Transactional
    public IssueNoteResponse createIssueNote(CreateIssueNoteRequest request, Integer userId) {
        log.info("Creating new issue note for company: {}, department: {}",
                request.companyId(), request.departmentId());

        // Generate issue note number
        String issueNoteNumber = generateIssueNoteNumber();

        // Supervisor bypass: DEPTHEAD creators skip RM and go directly to Stores
        boolean isDeptHead = isCreatorDeptHead();

        // Build issue note entity
        IssueNote issueNote = IssueNote.builder()
                .issueNoteNumber(issueNoteNumber)
                // issue_note_year is NOT NULL in the DB and must never depend on the
                // frontend payload — computed server-side (India FY, e.g. "2026-27"),
                // same helper the indent flow uses.
                .issueNoteYear(com.nslindia.procurezone.indent.IndentService.getCurrentFinancialYear())
                .issueDate(LocalDateTime.now())
                .companyId(request.companyId())
                .departmentId(request.departmentId())
                .sectionId(request.sectionId())
                .plantId(request.plantId())
                .issuedTo(request.issuedTo())
                .purpose(request.purpose())
                .comments(request.comments())
                .createdBy(userId)
                .status(isDeptHead ? 3 : 1) // DEPTHEAD skips RM, goes directly to stores
                // Two-column workflow model (legacy convention, same as production data):
                // approvedStatus 1=pending RM, 3=RM approved; storesByStatus 1=pending stores.
                // DEPTHEAD bypass lands directly in the stores queue as (3,1).
                .approvedStatus(isDeptHead ? 3 : 1)
                .storesByStatus(1)
                .supervisorBypass(isDeptHead)
                .lastModifiedDate(LocalDateTime.now())
                .lastModifiedBy(userId)
                .build();

        // Save issue note first to get ID
        issueNote = issueNoteRepository.save(issueNote);

        // Create line items
        for (var lineItem : request.lineItems()) {
            if (lineItem.quantityStores() != null
                    && lineItem.quantity().compareTo(lineItem.quantityStores()) > 0) {
                throw new IllegalArgumentException(
                        "Requested quantity for material ID " + lineItem.materialId()
                        + " exceeds available stock ("
                        + lineItem.quantity() + " > " + lineItem.quantityStores() + ")");
            }
            IssueNoteDetails detail = IssueNoteDetails.builder()
                    .issueNote(issueNote)
                    .materialId(lineItem.materialId())
                    .unitOfMeasureId(lineItem.unitOfMeasureId())
                    .quantity(lineItem.quantity())
                    .quantityStores(lineItem.quantityStores())
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

        // Email notification
        final IssueNote savedNote = issueNote;
        java.util.Map<String, Object> createdVars = new HashMap<>();
        createdVars.put("createdBy", "User #" + userId);
        createdVars.put("issuedTo", savedNote.getIssuedTo() != null ? savedNote.getIssuedTo() : "N/A");
        createdVars.put("department", "Dept #" + savedNote.getDepartmentId());
        sendIssueNoteNotification("ISSUE_NOTE_CREATED", savedNote, createdVars);

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
        issueNote.setRmApprovedStatus(1); // Approved (legacy rm_status audit column)
        issueNote.setApprovedStatus(3);   // two-column model: 3 = RM approved (drives displayStatus)
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
        java.util.Map<String, Object> rmApprovedVars = new HashMap<>();
        rmApprovedVars.put("approverName", "User #" + userId);
        rmApprovedVars.put("approvalDate", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        rmApprovedVars.put("department", "Dept #" + issueNote.getDepartmentId());
        sendIssueNoteNotification("ISSUE_NOTE_RM_APPROVED", issueNote, rmApprovedVars);
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
        issueNote.setRmApprovedStatus(6); // Rejected (legacy rm_status audit column)
        issueNote.setApprovedStatus(2);   // two-column model: 2 = RM rejected (drives displayStatus)
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
        java.util.Map<String, Object> rmRejectedVars = new HashMap<>();
        rmRejectedVars.put("rejectorName", "User #" + userId);
        rmRejectedVars.put("rejectionDate", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        rmRejectedVars.put("reason", request.reason() != null ? request.reason() : "No reason provided");
        rmRejectedVars.put("department", "Dept #" + issueNote.getDepartmentId());
        sendIssueNoteNotification("ISSUE_NOTE_RM_REJECTED", issueNote, rmRejectedVars);
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
        issueNote.setStoresByStatus(11); // 11 = Issued, matches legacy storesby_status value
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
        java.util.Map<String, Object> issuedVars = new HashMap<>();
        issuedVars.put("issuedByName", "User #" + userId);
        issuedVars.put("department", "Dept #" + issueNote.getDepartmentId());
        issuedVars.put("issueDate", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        sendIssueNoteNotification("ISSUE_NOTE_ISSUED", issueNote, issuedVars);
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
        issueNote.setStoresByStatus(2); // 2 = Rejected, matches legacy storesby_status value
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
        java.util.Map<String, Object> storesRejectedVars = new HashMap<>();
        storesRejectedVars.put("rejectorName", "User #" + userId);
        storesRejectedVars.put("rejectionDate", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        storesRejectedVars.put("reason", request.reason() != null ? request.reason() : "No reason provided");
        storesRejectedVars.put("department", "Dept #" + issueNote.getDepartmentId());
        sendIssueNoteNotification("ISSUE_NOTE_STORES_REJECTED", issueNote, storesRejectedVars);
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

        // Soft-delete: status=0 means cancelled/inactive, matches legacy issue_note_status=0
        issueNote.setStatus(0);
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
     * Get all issue notes with pagination and combined filters.
     * approvedStatus + storesByStatus drive the two-column workflow filter.
     * Visibility is role-scoped:
     *   USER             → own issue notes only
     *   SUPERVISOR       → own + direct subordinates'
     *   DEPTHEAD / PLANTMANAGER → their department
     *   PROCUREMENT / ISSUECONFIRM / ADMIN / SUPERADMIN → global
     */
    @Transactional(readOnly = true)
    public Page<IssueNoteSummaryResponse> getAll(int page, int size,
            String search, Integer approvedStatus, Integer storesByStatus, Integer departmentId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate"));
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.nslindia.procurezone.security.UserPrincipal cu) {
            java.util.Set<String> roles = cu.roles();
            boolean isGlobal = roles.stream().anyMatch(r ->
                    "SUPERADMIN".equals(r) || "ADMIN".equals(r) ||
                    "PROCUREMENT".equals(r) || "ISSUECONFIRM".equals(r));
            boolean isDeptScoped = !isGlobal && roles.stream().anyMatch(r ->
                    "DEPTHEAD".equals(r) || "PLANTMANAGER".equals(r));
            boolean isSupervisor = !isGlobal && !isDeptScoped && roles.contains("SUPERVISOR");
            boolean isUserOnly   = !isGlobal && !isDeptScoped && !isSupervisor;

            if (isDeptScoped && cu.deptId() != null) {
                return issueNoteRepository
                        .filterIssueNotes(normalizedSearch, approvedStatus, storesByStatus, cu.deptId(), pageable)
                        .map(this::mapToSummaryResponse);
            } else if (isSupervisor && cu.employeeNumber() != null) {
                java.util.List<Integer> empNumbers = new java.util.ArrayList<>();
                empNumbers.add(cu.employeeNumber());
                empNumbers.addAll(employeeReportingRepository.findSubordinateNumbers(cu.employeeNumber()));
                return issueNoteRepository
                        .filterIssueNotesForCreators(empNumbers, normalizedSearch, approvedStatus, storesByStatus, pageable)
                        .map(this::mapToSummaryResponse);
            } else if (isUserOnly && cu.employeeNumber() != null) {
                java.util.List<Integer> empNumbers = java.util.List.of(cu.employeeNumber());
                return issueNoteRepository
                        .filterIssueNotesForCreators(empNumbers, normalizedSearch, approvedStatus, storesByStatus, pageable)
                        .map(this::mapToSummaryResponse);
            }
            // isGlobal or edge case: fall through to unscoped query.
        }

        return issueNoteRepository
                .filterIssueNotes(normalizedSearch, approvedStatus, storesByStatus, departmentId, pageable)
                .map(this::mapToSummaryResponse);
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
     * Export all issue notes (up to 10,000 rows) for file download, with optional filters.
     */
    @Transactional(readOnly = true)
    public List<IssueNoteSummaryResponse> exportAll(Integer status, Integer departmentId) {
        Pageable exportPageable = PageRequest.of(0, 10_000, Sort.by(Sort.Direction.DESC, "issueDate"));
        Page<IssueNote> page;
        if (status != null && departmentId != null) {
            page = issueNoteRepository.findByDepartmentIdAndStatus(departmentId, status, exportPageable);
        } else if (status != null) {
            page = issueNoteRepository.findByStatus(status, exportPageable);
        } else if (departmentId != null) {
            page = issueNoteRepository.findByDepartmentId(departmentId, exportPageable);
        } else {
            page = issueNoteRepository.findAll(exportPageable);
        }
        return page.map(this::mapToSummaryResponse).getContent();
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

    private boolean isCreatorDeptHead() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DEPTHEAD".equals(a.getAuthority()));
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

    /** Read-only preview of what the next issue note number will be (does not consume the number). */
    @Transactional(readOnly = true)
    public String previewNextIssueNoteNumber() {
        return generateIssueNoteNumber();
    }

    /** Null-safe repository name lookup helpers (IssueNote stores raw FK integers,
     *  unlike Indent which has entity relationships — so names resolve via repositories). */
    private String resolveEmployeeName(Integer empNumber) {
        if (empNumber == null) return null;
        return employeeRepository.findById(empNumber).map(Employee::getEmpName).orElse(null);
    }

    private IssueNoteResponse mapToResponse(IssueNote issueNote) {
        List<IssueNoteResponse.IssueNoteDetailResponse> detailResponses = issueNote.getDetails().stream()
                .map(d -> {
                    var material = d.getMaterialId() != null
                            ? materialRepository.findById(d.getMaterialId()).orElse(null) : null;
                    var uom = d.getUnitOfMeasureId() != null
                            ? unitOfMeasureRepository.findById(d.getUnitOfMeasureId()).orElse(null) : null;
                    return new IssueNoteResponse.IssueNoteDetailResponse(
                            d.getId(),
                            d.getMaterialId(),
                            material != null ? material.getCode() : null,
                            material != null ? material.getName() : null,
                            d.getUnitOfMeasureId(),
                            uom != null ? uom.getCode() : null,
                            d.getQuantity(),
                            d.getRate(),
                            d.getAmount(),
                            d.getPurpose(),
                            d.getStatus());
                })
                .collect(Collectors.toList());

        BigDecimal totalAmount = issueNote.getDetails().stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String companyName = issueNote.getCompanyId() != null
                ? companyRepository.findById(issueNote.getCompanyId()).map(c -> c.getName()).orElse(null) : null;
        String departmentName = issueNote.getDepartmentId() != null
                ? departmentRepository.findById(issueNote.getDepartmentId()).map(dpt -> dpt.getName()).orElse(null) : null;
        String sectionName = issueNote.getSectionId() != null
                ? sectionRepository.findById(issueNote.getSectionId()).map(s -> s.getName()).orElse(null) : null;
        String plantName = issueNote.getPlantId() != null
                ? plantRepository.findById(issueNote.getPlantId()).map(p -> p.getName()).orElse(null) : null;

        return new IssueNoteResponse(
                issueNote.getId(),
                issueNote.getIssueNoteNumber(),
                issueNote.getIssueDate(),
                issueNote.getCompanyId(),
                companyName,
                issueNote.getDepartmentId(),
                departmentName,
                issueNote.getSectionId(),
                sectionName,
                issueNote.getPlantId(),
                plantName,
                issueNote.getIssuedTo(),
                issueNote.getPurpose(),
                issueNote.getComments(),
                issueNote.getCreatedBy(),
                issueNote.getCreatedBy(),
                resolveEmployeeName(issueNote.getCreatedBy()),
                issueNote.getApprovedBy(),
                issueNote.getApprovedByDate(),
                issueNote.getStoresBy(),
                issueNote.getStoresByDate(),
                resolveEmployeeName(issueNote.getStoresBy()),
                issueNote.getStatus(),
                deriveIssueNoteDisplayStatus(issueNote.getApprovedStatus(), issueNote.getStoresByStatus()),
                deriveIssueNoteDisplayStatus(issueNote.getApprovedStatus(), issueNote.getStoresByStatus()),
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
                deriveIssueNoteDisplayStatus(issueNote.getApprovedStatus(), issueNote.getStoresByStatus()),
                issueNote.getApprovedStatus(),
                issueNote.getStoresByStatus(),
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

    private void sendIssueNoteNotification(String templateCode, IssueNote issueNote,
            java.util.Map<String, Object> extraVars) {
        try {
            Employee creator = employeeRepository.findById(issueNote.getCreatedBy()).orElse(null);
            if (creator == null || creator.getEmail() == null) {
                log.warn("Cannot send {} - no creator email for issue note {}", templateCode,
                        issueNote.getIssueNoteNumber());
                return;
            }
            java.util.Map<String, Object> vars = new HashMap<>(extraVars);
            vars.put("issueNoteNumber", issueNote.getIssueNoteNumber());
            vars.put("issueDate", issueNote.getIssueDate() != null
                    ? issueNote.getIssueDate().toLocalDate().toString() : "N/A");
            emailService.sendEmailFromTemplate(templateCode, vars, creator.getEmail());
        } catch (Exception e) {
            log.error("Error sending {} for issue note {}: {}", templateCode,
                    issueNote.getIssueNoteNumber(), e.getMessage());
        }
    }
}
