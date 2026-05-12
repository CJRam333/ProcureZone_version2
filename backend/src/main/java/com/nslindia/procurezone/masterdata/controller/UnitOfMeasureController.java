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

import com.nslindia.procurezone.masterdata.dto.CreateUnitOfMeasureRequest;
import com.nslindia.procurezone.masterdata.dto.UnitOfMeasureResponse;
import com.nslindia.procurezone.masterdata.dto.UpdateUnitOfMeasureRequest;
import com.nslindia.procurezone.masterdata.service.UnitOfMeasureService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Unit of Measure master data management.
 * Provides endpoints for CRUD operations on units of measure.
 */
@RestController
@RequestMapping("/api/v1/unit-of-measures")
@Validated
public class UnitOfMeasureController {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasureController.class);

    private final UnitOfMeasureService unitOfMeasureService;

    public UnitOfMeasureController(UnitOfMeasureService unitOfMeasureService) {
        this.unitOfMeasureService = unitOfMeasureService;
    }

    /**
     * Get all units of measure with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of units of measure
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UnitOfMeasureResponse>> getAllUnitsOfMeasure(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/unit-of-measures - Fetching all units of measure with pagination");
        Page<UnitOfMeasureResponse> units = unitOfMeasureService.getAllUnitsOfMeasure(pageable);
        return ResponseEntity.ok(units);
    }

    /**
     * Get all active units of measure with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of active units of measure
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UnitOfMeasureResponse>> getActiveUnitsOfMeasure(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/unit-of-measures/active - Fetching active units of measure");
        Page<UnitOfMeasureResponse> units = unitOfMeasureService.getActiveUnitsOfMeasure(pageable);
        return ResponseEntity.ok(units);
    }

    /**
     * Search units of measure by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active units only (default: false)
     * @param pageable   pagination parameters
     * @return page of matching units of measure
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UnitOfMeasureResponse>> searchUnitsOfMeasure(
            @RequestParam String searchTerm,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/unit-of-measures/search - Searching units with term: '{}', activeOnly: {}",
                searchTerm, activeOnly);
        Page<UnitOfMeasureResponse> units = unitOfMeasureService.searchUnitsOfMeasure(searchTerm, activeOnly,
                pageable);
        return ResponseEntity.ok(units);
    }

    /**
     * Get a unit of measure by ID.
     * 
     * @param id the unit of measure ID
     * @return the unit of measure response
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnitOfMeasureResponse> getUnitOfMeasureById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/unit-of-measures/{} - Fetching unit by id", id);
        UnitOfMeasureResponse unit = unitOfMeasureService.getUnitOfMeasureById(id);
        return ResponseEntity.ok(unit);
    }

    /**
     * Create a new unit of measure.
     * 
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created unit of measure response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<UnitOfMeasureResponse> createUnitOfMeasure(
            @Valid @RequestBody CreateUnitOfMeasureRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/unit-of-measures - Creating unit with code: {}", request.code());
        UnitOfMeasureResponse created = unitOfMeasureService.createUnitOfMeasure(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing unit of measure.
     * 
     * @param id        the unit of measure ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated unit of measure response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<UnitOfMeasureResponse> updateUnitOfMeasure(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdateUnitOfMeasureRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/unit-of-measures/{} - Updating unit", id);
        UnitOfMeasureResponse updated = unitOfMeasureService.updateUnitOfMeasure(id, request, principal.email());
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a unit of measure (soft delete).
     * 
     * @param id        the unit of measure ID
     * @param principal the authenticated user
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteUnitOfMeasure(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/unit-of-measures/{} - Deleting unit", id);
        unitOfMeasureService.deleteUnitOfMeasure(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
