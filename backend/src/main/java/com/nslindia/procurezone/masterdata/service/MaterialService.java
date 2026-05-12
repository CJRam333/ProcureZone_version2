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
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.dto.MaterialResponse;
import com.nslindia.procurezone.masterdata.dto.CreateMaterialRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateMaterialRequest;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;

/**
 * Service class for Material master data management.
 * Handles business logic, validation, and audit logging for material
 * operations.
 */
@Service
@Transactional
public class MaterialService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialService.class);

    private final MaterialRepository materialRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public MaterialService(MaterialRepository materialRepository, EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.materialRepository = materialRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all materials with pagination.
     * 
     * @param pageable pagination information
     * @return page of material responses
     */
    @Transactional(readOnly = true)
    public Page<MaterialResponse> getAllMaterials(Pageable pageable) {
        logger.info("Fetching materials with pagination: {}", pageable);
        return materialRepository.findAll(pageable)
                .map(MaterialResponse::from);
    }

    /**
     * Get all active materials with pagination.
     * 
     * @param pageable pagination information
     * @return page of active material responses
     */
    @Transactional(readOnly = true)
    public Page<MaterialResponse> getActiveMaterials(Pageable pageable) {
        logger.info("Fetching active materials with pagination: {}", pageable);
        return materialRepository.findByStatus(1, pageable)
                .map(MaterialResponse::from);
    }

    /**
     * Search materials by code, name, or description.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active materials only
     * @param pageable   pagination information
     * @return page of matching material responses
     */
    @Transactional(readOnly = true)
    public Page<MaterialResponse> searchMaterials(String searchTerm, Boolean activeOnly, Pageable pageable) {
        logger.info("Searching materials with term: '{}', activeOnly: {}", searchTerm, activeOnly);

        if (searchTerm == null || searchTerm.isBlank()) {
            return activeOnly != null && activeOnly
                    ? getActiveMaterials(pageable)
                    : getAllMaterials(pageable);
        }

        Page<Material> materials = activeOnly != null && activeOnly
                ? materialRepository.searchByCodeOrNameOrDescriptionAndStatus(searchTerm, 1, pageable)
                : materialRepository.searchByCodeOrNameOrDescription(searchTerm, pageable);

        return materials.map(MaterialResponse::from);
    }

    /**
     * Get material by ID.
     * 
     * @param id the material ID
     * @return the material response
     * @throws ResourceNotFoundException if material not found
     */
    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(Integer id) {
        logger.info("Fetching material with id: {}", id);
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
        return MaterialResponse.from(material);
    }

    /**
     * Create a new material.
     * 
     * @param request  the create request
     * @param username the username of the user creating the material
     * @return the created material response
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if material code already exists
     */
    public MaterialResponse createMaterial(CreateMaterialRequest request, String username) {
        logger.info("Creating new material with code: {} by user: {}", request.code(), username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (materialRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Material with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create material entity
        Material material = new Material();
        material.setCode(request.code());
        material.setName(request.name());
        material.setDescription(request.description());
        material.setStatus(request.status());
        material.setLastModifiedDate(LocalDate.now());
        material.setLastModifiedBy(currentUser.getEmpNumber());

        // Save material
        Material savedMaterial = materialRepository.save(material);
        logger.info("Material created successfully with id: {}", savedMaterial.getId());

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "Material",
                savedMaterial.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Created material: %s - %s", savedMaterial.getCode(), savedMaterial.getName()));

        return MaterialResponse.from(savedMaterial);
    }

    /**
     * Update an existing material.
     * 
     * @param id       the material ID
     * @param request  the update request
     * @param username the username of the user updating the material
     * @return the updated material response
     * @throws ResourceNotFoundException  if material not found
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if material code already exists for
     *                                    different material
     */
    public MaterialResponse updateMaterial(Integer id, UpdateMaterialRequest request, String username) {
        logger.info("Updating material with id: {} by user: {}", id, username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Find existing material
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));

        // Check for duplicate code (excluding current material)
        if (materialRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Material with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store old values for audit
        String oldDetails = String.format("Code: %s, Name: %s, Status: %d",
                material.getCode(), material.getName(), material.getStatus());

        // Update material
        material.setCode(request.code());
        material.setName(request.name());
        material.setDescription(request.description());
        material.setStatus(request.status());
        material.setLastModifiedDate(LocalDate.now());
        material.setLastModifiedBy(currentUser.getEmpNumber());

        // Save updated material
        Material updatedMaterial = materialRepository.save(material);
        logger.info("Material updated successfully with id: {}", updatedMaterial.getId());

        // Audit log
        String newDetails = String.format("Code: %s, Name: %s, Status: %d",
                updatedMaterial.getCode(), updatedMaterial.getName(), updatedMaterial.getStatus());
        auditService.logEntityChange(
                "UPDATE",
                "Material",
                updatedMaterial.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Updated material from [%s] to [%s]", oldDetails, newDetails));

        return MaterialResponse.from(updatedMaterial);
    }

    /**
     * Delete a material by ID.
     * Note: This is a hard delete. Consider implementing soft delete if needed.
     * 
     * @param id       the material ID
     * @param username the username of the user deleting the material
     * @throws ResourceNotFoundException if material not found
     */
    public void deleteMaterial(Integer id, String username) {
        logger.info("Deleting material with id: {} by user: {}", id, username);

        // Find existing material
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store values for audit
        String materialDetails = String.format("Code: %s, Name: %s, Status: %d",
                material.getCode(), material.getName(), material.getStatus());

        // Delete material
        materialRepository.delete(material);
        logger.info("Material deleted successfully with id: {}", id);

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "Material",
                id,
                currentUser.getEmpNumber(),
                username,
                String.format("Deleted material: %s", materialDetails));
    }
}
