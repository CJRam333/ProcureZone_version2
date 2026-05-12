package com.seeds.procurezone.masterdata.service;

import com.seeds.procurezone.audit.service.AuditService;
import com.seeds.procurezone.common.exception.BadRequestException;
import com.seeds.procurezone.common.exception.DuplicateResourceException;
import com.seeds.procurezone.common.exception.ResourceNotFoundException;
import com.seeds.procurezone.employee.Employee;
import com.seeds.procurezone.employee.repository.EmployeeRepository;
import com.seeds.procurezone.masterdata.UnitOfMeasure;
import com.seeds.procurezone.masterdata.dto.CreateUnitOfMeasureRequest;
import com.seeds.procurezone.masterdata.dto.UnitOfMeasureResponse;
import com.seeds.procurezone.masterdata.dto.UpdateUnitOfMeasureRequest;
import com.seeds.procurezone.masterdata.repository.UnitOfMeasureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service layer for Unit of Measure management operations.
 * 
 * <p>
 * This service handles business logic for units of measure including:
 * </p>
 * <ul>
 * <li>Retrieving all units of measure with pagination</li>
 * <li>Filtering active units of measure</li>
 * <li>Searching units of measure by code or name</li>
 * <li>Getting unit of measure by ID</li>
 * <li>Creating new units of measure with validation</li>
 * <li>Updating existing units of measure</li>
 * <li>Soft-deleting units of measure</li>
 * <li>Audit logging for all CUD operations</li>
 * </ul>
 * 
 * <p>
 * All write operations are transactional and logged in the audit system.
 * </p>
 * 
 * @author ProcureZone Development Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
public class UnitOfMeasureService {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasureService.class);

    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    /**
     * Constructor with dependency injection.
     * Uses constructor injection (Spring Boot best practice, no @Autowired needed).
     * 
     * @param unitOfMeasureRepository Repository for unit of measure data access
     * @param employeeRepository      Repository for employee data access
     * @param auditService            Service for audit logging
     */
    public UnitOfMeasureService(
            UnitOfMeasureRepository unitOfMeasureRepository,
            EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Retrieves all units of measure with pagination.
     * 
     * @param pageable Pagination parameters (page number, size, sort)
     * @return Page of unit of measure responses
     */
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> getAllUnitsOfMeasure(Pageable pageable) {
        logger.debug("Fetching all units of measure with pagination: {}", pageable);
        return unitOfMeasureRepository.findAll(pageable)
                .map(UnitOfMeasureResponse::from);
    }

    /**
     * Retrieves only active units of measure with pagination.
     * 
     * @param pageable Pagination parameters (page number, size, sort)
     * @return Page of active unit of measure responses
     */
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> getActiveUnitsOfMeasure(Pageable pageable) {
        logger.debug("Fetching active units of measure with pagination: {}", pageable);
        return unitOfMeasureRepository.findByStatus(1, pageable)
                .map(UnitOfMeasureResponse::from);
    }

    /**
     * Searches units of measure by code or name with optional status filter.
     * Search is case-insensitive and uses partial matching.
     * 
     * @param searchTerm The term to search for in code or name
     * @param activeOnly If true, only returns active units of measure
     * @param pageable   Pagination parameters
     * @return Page of matching unit of measure responses
     */
    @Transactional(readOnly = true)
    public Page<UnitOfMeasureResponse> searchUnitsOfMeasure(String searchTerm, boolean activeOnly, Pageable pageable) {
        logger.debug("Searching units of measure with term: '{}', activeOnly: {}", searchTerm, activeOnly);

        String searchPattern = "%" + searchTerm + "%";

        Page<UnitOfMeasure> unitsOfMeasure;
        if (activeOnly) {
            unitsOfMeasure = unitOfMeasureRepository.searchByCodeOrNameAndStatus(searchPattern, 1, pageable);
        } else {
            unitsOfMeasure = unitOfMeasureRepository.searchByCodeOrName(searchPattern, pageable);
        }

        return unitsOfMeasure.map(UnitOfMeasureResponse::from);
    }

    /**
     * Retrieves a unit of measure by its ID.
     * 
     * @param id The ID of the unit of measure
     * @return The unit of measure response
     * @throws ResourceNotFoundException if unit of measure not found
     */
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse getUnitOfMeasureById(Integer id) {
        logger.debug("Fetching unit of measure by ID: {}", id);

        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found with ID: " + id));

        return UnitOfMeasureResponse.from(unitOfMeasure);
    }

    /**
     * Creates a new unit of measure.
     * 
     * <p>
     * Validation performed:
     * </p>
     * <ul>
     * <li>Status must be 0 or 1</li>
     * <li>Unit of measure code must be unique</li>
     * <li>Current user must exist in the system</li>
     * </ul>
     * 
     * @param request  The create request containing unit of measure details
     * @param username The username of the current user (from authentication
     *                 context)
     * @return The created unit of measure response
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if code already exists
     */
    public UnitOfMeasureResponse createUnitOfMeasure(CreateUnitOfMeasureRequest request, String username) {
        logger.info("Creating new unit of measure with code: {} by user: {}", request.code(), username);

        // Validate status
        request.validateStatus();

        // Check for duplicate code
        if (unitOfMeasureRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Unit of measure with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found: " + username));

        // Create new unit of measure
        UnitOfMeasure unitOfMeasure = new UnitOfMeasure();
        unitOfMeasure.setCode(request.code());
        unitOfMeasure.setName(request.name());
        unitOfMeasure.setStatus(request.status());
        unitOfMeasure.setLastModifiedDate(LocalDate.now());
        unitOfMeasure.setLastModifiedBy(currentUser.getId());

        // Save unit of measure
        UnitOfMeasure savedUnitOfMeasure = unitOfMeasureRepository.save(unitOfMeasure);

        // Log to audit
        auditService.logUnitOfMeasureCreated(
                savedUnitOfMeasure.getId(),
                savedUnitOfMeasure.getCode(),
                currentUser.getId(),
                username);

        logger.info("Successfully created unit of measure with ID: {}", savedUnitOfMeasure.getId());
        return UnitOfMeasureResponse.from(savedUnitOfMeasure);
    }

    /**
     * Updates an existing unit of measure.
     * 
     * <p>
     * Validation performed:
     * </p>
     * <ul>
     * <li>Unit of measure must exist</li>
     * <li>Status must be 0 or 1</li>
     * <li>Unit of measure code must be unique (excluding current unit of
     * measure)</li>
     * <li>Current user must exist in the system</li>
     * </ul>
     * 
     * @param id       The ID of the unit of measure to update
     * @param request  The update request containing new unit of measure details
     * @param username The username of the current user (from authentication
     *                 context)
     * @return The updated unit of measure response
     * @throws ResourceNotFoundException  if unit of measure not found
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if code already exists for another unit of
     *                                    measure
     */
    public UnitOfMeasureResponse updateUnitOfMeasure(Integer id, UpdateUnitOfMeasureRequest request, String username) {
        logger.info("Updating unit of measure ID: {} by user: {}", id, username);

        // Validate status
        request.validateStatus();

        // Get existing unit of measure
        UnitOfMeasure existingUnitOfMeasure = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found with ID: " + id));

        // Check for duplicate code (excluding current unit of measure)
        if (unitOfMeasureRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Unit of measure with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found: " + username));

        // Capture old values for audit
        String oldDetails = String.format("Code: %s, Name: %s, Status: %d",
                existingUnitOfMeasure.getCode(), existingUnitOfMeasure.getName(), existingUnitOfMeasure.getStatus());

        // Update unit of measure
        existingUnitOfMeasure.setCode(request.code());
        existingUnitOfMeasure.setName(request.name());
        existingUnitOfMeasure.setStatus(request.status());
        existingUnitOfMeasure.setLastModifiedDate(LocalDate.now());
        existingUnitOfMeasure.setLastModifiedBy(currentUser.getId());

        // Save updated unit of measure
        UnitOfMeasure updatedUnitOfMeasure = unitOfMeasureRepository.save(existingUnitOfMeasure);

        // Capture new values for audit
        String newDetails = String.format("Code: %s, Name: %s, Status: %d",
                updatedUnitOfMeasure.getCode(), updatedUnitOfMeasure.getName(), updatedUnitOfMeasure.getStatus());

        // Log to audit
        auditService.logUnitOfMeasureUpdated(
                updatedUnitOfMeasure.getId(),
                updatedUnitOfMeasure.getCode(),
                oldDetails,
                newDetails,
                currentUser.getId(),
                username);

        logger.info("Successfully updated unit of measure ID: {}", id);
        return UnitOfMeasureResponse.from(updatedUnitOfMeasure);
    }

    /**
     * Soft-deletes a unit of measure by setting its status to inactive.
     * 
     * <p>
     * This is a soft delete operation. The unit of measure is not physically
     * removed
     * from the database, but its status is set to 0 (inactive).
     * </p>
     * 
     * @param id       The ID of the unit of measure to delete
     * @param username The username of the current user (from authentication
     *                 context)
     * @throws ResourceNotFoundException if unit of measure not found
     */
    public void deleteUnitOfMeasure(Integer id, String username) {
        logger.info("Deleting unit of measure ID: {} by user: {}", id, username);

        // Get existing unit of measure
        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found with ID: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found: " + username));

        // Soft delete (set status to inactive)
        unitOfMeasure.setStatus(0);
        unitOfMeasure.setLastModifiedDate(LocalDate.now());
        unitOfMeasure.setLastModifiedBy(currentUser.getId());
        unitOfMeasureRepository.save(unitOfMeasure);

        // Log to audit
        auditService.logUnitOfMeasureDeleted(
                unitOfMeasure.getId(),
                unitOfMeasure.getCode(),
                currentUser.getId(),
                username);

        logger.info("Successfully deleted (soft) unit of measure ID: {}", id);
    }
}
