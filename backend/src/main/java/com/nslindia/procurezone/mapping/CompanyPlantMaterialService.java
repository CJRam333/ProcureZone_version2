package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.mapping.dto.CompanyPlantMaterialRequest;
import com.nslindia.procurezone.mapping.dto.CompanyPlantMaterialResponse;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.PlantRepository;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Company-Plant-Material Mapping operations
 * Handles CRUD and validation for material availability at plants
 * 
 * @author NSL India
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CompanyPlantMaterialService {

    private final CompanyPlantMaterialMapRepository repository;
    private final CompanyRepository companyRepository;
    private final PlantRepository plantRepository;
    private final MaterialRepository materialRepository;

    /**
     * Create a new Company-Plant-Material mapping
     * 
     * @param request Mapping request data
     * @param userId  User ID creating the mapping
     * @return Created mapping response
     */
    @Transactional
    public CompanyPlantMaterialResponse createMapping(CompanyPlantMaterialRequest request, Integer userId) {
        // Validate company exists
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.companyId()));

        // Validate plant exists
        Plant plant = plantRepository.findById(request.plantId())
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found with id: " + request.plantId()));

        // Validate material exists
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Material not found with id: " + request.materialId()));

        // Check if mapping already exists
        repository.findByCompanyAndPlantAndMaterial(
                request.companyId(), request.plantId(), request.materialId()).ifPresent(existing -> {
                    throw new IllegalStateException(
                            String.format("Mapping already exists for Company %d, Plant %d, Material %d",
                                    request.companyId(), request.plantId(), request.materialId()));
                });

        // Create new mapping
        CompanyPlantMaterialMap mapping = CompanyPlantMaterialMap.builder()
                .company(company)
                .plant(plant)
                .material(material)
                .status(1)
                .quantity(request.quantity() != null ? request.quantity() : BigDecimal.ZERO)
                .reorderLevel(request.reorderLevel())
                .maxLevel(request.maxLevel())
                .lastModifiedBy(userId)
                .lastModifiedDate(LocalDate.now())
                .build();

        CompanyPlantMaterialMap saved = repository.save(mapping);
        return CompanyPlantMaterialResponse.from(saved);
    }

    /**
     * Update an existing Company-Plant-Material mapping
     * 
     * @param id      Mapping ID
     * @param request Updated mapping data
     * @param userId  User ID updating the mapping
     * @return Updated mapping response
     */
    @Transactional
    public CompanyPlantMaterialResponse updateMapping(Integer id, CompanyPlantMaterialRequest request, Integer userId) {
        CompanyPlantMaterialMap mapping = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));

        if (request.quantity() != null) {
            mapping.setQuantity(request.quantity());
        }
        if (request.status() != null) {
            mapping.setStatus(request.status());
        }
        if (request.reorderLevel() != null) {
            mapping.setReorderLevel(request.reorderLevel());
        }
        if (request.maxLevel() != null) {
            mapping.setMaxLevel(request.maxLevel());
        }

        mapping.setLastModifiedBy(userId);
        mapping.setLastModifiedDate(LocalDate.now());

        CompanyPlantMaterialMap updated = repository.save(mapping);
        return CompanyPlantMaterialResponse.from(updated);
    }

    /**
     * Deactivate a Company-Plant-Material mapping
     * 
     * @param id     Mapping ID
     * @param userId User ID performing the deactivation
     */
    @Transactional
    public void deactivateMapping(Integer id, Integer userId) {
        CompanyPlantMaterialMap mapping = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));

        mapping.setStatus(0);
        mapping.setLastModifiedBy(userId);
        mapping.setLastModifiedDate(LocalDate.now());
        repository.save(mapping);
    }

    /**
     * Get all active mappings for a specific plant
     * 
     * @param plantId Plant ID
     * @return List of active mappings for the plant
     */
    @Transactional(readOnly = true)
    public List<CompanyPlantMaterialResponse> getActiveMappingsByPlant(Integer plantId) {
        return repository.findActiveByPlant(plantId).stream()
                .map(CompanyPlantMaterialResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all mappings with pagination
     * 
     * @param pageable Pagination information
     * @return Page of mappings
     */
    @Transactional(readOnly = true)
    public Page<CompanyPlantMaterialResponse> getAllMappings(Pageable pageable) {
        return repository.findAll(pageable)
                .map(CompanyPlantMaterialResponse::from);
    }

    /**
     * Get all active mappings for a specific company
     * 
     * @param companyId Company ID
     * @return List of active mappings for the company
     */
    @Transactional(readOnly = true)
    public List<CompanyPlantMaterialResponse> getActiveMappingsByCompany(Integer companyId) {
        return repository.findActiveByCompany(companyId).stream()
                .map(CompanyPlantMaterialResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all materials available at a specific company-plant combination
     * 
     * @param companyId Company ID
     * @param plantId   Plant ID
     * @return List of available materials
     */
    @Transactional(readOnly = true)
    public List<CompanyPlantMaterialResponse> getMaterialsByCompanyAndPlant(Integer companyId, Integer plantId) {
        return repository.findMaterialsByCompanyAndPlant(companyId, plantId).stream()
                .map(CompanyPlantMaterialResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Check if a material is available at a specific company-plant combination
     * CRITICAL: Used by InventoryService for validation
     * 
     * @param companyId  Company ID
     * @param plantId    Plant ID
     * @param materialId Material ID
     * @return true if material is available and active, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isMaterialAvailable(Integer companyId, Integer plantId, Integer materialId) {
        return repository.isMaterialAvailableAtPlant(companyId, plantId, materialId);
    }

    /**
     * Get materials that need reordering (stock below reorder level)
     * 
     * @param plantId Plant ID
     * @return List of materials needing reorder
     */
    @Transactional(readOnly = true)
    public List<CompanyPlantMaterialResponse> getMaterialsForReorder(Integer plantId) {
        return repository.findMaterialsForReorder(plantId).stream()
                .map(CompanyPlantMaterialResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific mapping by ID
     * 
     * @param id Mapping ID
     * @return Mapping response
     */
    @Transactional(readOnly = true)
    public CompanyPlantMaterialResponse getMappingById(Integer id) {
        CompanyPlantMaterialMap mapping = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with id: " + id));
        return CompanyPlantMaterialResponse.from(mapping);
    }
}
