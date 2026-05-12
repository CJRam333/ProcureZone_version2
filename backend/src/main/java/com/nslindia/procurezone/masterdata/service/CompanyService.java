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
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.dto.CompanyResponse;
import com.nslindia.procurezone.masterdata.dto.CreateCompanyRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateCompanyRequest;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;

/**
 * Service class for Company master data management.
 * Handles business logic, validation, and audit logging for company operations.
 */
@Service
@Transactional
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public CompanyService(CompanyRepository companyRepository, EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all companies with pagination.
     * 
     * @param pageable pagination information
     * @return page of company responses
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        logger.info("Fetching companies with pagination: {}", pageable);
        return companyRepository.findAll(pageable)
                .map(CompanyResponse::from);
    }

    /**
     * Get all active companies with pagination.
     * 
     * @param pageable pagination information
     * @return page of active company responses
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getActiveCompanies(Pageable pageable) {
        logger.info("Fetching active companies with pagination: {}", pageable);
        return companyRepository.findByStatus(1, pageable)
                .map(CompanyResponse::from);
    }

    /**
     * Search companies by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active companies only
     * @param pageable   pagination information
     * @return page of matching company responses
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchCompanies(String searchTerm, Boolean activeOnly, Pageable pageable) {
        logger.info("Searching companies with term: '{}', activeOnly: {}", searchTerm, activeOnly);

        if (searchTerm == null || searchTerm.isBlank()) {
            return activeOnly != null && activeOnly
                    ? getActiveCompanies(pageable)
                    : getAllCompanies(pageable);
        }

        Page<Company> companies = activeOnly != null && activeOnly
                ? companyRepository.searchByCodeOrNameAndStatus(searchTerm, 1, pageable)
                : companyRepository.searchByCodeOrName(searchTerm, pageable);

        return companies.map(CompanyResponse::from);
    }

    /**
     * Get company by ID.
     * 
     * @param id the company ID
     * @return the company response
     * @throws ResourceNotFoundException if company not found
     */
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Integer id) {
        logger.info("Fetching company with id: {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return CompanyResponse.from(company);
    }

    /**
     * Create a new company.
     * 
     * @param request  the create request
     * @param username the username of the user creating the company
     * @return the created company response
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if company code already exists
     */
    public CompanyResponse createCompany(CreateCompanyRequest request, String username) {
        logger.info("Creating new company with code: {} by user: {}", request.code(), username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (companyRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Company with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create company entity
        Company company = new Company();
        company.setCode(request.code());
        company.setName(request.name());
        company.setStatus(request.status());
        company.setLastModifiedDate(LocalDate.now());
        company.setLastModifiedBy(currentUser.getEmpNumber());

        // Save company
        Company savedCompany = companyRepository.save(company);
        logger.info("Company created successfully with id: {}", savedCompany.getId());

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "Company",
                savedCompany.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Created company: %s - %s", savedCompany.getCode(), savedCompany.getName()));

        return CompanyResponse.from(savedCompany);
    }

    /**
     * Update an existing company.
     * 
     * @param id       the company ID
     * @param request  the update request
     * @param username the username of the user updating the company
     * @return the updated company response
     * @throws ResourceNotFoundException  if company not found
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if company code already exists for
     *                                    different company
     */
    public CompanyResponse updateCompany(Integer id, UpdateCompanyRequest request, String username) {
        logger.info("Updating company with id: {} by user: {}", id, username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Find existing company
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        // Check for duplicate code (excluding current company)
        if (companyRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Company with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store old values for audit
        String oldDetails = String.format("Code: %s, Name: %s, Status: %d",
                company.getCode(), company.getName(), company.getStatus());

        // Update company
        company.setCode(request.code());
        company.setName(request.name());
        company.setStatus(request.status());
        company.setLastModifiedDate(LocalDate.now());
        company.setLastModifiedBy(currentUser.getEmpNumber());

        // Save updated company
        Company updatedCompany = companyRepository.save(company);
        logger.info("Company updated successfully with id: {}", updatedCompany.getId());

        // Audit log
        String newDetails = String.format("Code: %s, Name: %s, Status: %d",
                updatedCompany.getCode(), updatedCompany.getName(), updatedCompany.getStatus());
        auditService.logEntityChange(
                "UPDATE",
                "Company",
                updatedCompany.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Updated company from [%s] to [%s]", oldDetails, newDetails));

        return CompanyResponse.from(updatedCompany);
    }

    /**
     * Delete a company by ID.
     * Note: This is a hard delete. Consider implementing soft delete if needed.
     * 
     * @param id       the company ID
     * @param username the username of the user deleting the company
     * @throws ResourceNotFoundException if company not found
     */
    public void deleteCompany(Integer id, String username) {
        logger.info("Deleting company with id: {} by user: {}", id, username);

        // Find existing company
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store values for audit
        String companyDetails = String.format("Code: %s, Name: %s, Status: %d",
                company.getCode(), company.getName(), company.getStatus());

        // Delete company
        companyRepository.delete(company);
        logger.info("Company deleted successfully with id: {}", id);

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "Company",
                id,
                currentUser.getEmpNumber(),
                username,
                String.format("Deleted company: %s", companyDetails));
    }
}
