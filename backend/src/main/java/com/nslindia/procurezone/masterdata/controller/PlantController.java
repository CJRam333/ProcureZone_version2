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

import com.nslindia.procurezone.masterdata.dto.CreatePlantRequest;
import com.nslindia.procurezone.masterdata.dto.PlantResponse;
import com.nslindia.procurezone.masterdata.dto.UpdatePlantRequest;
import com.nslindia.procurezone.masterdata.service.PlantService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Plant master data management.
 */
@RestController
@RequestMapping("/api/v1/plants")
@Validated
public class PlantController {

    private static final Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    /**
     * Get all plants with pagination.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PlantResponse>> getAllPlants(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/plants - Fetching all plants");
        Page<PlantResponse> plants = plantService.getAllPlants(pageable);
        return ResponseEntity.ok(plants);
    }

    /**
     * Get all active plants with pagination.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PlantResponse>> getActivePlants(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/plants/active - Fetching active plants");
        Page<PlantResponse> plants = plantService.getActivePlants(pageable);
        return ResponseEntity.ok(plants);
    }

    /**
     * Search plants by code or name.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PlantResponse>> searchPlants(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/plants/search - searchTerm: {}, activeOnly: {}", searchTerm, activeOnly);
        Page<PlantResponse> plants = plantService.searchPlants(searchTerm, activeOnly, pageable);
        return ResponseEntity.ok(plants);
    }

    /**
     * Get plant by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlantResponse> getPlantById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/plants/{} - Fetching plant by ID", id);
        PlantResponse plant = plantService.getPlantById(id);
        return ResponseEntity.ok(plant);
    }

    /**
     * Create a new plant.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PlantResponse> createPlant(
            @Valid @RequestBody CreatePlantRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/plants - Creating plant with code: {} by user: {}",
                request.code(), principal.email());
        PlantResponse plant = plantService.createPlant(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(plant);
    }

    /**
     * Update an existing plant.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<PlantResponse> updatePlant(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdatePlantRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/plants/{} - Updating plant by user: {}", id, principal.email());
        PlantResponse plant = plantService.updatePlant(id, request, principal.email());
        return ResponseEntity.ok(plant);
    }

    /**
     * Delete a plant.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deletePlant(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/plants/{} - Deleting plant by user: {}", id, principal.email());
        plantService.deletePlant(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
