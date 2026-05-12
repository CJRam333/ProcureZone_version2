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

import com.nslindia.procurezone.masterdata.dto.MaterialResponse;
import com.nslindia.procurezone.masterdata.dto.CreateMaterialRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateMaterialRequest;
import com.nslindia.procurezone.masterdata.service.MaterialService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Material master data management.
 * Provides endpoints for CRUD operations on materials with advanced search
 * capabilities.
 */
@RestController
@RequestMapping("/api/v1/materials")
@Validated
public class MaterialController {

    private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * Get all materials with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of materials
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MaterialResponse>> getAllMaterials(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/materials - Fetching all materials with pagination");
        Page<MaterialResponse> materials = materialService.getAllMaterials(pageable);
        return ResponseEntity.ok(materials);
    }

    /**
     * Get all active materials with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of active materials
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MaterialResponse>> getActiveMaterials(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/materials/active - Fetching active materials");
        Page<MaterialResponse> materials = materialService.getActiveMaterials(pageable);
        return ResponseEntity.ok(materials);
    }

    /**
     * Search materials by code, name, or description.
     * This endpoint provides comprehensive search across all material fields.
     * 
     * @param searchTerm the search term (searches in code, name, and description)
     * @param activeOnly filter for active materials only
     * @param pageable   pagination parameters
     * @return page of matching materials
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MaterialResponse>> searchMaterials(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/materials/search - searchTerm: {}, activeOnly: {}", searchTerm, activeOnly);
        Page<MaterialResponse> materials = materialService.searchMaterials(searchTerm, activeOnly, pageable);
        return ResponseEntity.ok(materials);
    }

    /**
     * Get material by ID.
     * 
     * @param id the material ID
     * @return the material
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/materials/{} - Fetching material by ID", id);
        MaterialResponse material = materialService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }

    /**
     * Create a new material.
     * 
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created material
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<MaterialResponse> createMaterial(
            @Valid @RequestBody CreateMaterialRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/materials - Creating material with code: {}", request.code());
        MaterialResponse material = materialService.createMaterial(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(material);
    }

    /**
     * Update an existing material.
     * 
     * @param id        the material ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated material
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdateMaterialRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/materials/{} - Updating material", id);
        MaterialResponse material = materialService.updateMaterial(id, request, principal.email());
        return ResponseEntity.ok(material);
    }

    /**
     * Delete a material by ID.
     * 
     * @param id        the material ID
     * @param principal the authenticated user
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/materials/{} - Deleting material", id);
        materialService.deleteMaterial(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
