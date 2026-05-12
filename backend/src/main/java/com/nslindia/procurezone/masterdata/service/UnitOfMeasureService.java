package com.nslindia.procurezone.masterdata.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.DuplicateResourceException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;
import com.nslindia.procurezone.masterdata.dto.CreateUnitOfMeasureRequest;
import com.nslindia.procurezone.masterdata.dto.UnitOfMeasureResponse;
import com.nslindia.procurezone.masterdata.dto.UpdateUnitOfMeasureRequest;
import com.nslindia.procurezone.masterdata.repository.UnitOfMeasureRepository;

/**
 * Service class for Unit of Measure master data management.
 * Handles business logic, validation, and audit logging for UOM operations.
 */
@Service
@Transactional
public class UnitOfMeasureService {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasureService.class);

    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public UnitOfMeasureService(UnitOfMeasureRepository unitOfMeasureRepository,
            EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all units of measure with pagination.
     * 
     * @param pageable pagination information
     * @return page of unit of measure responses
     */
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> getAllUnitsOfMeasure(Pageable pageable) {
        logger.info("Fetching units of measure with pagination: {}", pageable);
        return unitOfMeasureRepository.findAll(pageable)
                .map(UnitOfMeasureResponse::fromEntity);
    }

    /**
     * Get all active units of measure with pagination.
     * 
     * @param pageable pagination information
     * @return page of active unit of measure responses
     */
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> getActiveUnitsOfMeasure(Pageable pageable) {
        logger.info("Fetching active units of measure with pagination: {}", pageable);
        return unitOfMeasureRepository.findByStatus(1, pageable)
                .map(UnitOfMeasureResponse::fromEntity);
    }

    /**
     * Search units of measure by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active units only
     * @param pageable   pagination information
     * @return page of matching unit of measure responses
     */
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> searchUnitsOfMeasure(String searchTerm, Boolean activeOnly,
            Pageable pageable) {
        logger.info("Searching units of measure with term: '{}', activeOnly: {}", searchTerm, activeOnly);

        if (activeOnly != null && activeOnly) {
            return unitOfMeasureRepository.searchByCodeOrNameAndStatus(searchTerm, 1, pageable)
                    .map(UnitOfMeasureResponse::fromEntity);
        } else {
            return unitOfMeasureRepository.searchByCodeOrName(searchTerm, pageable)
                    .map(UnitOfMeasureResponse::fromEntity);
        }
    }

    /**
     * Get a unit of measure by ID.
     * 
     * @param id the unit of measure ID
     * @return unit of measure response
     * @throws ResourceNotFoundException if unit not found
     */
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse getUnitOfMeasureById(Integer id) {
        logger.info("Fetching unit of measure with id: {}", id);

        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of Measure not found with id: " + id));

        return UnitOfMeasureResponse.fromEntity(unitOfMeasure);
    }

    /**
     * Create a new unit of measure.
     * 
     * @param request  the create request
     * @param username the username of the user creating the unit
     * @return created unit of measure response
     * @throws DuplicateResourceException if code already exists
     */
    public UnitOfMeasureResponse createUnitOfMeasure(CreateUnitOfMeasureRequest request, String username) {
        logger.info("Creating new unit of measure with code: {}", request.code());

        // Normalize input
        CreateUnitOfMeasureRequest normalized = request.normalized();

        // Validate status
        if (!normalized.isValidStatus()) {
            throw new BadRequestException("Status must be either 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (unitOfMeasureRepository.existsByCode(normalized.code())) {
            throw new DuplicateResourceException(
                    "Unit of Measure with code '" + normalized.code() + "' already exists");
        }

        // Get employee for audit
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + username));

        // Create entity
        UnitOfMeasure unitOfMeasure = new UnitOfMeasure();
        unitOfMeasure.setCode(normalized.code());
        unitOfMeasure.setName(normalized.name());
        unitOfMeasure.setStatus(normalized.status());
        unitOfMeasure.setLastModifiedDate(LocalDate.now());
        unitOfMeasure.setLastModifiedBy(employee.getEmpNumber());

        // Save
        UnitOfMeasure saved = unitOfMeasureRepository.save(unitOfMeasure);
        logger.info("Created unit of measure with id: {}, code: {}", saved.getId(), saved.getCode());

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "UnitOfMeasure",
                saved.getId(),
                employee.getEmpNumber(),
                username,
                String.format("Created unit of measure: %s - %s", saved.getCode(), saved.getName()));

        return UnitOfMeasureResponse.fromEntity(saved);
    }

    /**
     * Update an existing unit of measure.
     * 
     * @param id       the unit of measure ID
     * @param request  the update request
     * @param username the username of the user updating the unit
     * @return updated unit of measure response
     * @throws ResourceNotFoundException  if unit not found
     * @throws DuplicateResourceException if code already exists
     */
    public UnitOfMeasureResponse updateUnitOfMeasure(Integer id, UpdateUnitOfMeasureRequest request,
            String username) {
        logger.info("Updating unit of measure with id: {}", id);

        // Normalize input
        UpdateUnitOfMeasureRequest normalized = request.normalized();

        // Validate status
        if (!normalized.isValidStatus()) {
            throw new BadRequestException("Status must be either 0 (inactive) or 1 (active)");
        }

        // Check if unit exists
        UnitOfMeasure existing = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of Measure not found with id: " + id));

        // Check for duplicate code (excluding current unit)
        if (unitOfMeasureRepository.existsByCodeAndIdNot(normalized.code(), id)) {
            throw new DuplicateResourceException(
                    "Unit of Measure with code '" + normalized.code() + "' already exists");
        }

        // Get employee for audit
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + username));

        // Track changes for audit
        String oldCode = existing.getCode();
        String oldName = existing.getName();
        Integer oldStatus = existing.getStatus();

        // Update entity
        existing.setCode(normalized.code());
        existing.setName(normalized.name());
        existing.setStatus(normalized.status());
        existing.setLastModifiedDate(LocalDate.now());
        existing.setLastModifiedBy(employee.getEmpNumber());

        // Save
        UnitOfMeasure updated = unitOfMeasureRepository.save(existing);
        logger.info("Updated unit of measure with id: {}, code: {}", updated.getId(), updated.getCode());

        // Audit log
        String changes = String.format("Code: %s → %s | Name: %s → %s | Status: %d → %d",
                oldCode, normalized.code(),
                oldName, normalized.name(),
                oldStatus, normalized.status());
        auditService.logEntityChange(
                "UPDATE",
                "UnitOfMeasure",
                updated.getId(),
                employee.getEmpNumber(),
                username,
                changes);

        return UnitOfMeasureResponse.fromEntity(updated);
    }

    /**
     * Delete a unit of measure (soft delete by setting status to 0).
     * 
     * @param id       the unit of measure ID
     * @param username the username of the user deleting the unit
     * @throws ResourceNotFoundException if unit not found
     */
    public void deleteUnitOfMeasure(Integer id, String username) {
        logger.info("Deleting unit of measure with id: {}", id);

        // Check if unit exists
        UnitOfMeasure existing = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of Measure not found with id: " + id));

        // Get employee for audit
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + username));

        // Soft delete
        existing.setStatus(0);
        existing.setLastModifiedDate(LocalDate.now());
        existing.setLastModifiedBy(employee.getEmpNumber());

        // Save
        unitOfMeasureRepository.save(existing);
        logger.info("Soft deleted unit of measure with id: {}, code: {}", id, existing.getCode());

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "UnitOfMeasure",
                existing.getId(),
                employee.getEmpNumber(),
                username,
                String.format("Soft deleted unit of measure: %s - %s", existing.getCode(), existing.getName()));
    }
}
