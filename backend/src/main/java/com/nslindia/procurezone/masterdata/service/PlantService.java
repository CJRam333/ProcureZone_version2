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
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.PlantRepository;
import com.nslindia.procurezone.masterdata.dto.CreatePlantRequest;
import com.nslindia.procurezone.masterdata.dto.PlantResponse;
import com.nslindia.procurezone.masterdata.dto.UpdatePlantRequest;

/**
 * Service for managing plants.
 */
@Service
@Transactional
public class PlantService {

    private static final Logger logger = LoggerFactory.getLogger(PlantService.class);

    private final PlantRepository plantRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public PlantService(PlantRepository plantRepository,
            EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.plantRepository = plantRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all plants with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PlantResponse> getAllPlants(Pageable pageable) {
        logger.debug("Fetching all plants with pagination: {}", pageable);
        return plantRepository.findAll(pageable).map(PlantResponse::from);
    }

    /**
     * Get only active plants with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PlantResponse> getActivePlants(Pageable pageable) {
        logger.debug("Fetching active plants with pagination: {}", pageable);
        return plantRepository.findByStatus(1, pageable).map(PlantResponse::from);
    }

    /**
     * Search plants by code or name.
     */
    @Transactional(readOnly = true)
    public Page<PlantResponse> searchPlants(String searchTerm, Boolean activeOnly, Pageable pageable) {
        logger.debug("Searching plants with term: {}, activeOnly: {}", searchTerm, activeOnly);

        if (activeOnly != null && activeOnly) {
            return plantRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseAndStatus(
                    searchTerm, searchTerm, 1, pageable).map(PlantResponse::from);
        } else {
            return plantRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                    searchTerm, searchTerm, pageable).map(PlantResponse::from);
        }
    }

    /**
     * Get a plant by ID.
     */
    @Transactional(readOnly = true)
    public PlantResponse getPlantById(Integer id) {
        logger.debug("Fetching plant with id: {}", id);
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + id));
        return PlantResponse.from(plant);
    }

    /**
     * Create a new plant.
     */
    public PlantResponse createPlant(CreatePlantRequest request, String username) {
        logger.info("Creating new plant with code: {} by user: {}", request.code(), username);

        // Validate status
        if (request.status() != 0 && request.status() != 1) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (plantRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Plant with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create new plant
        Plant plant = new Plant();
        plant.setCode(request.code());
        plant.setName(request.name());
        plant.setStatus(request.status());
        plant.setLastModifiedDate(LocalDate.now());
        plant.setLastModifiedBy(currentUser.getEmpNumber());

        // Save plant
        Plant savedPlant = plantRepository.save(plant);
        logger.info("Plant created successfully with id: {}", savedPlant.getId());

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "Plant",
                savedPlant.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Created plant: %s - %s", savedPlant.getCode(), savedPlant.getName()));

        return PlantResponse.from(savedPlant);
    }

    /**
     * Update an existing plant.
     */
    public PlantResponse updatePlant(Integer id, UpdatePlantRequest request, String username) {
        logger.info("Updating plant with id: {} by user: {}", id, username);

        // Validate status
        if (request.status() != 0 && request.status() != 1) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Find existing plant
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store old values for audit
        String oldDetails = String.format("Code: %s, Name: %s, Status: %d",
                plant.getCode(), plant.getName(), plant.getStatus());

        // Update plant (code cannot be changed)
        plant.setName(request.name());
        plant.setStatus(request.status());
        plant.setLastModifiedDate(LocalDate.now());
        plant.setLastModifiedBy(currentUser.getEmpNumber());

        // Save updated plant
        Plant updatedPlant = plantRepository.save(plant);
        logger.info("Plant updated successfully with id: {}", updatedPlant.getId());

        // Audit log
        String newDetails = String.format("Code: %s, Name: %s, Status: %d",
                updatedPlant.getCode(), updatedPlant.getName(), updatedPlant.getStatus());
        auditService.logEntityChange(
                "UPDATE",
                "Plant",
                updatedPlant.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Updated plant from [%s] to [%s]", oldDetails, newDetails));

        return PlantResponse.from(updatedPlant);
    }

    /**
     * Delete a plant by ID.
     */
    public void deletePlant(Integer id, String username) {
        logger.info("Deleting plant with id: {} by user: {}", id, username);

        // Find existing plant
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store values for audit
        String plantDetails = String.format("Code: %s, Name: %s, Status: %d",
                plant.getCode(), plant.getName(), plant.getStatus());

        // Delete plant
        plantRepository.delete(plant);
        logger.info("Plant deleted successfully with id: {}", id);

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "Plant",
                id,
                currentUser.getEmpNumber(),
                username,
                String.format("Deleted plant: %s", plantDetails));
    }
}
