package com.nslindia.procurezone.plantindent;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.common.exception.BusinessException;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;
import com.nslindia.procurezone.plantindent.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

/**
 * Service for Plant Indent operations (R&D / Production workflow).
 * 
 * 8-Status Workflow:
 * 0 = Draft, 1 = Pending DEO/QM, 2 = Pending Manager,
 * 3 = Approved, 4 = Rejected DEO, 5 = Rejected Mgr,
 * 6 = Processing, 7 = Completed
 * 
 * @author NSL India
 * @version 2.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlantIndentService {

    private static final String ENTITY_TYPE = "Plant Indent";

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PENDING_DEO = 1;
    public static final int STATUS_PENDING_MANAGER = 2;
    public static final int STATUS_APPROVED = 3;
    public static final int STATUS_REJECTED_DEO = 4;
    public static final int STATUS_REJECTED_MANAGER = 5;
    public static final int STATUS_PROCESSING = 6;
    public static final int STATUS_COMPLETED = 7;

    private final PlantIndentRepository plantIndentRepository;
    private final PlantIndentDetailRepository plantIndentDetailRepository;
    private final AuditService auditService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create a new Plant Indent
     */
    public PlantIndentResponse createPlantIndent(CreatePlantIndentRequest request, Integer userId) {
        log.info("Creating plant indent for employee: {} at plant: {}",
                request.employeeNumber(), request.plantId());

        // Generate indent number: legacy-compatible sequential numeric.
        // Mirrors legacy getIndentNo1(): ORDER BY indent_id DESC LIMIT 1, parseLong + 1.
        String indentNumber = generateLegacyNumericPlantIndentNumber();

        PlantIndent plantIndent = PlantIndent.builder()
                .indentNumber(indentNumber)
                .indentYear(String.valueOf(java.time.Year.now().getValue()))
                .indentDate(LocalDateTime.now().toString().substring(0, 10))
                .companyId(null)
                .departmentId(null)
                .plantId(request.plantId())
                .employeeId(parseEmployeeNumber(request.employeeNumber()))
                .remarks(request.remarks())
                .cropTypeId(request.cropTypeId())
                .cropName(request.cropId() != null ? String.valueOf(request.cropId()) : null)
                .packProcess(request.packProcess())
                .outputMaterial(request.outputMaterial())
                .outputDescription(request.outputDescription())
                .batchNumber(request.batchNumber() != null ? String.valueOf(request.batchNumber()) : null)
                .masterUom(request.masterUom())
                .lineCode(request.lineCode())
                .lineDescription(request.lineDescription())
                .expectedQuantity(request.expectedQuantity() != null ? Integer.parseInt(request.expectedQuantity()) : null)
                .status(STATUS_DRAFT)
                .createdBy(userId)
                .lastModifiedBy(userId)
                .build();

        plantIndent = plantIndentRepository.save(plantIndent);

        // Create line items
        if (request.details() != null) {
            for (PlantIndentDetailRequest detailReq : request.details()) {
                PlantIndentDetail detail = createDetailFromRequest(detailReq, plantIndent, userId);
                plantIndent.addDetail(detail);
            }
            plantIndent = plantIndentRepository.save(plantIndent);
        }

        auditService.logEntityChange(
                "PLANT_INDENT_CREATED", ENTITY_TYPE, plantIndent.getId(),
                userId, "User " + userId,
                String.format("Plant indent %s created with %d line items",
                        indentNumber, request.details() != null ? request.details().size() : 0));

        log.info("Created plant indent: {}", indentNumber);
        return mapToResponse(plantIndent);
    }

    /**
     * Get plant indent by ID
     */
    @Transactional(readOnly = true)
    public PlantIndentResponse getPlantIndentById(Integer id) {
        PlantIndent plantIndent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found with ID: " + id));
        return mapToResponse(plantIndent);
    }

    /**
     * Get plant indent by number
     */
    @Transactional(readOnly = true)
    public PlantIndentResponse getPlantIndentByNumber(String indentNumber) {
        PlantIndent plantIndent = plantIndentRepository.findByIndentNumber(indentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + indentNumber));
        return mapToResponse(plantIndent);
    }

    /**
     * List plant indents with optional combined filters.
     * Any null param is treated as "no filter for this dimension".
     */
    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> listPlantIndents(
            String search, Integer plantId, Integer status,
            String empSearch, String fromDate, String toDate,
            Pageable pageable) {
        String searchParam    = (search    != null && !search.isBlank())    ? search    : null;
        String empSearchParam = (empSearch != null && !empSearch.isBlank()) ? empSearch : null;
        String fromParam      = (fromDate  != null && !fromDate.isBlank())  ? fromDate  : null;
        String toParam        = (toDate    != null && !toDate.isBlank())    ? toDate    : null;
        return plantIndentRepository
                .searchWithFilters(searchParam, plantId, status, empSearchParam, fromParam, toParam, pageable)
                .map(this::mapToResponse);
    }

    /**
     * List plant indents by plant
     */
    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> listPlantIndentsByPlant(Integer plantId, Pageable pageable) {
        return plantIndentRepository.findByPlantId(plantId, pageable).map(this::mapToResponse);
    }

    /**
     * List plant indents by employee
     */
    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> listPlantIndentsByEmployee(String employeeNumber, Pageable pageable) {
        Integer empId = parseEmployeeNumber(employeeNumber);
        return plantIndentRepository.findByEmployeeId(empId, pageable).map(this::mapToResponse);
    }

    /**
     * Search plant indents
     */
    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> searchPlantIndents(String searchTerm, Pageable pageable) {
        return plantIndentRepository.search(searchTerm, pageable).map(this::mapToResponse);
    }

    /**
     * Update plant indent
     */
    public PlantIndentResponse updatePlantIndent(Integer id, CreatePlantIndentRequest request, Integer userId) {
        log.info("Updating plant indent ID: {}", id);

        PlantIndent plantIndent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));

        plantIndent.setPlantId(request.plantId());
        plantIndent.setEmployeeId(parseEmployeeNumber(request.employeeNumber()));
        plantIndent.setRemarks(request.remarks());
        plantIndent.setCropTypeId(request.cropTypeId());
        plantIndent.setCropName(request.cropId() != null ? String.valueOf(request.cropId()) : null);
        plantIndent.setPackProcess(request.packProcess());
        plantIndent.setOutputMaterial(request.outputMaterial());
        plantIndent.setOutputDescription(request.outputDescription());
        plantIndent.setBatchNumber(request.batchNumber() != null ? String.valueOf(request.batchNumber()) : null);
        plantIndent.setMasterUom(request.masterUom());
        plantIndent.setLineCode(request.lineCode());
        plantIndent.setLineDescription(request.lineDescription());
        plantIndent.setExpectedQuantity(request.expectedQuantity() != null ? Integer.parseInt(request.expectedQuantity()) : null);
        plantIndent.setLastModifiedBy(userId);

        // Update details
        if (request.details() != null && !request.details().isEmpty()) {
            plantIndent.getDetails().clear();
            plantIndentDetailRepository.flush();
            for (PlantIndentDetailRequest detailReq : request.details()) {
                PlantIndentDetail detail = createDetailFromRequest(detailReq, plantIndent, userId);
                plantIndent.addDetail(detail);
            }
        }

        plantIndent = plantIndentRepository.save(plantIndent);

        auditService.logEntityChange("PLANT_INDENT_UPDATED", ENTITY_TYPE, id, userId, "User " + userId,
                "Plant indent " + plantIndent.getIndentNumber() + " updated");

        return mapToResponse(plantIndent);
    }

    /**
     * Delete plant indent
     */
    public void deletePlantIndent(Integer id, Integer userId) {
        PlantIndent plantIndent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        String indentNumber = plantIndent.getIndentNumber();
        plantIndentRepository.delete(plantIndent);
        auditService.logEntityChange("PLANT_INDENT_DELETED", ENTITY_TYPE, id, userId, "User " + userId,
                "Plant indent " + indentNumber + " deleted");
    }

    // ========== HELPER METHODS ==========

    /**
     * Generates the next legacy-compatible numeric plant indent number.
     *
     * Step 1 — normal case: fetch the most recently inserted record (ORDER BY indent_id DESC LIMIT 1),
     *   parse indent_no as Long, increment by 1.
     *
     * Step 2 — fallback when the latest record has a non-numeric indent_no
     *   (e.g. PIND/... records created by a previous mis-configured deployment):
     *   scan all rows, take MAX of those whose indent_no is purely numeric, +1.
     *
     * Step 3 — no numeric records at all: return "1".
     */
    private String generateLegacyNumericPlantIndentNumber() {
        List<String> latest = plantIndentRepository.findLatestPlantIndentNumbers(PageRequest.of(0, 1));
        if (!latest.isEmpty() && latest.get(0) != null) {
            try {
                return String.valueOf(Long.parseLong(latest.get(0)) + 1);
            } catch (NumberFormatException e) {
                log.warn("Latest plant indent_no '{}' is non-numeric; falling back to MAX numeric scan",
                        latest.get(0));
            }
        }
        Long maxNumeric = plantIndentRepository.findMaxNumericPlantIndentNumber();
        if (maxNumeric != null && maxNumeric > 0) {
            return String.valueOf(maxNumeric + 1);
        }
        return "1";
    }

    private Integer parseEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null) return null;
        try {
            return Integer.parseInt(employeeNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private PlantIndentDetail createDetailFromRequest(PlantIndentDetailRequest req,
            PlantIndent plantIndent, Integer userId) {
        PlantIndentDetail.PlantIndentDetailBuilder builder = PlantIndentDetail.builder()
                .plantIndent(plantIndent)
                .quantity(req.quantity())
                .stockAvailable(req.stockAvailable())
                .pricing(req.pricing())
                .purpose(req.purpose())
                .vendor(req.vendor())
                .status(1)
                .lastModifiedDate(LocalDateTime.now())
                .lastModifiedBy(userId)
                .indentMaterial(req.indentMaterial())
                .indentMaterialDescription(req.indentMaterialDescription())
                .lotNumber(req.lotNumber())
                .storageLocation(req.storageLocation());

        // Set material FK if provided — use find() so a missing ID fails fast on write.
        if (req.materialId() != null && req.materialId() > 0) {
            Material material = entityManager.find(Material.class, req.materialId());
            if (material == null) {
                throw new ResourceNotFoundException("Material not found with id: " + req.materialId());
            }
            builder.material(material);
        }
        // Set UOM FK if provided
        if (req.unitOfMeasureId() != null && req.unitOfMeasureId() > 0) {
            UnitOfMeasure uom = entityManager.find(UnitOfMeasure.class, req.unitOfMeasureId());
            if (uom == null) {
                throw new ResourceNotFoundException("UnitOfMeasure not found with id: " + req.unitOfMeasureId());
            }
            builder.unitOfMeasure(uom);
        }

        // QC Parameters
        builder.stl(req.stl()).odv(req.odv()).got(req.got()).elisa(req.elisa())
                .sdcls(req.sdcls()).stats(req.stats()).skipd(req.skipd()).inspdt(req.inspdt())
                .moisture(req.moisture()).pureSeed(req.pureSeed()).inertMatter(req.inertMatter())
                .ocsCount(req.ocsCount()).weedSeedCount(req.weedSeedCount()).grain(req.grain())
                .blackSeeds(req.blackSeeds()).pinholeSeed(req.pinholeSeed())
                .odvRes(req.odvRes()).bulkDensity(req.bulkDensity()).thsw(req.thsw())
                .coldVigourGermNormal(req.coldVigourGermNormal())
                .firstCountNormal(req.firstCountNormal()).germNormal(req.germNormal())
                .fetNormal(req.fetNormal()).soilCountDays(req.soilCountDays())
                .aavGermNormal(req.aavGermNormal())
                .gotGp(req.gotGp()).gotFemale(req.gotFemale()).gotOthers(req.gotOthers())
                .bg1(req.bg1()).bg2(req.bg2()).ht(req.ht()).fqr(req.fqr())
                .q1(req.q1()).q2(req.q2()).q3(req.q3()).q4(req.q4())
                .q5(req.q5()).q6(req.q6()).q7(req.q7()).q8(req.q8()).q9(req.q9());

        return builder.build();
    }

    private PlantIndentResponse mapToResponse(PlantIndent pi) {
        List<PlantIndentDetailResponse> details = pi.getDetails().stream()
                .map(this::mapDetailToResponse)
                .collect(Collectors.toList());

        // Look up employee name
        String employeeName = null;
        if (pi.getEmployeeId() != null) {
            try {
                var emp = entityManager.find(com.nslindia.procurezone.identity.Employee.class, pi.getEmployeeId());
                if (emp != null) employeeName = emp.getFullName();
            } catch (Exception e) {
                log.debug("Could not resolve employee name for ID: {}", pi.getEmployeeId());
            }
        }

        // Look up plant name
        String plantName = null;
        if (pi.getPlantId() != null && pi.getPlantId() > 0) {
            try {
                var plant = entityManager.find(com.nslindia.procurezone.masterdata.Plant.class, pi.getPlantId());
                if (plant != null) plantName = plant.getName();
            } catch (Exception e) {
                log.debug("Could not resolve plant name for ID: {}", pi.getPlantId());
            }
        }

        // Look up crop type name
        String cropTypeName = null;
        if (pi.getCropTypeId() != null && pi.getCropTypeId() > 0) {
            try {
                var cropType = entityManager.find(com.nslindia.procurezone.masterdata.CropType.class, pi.getCropTypeId());
                if (cropType != null) cropTypeName = cropType.getDivisionName();
            } catch (Exception e) {
                log.debug("Could not resolve crop type name for ID: {}", pi.getCropTypeId());
            }
        }

        return new PlantIndentResponse(
                pi.getId(),
                pi.getIndentCode(),
                pi.getEmployeeNumber(),
                employeeName,
                pi.getPlantId(),
                plantName,
                pi.getIndentNumber(),
                pi.getRemarks() != null ? pi.getRemarks() : pi.getIndentRemarks(),
                pi.getCropTypeId(),
                cropTypeName,
                null, // cropId (stored as string in cropName)
                pi.getCropName(),
                pi.getPackProcess(),
                pi.getOutputMaterial(),
                pi.getOutputDescription(),
                pi.getBatchNumber() != null ? parseIntSafe(pi.getBatchNumber()) : null,
                pi.getMasterUom(),
                pi.getLineCode(),
                pi.getLineDescription(),
                pi.getExpectedQuantity() != null ? String.valueOf(pi.getExpectedQuantity()) : null,
                pi.getStatus(),
                pi.getStatusName(),
                pi.getCreatedDate(),
                details.size(),
                details);
    }

    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private PlantIndentDetailResponse mapDetailToResponse(PlantIndentDetail detail) {
        // Hibernate proxy is never null even for missing FK targets — resolve safely.
        Integer materialId = null;
        String materialCode = null;
        String materialName = null;
        try {
            Material m = detail.getMaterial();
            if (m != null) {
                materialId = m.getId();
                materialCode = m.getCode();
                materialName = m.getName();
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("indent_details_material FK points to deleted material (detail id={})", detail.getId());
        }

        Integer uomId = null;
        String uomCode = null;
        String uomName = null;
        try {
            UnitOfMeasure u = detail.getUnitOfMeasure();
            if (u != null) {
                uomId = u.getId();
                uomCode = u.getCode();
                uomName = u.getName();
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("indent_details_umo FK points to deleted UOM (detail id={})", detail.getId());
        }

        return new PlantIndentDetailResponse(
                detail.getId(),
                materialId,
                materialCode,
                materialName,
                uomId,
                uomCode,
                uomName,
                detail.getQuantity(),
                detail.getRmQuantity(),
                detail.getDeptQuantity(),
                detail.getStockAvailable(),
                detail.getPricing(),
                detail.getPurpose(),
                detail.getVendor(),
                detail.getStatus(),
                detail.getIndentMaterial(),
                detail.getIndentMaterialDescription(),
                detail.getLotNumber(),
                detail.getStorageLocation(),
                detail.getStl(), detail.getOdv(), detail.getGot(), detail.getElisa(),
                detail.getSdcls(), detail.getStats(), detail.getSkipd(), detail.getInspdt(),
                detail.getMoisture(), detail.getPureSeed(), detail.getInertMatter(),
                detail.getOcsCount(), detail.getWeedSeedCount(), detail.getGrain(),
                detail.getBlackSeeds(), detail.getPinholeSeed(), detail.getOdvRes(),
                detail.getBulkDensity(), detail.getThsw(),
                detail.getColdVigourGermNormal(), detail.getFirstCountNormal(),
                detail.getGermNormal(), detail.getFetNormal(), detail.getSoilCountDays(),
                detail.getAavGermNormal(),
                detail.getGotGp(), detail.getGotFemale(), detail.getGotOthers(),
                detail.getBg1(), detail.getBg2(), detail.getHt(), detail.getFqr(),
                detail.getQ1(), detail.getQ2(), detail.getQ3(), detail.getQ4(),
                detail.getQ5(), detail.getQ6(), detail.getQ7(), detail.getQ8(), detail.getQ9(),
                detail.getLastModifiedDate());
    }

    // ========== 8-STATUS WORKFLOW METHODS ==========

    public PlantIndentResponse submitForReview(Integer id, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isDraft()) {
            throw new BusinessException("Only draft indents can be submitted. Current: " + indent.getStatusName());
        }
        indent.setStatus(STATUS_PENDING_DEO);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_SUBMITTED", ENTITY_TYPE, id, userId, "User " + userId,
                "Submitted for DEO review: " + indent.getIndentNumber());
        return mapToResponse(indent);
    }

    public PlantIndentResponse deoApprove(Integer id, String remarks, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isPendingDeoReview()) {
            throw new BusinessException("Not pending DEO review. Current: " + indent.getStatusName());
        }
        indent.setStatus(STATUS_PENDING_MANAGER);
        indent.setApprovedBy(userId);
        indent.setApprovedByDate(LocalDateTime.now().toString().substring(0, 10));
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_DEO_APPROVED", ENTITY_TYPE, id, userId, "User " + userId,
                "DEO approved: " + remarks);
        return mapToResponse(indent);
    }

    public PlantIndentResponse deoReject(Integer id, String remarks, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isPendingDeoReview()) {
            throw new BusinessException("Not pending DEO review. Current: " + indent.getStatusName());
        }
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new BusinessException("Rejection remarks are required");
        }
        indent.setStatus(STATUS_REJECTED_DEO);
        indent.setApprovedBy(userId);
        indent.setApprovedByDate(LocalDateTime.now().toString().substring(0, 10));
        indent.setIndentRemarks(remarks);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_DEO_REJECTED", ENTITY_TYPE, id, userId, "User " + userId,
                "DEO rejected: " + remarks);
        return mapToResponse(indent);
    }

    public PlantIndentResponse managerApprove(Integer id, String remarks, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isPendingManagerApproval()) {
            throw new BusinessException("Not pending manager approval. Current: " + indent.getStatusName());
        }
        indent.setStatus(STATUS_APPROVED);
        indent.setFinalApprovedBy(userId);
        indent.setFinalDate(LocalDateTime.now().toString().substring(0, 10));
        indent.setFinalRemarks(remarks);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_MANAGER_APPROVED", ENTITY_TYPE, id, userId, "User " + userId,
                "Manager approved: " + remarks);
        return mapToResponse(indent);
    }

    public PlantIndentResponse managerReject(Integer id, String remarks, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isPendingManagerApproval()) {
            throw new BusinessException("Not pending manager approval. Current: " + indent.getStatusName());
        }
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new BusinessException("Rejection remarks are required");
        }
        indent.setStatus(STATUS_REJECTED_MANAGER);
        indent.setFinalApprovedBy(userId);
        indent.setFinalDate(LocalDateTime.now().toString().substring(0, 10));
        indent.setFinalRemarks(remarks);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_MANAGER_REJECTED", ENTITY_TYPE, id, userId, "User " + userId,
                "Manager rejected: " + remarks);
        return mapToResponse(indent);
    }

    public PlantIndentResponse startProcessing(Integer id, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isApproved()) {
            throw new BusinessException("Only approved indents can be processed. Current: " + indent.getStatusName());
        }
        indent.setStatus(STATUS_PROCESSING);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_PROCESSING", ENTITY_TYPE, id, userId, "User " + userId,
                "Processing started: " + indent.getIndentNumber());
        return mapToResponse(indent);
    }

    public PlantIndentResponse completeProcessing(Integer id, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isProcessing()) {
            throw new BusinessException("Only processing indents can be completed. Current: " + indent.getStatusName());
        }
        indent.setStatus(STATUS_COMPLETED);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_COMPLETED", ENTITY_TYPE, id, userId, "User " + userId,
                "Completed: " + indent.getIndentNumber());
        return mapToResponse(indent);
    }

    public PlantIndentResponse resubmit(Integer id, Integer userId) {
        PlantIndent indent = plantIndentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant indent not found: " + id));
        if (!indent.isRejectedByDeo() && !indent.isRejectedByManager()) {
            throw new BusinessException("Only rejected indents can be resubmitted. Current: " + indent.getStatusName());
        }
        indent.setStatus(STATUS_DRAFT);
        indent.setApprovedBy(null);
        indent.setApprovedByDate(null);
        indent.setFinalApprovedBy(null);
        indent.setFinalDate(null);
        indent.setLastModifiedBy(userId);
        indent = plantIndentRepository.save(indent);
        auditService.logEntityChange("PLANT_INDENT_RESUBMITTED", ENTITY_TYPE, id, userId, "User " + userId,
                "Resubmitted: " + indent.getIndentNumber());
        return mapToResponse(indent);
    }

    // ========== ROLE-SPECIFIC LIST METHODS ==========

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getDeoQueue(Integer plantId, Pageable pageable) {
        return plantIndentRepository.findDeoQueueByPlant(plantId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getManagerQueue(Integer plantId, Pageable pageable) {
        return plantIndentRepository.findManagerQueueByPlant(plantId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getProcessingQueue(Integer plantId, Pageable pageable) {
        return plantIndentRepository.findProcessingQueueByPlant(plantId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getRejectedItems(Integer plantId, Pageable pageable) {
        return plantIndentRepository.findRejectedByPlant(plantId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getMyDrafts(String employeeNumber, Pageable pageable) {
        Integer empId = parseEmployeeNumber(employeeNumber);
        return plantIndentRepository.findByEmployeeIdAndStatus(empId, STATUS_DRAFT, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getByStatus(Integer status, Pageable pageable) {
        return plantIndentRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantIndentResponse> getByPlantAndStatus(Integer plantId, Integer status, Pageable pageable) {
        return plantIndentRepository.findByPlantIdAndStatus(plantId, status, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardCounts(Integer plantId) {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> statusCounts = plantIndentRepository.getStatusCountsByPlant(plantId);

        counts.put("draft", 0L);
        counts.put("pendingDeo", 0L);
        counts.put("pendingManager", 0L);
        counts.put("approved", 0L);
        counts.put("rejectedDeo", 0L);
        counts.put("rejectedManager", 0L);
        counts.put("processing", 0L);
        counts.put("completed", 0L);
        counts.put("total", 0L);

        long total = 0;
        for (Object[] row : statusCounts) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            total += count;
            switch (status) {
                case STATUS_DRAFT -> counts.put("draft", count);
                case STATUS_PENDING_DEO -> counts.put("pendingDeo", count);
                case STATUS_PENDING_MANAGER -> counts.put("pendingManager", count);
                case STATUS_APPROVED -> counts.put("approved", count);
                case STATUS_REJECTED_DEO -> counts.put("rejectedDeo", count);
                case STATUS_REJECTED_MANAGER -> counts.put("rejectedManager", count);
                case STATUS_PROCESSING -> counts.put("processing", count);
                case STATUS_COMPLETED -> counts.put("completed", count);
            }
        }
        counts.put("total", total);
        return counts;
    }
}
