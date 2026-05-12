package com.nslindia.procurezone.masterdata.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.nslindia.procurezone.masterdata.dto.CompanyResponse;
import com.nslindia.procurezone.masterdata.dto.CreateCompanyRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateCompanyRequest;
import com.nslindia.procurezone.masterdata.service.CompanyService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Company master data management.
 * Provides endpoints for CRUD operations on companies.
 */
@RestController
@RequestMapping("/api/v1/companies")
@Validated
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Get all companies with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of companies
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/companies - Fetching all companies with pagination");
        Page<CompanyResponse> companies = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * Get all active companies with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of active companies
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyResponse>> getActiveCompanies(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/companies/active - Fetching active companies");
        Page<CompanyResponse> companies = companyService.getActiveCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * Search companies by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active companies only
     * @param pageable   pagination parameters
     * @return page of matching companies
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyResponse>> searchCompanies(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/companies/search - searchTerm: {}, activeOnly: {}", searchTerm, activeOnly);
        Page<CompanyResponse> companies = companyService.searchCompanies(searchTerm, activeOnly, pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * Get company by ID.
     * 
     * @param id the company ID
     * @return the company
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/companies/{} - Fetching company by ID", id);
        CompanyResponse company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    /**
     * Create a new company.
     * 
     * @param request   the company creation request
     * @param principal the authenticated user
     * @return the created company
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/companies - Creating company with code: {} by user: {} with authorities: {}",
                request.code(), principal.email(), principal.authorities());
        CompanyResponse company = companyService.createCompany(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    /**
     * Update an existing company.
     * 
     * @param id        the company ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated company
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdateCompanyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/companies/{} - Updating company", id);
        CompanyResponse company = companyService.updateCompany(id, request, principal.email());
        return ResponseEntity.ok(company);
    }

    /**
     * Delete a company by ID.
     * 
     * @param id        the company ID
     * @param principal the authenticated user
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/companies/{} - Deleting company", id);
        companyService.deleteCompany(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
