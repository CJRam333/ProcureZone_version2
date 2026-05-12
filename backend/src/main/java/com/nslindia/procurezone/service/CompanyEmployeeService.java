package com.nslindia.procurezone.service;

import com.nslindia.procurezone.dto.CompanyEmployeeResponse;
import com.nslindia.procurezone.dto.CreateCompanyEmployeeRequest;
import com.nslindia.procurezone.dto.UpdateCompanyEmployeeRequest;
import com.nslindia.procurezone.entity.CompanyEmployee;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.repository.CompanyEmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyEmployeeService {

    private final CompanyEmployeeRepository companyEmployeeRepository;

    @Transactional
    public CompanyEmployeeResponse create(CreateCompanyEmployeeRequest request) {
        log.info("Creating company-employee mapping: company={}, employee={}",
                request.getCompanyId(), request.getEmployeeNumber());

        if (companyEmployeeRepository.existsByCompanyIdAndEmployeeNumber(
                request.getCompanyId(), request.getEmployeeNumber())) {
            throw new BadRequestException(
                    "Mapping already exists for company " + request.getCompanyId() +
                            " and employee " + request.getEmployeeNumber());
        }

        CompanyEmployee mapping = new CompanyEmployee();
        mapping.setCompanyId(request.getCompanyId());
        mapping.setEmployeeNumber(request.getEmployeeNumber());
        mapping.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        CompanyEmployee saved = companyEmployeeRepository.save(mapping);
        log.info("Created company-employee mapping with ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CompanyEmployeeResponse> getAllMappings(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return companyEmployeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompanyEmployeeResponse> getAllActiveMappings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return companyEmployeeRepository.findAllActive(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CompanyEmployeeResponse getMappingById(Integer id) {
        CompanyEmployee mapping = companyEmployeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company-employee mapping not found with ID: " + id));
        return toResponse(mapping);
    }

    @Transactional(readOnly = true)
    public Page<CompanyEmployeeResponse> getEmployeesByCompany(Integer companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyEmployeeRepository.findEmployeesByCompany(companyId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompanyEmployeeResponse> getCompaniesByEmployee(Integer employeeNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyEmployeeRepository.findCompaniesByEmployee(employeeNumber, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Integer> getActiveEmployeeNumbers(Integer companyId) {
        return companyEmployeeRepository.findActiveEmployeeNumbersByCompany(companyId);
    }

    @Transactional(readOnly = true)
    public long countActiveEmployees(Integer companyId) {
        return companyEmployeeRepository.countActiveEmployeesByCompany(companyId);
    }

    @Transactional
    public CompanyEmployeeResponse update(Integer id, UpdateCompanyEmployeeRequest request) {
        CompanyEmployee mapping = companyEmployeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company-employee mapping not found with ID: " + id));

        if (request.getStatus() != null) {
            mapping.setStatus(request.getStatus());
        }

        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        CompanyEmployee updated = companyEmployeeRepository.save(mapping);
        log.info("Updated company-employee mapping ID: {}", id);

        return toResponse(updated);
    }

    @Transactional
    public void delete(Integer id) {
        CompanyEmployee mapping = companyEmployeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company-employee mapping not found with ID: " + id));

        mapping.deactivate();
        mapping.setLastModifiedDate(LocalDate.now());
        mapping.setLastModifiedBy(getCurrentUserId());

        companyEmployeeRepository.save(mapping);
        log.info("Soft deleted company-employee mapping ID: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean isEmployeeAssigned(Integer companyId, Integer employeeNumber) {
        return companyEmployeeRepository.isEmployeeAssignedToCompany(companyId, employeeNumber);
    }

    private CompanyEmployeeResponse toResponse(CompanyEmployee mapping) {
        CompanyEmployeeResponse response = new CompanyEmployeeResponse();
        response.setId(mapping.getId());
        response.setCompanyId(mapping.getCompanyId());
        response.setEmployeeNumber(mapping.getEmployeeNumber());
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
