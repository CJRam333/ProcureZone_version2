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

import com.nslindia.procurezone.masterdata.dto.SectionResponse;
import com.nslindia.procurezone.masterdata.dto.CreateSectionRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateSectionRequest;
import com.nslindia.procurezone.masterdata.service.SectionService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Section master data management.
 * Provides endpoints for CRUD operations on sections.
 */
@RestController
@RequestMapping("/api/v1/sections")
@Validated
public class SectionController {

    private static final Logger logger = LoggerFactory.getLogger(SectionController.class);

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    /**
     * Get all sections with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of sections
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SectionResponse>> getAllSections(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/sections - Fetching all sections with pagination");
        Page<SectionResponse> sections = sectionService.getAllSections(pageable);
        return ResponseEntity.ok(sections);
    }

    /**
     * Get all active sections with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of active sections
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SectionResponse>> getActiveSections(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/sections/active - Fetching active sections");
        Page<SectionResponse> sections = sectionService.getActiveSections(pageable);
        return ResponseEntity.ok(sections);
    }

    /**
     * Search sections by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active sections only
     * @param pageable   pagination parameters
     * @return page of matching sections
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SectionResponse>> searchSections(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/sections/search - searchTerm: {}, activeOnly: {}", searchTerm, activeOnly);
        Page<SectionResponse> sections = sectionService.searchSections(searchTerm, activeOnly, pageable);
        return ResponseEntity.ok(sections);
    }

    /**
     * Get section by ID.
     * 
     * @param id the section ID
     * @return the section
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SectionResponse> getSectionById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/sections/{} - Fetching section by ID", id);
        SectionResponse section = sectionService.getSectionById(id);
        return ResponseEntity.ok(section);
    }

    /**
     * Create a new section.
     * 
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created section
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<SectionResponse> createSection(
            @Valid @RequestBody CreateSectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/sections - Creating new section: {}", request.code());
        SectionResponse section = sectionService.createSection(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(section);
    }

    /**
     * Update an existing section.
     * 
     * @param id        the section ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated section
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdateSectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/sections/{} - Updating section", id);
        SectionResponse section = sectionService.updateSection(id, request, principal.email());
        return ResponseEntity.ok(section);
    }

    /**
     * Delete a section.
     * 
     * @param id        the section ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deleteSection(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/sections/{} - Deleting section", id);
        sectionService.deleteSection(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
