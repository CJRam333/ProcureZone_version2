package com.nslindia.procurezone.indent;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.repository.EmployeeReportingRepository;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.EmployeeRole;
import com.nslindia.procurezone.identity.EmployeeRoleRepository;
import com.nslindia.procurezone.identity.ReportingHierarchyService;
import com.nslindia.procurezone.indent.dto.ApprovalWorkflowResponse;
import com.nslindia.procurezone.indent.dto.CreateIndentRequest;
import com.nslindia.procurezone.indent.dto.IndentDetailRequest;
import com.nslindia.procurezone.indent.dto.IndentDetailResponse;
import com.nslindia.procurezone.indent.dto.IndentListResponse;
import com.nslindia.procurezone.indent.dto.IndentResponse;
import com.nslindia.procurezone.indent.dto.L1ApprovalRequest;
import com.nslindia.procurezone.indent.dto.L2ApprovalRequest;
import com.nslindia.procurezone.indent.dto.PendingApprovalResponse;
import com.nslindia.procurezone.indent.dto.UpdateIndentRequest;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Department;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.Section;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;
import com.nslindia.procurezone.masterdata.repository.SectionRepository;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.security.PlantSecurityService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Service for indent management operations.
 * Handles CRUD operations, workflow, and business logic.
 */
@Service
@Transactional
public class IndentService {

        private static final Logger logger = LoggerFactory.getLogger(IndentService.class);

        /** Sanity ceiling for any line-item quantity (business rule: no per-stage monotonic limit). */
        private static final java.math.BigDecimal MAX_QUANTITY = java.math.BigDecimal.valueOf(99999);

        private static final Map<Integer, String> SPRING_STATUS_LABELS = Map.of(
                1, "Draft",
                2, "Submitted",
                3, "Dept Head Approved",
                4, "Rejected",
                5, "Proc. In Progress",
                6, "PO Created",
                7, "On Hold",
                8, "Completed"
        );

        private String resolveStatusLabel(IndentStatus status) {
                if (status == null || status.getId() == null) return null;
                return SPRING_STATUS_LABELS.getOrDefault(status.getId(), status.getName());
        }

        /**
         * Derives the user-visible operational status label from the three workflow FK columns.
         * Hierarchical: RM stage decides first (approvedStatus), then Dept Head (finalStatus),
         * then procurement sub-stage (procurementStatus). Covers every combination observed in
         * production data (including legacy edge cases 3-5-6, 2-2-2, 4-1-1 and the pre-repair
         * corruption triples 3-3-x / 3-5-x). "In Progress" is a true fallback and should be
         * extremely rare.
         */
        static String deriveDisplayStatus(
                        Integer approvedId, Integer finalId, Integer procurementId) {

                if (approvedId == null) return "Pending";

                // RM stage
                if (approvedId == 1) return "Pending RM Approval";
                if (approvedId == 2) return "RM Rejected";

                // approvedId == 3 (RM approved) — now look at final status
                if (approvedId == 3) {
                        if (finalId == null || finalId == 1) return "RM Approved"; // awaiting Dept Head
                        if (finalId == 2) return "Dept. Head Rejected";

                        // finalId == 4 (Dept Head approved) — now look at procurement status
                        if (finalId == 4) {
                                if (procurementId == null || procurementId <= 4) return "Dept. Head Approved";
                                if (procurementId == 5) return "Quotations Collected";
                                if (procurementId == 6) return "Negotiation Done";
                                if (procurementId == 7) return "PO Released";
                                if (procurementId == 8) return "Hold";
                                if (procurementId == 9) return "Cash Buy";
                                if (procurementId == 10) return "Goods Receipt";
                                if (procurementId == 11) return "Goods Issued";
                                return "In Procurement";
                        }

                        // finalId == 3 — legacy l2Approve corruption (3,3,x) before the 2026-07-02
                        // code fix; rows remain until the owner runs docs/fix-l2-approved-indents.sql
                        if (finalId == 3) return "Dept. Head Approved";

                        // finalId == 5 — legacy edge case (appears in production as 3,5,6 and as
                        // pre-repair 3,5,1 from the old finalApproveIndent bug)
                        if (finalId == 5) {
                                if (procurementId != null && procurementId == 6) return "Negotiation Done";
                                return "In Procurement";
                        }
                }

                // approvedId == 4 — legacy edge (4,1,1) — treat as approved / in workflow
                if (approvedId == 4) return "RM Approved";

                return "In Progress"; // true fallback — should now be extremely rare
        }

        private final IndentRepository indentRepository;
        private final IndentDetailRepository indentDetailRepository;
        private final EmployeeRepository employeeRepository;
        private final AuditService auditService;
        private final ApprovalWorkflowRepository approvalWorkflowRepository;
        private final PlantSecurityService plantSecurityService;
        private final EmailService emailService;
        private final ReportingHierarchyService reportingHierarchyService;
        private final EmployeeRoleRepository employeeRoleRepository;
        private final SectionRepository sectionRepository;
        private final EmployeeReportingRepository employeeReportingRepository;
        private final com.nslindia.procurezone.repository.CompanyEmployeeRepository companyEmployeeRepository;
        // Company/stock resolution reads the SAME table the dropdown & Inventory use
        // (tbl_map_company_plant_material via CompanyPlantMaterialMap), NOT tbl_pz_map_company_plant_material.
        private final com.nslindia.procurezone.mapping.CompanyPlantMaterialMapRepository companyPlantMaterialMapRepository;
        private final IndentDetailQtyAuditRepository qtyAuditRepository;

        @PersistenceContext
        private EntityManager entityManager;

        public IndentService(IndentRepository indentRepository,
                        IndentDetailRepository indentDetailRepository,
                        EmployeeRepository employeeRepository,
                        AuditService auditService,
                        ApprovalWorkflowRepository approvalWorkflowRepository,
                        PlantSecurityService plantSecurityService,
                        EmailService emailService,
                        ReportingHierarchyService reportingHierarchyService,
                        EmployeeRoleRepository employeeRoleRepository,
                        SectionRepository sectionRepository,
                        EmployeeReportingRepository employeeReportingRepository,
                        com.nslindia.procurezone.repository.CompanyEmployeeRepository companyEmployeeRepository,
                        com.nslindia.procurezone.mapping.CompanyPlantMaterialMapRepository companyPlantMaterialMapRepository,
                        IndentDetailQtyAuditRepository qtyAuditRepository) {
                this.indentRepository = indentRepository;
                this.indentDetailRepository = indentDetailRepository;
                this.employeeRepository = employeeRepository;
                this.auditService = auditService;
                this.approvalWorkflowRepository = approvalWorkflowRepository;
                this.plantSecurityService = plantSecurityService;
                this.emailService = emailService;
                this.reportingHierarchyService = reportingHierarchyService;
                this.employeeRoleRepository = employeeRoleRepository;
                this.sectionRepository = sectionRepository;
                this.employeeReportingRepository = employeeReportingRepository;
                this.companyEmployeeRepository = companyEmployeeRepository;
                this.companyPlantMaterialMapRepository = companyPlantMaterialMapRepository;
                this.qtyAuditRepository = qtyAuditRepository;
        }

        /**
         * Create a new indent with details
         */
        public IndentResponse createIndent(CreateIndentRequest request, String username) {
                logger.info("Creating indent for user: {}", username);

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Capture optional geo metadata server-side from the creating employee's record. These
                // are informational only and must NEVER block creation — every lookup is null-safe and
                // any missing value simply stays null (the columns are all nullable).
                //   company    ← tbl_map_company_emp (primary/first mapped company), else null
                //   department  ← emp_department, else null
                //   plant       ← NOT set from the employee: emp_location is a LOCATION id and plant is
                //                 a distinct master (tbl_plant_master vs tbl_location_master), so we must
                //                 not write a location id into the plant column. No employee→plant-id
                //                 exists, so plant stays null unless a client explicitly supplies one.
                //   section     ← no employee source; null unless supplied.
                // Each still honours an explicit request value as a fallback so older clients keep working.
                Integer resolvedCompanyId = request.companyId();
                try {
                        List<Integer> creatorCompanyIds =
                                companyEmployeeRepository.findCompanyIdsByEmpNumber(currentUser.getEmpNumber());
                        if (creatorCompanyIds != null && !creatorCompanyIds.isEmpty()) {
                                resolvedCompanyId = creatorCompanyIds.get(0);
                        }
                } catch (Exception e) {
                        logger.warn("Company lookup failed for employee {}; leaving company null",
                                        currentUser.getEmpNumber(), e);
                }
                Integer resolvedDepartmentId = currentUser.getDepartmentId() != null
                        ? currentUser.getDepartmentId() : request.departmentId();
                Integer resolvedPlantId = request.plantId(); // never from emp_location (distinct master)

                // Create indent entity
                Indent indent = new Indent();
                indent.setIndentYear(String.valueOf(Year.now().getValue()));
                indent.setIndentDate(LocalDateTime.now());
                if (resolvedCompanyId != null) {
                        indent.setCompany(entityManager.getReference(Company.class, resolvedCompanyId));
                }
                if (resolvedDepartmentId != null) {
                        indent.setDepartment(entityManager.getReference(Department.class, resolvedDepartmentId));
                }

                // Resolve section: use provided value if valid, otherwise fall back to first active section
                Integer sectionId = (request.sectionId() != null && request.sectionId() > 0)
                        ? request.sectionId() : null;
                if (sectionId == null) {
                        var activeSections = sectionRepository.findByStatus(1, PageRequest.of(0, 1));
                        sectionId = activeSections.isEmpty() ? null : activeSections.getContent().get(0).getId();
                }
                if (sectionId != null) {
                        indent.setSection(entityManager.getReference(Section.class, sectionId));
                }

                if (resolvedPlantId != null) {
                        indent.setPlant(entityManager.getReference(Plant.class, resolvedPlantId));
                }

                // The creator is the requesting employee (form no longer sends a separate employeeId).
                Integer indentEmployeeId = request.employeeId() != null
                        ? request.employeeId() : currentUser.getEmpNumber();
                indent.setEmployee(entityManager.getReference(Employee.class, indentEmployeeId));
                indent.setComments(request.comments());
                indent.setDeliveryDate(request.deliveryDate());
                indent.setCreatedBy(currentUser);
                indent.setStatus(entityManager.getReference(IndentStatus.class, 1)); // soft-delete active flag

                // Initialize three-column workflow status to Pending (ID=1)
                // DEPTHEAD bypass is intentionally NOT applied at creation time.
                // Auto-approval only fires in submitIndent() when the submitter is a DEPTHEAD
                // with no supervisor of their own in the reporting hierarchy.
                indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 1));
                indent.setFinalStatus(entityManager.getReference(IndentStatus.class, 1));
                indent.setProcurementStatus(entityManager.getReference(IndentStatus.class, 1));

                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                // Generate indent number: legacy-compatible sequential numeric
                // Mirrors legacy getIndentNo1(): ORDER BY indent_id DESC LIMIT 1, parseLong + 1
                String indentNumber = generateLegacyNumericIndentNumber();
                String indentYear = String.valueOf(Year.now().getValue());

                indent.setIndentNumber(indentNumber);
                indent.setIndentYear(indentYear);

                // Save indent first to get ID
                indent = indentRepository.save(indent); // Create indent details
                final Indent savedIndent = indent;
                for (IndentDetailRequest detailReq : request.details()) {
                        IndentDetail detail = new IndentDetail();
                        detail.setMaterial(entityManager.getReference(Material.class, detailReq.materialId()));
                        detail.setCompanyId(detailReq.companyId());
                        detail.setUnitOfMeasure(
                                        entityManager.getReference(UnitOfMeasure.class, detailReq.unitOfMeasureId()));
                        detail.setQuantity(detailReq.quantity());
                        detail.setRmQuantity(detailReq.rmQuantity());
                        detail.setDeptQuantity(detailReq.deptQuantity());
                        detail.setStockAvailable(detailReq.stockAvailable() != null ? detailReq.stockAvailable() : java.math.BigDecimal.ZERO);
                        detail.setPricing(detailReq.pricing() != null ? detailReq.pricing() : java.math.BigDecimal.ZERO);
                        detail.setPurpose(detailReq.purpose());
                        detail.setVendor(detailReq.vendor());
                        detail.setStatus(detailReq.status() != null ? detailReq.status() : 1);
                        detail.setLastModifiedDate(LocalDateTime.now());
                        detail.setLastModifiedBy(currentUser.getEmpNumber());

                        savedIndent.addDetail(detail);
                }

                // Save updated indent with details
                indent = indentRepository.save(savedIndent);

                // Audit log
                auditService.logEntityChange(
                                "CREATE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Created indent %s with %d line items", indentNumber,
                                                request.details().size()));

                logger.info("Created indent {} with {} details", indentNumber, request.details().size());

                return toIndentResponse(indent);
        }

        /**
         * Get indent by ID
         */
        // TODO(security): add caller-supplied UserPrincipal and check that the requesting
        // user's department matches indent.departmentId, or that the role is ADMIN/SUPERADMIN.
        // Currently any authenticated user can fetch any indent by ID (horizontal privilege gap).
        @Transactional(readOnly = true)
        public IndentResponse getIndentById(Integer id) {
                logger.info("Fetching indent with ID: {}", id);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                return toIndentResponse(indent);
        }

        /**
         * List indents with pagination.
         * approvedStatusId, finalStatusId, procurementStatusId filter by the three-column
         * workflow state; pass null to skip that filter dimension.
         */
        @Transactional(readOnly = true)
        public Page<IndentListResponse> filterIndents(
                        String search, Integer statusId, Integer departmentId, Integer plantId,
                        Integer companyId, LocalDateTime fromDate, LocalDateTime toDate,
                        Integer approvedStatusId, Integer finalStatusId, Integer procurementStatusId,
                        Pageable pageable) {

                // Role-based visibility — FAIL CLOSED. A non-admin principal that cannot be resolved
                // to an employee number (or a request with no/!UserPrincipal principal) NEVER receives
                // the unscoped list; it gets an EMPTY page, logged at ERROR so any recurrence is
                // immediately visible in app.log (this is how we catch the real trigger if it recurs).
                //   ADMIN / SUPERADMIN → global (unscoped, intended)
                //   PROCUREMENT        → only indents that reached procurement stage (approvedStatus=3, finalStatus=4)
                //   DEPTHEAD / PLANTMANAGER → own + 2 levels of reports
                //   SUPERVISOR         → own + direct subordinates
                //   USER               → own indents only
                var auth = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
                if (auth == null || !(auth.getPrincipal() instanceof com.nslindia.procurezone.security.UserPrincipal cu)) {
                        // No resolvable principal — fail closed, never leak the full list.
                        logger.error("filterIndents: no UserPrincipal in the security context for a list request "
                                        + "— returning EMPTY rather than unscoped data. Authentication: {}", auth);
                        return Page.empty(pageable);
                }

                java.util.Set<String> roles = cu.roles();
                boolean isGlobal = roles.stream().anyMatch(r ->
                        "SUPERADMIN".equals(r) || "ADMIN".equals(r));

                if (isGlobal) {
                        // ADMIN / SUPERADMIN — unscoped, intended.
                        return indentRepository
                                        .filterIndents(search, statusId, departmentId, plantId, companyId, fromDate, toDate,
                                                       approvedStatusId, finalStatusId, procurementStatusId, pageable)
                                        .map(this::toIndentListResponse);
                }

                boolean isProcurement = roles.contains("PROCUREMENT");
                if (isProcurement) {
                        // PROCUREMENT sees only indents that reached the procurement stage:
                        // approvedStatus=3 (RM approved) + finalStatus=4 (DeptHead approved, forwarded to procurement).
                        // procurementStatus is passed through so the user can still filter by sub-stage.
                        return indentRepository
                                        .filterIndents(search, statusId, departmentId, plantId, companyId, fromDate, toDate,
                                                3, 4, procurementStatusId, pageable)
                                        .map(this::toIndentListResponse);
                }

                // Every remaining role (DEPTHEAD/PLANTMANAGER, SUPERVISOR, USER) is creator-scoped and
                // REQUIRES a resolvable employee number. Missing it → fail closed (empty), never unscoped.
                if (cu.employeeNumber() == null) {
                        logger.error("filterIndents: could not resolve an employee number for a non-admin principal "
                                        + "(roles={}) — returning EMPTY rather than unscoped data.", roles);
                        return Page.empty(pageable);
                }

                boolean isDeptScoped = roles.stream().anyMatch(r ->
                        "DEPTHEAD".equals(r) || "PLANTMANAGER".equals(r));
                if (isDeptScoped) {
                        // 2-level hierarchy: own + direct reports + their direct reports
                        java.util.List<Integer> deptEmpNumbers = new java.util.ArrayList<>();
                        deptEmpNumbers.add(cu.employeeNumber());
                        java.util.List<Integer> deptL1Reports = employeeReportingRepository.findSubordinateNumbers(cu.employeeNumber());
                        deptEmpNumbers.addAll(deptL1Reports);
                        for (Integer l1Emp : deptL1Reports) {
                                deptEmpNumbers.addAll(employeeReportingRepository.findSubordinateNumbers(l1Emp));
                        }
                        return indentRepository
                                        .filterIndentsForCreators(deptEmpNumbers, search, statusId, plantId,
                                                companyId, fromDate, toDate, approvedStatusId, finalStatusId,
                                                procurementStatusId, pageable)
                                        .map(this::toIndentListResponse);
                }

                boolean isSupervisor = roles.contains("SUPERVISOR");
                if (isSupervisor) {
                        java.util.List<Integer> empNumbers = new java.util.ArrayList<>();
                        empNumbers.add(cu.employeeNumber());
                        empNumbers.addAll(employeeReportingRepository.findSubordinateNumbers(cu.employeeNumber()));
                        return indentRepository
                                        .filterIndentsForCreators(empNumbers, search, statusId, plantId,
                                                companyId, fromDate, toDate, approvedStatusId, finalStatusId,
                                                procurementStatusId, pageable)
                                        .map(this::toIndentListResponse);
                }

                // Default non-admin (USER) — own indents only.
                java.util.List<Integer> ownNumbers = java.util.List.of(cu.employeeNumber());
                return indentRepository
                                .filterIndentsForCreators(ownNumbers, search, statusId, plantId,
                                        companyId, fromDate, toDate, approvedStatusId, finalStatusId,
                                        procurementStatusId, pageable)
                                .map(this::toIndentListResponse);
        }

        /**
         * Export indents matching filters — returns up to 50,000 rows for file export.
         * Routes through the role-scoped filterIndents() service method (NOT the raw repository),
         * so the exported rows are EXACTLY what the caller would see in the filtered list — same
         * visibility scope, same filters. This closes a prior leak where export hit the unscoped
         * repository query directly and returned all indents regardless of role.
         */
        @Transactional(readOnly = true)
        public java.util.List<IndentListResponse> exportIndents(
                        String search, Integer statusId, Integer departmentId, Integer plantId,
                        Integer companyId, LocalDateTime fromDate, LocalDateTime toDate,
                        Integer approvedStatusId, Integer finalStatusId, Integer procurementStatusId) {
                Pageable exportPageable = PageRequest.of(0, 50_000,
                        Sort.by("indentDate").descending());
                return filterIndents(search, statusId, departmentId, plantId, companyId, fromDate, toDate,
                                approvedStatusId, finalStatusId, procurementStatusId, exportPageable)
                                .getContent();
        }

        public Page<IndentListResponse> listIndents(Pageable pageable) {
                logger.info("Listing indents with pagination: {}", pageable);

                // A.1 FIX: Apply plant filtering based on user's allowed plants
                List<Integer> allowedPlantIds = plantSecurityService.getAllowedPlantIds();

                // A.2 FIX: Apply department filtering for DEPTHEAD role
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();

                boolean isDeptHead = false;
                Integer userDeptId = null;

                if (auth != null && auth
                                .getPrincipal() instanceof com.nslindia.procurezone.security.UserPrincipal principal) {
                        isDeptHead = plantSecurityService.isDeptHead(principal);
                        if (isDeptHead) {
                                userDeptId = plantSecurityService.getUserDepartmentId(principal);
                                logger.debug("A.2 FIX: User is DEPTHEAD, filtering by department: {}", userDeptId);
                        }
                }

                if (allowedPlantIds.isEmpty()) {
                        // User has global access or no plant assigned
                        if (isDeptHead && userDeptId != null) {
                                // A.2 FIX: DEPTHEAD with global plant access - filter by department only
                                logger.debug("DEPTHEAD with global access, filtering by department: {}", userDeptId);
                                return indentRepository.findByDepartmentIdScoped(userDeptId, pageable)
                                                .map(this::toIndentListResponse);
                        }
                        // User has global access - return all indents
                        logger.debug("User has global/no-plant access, returning all indents");
                        return indentRepository.findAll(pageable)
                                        .map(this::toIndentListResponse);
                }

                // User has specific plant access
                if (isDeptHead && userDeptId != null) {
                        // A.2 FIX: DEPTHEAD with specific plant access - filter by both
                        logger.debug("DEPTHEAD filtering by plants: {} and department: {}", allowedPlantIds,
                                        userDeptId);
                        return indentRepository.findByPlantIdInAndDepartmentId(allowedPlantIds, userDeptId, pageable)
                                        .map(this::toIndentListResponse);
                }

                // User has specific plant access - filter by allowed plants
                logger.debug("Filtering indents by plants: {}", allowedPlantIds);
                return indentRepository.findByPlantIdIn(allowedPlantIds, pageable)
                                .map(this::toIndentListResponse);
        }

        /**
         * List indents by status
         */
        @Transactional(readOnly = true)
        public Page<IndentListResponse> listIndentsByStatus(Integer statusId, Pageable pageable) {
                logger.info("Listing indents with status ID: {}", statusId);

                return indentRepository.findByStatusId(statusId, pageable)
                                .map(this::toIndentListResponse);
        }

        /**
         * List indents by employee
         */
        @Transactional(readOnly = true)
        public Page<IndentListResponse> listIndentsByEmployee(Integer empNumber, Pageable pageable) {
                logger.info("Listing indents for employee: {}", empNumber);

                return indentRepository.findByCreatedByEmployeeNumber(empNumber, pageable)
                                .map(this::toIndentListResponse);
        }

        /**
         * Search indents
         */
        @Transactional(readOnly = true)
        public Page<IndentListResponse> searchIndents(String searchTerm, Pageable pageable) {
                logger.info("Searching indents with term: {}", searchTerm);

                return indentRepository.searchIndents(searchTerm, pageable)
                                .map(this::toIndentListResponse);
        }

        /**
         * Update indent (only in Draft status)
         */
        public IndentResponse updateIndent(Integer id, UpdateIndentRequest request, String username) {
                logger.info("Updating indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Check if indent is in draft status
                if (indent.getStatus().getId() != 1) {
                        throw new IllegalStateException(
                                        "Only draft indents can be updated. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update indent fields
                if (request.companyId() != null) {
                        indent.setCompany(entityManager.getReference(Company.class, request.companyId()));
                }
                if (request.departmentId() != null) {
                        indent.setDepartment(entityManager.getReference(Department.class, request.departmentId()));
                }
                if (request.sectionId() != null) {
                        indent.setSection(entityManager.getReference(Section.class, request.sectionId()));
                }
                if (request.plantId() != null) {
                        indent.setPlant(entityManager.getReference(Plant.class, request.plantId()));
                }
                if (request.employeeId() != null) {
                        indent.setEmployee(entityManager.getReference(Employee.class, request.employeeId()));
                }
                if (request.comments() != null) {
                        indent.setComments(request.comments());
                }
                if (request.deliveryDate() != null) {
                        indent.setDeliveryDate(request.deliveryDate());
                }

                // Update details if provided
                if (request.details() != null && !request.details().isEmpty()) {
                        // Remove existing details
                        indent.getDetails().clear();
                        indentDetailRepository.flush();

                        // Add new details
                        for (IndentDetailRequest detailReq : request.details()) {
                                IndentDetail detail = new IndentDetail();
                                detail.setMaterial(entityManager.getReference(Material.class, detailReq.materialId()));
                                detail.setCompanyId(detailReq.companyId());
                                detail.setUnitOfMeasure(entityManager.getReference(UnitOfMeasure.class,
                                                detailReq.unitOfMeasureId()));
                                detail.setQuantity(detailReq.quantity());
                                detail.setRmQuantity(detailReq.rmQuantity());
                                detail.setDeptQuantity(detailReq.deptQuantity());
                                detail.setStockAvailable(detailReq.stockAvailable() != null ? detailReq.stockAvailable() : java.math.BigDecimal.ZERO);
                                detail.setPricing(detailReq.pricing() != null ? detailReq.pricing() : java.math.BigDecimal.ZERO);
                                detail.setPurpose(detailReq.purpose());
                                detail.setVendor(detailReq.vendor());
                                detail.setStatus(detailReq.status() != null ? detailReq.status() : 1);
                                detail.setLastModifiedDate(LocalDateTime.now());
                                detail.setLastModifiedBy(currentUser.getEmpNumber());

                                indent.addDetail(detail);
                        }
                }

                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Audit log
                auditService.logEntityChange(
                                "UPDATE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Updated indent %s", indent.getIndentNumber()));

                logger.info("Updated indent {}", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Delete/Cancel indent (soft delete - change status)
         * B.3 FIX: Added cancellation reason support
         */
        public void deleteIndent(Integer id, String username) {
                // Delegate to cancelIndent with no reason (for backward compatibility)
                cancelIndent(id, null, username);
        }

        /**
         * B.3 FIX: Cancel indent with mandatory reason and email notification
         */
        public void cancelIndent(Integer id, String reason, String username) {
                logger.info("Cancelling indent ID: {} by user: {} with reason: {}", id, username, reason);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only draft indents can be cancelled
                if (indent.getStatus().getId() != 1) {
                        throw new IllegalStateException(
                                        "Only draft indents can be cancelled. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Change status to 4 (Rejected/Cancelled)
                indent.setStatus(entityManager.getReference(IndentStatus.class, 4));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                // B.3 FIX: Store cancellation reason in remarks
                if (reason != null && !reason.isBlank()) {
                        String cancellationRemarks = String.format("Cancelled by %s on %s. Reason: %s",
                                        currentUser.getFullName(),
                                        LocalDateTime.now()
                                                        .format(java.time.format.DateTimeFormatter
                                                                        .ofPattern("yyyy-MM-dd HH:mm")),
                                        reason);
                        indent.setRemarks(cancellationRemarks);
                }

                indentRepository.save(indent);

                // Audit log with cancellation reason
                String auditMessage = reason != null
                                ? String.format("Cancelled indent %s. Reason: %s", indent.getIndentNumber(), reason)
                                : String.format("Cancelled indent %s", indent.getIndentNumber());

                auditService.logEntityChange(
                                "CANCEL",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                auditMessage);

                // B.3 FIX: Send email notification to indent creator
                sendCancellationNotification(indent, currentUser, reason);

                logger.info("Cancelled indent {}", indent.getIndentNumber());
        }

        /**
         * B.3 FIX: Send email notification for indent cancellation
         */
        private void sendCancellationNotification(Indent indent, Employee cancelledBy, String reason) {
                try {
                        // Get indent creator
                        Employee indentCreator = indent.getEmployee();
                        if (indentCreator == null || indentCreator.getEmail() == null) {
                                logger.warn("Cannot send cancellation notification - no creator email for indent {}",
                                                indent.getIndentNumber());
                                return;
                        }

                        // Build email variables
                        java.util.Map<String, Object> variables = new java.util.HashMap<>();
                        variables.put("indentNumber", indent.getIndentNumber());
                        variables.put("creatorName", indentCreator.getFullName());
                        variables.put("cancelledByName", cancelledBy.getFullName());
                        variables.put("cancellationDate", LocalDateTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        variables.put("reason", reason != null ? reason : "No reason provided");
                        variables.put("department",
                                        indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A");

                        // Try to send using template (subject comes from template)
                        boolean sent = emailService.sendEmailFromTemplate(
                                        "INDENT_CANCELLED",
                                        variables,
                                        indentCreator.getEmail());

                        if (!sent) {
                                logger.warn("Failed to send cancellation email for indent {}",
                                                indent.getIndentNumber());
                        }
                } catch (Exception e) {
                        logger.error("Error sending cancellation notification for indent {}: {}",
                                        indent.getIndentNumber(), e.getMessage());
                }
        }

        /**
         * Submit indent for approval
         * 
         * Legacy Business Logic: If the submitter has DEPTHEAD role (role_id=4),
         * the L1 approval is automatically bypassed and the indent goes directly
         * to L2 approval status (submitted with L1 auto-approved).
         */
        public IndentResponse submitIndent(Integer id, String username) {
                logger.info("Submitting indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only draft indents can be submitted
                if (indent.getStatus().getId() != 1) {
                        throw new IllegalStateException(
                                        "Only draft indents can be submitted. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Change status to 2 (Submitted)
                indent.setStatus(entityManager.getReference(IndentStatus.class, 2));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                // DEPT-LEVEL AUTO-APPROVAL: mirror the frontend "Direct to Procurement" confirmation
                // popup EXACTLY. That popup fires for hasAnyRole(['DEPTHEAD','PLANTMANAGER']) with no
                // other condition, so the backend must bypass RM (L1) + Dept-Head (L2) and route
                // straight to Procurement for the SAME roles and with NO extra gate — reading the same
                // normalized principal roles the popup reads (from the JWT), so the two can't drift.
                //
                // ROOT CAUSE this replaces: the previous check was `isDeptHead(DEPTHEAD only) &&
                // !hasSupervisor`. A PLANTMANAGER (not covered) — or a DEPTHEAD who reports to someone
                // (hasSupervisor=true) — saw the popup but the bypass silently no-opped, leaving the
                // indent at "Pending RM Approval". Aligning to the popup fixes both cases.
                // Read the raiser's normalized principal roles once (same signal the frontend popups
                // use). DEPTHEAD/PLANTMANAGER bypass BOTH stages to Procurement; a plain SUPERVISOR
                // bypasses only the RM stage (they ARE the RM) and lands in the Dept-Head queue.
                var bypassAuth = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
                java.util.Set<String> raiserRoles =
                        (bypassAuth != null
                                && bypassAuth.getPrincipal() instanceof com.nslindia.procurezone.security.UserPrincipal bp
                                && bp.roles() != null)
                                        ? bp.roles() : java.util.Set.of();
                boolean isDeptLevelRaiser = raiserRoles.contains("DEPTHEAD") || raiserRoles.contains("PLANTMANAGER");
                boolean isSupervisorRaiser = !isDeptLevelRaiser && raiserRoles.contains("SUPERVISOR");

                if (isDeptLevelRaiser) {
                        logger.info("Dept-level auto-approval: user {} is DEPTHEAD/PLANTMANAGER — "
                                        + "bypassing L1+L2, routing indent to Procurement", username);

                        // Auto-approve at BOTH L1 and L2 without changes: copy the requested quantity
                        // into rmQuantity AND deptQuantity so the line reflects a completed Dept-Head
                        // approval (the Dept Head column then shows a value, consistent with a manual
                        // l2Approve; deptQuantity stays null only when a Dept Head has NOT yet acted).
                        for (IndentDetail detail : indent.getDetails()) {
                                if (detail.getRmQuantity() == null) {
                                        detail.setRmQuantity(detail.getQuantity());
                                }
                                if (detail.getDeptQuantity() == null) {
                                        detail.setDeptQuantity(detail.getRmQuantity() != null
                                                        ? detail.getRmQuantity() : detail.getQuantity());
                                }
                                detail.setLastModifiedDate(LocalDateTime.now());
                                detail.setLastModifiedBy(currentUser.getEmpNumber());
                        }

                        // Full three-column state: RM approved (approved=3) + Dept-Head approved
                        // (final=4) + arrived at Procurement (procurement=4) — identical to a normal
                        // l2Approve end state, so deriveDisplayStatus(3,4,4) = "Dept. Head Approved"
                        // (procurement stage) instead of stopping at the Dept-Head queue.
                        indent.setStatus(entityManager.getReference(IndentStatus.class, 3)); // Dept Head Approved
                        indent.setApprovedBy(currentUser);
                        indent.setApprovedByDate(LocalDateTime.now());
                        indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 3));
                        indent.setFinalApprovedBy(currentUser);
                        indent.setFinalApprovedDate(LocalDateTime.now());
                        indent.setFinalStatus(entityManager.getReference(IndentStatus.class, 4));
                        indent.setProcurementStatus(entityManager.getReference(IndentStatus.class, 4));
                        indent.setRemarks("L1+L2 auto-approved (submitter is DEPTHEAD/PLANTMANAGER) — routed to Procurement");
                } else if (isSupervisorRaiser) {
                        logger.info("Supervisor auto-approval: user {} is SUPERVISOR (the RM) — "
                                        + "bypassing the RM stage, routing indent to the Dept-Head queue", username);

                        // Auto-approve L1 only (the Supervisor IS the RM): copy the requested quantity
                        // into rmQuantity. deptQuantity stays NULL — the Dept Head has not acted yet, so
                        // the Dept Head column correctly renders blank until they do.
                        for (IndentDetail detail : indent.getDetails()) {
                                if (detail.getRmQuantity() == null) {
                                        detail.setRmQuantity(detail.getQuantity());
                                        detail.setLastModifiedDate(LocalDateTime.now());
                                        detail.setLastModifiedBy(currentUser.getEmpNumber());
                                }
                        }

                        // Three-column state = RM approved (approved=3), awaiting Dept Head
                        // (finalStatus=1, procurementStatus=1 — left as set at creation). status stays 2
                        // (Submitted). deriveDisplayStatus(3,1,1) = "RM Approved" → the Dept-Head queue
                        // (findDeptHeadQueueForCreators filters approvedStatus=3 AND finalStatus=1).
                        indent.setApprovedBy(currentUser);
                        indent.setApprovedByDate(LocalDateTime.now());
                        indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 3));
                        indent.setRemarks("L1 auto-approved (submitter is SUPERVISOR / RM) — awaiting Dept Head");
                }

                indent = indentRepository.save(indent);

                // Record workflow action
                boolean autoApproved = isDeptLevelRaiser || isSupervisorRaiser;
                String action = isDeptLevelRaiser ? "SUBMITTED_AUTO_L1_L2"
                                : isSupervisorRaiser ? "SUBMITTED_AUTO_L1" : "SUBMITTED";
                String bypassNote = isDeptLevelRaiser
                                ? "L1+L2 auto-approved (DEPTHEAD/PLANTMANAGER) — routed to Procurement"
                                : isSupervisorRaiser
                                        ? "L1 auto-approved (SUPERVISOR/RM) — awaiting Dept Head"
                                        : null;
                recordWorkflowAction(indent, currentUser, action, bypassNote, 0);

                // Audit log
                String auditMessage = autoApproved
                                ? String.format("Submitted indent %s for approval (%s)", indent.getIndentNumber(), bypassNote)
                                : String.format("Submitted indent %s for approval", indent.getIndentNumber());
                auditService.logEntityChange(
                                "SUBMIT",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                auditMessage);

                logger.info("Submitted indent {} for approval{}", indent.getIndentNumber(),
                                autoApproved ? " (L1+L2 auto-approved → Procurement)" : "");

                return toIndentResponse(indent);
        }

        /**
         * Approve indent (Department Head level)
         */
        public IndentResponse approveIndent(Integer id, String username, String remarks) {
                logger.info("Approving indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only submitted indents can be approved
                if (indent.getStatus().getId() != 2) {
                        throw new IllegalStateException(
                                        "Only submitted indents can be approved. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Hierarchy check: Supervisors (RMs) must be in the creator's reporting chain.
                // Dept Heads and above have department-level authority and skip the check.
                boolean hasDeptLevelAuth = hasRoleByCode(currentUser.getEmpNumber(), "Department Head")
                                || hasRoleByCode(currentUser.getEmpNumber(), "Plant Manager")
                                || hasRoleByCode(currentUser.getEmpNumber(), "Admin")
                                || hasRoleByCode(currentUser.getEmpNumber(), "Super Admin");
                if (!hasDeptLevelAuth) {
                        Employee indentCreator = indent.getEmployee();
                        if (indentCreator != null && indentCreator.getEmpNumber() != null) {
                                boolean canApprove = reportingHierarchyService.canApproveFor(
                                                indentCreator.getEmpNumber(), currentUser.getEmpNumber());
                                if (!canApprove) {
                                        throw new IllegalStateException(
                                                        "You are not authorized to approve this indent. " +
                                                        "You must be in the reporting chain of the indent creator.");
                                }
                        }
                }

                // Update approval fields
                indent.setApprovedBy(currentUser);
                indent.setApprovedByDate(LocalDateTime.now());
                indent.setRemarks(remarks);
                indent.setStatus(entityManager.getReference(IndentStatus.class, 3)); // Department Head Approved
                indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 3));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "APPROVED", remarks, 1);

                // Audit log
                auditService.logEntityChange(
                                "APPROVE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Approved indent %s (Department Head)", indent.getIndentNumber()));

                logger.info("Approved indent {} by Department Head", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * L1 Approval (RM/Section Head) with quantity adjustment.
         * 
         * This implements the legacy workflow where the Reporting Manager can:
         * 1. Approve the indent for their subordinate
         * 2. Adjust quantities for each line item (rmQuantity field)
         * 3. Add remarks for the adjustment
         * 
         * Status: 2 (Submitted) → 2 (still Submitted, but with L1 approval recorded)
         * The approvedStatus field tracks the L1 approval.
         * 
         * @param id       the indent ID
         * @param request  L1ApprovalRequest with remarks and optional quantity
         *                 adjustments
         * @param username the approving user's email
         * @return updated IndentResponse
         */
        public IndentResponse l1Approve(Integer id, L1ApprovalRequest request, String username) {
                logger.info("L1 Approving indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only submitted indents can be L1 approved (status = 2)
                if (indent.getStatus().getId() != 2) {
                        throw new IllegalStateException(
                                        "Only submitted indents can be L1 approved. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Verify approver has authority (is RM of indent creator)
                Employee indentCreator = indent.getEmployee();
                if (indentCreator != null && indentCreator.getEmpNumber() != null) {
                        boolean canApprove = reportingHierarchyService.canApproveFor(
                                        indentCreator.getEmpNumber(),
                                        currentUser.getEmpNumber());
                        if (!canApprove) {
                                logger.warn("User {} attempted L1 approval without authority for indent {}",
                                                username, indent.getIndentNumber());
                                throw new IllegalStateException(
                                                "You are not authorized to L1 approve this indent. " +
                                                                "You must be in the reporting chain of the indent creator.");
                        }
                }

                // Apply quantity adjustments if provided. Audit rows are collected here and saved
                // after the indent persists — one row per line that ACTUALLY changed.
                LocalDateTime editedAt = LocalDateTime.now();
                java.util.List<IndentDetailQtyAudit> audits = new java.util.ArrayList<>();
                if (request.adjustments() != null && !request.adjustments().isEmpty()) {
                        for (L1ApprovalRequest.LineItemAdjustment adj : request.adjustments()) {
                                IndentDetail detail = indent.getDetails().stream()
                                                .filter(d -> d.getId().equals(adj.detailId()))
                                                .findFirst()
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Indent detail not found with ID: " + adj.detailId()));

                                // Sanity bound only (business decision: monotonic-decrease limit removed):
                                // 0 < rmQuantity <= 99999. RM may increase or decrease freely, but not to zero.
                                if (adj.rmQuantity().signum() <= 0
                                                || adj.rmQuantity().compareTo(MAX_QUANTITY) > 0) {
                                        throw new IllegalArgumentException(
                                                        String.format("RM quantity (%.2f) must be greater than 0 and at most 99999 for line item %d",
                                                                        adj.rmQuantity(), adj.detailId()));
                                }

                                // Audit only a real change (old = original indent_details_qty).
                                if (adj.rmQuantity().compareTo(detail.getQuantity()) != 0) {
                                        audits.add(new IndentDetailQtyAudit(detail.getId(), "RM",
                                                        detail.getQuantity(), adj.rmQuantity(),
                                                        currentUser.getEmpNumber(), editedAt));
                                }

                                detail.setRmQuantity(adj.rmQuantity());
                                detail.setLastModifiedDate(editedAt);
                                detail.setLastModifiedBy(currentUser.getEmpNumber());

                                logger.debug("L1 adjusted line item {}: qty {} -> rm_qty {}",
                                                adj.detailId(), detail.getQuantity(), adj.rmQuantity());
                        }
                } else {
                        // No adjustments - copy original quantity to rmQuantity for all items
                        for (IndentDetail detail : indent.getDetails()) {
                                if (detail.getRmQuantity() == null) {
                                        detail.setRmQuantity(detail.getQuantity());
                                        detail.setLastModifiedDate(editedAt);
                                        detail.setLastModifiedBy(currentUser.getEmpNumber());
                                }
                        }
                }

                // Update L1 approval fields
                indent.setApprovedBy(currentUser);
                indent.setApprovedByDate(LocalDateTime.now());
                indent.setRemarks(request.remarks());
                indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 3)); // L1 Approved → DeptHead queue
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Persist the per-line quantity-edit audit trail (authoritative history).
                if (!audits.isEmpty()) {
                        qtyAuditRepository.saveAll(audits);
                }

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "L1_APPROVED", request.remarks(), 1);

                // Audit log
                String adjustmentInfo = request.adjustments() != null
                                ? String.format(" with %d quantity adjustments", request.adjustments().size())
                                : "";
                auditService.logEntityChange(
                                "L1_APPROVE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("L1 Approved indent %s (RM/Section Head)%s",
                                                indent.getIndentNumber(), adjustmentInfo));

                // Send email notification to indent creator about L1 approval
                sendL1ApprovalNotification(indent, currentUser);

                logger.info("L1 Approved indent {} by RM", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * L1 Rejection (RM/Section Head).
         */
        public IndentResponse l1Reject(Integer id, String username, String remarks) {
                logger.info("L1 Rejecting indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only submitted indents can be L1 rejected (status = 2)
                if (indent.getStatus().getId() != 2) {
                        throw new IllegalStateException(
                                        "Only submitted indents can be L1 rejected. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update L1 rejection fields
                indent.setApprovedBy(currentUser);
                indent.setApprovedByDate(LocalDateTime.now());
                indent.setRemarks(remarks);
                indent.setStatus(entityManager.getReference(IndentStatus.class, 4)); // Rejected
                indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 4)); // L1 Rejected
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "L1_REJECTED", remarks, 1);

                // Audit log
                auditService.logEntityChange(
                                "L1_REJECT",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("L1 Rejected indent %s: %s", indent.getIndentNumber(), remarks));

                logger.info("L1 Rejected indent {}", indent.getIndentNumber());
                sendL1RejectionNotification(indent, currentUser, remarks);

                return toIndentResponse(indent);
        }

        /**
         * L2 Approval (Department Head) with quantity adjustment.
         * 
         * This implements the legacy workflow where the Department Head can:
         * 1. Approve the indent after L1 approval
         * 2. Further adjust quantities (deptQuantity field)
         * 3. Add remarks
         * 
         * Status: 2 (Submitted with L1 approval) → 3 (Department Head Approved)
         * 
         * @param id       the indent ID
         * @param request  L2ApprovalRequest with remarks and optional quantity
         *                 adjustments
         * @param username the approving user's email
         * @return updated IndentResponse
         */
        public IndentResponse l2Approve(Integer id, L2ApprovalRequest request, String username) {
                logger.info("L2 Approving indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Can be L2 approved if status = 2 and has L1 approval (approvedStatus set)
                if (indent.getStatus().getId() != 2) {
                        throw new IllegalStateException(
                                        "Only submitted indents can be L2 approved. Current status: "
                                                        + indent.getStatus().getName());
                }

                // Check if L1 approval exists
                if (indent.getApprovedBy() == null) {
                        throw new IllegalStateException(
                                        "Indent must be L1 approved before L2 approval. Please wait for RM approval.");
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Apply quantity adjustments if provided. Audit rows collected here, saved after
                // the indent persists — one row per line that ACTUALLY changed.
                LocalDateTime editedAt = LocalDateTime.now();
                java.util.List<IndentDetailQtyAudit> audits = new java.util.ArrayList<>();
                if (request.adjustments() != null && !request.adjustments().isEmpty()) {
                        for (L2ApprovalRequest.LineItemAdjustment adj : request.adjustments()) {
                                IndentDetail detail = indent.getDetails().stream()
                                                .filter(d -> d.getId().equals(adj.detailId()))
                                                .findFirst()
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Indent detail not found with ID: " + adj.detailId()));

                                // Get the reference quantity (RM quantity if set, otherwise original)
                                java.math.BigDecimal referenceQty = detail.getRmQuantity() != null
                                                ? detail.getRmQuantity()
                                                : detail.getQuantity();

                                // Sanity bound only (business decision: monotonic-decrease limit removed):
                                // 0 < deptQuantity <= 99999. DeptHead may increase or decrease freely, but not to zero.
                                if (adj.deptQuantity().signum() <= 0
                                                || adj.deptQuantity().compareTo(MAX_QUANTITY) > 0) {
                                        throw new IllegalArgumentException(
                                                        String.format("Dept quantity (%.2f) must be greater than 0 and at most 99999 for line item %d",
                                                                        adj.deptQuantity(), adj.detailId()));
                                }

                                // Audit only a real change (old = the reference qty the DeptHead saw).
                                if (adj.deptQuantity().compareTo(referenceQty) != 0) {
                                        audits.add(new IndentDetailQtyAudit(detail.getId(), "DEPTHEAD",
                                                        referenceQty, adj.deptQuantity(),
                                                        currentUser.getEmpNumber(), editedAt));
                                }

                                detail.setDeptQuantity(adj.deptQuantity());
                                detail.setLastModifiedDate(editedAt);
                                detail.setLastModifiedBy(currentUser.getEmpNumber());

                                logger.debug("L2 adjusted line item {}: rm_qty {} -> dept_qty {}",
                                                adj.detailId(), referenceQty, adj.deptQuantity());
                        }
                } else {
                        // No adjustments - copy RM quantity to deptQuantity for all items
                        for (IndentDetail detail : indent.getDetails()) {
                                if (detail.getDeptQuantity() == null) {
                                        java.math.BigDecimal referenceQty = detail.getRmQuantity() != null
                                                        ? detail.getRmQuantity()
                                                        : detail.getQuantity();
                                        detail.setDeptQuantity(referenceQty);
                                        detail.setLastModifiedDate(editedAt);
                                        detail.setLastModifiedBy(currentUser.getEmpNumber());
                                }
                        }
                }

                // Update L2 approval fields
                indent.setFinalApprovedBy(currentUser);
                indent.setFinalApprovedDate(LocalDateTime.now());
                indent.setFinalRemarks(request.remarks());
                indent.setStatus(entityManager.getReference(IndentStatus.class, 3)); // Department Head Approved
                // Three-column state must be (approvedStatus=3, finalStatus=4, procurementStatus=4)
                // so deriveDisplayStatus() returns "Dept. Head Approved" and the frontend
                // awaitingProcurement flag fires correctly.
                indent.setFinalStatus(entityManager.getReference(IndentStatus.class, 4));
                indent.setProcurementStatus(entityManager.getReference(IndentStatus.class, 4));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Persist the per-line quantity-edit audit trail (authoritative history).
                if (!audits.isEmpty()) {
                        qtyAuditRepository.saveAll(audits);
                }

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "L2_APPROVED", request.remarks(), 2);

                // Audit log
                String adjustmentInfo = request.adjustments() != null
                                ? String.format(" with %d quantity adjustments", request.adjustments().size())
                                : "";
                auditService.logEntityChange(
                                "L2_APPROVE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("L2 Approved indent %s (Dept Head)%s",
                                                indent.getIndentNumber(), adjustmentInfo));

                // Send email notification
                sendL2ApprovalNotification(indent, currentUser);

                logger.info("L2 Approved indent {} by Dept Head", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * L2 Rejection (Department Head).
         */
        public IndentResponse l2Reject(Integer id, String username, String remarks) {
                logger.info("L2 Rejecting indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Can be L2 rejected if status = 2 and has L1 approval
                if (indent.getStatus().getId() != 2) {
                        throw new IllegalStateException(
                                        "Only submitted indents can be L2 rejected. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update L2 rejection fields
                indent.setFinalApprovedBy(currentUser);
                indent.setFinalApprovedDate(LocalDateTime.now());
                indent.setFinalRemarks(remarks);
                indent.setStatus(entityManager.getReference(IndentStatus.class, 4)); // Rejected
                indent.setFinalStatus(entityManager.getReference(IndentStatus.class, 4)); // L2 Rejected
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "L2_REJECTED", remarks, 2);

                // Audit log
                auditService.logEntityChange(
                                "L2_REJECT",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("L2 Rejected indent %s: %s", indent.getIndentNumber(), remarks));

                logger.info("L2 Rejected indent {}", indent.getIndentNumber());
                sendL2RejectionNotification(indent, currentUser, remarks);

                return toIndentResponse(indent);
        }

        /**
         * Get pending L1 approvals for a supervisor (their direct reports' indents).
         */
        @Transactional(readOnly = true)
        public List<PendingApprovalResponse> getPendingL1Approvals(String username) {
                logger.info("Fetching pending L1 approvals for user: {}", username);

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Get all subordinates
                List<Integer> subordinateIds = reportingHierarchyService
                                .getApprovableEmployees(currentUser.getEmpNumber(), false);

                if (subordinateIds.isEmpty()) {
                        logger.debug("No subordinates found for user: {}", username);
                        return List.of();
                }

                // Three-column filter: approvedStatus=1 (Pending RM approval), active
                List<Indent> pendingIndents = indentRepository.findRmQueueForEmployees(subordinateIds);

                return pendingIndents.stream()
                                .map(this::toPendingApprovalResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get pending L2 approvals for a department head.
         * Reads deptId from the current user's JWT — SUPERADMIN/ADMIN see all departments.
         */
        @Transactional(readOnly = true)
        public List<PendingApprovalResponse> getPendingL2Approvals(String username) {
                var auth = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();
                List<Indent> pendingIndents;
                if (auth != null && auth.getPrincipal() instanceof com.nslindia.procurezone.security.UserPrincipal cu) {
                        boolean isAdminOrSuper = cu.roles().stream()
                                        .anyMatch(r -> "SUPERADMIN".equals(r) || "ADMIN".equals(r));
                        if (isAdminOrSuper) {
                                logger.info("Fetching L2 approvals for ADMIN user: {} — all departments", username);
                                pendingIndents = indentRepository.findDeptHeadQueue();
                        } else {
                                // Hierarchy-based: own + 2-level subordinates
                                java.util.List<Integer> hierEmpNumbers = new java.util.ArrayList<>();
                                if (cu.employeeNumber() != null) {
                                        hierEmpNumbers.add(cu.employeeNumber());
                                        java.util.List<Integer> l1Reports = employeeReportingRepository.findSubordinateNumbers(cu.employeeNumber());
                                        hierEmpNumbers.addAll(l1Reports);
                                        for (Integer l1EmpNo : l1Reports) {
                                                hierEmpNumbers.addAll(employeeReportingRepository.findSubordinateNumbers(l1EmpNo));
                                        }
                                }
                                logger.info("Fetching L2 approvals for user: {} hierarchy ({} employees)", username, hierEmpNumbers.size());
                                if (hierEmpNumbers.isEmpty()) {
                                        return java.util.Collections.emptyList();
                                }
                                pendingIndents = indentRepository.findDeptHeadQueueForCreators(hierEmpNumbers);
                        }
                } else {
                        return java.util.Collections.emptyList();
                }
                return pendingIndents.stream()
                                .map(this::toPendingApprovalResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Send email notification for L1 approval.
         */
        private void sendL1ApprovalNotification(Indent indent, Employee approver) {
                try {
                        Employee indentCreator = indent.getEmployee();
                        if (indentCreator == null || indentCreator.getEmail() == null) {
                                logger.warn("Cannot send L1 approval notification - no creator email for indent {}",
                                                indent.getIndentNumber());
                                return;
                        }

                        java.util.Map<String, Object> variables = new java.util.HashMap<>();
                        variables.put("indentNumber", indent.getIndentNumber());
                        variables.put("creatorName", indentCreator.getFullName());
                        variables.put("approverName", approver.getFullName());
                        variables.put("approvalDate", LocalDateTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        variables.put("remarks", indent.getRemarks() != null ? indent.getRemarks() : "No remarks");
                        variables.put("department",
                                        indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A");

                        emailService.sendEmailFromTemplate("INDENT_L1_APPROVED", variables, indentCreator.getEmail());
                } catch (Exception e) {
                        logger.error("Error sending L1 approval notification for indent {}: {}",
                                        indent.getIndentNumber(), e.getMessage());
                }
        }

        /**
         * Send email notification for L2 approval.
         */
        private void sendL2ApprovalNotification(Indent indent, Employee approver) {
                try {
                        Employee indentCreator = indent.getEmployee();
                        if (indentCreator == null || indentCreator.getEmail() == null) {
                                logger.warn("Cannot send L2 approval notification - no creator email for indent {}",
                                                indent.getIndentNumber());
                                return;
                        }

                        java.util.Map<String, Object> variables = new java.util.HashMap<>();
                        variables.put("indentNumber", indent.getIndentNumber());
                        variables.put("creatorName", indentCreator.getFullName());
                        variables.put("approverName", approver.getFullName());
                        variables.put("approvalDate", LocalDateTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        variables.put("remarks",
                                        indent.getFinalRemarks() != null ? indent.getFinalRemarks() : "No remarks");
                        variables.put("department",
                                        indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A");

                        emailService.sendEmailFromTemplate("INDENT_L2_APPROVED", variables, indentCreator.getEmail());
                } catch (Exception e) {
                        logger.error("Error sending L2 approval notification for indent {}: {}",
                                        indent.getIndentNumber(), e.getMessage());
                }
        }

        private void sendL1RejectionNotification(Indent indent, Employee rejector) {
                sendL1RejectionNotification(indent, rejector, null);
        }

        private void sendL1RejectionNotification(Indent indent, Employee rejector, String remarks) {
                try {
                        Employee indentCreator = indent.getEmployee();
                        if (indentCreator == null || indentCreator.getEmail() == null) {
                                logger.warn("Cannot send L1 rejection notification - no creator email for indent {}",
                                                indent.getIndentNumber());
                                return;
                        }
                        java.util.Map<String, Object> variables = new java.util.HashMap<>();
                        variables.put("indentNumber", indent.getIndentNumber());
                        variables.put("creatorName", indentCreator.getFullName());
                        variables.put("rejectorName", rejector.getFullName());
                        variables.put("rejectionDate", LocalDateTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        variables.put("reason", remarks != null ? remarks : "No reason provided");
                        variables.put("department",
                                        indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A");
                        emailService.sendEmailFromTemplate("INDENT_L1_REJECTED", variables, indentCreator.getEmail());
                } catch (Exception e) {
                        logger.error("Error sending L1 rejection notification for indent {}: {}",
                                        indent.getIndentNumber(), e.getMessage());
                }
        }

        private void sendL2RejectionNotification(Indent indent, Employee rejector) {
                sendL2RejectionNotification(indent, rejector, null);
        }

        private void sendL2RejectionNotification(Indent indent, Employee rejector, String remarks) {
                try {
                        Employee indentCreator = indent.getEmployee();
                        if (indentCreator == null || indentCreator.getEmail() == null) {
                                logger.warn("Cannot send L2 rejection notification - no creator email for indent {}",
                                                indent.getIndentNumber());
                                return;
                        }
                        java.util.Map<String, Object> variables = new java.util.HashMap<>();
                        variables.put("indentNumber", indent.getIndentNumber());
                        variables.put("creatorName", indentCreator.getFullName());
                        variables.put("rejectorName", rejector.getFullName());
                        variables.put("rejectionDate", LocalDateTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        variables.put("reason", remarks != null ? remarks : "No reason provided");
                        variables.put("department",
                                        indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A");
                        emailService.sendEmailFromTemplate("INDENT_L2_REJECTED", variables, indentCreator.getEmail());
                } catch (Exception e) {
                        logger.error("Error sending L2 rejection notification for indent {}: {}",
                                        indent.getIndentNumber(), e.getMessage());
                }
        }

        /**
         * Reject indent
         */
        public IndentResponse rejectIndent(Integer id, String username, String remarks) {
                logger.info("Rejecting indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only submitted or in-approval indents can be rejected (status = 2, 3, or 5)
                // Status 4 = Rejected — already a terminal state, cannot reject again
                int currentStatus = indent.getStatus().getId();
                if (currentStatus != 2 && currentStatus != 3 && currentStatus != 5) {
                        throw new IllegalStateException(
                                        "Cannot reject indent in current status: " + indent.getStatus().getName() +
                                                        ". Only submitted or pending approval indents can be rejected.");
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update rejection fields
                indent.setApprovedBy(currentUser);
                indent.setApprovedByDate(LocalDateTime.now());
                indent.setRemarks(remarks);
                indent.setStatus(entityManager.getReference(IndentStatus.class, 4)); // Rejected
                indent.setApprovedStatus(entityManager.getReference(IndentStatus.class, 4));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "REJECTED", remarks, 1);

                // Audit log
                auditService.logEntityChange(
                                "REJECT",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Rejected indent %s: %s", indent.getIndentNumber(), remarks));

                logger.info("Rejected indent {}", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Final Approve indent (Finance/Plant Manager level)
         * Legacy status flow: 3 (APPROVED) → 5 (PROCUREMENT_IN_PROGRESS)
         * 
         * In the legacy database:
         * 1=CREATED, 2=SUBMITTED, 3=APPROVED, 4=REJECTED,
         * 5=PROCUREMENT_IN_PROGRESS, 6=PO_CREATED, 7=CLOSED
         */
        public IndentResponse finalApproveIndent(Integer id, String username, String remarks) {
                logger.info("Final approving indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only approved indents can be final approved (status = 3 in legacy)
                if (indent.getStatus().getId() != 3) {
                        throw new IllegalStateException(
                                        "Only approved indents can be moved to procurement. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update final approval fields
                indent.setFinalApprovedBy(currentUser);
                indent.setFinalApprovedDate(LocalDateTime.now());
                indent.setFinalRemarks(remarks);
                // status=5 keeps the smart-router routing PROCUREMENT's next action to procurementApproveIndent()
                indent.setStatus(entityManager.getReference(IndentStatus.class, 5));
                // Three-column state must be (approvedStatus=3, finalStatus=4, procurementStatus=4)
                // so deriveDisplayStatus() returns "Dept. Head Approved" and the frontend
                // awaitingProcurement flag fires. finalStatus=4 NOT 5 — the matrix entry is (3,4,4).
                indent.setFinalStatus(entityManager.getReference(IndentStatus.class, 4));
                indent.setProcurementStatus(entityManager.getReference(IndentStatus.class, 4));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "FINAL_APPROVED", remarks, 2);

                // Audit log
                auditService.logEntityChange(
                                "FINAL_APPROVE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Final approved indent %s - moved to procurement",
                                                indent.getIndentNumber()));

                logger.info("Final approved indent {} - moved to procurement", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Procurement Approve indent — creates PO
         * Legacy status flow: 5 (PROCUREMENT_IN_PROGRESS) → 6 (PO_CREATED)
         */
        public IndentResponse procurementApproveIndent(Integer id, String username, String remarks) {
                logger.info("Procurement approving indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only procurement-in-progress indents can be PO created (status = 5 in legacy)
                if (indent.getStatus().getId() != 5) {
                        throw new IllegalStateException(
                                        "Only procurement-in-progress indents can create PO. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update procurement approval fields
                indent.setProcurementBy(currentUser);
                indent.setStatus(entityManager.getReference(IndentStatus.class, 6)); // PO_CREATED
                indent.setProcurementStatus(entityManager.getReference(IndentStatus.class, 6));
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "PO_CREATED", remarks, 3);

                // Audit log
                auditService.logEntityChange(
                                "PROCUREMENT_APPROVE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("PO created for indent %s", indent.getIndentNumber()));

                logger.info("PO created for indent {}", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Put indent on hold
         * Any approved status → Status 7 (On Hold)
         */
        public IndentResponse holdIndent(Integer id, String username, String remarks) {
                logger.info("Putting indent ID: {} on hold by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Can only hold indents in approval workflow (status 2-5)
                int currentStatus = indent.getStatus().getId();
                if (currentStatus < 2 || currentStatus > 5) {
                        throw new IllegalStateException(
                                        "Cannot hold indent in current status: " + indent.getStatus().getName() +
                                                        ". Only indents in approval workflow can be held.");
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Store previous status for resuming (use remarks to track)
                String holdRemarks = String.format("HOLD_FROM_STATUS_%d: %s", currentStatus, remarks);

                // Update hold fields
                indent.setStatus(entityManager.getReference(IndentStatus.class, 7)); // On Hold
                indent.setRemarks(holdRemarks);
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "ON_HOLD", remarks, currentStatus);

                // Audit log
                auditService.logEntityChange(
                                "HOLD",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Put indent %s on hold: %s", indent.getIndentNumber(), remarks));

                logger.info("Put indent {} on hold", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Complete indent
         * Status 5 (Procurement Approved) → Status 8 (Completed)
         */
        public IndentResponse completeIndent(Integer id, String username, String remarks) {
                logger.info("Completing indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only procurement approved indents can be completed (status = 5)
                if (indent.getStatus().getId() != 5) {
                        throw new IllegalStateException(
                                        "Only procurement approved indents can be completed. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Update completion fields
                indent.setStatus(entityManager.getReference(IndentStatus.class, 8)); // Completed
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "COMPLETED", remarks, 4);

                // Audit log
                auditService.logEntityChange(
                                "COMPLETE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Completed indent %s", indent.getIndentNumber()));

                logger.info("Completed indent {}", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Resume indent from hold
         * Status 7 (On Hold) → Previous status (decoded from remarks) or Status 3
         */
        public IndentResponse resumeIndent(Integer id, String username, String remarks) {
                logger.info("Resuming indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Only on hold indents can be resumed (status = 7)
                if (indent.getStatus().getId() != 7) {
                        throw new IllegalStateException(
                                        "Only on-hold indents can be resumed. Current status: "
                                                        + indent.getStatus().getName());
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Try to restore previous status from remarks
                int previousStatus = 3; // Default to Dept Head Approved
                String currentRemarks = indent.getRemarks();
                if (currentRemarks != null && currentRemarks.startsWith("HOLD_FROM_STATUS_")) {
                        try {
                                int underscoreIdx = currentRemarks.indexOf(":", 17);
                                if (underscoreIdx > 0) {
                                        previousStatus = Integer.parseInt(currentRemarks.substring(17, underscoreIdx));
                                }
                        } catch (NumberFormatException e) {
                                logger.warn("Could not parse previous status from remarks, defaulting to 3");
                        }
                }

                // Update resume fields
                indent.setStatus(entityManager.getReference(IndentStatus.class, previousStatus));
                indent.setRemarks(remarks != null ? remarks : "Resumed from hold");
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                // Record workflow action
                recordWorkflowAction(indent, currentUser, "RESUMED", remarks, previousStatus);

                // Audit log
                auditService.logEntityChange(
                                "RESUME",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Resumed indent %s to status %d", indent.getIndentNumber(),
                                                previousStatus));

                logger.info("Resumed indent {} to status {}", indent.getIndentNumber(), previousStatus);

                return toIndentResponse(indent);
        }

        /**
         * Update procurement sub-stage on an indent.
         * Sub-status values (indent_procurement_status FK):
         *   5 = Quotations Collected, 6 = Negotiation Done,
         *   7 = PO Released (poNumber + deliveryDate required),
         *   8 = Hold (remarks required), 9 = Cash Buy (deliveryDate required)
         */
        public IndentResponse updateProcurementStatus(
                        Integer id, String username,
                        Integer procurementSubStatus, String poNumber,
                        java.time.LocalDate deliveryDate, String remarks) {
                logger.info("Updating procurement sub-status on indent {} to {} by {}", id, procurementSubStatus, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                // Must be in Dept-Head-Approved state (finalStatus id = 4)
                if (indent.getFinalStatus() == null || indent.getFinalStatus().getId() != 4) {
                        throw new IllegalStateException(
                                        "Procurement update requires Dept Head approval first. Current final status: "
                                                        + (indent.getFinalStatus() != null ? indent.getFinalStatus().getName() : "none"));
                }

                // Validate required fields per sub-status
                if (procurementSubStatus == 7 && (poNumber == null || deliveryDate == null)) {
                        throw new IllegalArgumentException("PO Released requires poNumber and deliveryDate");
                }
                if (procurementSubStatus == 8 && (remarks == null || remarks.isBlank())) {
                        throw new IllegalArgumentException("Hold requires remarks");
                }
                if (procurementSubStatus == 9 && deliveryDate == null) {
                        throw new IllegalArgumentException("Cash Buy requires deliveryDate");
                }

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                indent.setProcurementStatus(entityManager.getReference(IndentStatus.class, procurementSubStatus));
                if (poNumber != null) indent.setPoNumber(poNumber);
                if (deliveryDate != null) indent.setDeliveryDate(deliveryDate);
                if (remarks != null) indent.setProcurementRemarks(remarks);
                indent.setProcurementBy(currentUser);
                indent.setLastModifiedDate(LocalDateTime.now());
                indent.setLastModifiedBy(currentUser.getEmpNumber());

                indent = indentRepository.save(indent);

                auditService.logEntityChange(
                                "PROCUREMENT_UPDATE",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Procurement sub-status updated to %d on indent %s",
                                                procurementSubStatus, indent.getIndentNumber()));

                return toIndentResponse(indent);
        }

        /**
         * Request additional information on an indent
         */
        public IndentResponse requestInfo(Integer id, String username, String infoRequested) {
                logger.info("Requesting info on indent ID: {} by user: {}", id, username);

                Indent indent = indentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + id));

                Employee currentUser = employeeRepository.findByEmail(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                // Record workflow action for info request
                recordWorkflowActionWithInfo(indent, currentUser, "INFO_REQUESTED", infoRequested, null, 1);

                // Audit log
                auditService.logEntityChange(
                                "REQUEST_INFO",
                                "Indent",
                                indent.getId(),
                                currentUser.getEmpNumber(),
                                username,
                                String.format("Requested additional info on indent %s", indent.getIndentNumber()));

                logger.info("Requested additional info on indent {}", indent.getIndentNumber());

                return toIndentResponse(indent);
        }

        /**
         * Get pending approvals for the calling user.
         * SUPERVISOR (RM-level): routed to L1 queue (subordinates' submitted indents).
         * DEPTHEAD / PLANTMANAGER / ADMIN / SUPERADMIN: DeptHead L2 queue.
         */
        @Transactional(readOnly = true)
        public List<PendingApprovalResponse> getPendingApprovals(String username, Integer departmentId) {
                logger.info("Fetching pending approvals for user: {} in department: {}", username, departmentId);

                var auth = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof com.nslindia.procurezone.security.UserPrincipal cu) {
                        boolean isSupervisorOnly = cu.roles().contains("SUPERVISOR")
                                        && cu.roles().stream().noneMatch(r ->
                                                "DEPTHEAD".equals(r) || "PLANTMANAGER".equals(r)
                                                || "ADMIN".equals(r) || "SUPERADMIN".equals(r));
                        if (isSupervisorOnly) {
                                logger.info("Routing SUPERVISOR {} to L1 RM pending queue", username);
                                return getPendingL1Approvals(username);
                        }
                        boolean isDeptHeadRole = cu.roles().stream()
                                        .anyMatch(r -> "DEPTHEAD".equals(r) || "PLANTMANAGER".equals(r));
                        if (isDeptHeadRole) {
                                logger.info("Routing DEPTHEAD {} to hierarchy-based L2 queue", username);
                                return getPendingL2Approvals(username);
                        }
                }

                // ADMIN/SUPERADMIN fallback: global DeptHead queue
                List<Indent> pendingIndents = (departmentId != null)
                                ? indentRepository.findDeptHeadQueueByDepartment(departmentId)
                                : indentRepository.findDeptHeadQueue();

                return pendingIndents.stream()
                                .map(this::toPendingApprovalResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get approval workflow history for an indent
         */
        @Transactional(readOnly = true)
        public List<ApprovalWorkflowResponse> getApprovalHistory(Integer indentId) {
                logger.info("Fetching approval history for indent ID: {}", indentId);

                // Verify indent exists
                indentRepository.findById(indentId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Indent not found with ID: " + indentId));

                List<ApprovalWorkflow> history = approvalWorkflowRepository
                                .findByIndentIdOrderByActionDateAsc(indentId);

                return history.stream()
                                .map(this::toApprovalWorkflowResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get indents by department (for Department Head)
         */
        @Transactional(readOnly = true)
        public Page<IndentListResponse> getIndentsByDepartment(Integer departmentId, Pageable pageable) {
                logger.info("Fetching indents for department ID: {}", departmentId);

                return indentRepository.findByDepartmentId(departmentId, pageable)
                                .map(this::toIndentListResponse);
        }

        // Helper methods

        /**
         * Record workflow action
         */
        private void recordWorkflowAction(Indent indent, Employee approver, String action, String remarks,
                        Integer level) {
                ApprovalWorkflow workflow = new ApprovalWorkflow();
                workflow.setIndent(indent);
                workflow.setApprover(approver);
                workflow.setAction(action);
                workflow.setActionDate(LocalDateTime.now());
                workflow.setRemarks(remarks);
                workflow.setLevel(level);

                approvalWorkflowRepository.save(workflow);
                logger.debug("Recorded workflow action: {} by {} for indent {}", action, approver.getEmpName(),
                                indent.getIndentNumber());
        }

        /**
         * Record workflow action with info request
         */
        private void recordWorkflowActionWithInfo(Indent indent, Employee approver, String action, String infoRequested,
                        String remarks, Integer level) {
                ApprovalWorkflow workflow = new ApprovalWorkflow();
                workflow.setIndent(indent);
                workflow.setApprover(approver);
                workflow.setAction(action);
                workflow.setActionDate(LocalDateTime.now());
                workflow.setInfoRequested(infoRequested);
                workflow.setRemarks(remarks);
                workflow.setLevel(level);

                approvalWorkflowRepository.save(workflow);
                logger.debug("Recorded workflow action: {} by {} for indent {}", action, approver.getEmpName(),
                                indent.getIndentNumber());
        }

        private PendingApprovalResponse toPendingApprovalResponse(Indent indent) {
                // Get the submission date from workflow history
                LocalDateTime submittedDate = indent.getLastModifiedDate();

                return new PendingApprovalResponse(
                                indent.getId(),
                                indent.getIndentNumber(),
                                indent.getIndentDate(),
                                indent.getCompany() != null ? indent.getCompany().getName() : null,
                                indent.getDepartment() != null ? indent.getDepartment().getName() : null,
                                indent.getEmployee() != null ? indent.getEmployee().getEmpName() : null,
                                indent.getDeliveryDate(),
                                indent.getStatus() != null ? indent.getStatus().getName() : null,
                                indent.getDetails().size(),
                                submittedDate);
        }

        private ApprovalWorkflowResponse toApprovalWorkflowResponse(ApprovalWorkflow workflow) {
                return new ApprovalWorkflowResponse(
                                workflow.getId(),
                                workflow.getIndent() != null ? workflow.getIndent().getId() : null,
                                workflow.getApprover() != null ? workflow.getApprover().getEmployeeNumber() : null,
                                workflow.getApprover() != null ? workflow.getApprover().getEmpName() : null,
                                workflow.getAction(),
                                workflow.getActionDate(),
                                workflow.getRemarks(),
                                workflow.getLevel(),
                                workflow.getInfoRequested());
        }

        // Mapping methods

        private IndentResponse toIndentResponse(Indent indent) {
                // Cache company-name lookups per distinct materialId so we do at most one query
                // per material across all line items of this indent.
                Map<Integer, String> companiesByMaterial = new java.util.HashMap<>();
                // Cache current-stock lookups per distinct materialId (one query per material).
                Map<Integer, java.math.BigDecimal> stockByMaterial = new java.util.HashMap<>();
                // Per-line quantity-edit history, resolved from the audit table in one query.
                Map<Integer, java.util.List<com.nslindia.procurezone.common.dto.QuantityEditDTO>> historyByDetail =
                                buildIndentQtyHistory(indent.getDetails());
                List<IndentDetailResponse> detailResponses = indent.getDetails().stream()
                                .map(detail -> toIndentDetailResponse(detail, companiesByMaterial, stockByMaterial, historyByDetail))
                                .collect(Collectors.toList());

                return new IndentResponse(
                                indent.getId(),
                                indent.getIndentNumber(),
                                indent.getIndentYear(),
                                indent.getIndentDate(),
                                indent.getCompany() != null ? indent.getCompany().getId() : null,
                                indent.getCompany() != null ? indent.getCompany().getName() : null,
                                indent.getDepartment() != null ? indent.getDepartment().getId() : null,
                                indent.getDepartment() != null ? indent.getDepartment().getName() : null,
                                indent.getSection() != null ? indent.getSection().getId() : null,
                                indent.getSection() != null ? indent.getSection().getName() : null,
                                indent.getPlant() != null ? indent.getPlant().getId() : null,
                                indent.getPlant() != null ? indent.getPlant().getName() : null,
                                indent.getEmployee() != null ? indent.getEmployee().getEmployeeNumber().intValue()
                                                : null,
                                indent.getEmployee() != null ? indent.getEmployee().getEmpName() : null,
                                indent.getComments(),
                                indent.getDeliveryDate(),
                                indent.getPoNumber(),
                                indent.getCreatedBy() != null ? indent.getCreatedBy().getEmployeeNumber().intValue()
                                                : null,
                                indent.getCreatedBy() != null ? indent.getCreatedBy().getEmpName() : null,
                                indent.getApprovedBy() != null ? indent.getApprovedBy().getEmployeeNumber().intValue()
                                                : null,
                                indent.getApprovedBy() != null ? indent.getApprovedBy().getEmpName() : null,
                                indent.getApprovedByDate(),
                                indent.getFinalApprovedBy() != null
                                                ? indent.getFinalApprovedBy().getEmployeeNumber().intValue()
                                                : null,
                                indent.getFinalApprovedBy() != null ? indent.getFinalApprovedBy().getEmpName() : null,
                                indent.getFinalApprovedDate(),
                                indent.getProcurementBy() != null
                                                ? indent.getProcurementBy().getEmployeeNumber().intValue()
                                                : null,
                                indent.getProcurementBy() != null ? indent.getProcurementBy().getEmpName() : null,
                                indent.getRemarks(),
                                indent.getFinalRemarks(),
                                indent.getStatus() != null ? indent.getStatus().getId() : null,
                                resolveStatusLabel(indent.getStatus()),
                                indent.getApprovedStatus() != null ? indent.getApprovedStatus().getId() : null,
                                resolveStatusLabel(indent.getApprovedStatus()),
                                indent.getFinalStatus() != null ? indent.getFinalStatus().getId() : null,
                                resolveStatusLabel(indent.getFinalStatus()),
                                indent.getProcurementStatus() != null ? indent.getProcurementStatus().getId() : null,
                                resolveStatusLabel(indent.getProcurementStatus()),
                                indent.getLastModifiedDate(),
                                indent.getLastModifiedBy(),
                                deriveDisplayStatus(
                                        indent.getApprovedStatus() != null ? indent.getApprovedStatus().getId() : null,
                                        indent.getFinalStatus() != null ? indent.getFinalStatus().getId() : null,
                                        indent.getProcurementStatus() != null ? indent.getProcurementStatus().getId() : null),
                                detailResponses);
        }

        private IndentDetailResponse toIndentDetailResponse(IndentDetail detail,
                        Map<Integer, String> companiesByMaterial,
                        Map<Integer, java.math.BigDecimal> stockByMaterial,
                        Map<Integer, java.util.List<com.nslindia.procurezone.common.dto.QuantityEditDTO>> historyByDetail) {
                Integer materialId = detail.getMaterial() != null ? detail.getMaterial().getId() : null;
                String companies = resolveLineCompany(detail.getCompanyId(), materialId, companiesByMaterial);
                java.math.BigDecimal currentStock = resolveCurrentStock(materialId, stockByMaterial);
                // Effective = the most-advanced stage's value: dept ?? rm ?? original.
                java.math.BigDecimal effective = detail.getDeptQuantity() != null ? detail.getDeptQuantity()
                                : (detail.getRmQuantity() != null ? detail.getRmQuantity() : detail.getQuantity());
                java.util.List<com.nslindia.procurezone.common.dto.QuantityEditDTO> history =
                                historyByDetail.getOrDefault(detail.getId(), java.util.List.of());
                return new IndentDetailResponse(
                                detail.getId(),
                                materialId,
                                detail.getMaterial() != null ? detail.getMaterial().getCode() : null,
                                detail.getMaterial() != null ? detail.getMaterial().getName() : null,
                                detail.getMaterial() != null ? detail.getMaterial().getDescription() : null,
                                detail.getUnitOfMeasure() != null ? detail.getUnitOfMeasure().getId() : null,
                                detail.getUnitOfMeasure() != null ? detail.getUnitOfMeasure().getCode() : null,
                                detail.getUnitOfMeasure() != null ? detail.getUnitOfMeasure().getName() : null,
                                detail.getQuantity(),
                                detail.getRmQuantity(),
                                detail.getDeptQuantity(),
                                effective,
                                detail.getStockAvailable(),
                                detail.getPricing(),
                                detail.getPurpose(),
                                detail.getVendor(),
                                detail.getStatus(),
                                detail.getCompanyId(),
                                companies,
                                history,
                                currentStock);
        }

        /**
         * Current authoritative stock for a material — aggregated map_quantity_stores across all
         * companies/plants (same source as the material dropdown / Inventory). Cached per materialId.
         */
        private java.math.BigDecimal resolveCurrentStock(Integer materialId, Map<Integer, java.math.BigDecimal> cache) {
                if (materialId == null) return null;
                return cache.computeIfAbsent(materialId, id ->
                                companyPlantMaterialMapRepository.sumQuantityByMaterial(id)
                                                .orElse(java.math.BigDecimal.ZERO));
        }

        /**
         * Builds each line item's quantity-edit history (oldest first) from the indent audit table,
         * in a single query. Editor emp-numbers are resolved to names with a per-call cache.
         */
        private Map<Integer, java.util.List<com.nslindia.procurezone.common.dto.QuantityEditDTO>> buildIndentQtyHistory(
                        java.util.List<IndentDetail> details) {
                Map<Integer, java.util.List<com.nslindia.procurezone.common.dto.QuantityEditDTO>> byDetail =
                                new java.util.HashMap<>();
                java.util.List<Integer> detailIds = details.stream()
                                .map(IndentDetail::getId)
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList());
                if (detailIds.isEmpty()) return byDetail;

                Map<Integer, String> nameCache = new java.util.HashMap<>();
                for (IndentDetailQtyAudit a : qtyAuditRepository.findByDetailIdInOrderByEditedAtAsc(detailIds)) {
                        String editorName = nameCache.computeIfAbsent(a.getEditedBy(), empNo ->
                                        employeeRepository.findById(empNo).map(Employee::getEmpName)
                                                        .orElse("Emp #" + empNo));
                        byDetail.computeIfAbsent(a.getDetailId(), k -> new java.util.ArrayList<>())
                                        .add(new com.nslindia.procurezone.common.dto.QuantityEditDTO(
                                                        a.getStage(), a.getOldQuantity(), a.getNewQuantity(),
                                                        editorName, a.getEditedAt()));
                }
                return byDetail;
        }

        /**
         * Resolve the comma-separated list of company names stocking a material, using a
         * per-request cache so each distinct materialId is queried at most once. Line items
         * do not capture their own company, so we return ALL active companies for the material.
         * Null/empty resolves to "".
         */
        private String resolveCompaniesForMaterial(Integer materialId, Map<Integer, String> cache) {
                if (materialId == null) return "";
                return cache.computeIfAbsent(materialId, id -> {
                        List<String> names = companyPlantMaterialMapRepository.findCompanyNamesByMaterial(id);
                        return (names == null || names.isEmpty()) ? "" : String.join(", ", names);
                });
        }

        /**
         * Company shown for a line item: the SPECIFIC company selected at creation, when captured.
         * Newer line items store the chosen companyId, so we show just that one company's name.
         * Legacy rows (companyId == null, or a company id that no longer resolves) fall back to the
         * multi-company resolver so they don't display blank. entityManager.find is L1-cached within
         * the transaction, so repeated look-ups of the same company cost nothing extra.
         */
        private String resolveLineCompany(Integer lineCompanyId, Integer materialId, Map<Integer, String> cache) {
                if (lineCompanyId != null) {
                        Company company = entityManager.find(Company.class, lineCompanyId);
                        if (company != null && company.getName() != null) {
                                return company.getName();
                        }
                }
                return resolveCompaniesForMaterial(materialId, cache);
        }

        private IndentListResponse toIndentListResponse(Indent indent) {
                // Cache company-name lookups per distinct materialId so we query each material at most
                // once across this indent's line items (reuses the same Map-repo resolver as the detail page).
                Map<Integer, String> companiesByMaterial = new java.util.HashMap<>();
                return new IndentListResponse(
                                indent.getId(),
                                indent.getIndentNumber(),
                                indent.getIndentYear(),
                                indent.getIndentDate(),
                                indent.getCompany() != null ? indent.getCompany().getName() : null,
                                indent.getDepartment() != null ? indent.getDepartment().getName() : null,
                                indent.getEmployee() != null ? indent.getEmployee().getEmpName() : null,
                                indent.getDeliveryDate(),
                                resolveStatusLabel(indent.getStatus()),
                                indent.getStatus() != null ? indent.getStatus().getId() : null,
                                indent.getApprovedStatus() != null ? indent.getApprovedStatus().getId() : null,
                                indent.getFinalStatus() != null ? indent.getFinalStatus().getId() : null,
                                indent.getProcurementStatus() != null ? indent.getProcurementStatus().getId() : null,
                                indent.getDetails().size(),
                                deriveDisplayStatus(
                                        indent.getApprovedStatus() != null ? indent.getApprovedStatus().getId() : null,
                                        indent.getFinalStatus() != null ? indent.getFinalStatus().getId() : null,
                                        indent.getProcurementStatus() != null ? indent.getProcurementStatus().getId() : null),
                                indent.getLastModifiedDate(),
                                indent.getDetails().stream()
                                        .map(d -> new IndentListResponse.ItemSummary(
                                                d.getMaterial() != null ? d.getMaterial().getName() : null,
                                                d.getMaterial() != null ? d.getMaterial().getDescription() : null,
                                                resolveLineCompany(
                                                        d.getCompanyId(),
                                                        d.getMaterial() != null ? d.getMaterial().getId() : null,
                                                        companiesByMaterial),
                                                d.getUnitOfMeasure() != null ? d.getUnitOfMeasure().getCode() : null,
                                                d.getQuantity()))
                                        .collect(Collectors.toList()));
        }

        /**
         * Generate the next indent number using the legacy sequential approach.
         *
         * Step 1 — mirrors legacy getIndentNo1():
         *   fetch the most recently inserted record (ORDER BY indent_id DESC LIMIT 1),
         *   parse indent_no as Long, increment by 1.
         *
         * Step 2 — fallback when the latest record has a non-numeric indent_no
         *   (e.g. alphanumeric records created by a previous mis-configured deployment):
         *   scan all rows, take MAX of those whose indent_no is purely numeric, +1.
         *
         * Step 3 — no numeric records at all: start from 1.
         */
        private String generateLegacyNumericIndentNumber() {
                List<String> latest = indentRepository.findLatestIndentNumbers(PageRequest.of(0, 1));
                if (!latest.isEmpty() && latest.get(0) != null) {
                        try {
                                return String.valueOf(Long.parseLong(latest.get(0)) + 1);
                        } catch (NumberFormatException e) {
                                logger.warn("Latest indent_no '{}' is non-numeric; falling back to MAX numeric scan",
                                                latest.get(0));
                        }
                }
                Long maxNumeric = indentRepository.findMaxNumericIndentNumber();
                if (maxNumeric != null && maxNumeric > 0) {
                        return String.valueOf(maxNumeric + 1);
                }
                return "1";
        }

        /** Returns the Indian financial year string, e.g. "2026-27" for Apr 2026 – Mar 2027. */
        public static String getCurrentFinancialYear() {
                java.time.LocalDate today = java.time.LocalDate.now();
                int year = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
                return year + "-" + String.valueOf(year + 1).substring(2);
        }

        /** Read-only preview of what the next indent number will be (does not consume the number). */
        @Transactional(readOnly = true)
        public String previewNextIndentNumber() {
                return generateLegacyNumericIndentNumber();
        }

        /**
         * Check if an employee has a specific role by role code.
         * Used for Supervisor Auto-Approval logic (DEPTHEAD bypasses L1).
         *
         * @param employeeNumber the employee number to check
         * @param roleCode       the role code to check for (e.g., "DEPTHEAD")
         * @return true if employee has the active role
         */
        private boolean hasRoleByCode(Integer employeeNumber, String roleCode) {
                if (employeeNumber == null || roleCode == null) {
                        return false;
                }

                java.util.List<EmployeeRole> roles = employeeRoleRepository.findByEmployee_EmployeeNumberAndStatus(
                                employeeNumber, 1); // 1 = active

                return roles.stream()
                                .anyMatch(er -> er.getRole() != null &&
                                                roleCode.equalsIgnoreCase(er.getRole().getCode()));
        }
}
