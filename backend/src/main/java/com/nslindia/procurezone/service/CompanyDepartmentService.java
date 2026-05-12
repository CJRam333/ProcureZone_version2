package com.nslindia.procurezone.service;

import com.nslindia.procurezone.dto.CompanyDepartmentResponse;
import com.nslindia.procurezone.dto.CreateCompanyDepartmentRequest;
import com.nslindia.procurezone.dto.UpdateCompanyDepartmentRequest;
import com.nslindia.procurezone.entity.CompanyDepartment;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.repository.CompanyDepartmentRepository;
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
 * Service for managing company-department mappings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyDepartmentService {

    private final CompanyDepartmentRepository companyDepartmentRepository;

    @Transactional
    public CompanyDepartmentResponse create(CreateCompanyDepartmentRequest request) {
        log.info("Creating company-department mapping: company={}, department={}",
                request.getCompanyId(), request.getDepartmentId());

        if (companyDepartmentRepository.existsByCompanyIdAndDepartmentId(
                request.getCompanyId(), request.getDepartmentId())) {
            throw new BadRequestException(
                    "Mapping already exists for company " + request.getCompanyId() +
                            " and department " + request.getDepartmentId());
        }

        CompanyDepartment mapping = new CompanyDepartment();
        mapping.setCompanyId(request.getCompanyId());
        mapping.setDepartmentId(request.getDepartmentId());
        mapping.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        CompanyDepartment saved = companyDepartmentRepository.save(mapping);
        log.info("Created company-department mapping with ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CompanyDepartmentResponse> getAllMappings(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return companyDepartmentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompanyDepartmentResponse> getAllActiveMappings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return companyDepartmentRepository.findAllActive(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CompanyDepartmentResponse getMappingById(Integer id) {
        CompanyDepartment mapping = companyDepartmentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Company-department mapping not found with ID: " + id));
        return toResponse(mapping);
    }

    @Transactional(readOnly = true)
    public Page<CompanyDepartmentResponse> getDepartmentsByCompany(Integer companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyDepartmentRepository.findDepartmentsByCompany(companyId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompanyDepartmentResponse> getCompaniesByDepartment(Integer departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyDepartmentRepository.findCompaniesByDepartment(departmentId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CompanyDepartmentResponse update(Integer id, UpdateCompanyDepartmentRequest request) {
        CompanyDepartment mapping = companyDepartmentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Company-department mapping not found with ID: " + id));

        if (request.getStatus() != null) {
            mapping.setStatus(request.getStatus());
        }

        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        CompanyDepartment updated = companyDepartmentRepository.save(mapping);
        log.info("Updated company-department mapping ID: {}", id);

        return toResponse(updated);
    }

    @Transactional
    public void delete(Integer id) {
        CompanyDepartment mapping = companyDepartmentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Company-department mapping not found with ID: " + id));

        mapping.deactivate();
        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        companyDepartmentRepository.save(mapping);
        log.info("Soft deleted company-department mapping ID: {}", id);
    }

    @Transactional
    public void syncCompanyDepartments(Integer companyId, java.util.List<Integer> departmentIds) {
        log.info("Batch syncing departments for company {}. Requested IDs: {}", companyId, departmentIds);

        java.util.List<CompanyDepartment> existingMappings = companyDepartmentRepository.findByCompanyId(companyId);
        
        java.util.Map<Integer, CompanyDepartment> existingMap = existingMappings.stream()
                .collect(java.util.stream.Collectors.toMap(CompanyDepartment::getDepartmentId, m -> m));

        int addedCount = 0;
        int deactivatedCount = 0;

        for (Integer deptId : departmentIds) {
            CompanyDepartment mapping = existingMap.get(deptId);
            if (mapping != null) {
                if (!mapping.isActive()) {
                    mapping.activate();
                    mapping.setLastModifiedDate(LocalDate.now());
                    mapping.setLastModifiedBy(getCurrentUserId());
                    companyDepartmentRepository.save(mapping);
                    addedCount++;
                }
                existingMap.remove(deptId);
            } else {
                CompanyDepartment newMapping = new CompanyDepartment();
                newMapping.setCompanyId(companyId);
                newMapping.setDepartmentId(deptId);
                newMapping.setStatus(1);
                newMapping.setLastModifiedDate(LocalDate.now());
                newMapping.setLastModifiedBy(getCurrentUserId());
                companyDepartmentRepository.save(newMapping);
                addedCount++;
            }
        }

        for (CompanyDepartment mapping : existingMap.values()) {
            if (mapping.isActive()) {
                mapping.deactivate();
                mapping.setLastModifiedDate(LocalDate.now());
                mapping.setLastModifiedBy(getCurrentUserId());
                companyDepartmentRepository.save(mapping);
                deactivatedCount++;
            }
        }

        log.info("Batch sync completed for company {}. Activated/Created: {}, Deactivated: {}", 
                companyId, addedCount, deactivatedCount);
    }

    @Transactional(readOnly = true)
    public boolean isDepartmentAccessible(Integer companyId, Integer departmentId) {
        return companyDepartmentRepository.isDepartmentAccessibleToCompany(companyId, departmentId);
    }

    private CompanyDepartmentResponse toResponse(CompanyDepartment mapping) {
        CompanyDepartmentResponse response = new CompanyDepartmentResponse();
        response.setId(mapping.getId());
        response.setCompanyId(mapping.getCompanyId());
        response.setDepartmentId(mapping.getDepartmentId());
        response.setStatus(mapping.getStatus());
        response.setStatusText(mapping.getStatusText());
        response.setLastModifiedDate(mapping.getLastModifiedDate());
        response.setLastModifiedBy(mapping.getLastModifiedBy());
        response.setActive(mapping.isActive());
        return response;
    }

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
