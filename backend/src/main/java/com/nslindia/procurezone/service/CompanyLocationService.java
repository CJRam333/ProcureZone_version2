package com.nslindia.procurezone.service;

import com.nslindia.procurezone.dto.CompanyLocationResponse;
import com.nslindia.procurezone.dto.CreateCompanyLocationRequest;
import com.nslindia.procurezone.dto.UpdateCompanyLocationRequest;
import com.nslindia.procurezone.entity.CompanyLocation;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.repository.CompanyLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for managing company-location mappings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyLocationService {

    private final CompanyLocationRepository companyLocationRepository;
    private final com.nslindia.procurezone.audit.AuditService auditService;

    /**
     * Create a new company-location mapping
     */
    @Transactional
    public CompanyLocationResponse create(CreateCompanyLocationRequest request) {
        log.info("Creating company-location mapping: company={}, location={}",
                request.getCompanyId(), request.getLocationId());

        // Check for duplicate
        if (companyLocationRepository.existsByCompanyIdAndLocationId(
                request.getCompanyId(), request.getLocationId())) {
            throw new BadRequestException(
                    "Mapping already exists for company " + request.getCompanyId() +
                            " and location " + request.getLocationId());
        }

        CompanyLocation mapping = new CompanyLocation();
        mapping.setCompanyId(request.getCompanyId());
        mapping.setLocationId(request.getLocationId());
        mapping.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        CompanyLocation saved = companyLocationRepository.save(mapping);
        log.info("Created company-location mapping with ID: {}", saved.getId());

        // Audit logging
        try {
            String details = String.format("Company-Location mapping created: Company ID=%d, Location ID=%d, Status=%d",
                    saved.getCompanyId(), saved.getLocationId(), saved.getStatus());
            auditService.logEntityChange("CREATE", "CompanyLocation", saved.getId(),
                    getCurrentUserId(), "system", details);
        } catch (Exception e) {
            log.warn("Failed to log audit for company-location creation", e);
        }

        return toResponse(saved);
    }

    /**
     * Get all mappings with pagination
     */
    @Transactional(readOnly = true)
    public Page<CompanyLocationResponse> getAllMappings(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return companyLocationRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Get all active mappings
     */
    @Transactional(readOnly = true)
    public Page<CompanyLocationResponse> getAllActiveMappings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return companyLocationRepository.findAllActive(pageable).map(this::toResponse);
    }

    /**
     * Get mapping by ID
     */
    @Transactional(readOnly = true)
    public CompanyLocationResponse getMappingById(Integer id) {
        CompanyLocation mapping = companyLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company-location mapping not found with ID: " + id));
        return toResponse(mapping);
    }

    /**
     * Get locations for a company
     */
    @Transactional(readOnly = true)
    public Page<CompanyLocationResponse> getLocationsByCompany(Integer companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyLocationRepository.findLocationsByCompany(companyId, pageable)
                .map(this::toResponse);
    }

    /**
     * Get companies for a location
     */
    @Transactional(readOnly = true)
    public Page<CompanyLocationResponse> getCompaniesByLocation(Integer locationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyLocationRepository.findCompaniesByLocation(locationId, pageable)
                .map(this::toResponse);
    }

    /**
     * Update mapping
     */
    @Transactional
    public CompanyLocationResponse update(Integer id, UpdateCompanyLocationRequest request) {
        CompanyLocation mapping = companyLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company-location mapping not found with ID: " + id));

        // Capture old values for audit
        String oldValues = String.format("Status=%d", mapping.getStatus());

        if (request.getStatus() != null) {
            mapping.setStatus(request.getStatus());
        }

        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        CompanyLocation updated = companyLocationRepository.save(mapping);
        log.info("Updated company-location mapping ID: {}", id);

        // Audit logging
        try {
            String newValues = String.format("Status=%d", updated.getStatus());
            String details = String.format("Company-Location mapping updated: ID=%d. Before: [%s]. After: [%s]",
                    id, oldValues, newValues);
            auditService.logEntityChange("UPDATE", "CompanyLocation", id,
                    getCurrentUserId(), "system", details);
        } catch (Exception e) {
            log.warn("Failed to log audit for company-location update", e);
        }

        return toResponse(updated);
    }

    /**
     * Delete mapping (soft delete)
     */
    @Transactional
    public void delete(Integer id) {
        CompanyLocation mapping = companyLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company-location mapping not found with ID: " + id));

        String oldValues = String.format("Company ID=%d, Location ID=%d, Status=%d",
                mapping.getCompanyId(), mapping.getLocationId(), mapping.getStatus());

        mapping.deactivate();
        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        companyLocationRepository.save(mapping);
        log.info("Soft deleted company-location mapping ID: {}", id);

        // Audit logging
        try {
            String details = String.format("Company-Location mapping deleted (soft): ID=%d. Values: [%s]",
                    id, oldValues);
            auditService.logEntityChange("DELETE", "CompanyLocation", id,
                    getCurrentUserId(), "system", details);
        } catch (Exception e) {
            log.warn("Failed to log audit for company-location deletion", e);
        }
    }

    /**
     * Batch sync locations for a company (activate requested, deactivate absent)
     */
    @Transactional
    public void syncCompanyLocations(Integer companyId, java.util.List<Integer> locationIds) {
        log.info("Batch syncing locations for company {}. Requested IDs: {}", companyId, locationIds);

        // 1. Fetch existing mappings
        java.util.List<CompanyLocation> existingMappings = companyLocationRepository.findByCompanyId(companyId);
        
        // 2. Index them by locationId
        java.util.Map<Integer, CompanyLocation> existingMap = existingMappings.stream()
                .collect(java.util.stream.Collectors.toMap(CompanyLocation::getLocationId, m -> m));

        int addedCount = 0;
        int deactivatedCount = 0;

        // 3. Process requested IDs
        for (Integer locId : locationIds) {
            CompanyLocation mapping = existingMap.get(locId);
            if (mapping != null) {
                // Exists, ensure it is active
                if (!mapping.isActive()) {
                    mapping.activate();
                    mapping.setLastModifiedDate(LocalDate.now());
                    mapping.setLastModifiedBy(getCurrentUserId());
                    companyLocationRepository.save(mapping);
                    addedCount++;
                }
                existingMap.remove(locId); // Remove from map so we know what's left
            } else {
                // Does not exist, create new
                CompanyLocation newMapping = new CompanyLocation();
                newMapping.setCompanyId(companyId);
                newMapping.setLocationId(locId);
                newMapping.setStatus(1);
                newMapping.setLastModifiedDate(LocalDate.now());
                newMapping.setLastModifiedBy(getCurrentUserId());
                companyLocationRepository.save(newMapping);
                addedCount++;
            }
        }

        // 4. Any remaining in existingMap were NOT in the request, so deactivate them
        for (CompanyLocation mapping : existingMap.values()) {
            if (mapping.isActive()) {
                mapping.deactivate();
                mapping.setLastModifiedDate(LocalDate.now());
                mapping.setLastModifiedBy(getCurrentUserId());
                companyLocationRepository.save(mapping);
                deactivatedCount++;
            }
        }

        log.info("Batch sync completed for company {}. Activated/Created: {}, Deactivated: {}", 
                companyId, addedCount, deactivatedCount);
        
        // Audit log
        try {
            String details = String.format("Company-Location batch sync: Company ID=%d. Activated/Created: %d, Deactivated: %d",
                    companyId, addedCount, deactivatedCount);
            auditService.logEntityChange("UPDATE", "CompanyLocationBatch", companyId,
                    getCurrentUserId(), "system", details);
        } catch (Exception e) {
            log.warn("Failed to log audit for company-location batch sync", e);
        }
    }

    /**
     * Check if location is accessible to company
     */
    @Transactional(readOnly = true)
    public boolean isLocationAccessible(Integer companyId, Integer locationId) {
        return companyLocationRepository.isLocationAccessibleToCompany(companyId, locationId);
    }

    /**
     * Convert entity to response DTO
     */
    private CompanyLocationResponse toResponse(CompanyLocation mapping) {
        CompanyLocationResponse response = new CompanyLocationResponse();
        response.setId(mapping.getId());
        response.setCompanyId(mapping.getCompanyId());
        response.setLocationId(mapping.getLocationId());
        response.setStatus(mapping.getStatus());
        response.setStatusText(mapping.getStatusText());
        response.setLastModifiedDate(mapping.getLastModifiedDate());
        response.setLastModifiedBy(mapping.getLastModifiedBy());
        response.setActive(mapping.isActive());
        return response;
    }

    /**
     * Get current user ID from SecurityContext
     */
    private Integer getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.nslindia.procurezone.security.UserPrincipal) {
                    return ((com.nslindia.procurezone.security.UserPrincipal) principal).employeeNumber();
                }
            }
            return 1; // Fallback for system operations
        } catch (Exception e) {
            return 1; // Fallback on error
        }
    }
}
