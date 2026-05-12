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

import com.nslindia.procurezone.masterdata.dto.CreateLocationRequest;
import com.nslindia.procurezone.masterdata.dto.LocationResponse;
import com.nslindia.procurezone.masterdata.dto.UpdateLocationRequest;
import com.nslindia.procurezone.masterdata.service.LocationService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Location master data management.
 */
@RestController
@RequestMapping("/api/v1/locations")
@Validated
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Get all locations with pagination.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LocationResponse>> getAllLocations(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/locations - Fetching all locations");
        Page<LocationResponse> locations = locationService.getAllLocations(pageable);
        return ResponseEntity.ok(locations);
    }

    /**
     * Get all active locations with pagination.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LocationResponse>> getActiveLocations(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/locations/active - Fetching active locations");
        Page<LocationResponse> locations = locationService.getActiveLocations(pageable);
        return ResponseEntity.ok(locations);
    }

    /**
     * Search locations by code or name.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LocationResponse>> searchLocations(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/locations/search - searchTerm: {}, activeOnly: {}", searchTerm, activeOnly);
        Page<LocationResponse> locations = locationService.searchLocations(searchTerm, activeOnly, pageable);
        return ResponseEntity.ok(locations);
    }

    /**
     * Get location by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/locations/{} - Fetching location by ID", id);
        LocationResponse location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    /**
     * Create a new location.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/locations - Creating location with code: {} by user: {}",
                request.code(), principal.email());
        LocationResponse location = locationService.createLocation(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    /**
     * Update an existing location.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/locations/{} - Updating location by user: {}", id, principal.email());
        LocationResponse location = locationService.updateLocation(id, request, principal.email());
        return ResponseEntity.ok(location);
    }

    /**
     * Delete a location.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/locations/{} - Deleting location by user: {}", id, principal.email());
        locationService.deleteLocation(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
