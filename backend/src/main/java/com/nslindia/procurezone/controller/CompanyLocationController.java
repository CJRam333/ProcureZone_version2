package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.dto.CompanyLocationResponse;
import com.nslindia.procurezone.dto.CreateCompanyLocationRequest;
import com.nslindia.procurezone.dto.UpdateCompanyLocationRequest;
import com.nslindia.procurezone.dto.BatchMappingRequest;
import com.nslindia.procurezone.service.CompanyLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for company-location mapping operations
 */
@RestController
@RequestMapping("/api/v1/company-locations")
@RequiredArgsConstructor
public class CompanyLocationController {

    private final CompanyLocationService companyLocationService;

    /**
     * Create a new company-location mapping
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> createMapping(
            @Valid @RequestBody CreateCompanyLocationRequest request) {
        CompanyLocationResponse response = companyLocationService.create(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-location mapping created successfully");
        result.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Get all company-location mappings with pagination
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyLocationResponse>> getAllMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<CompanyLocationResponse> mappings = companyLocationService.getAllMappings(page, size, sortBy, sortDir);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get all active company-location mappings
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyLocationResponse>> getAllActiveMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyLocationResponse> mappings = companyLocationService.getAllActiveMappings(page, size);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get mapping by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompanyLocationResponse> getMappingById(@PathVariable Integer id) {
        CompanyLocationResponse mapping = companyLocationService.getMappingById(id);
        return ResponseEntity.ok(mapping);
    }

    /**
     * Get locations for a company
     */
    @GetMapping("/company/{companyId}/locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyLocationResponse>> getLocationsByCompany(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyLocationResponse> locations = companyLocationService.getLocationsByCompany(companyId, page, size);
        return ResponseEntity.ok(locations);
    }

    /**
     * Get companies for a location
     */
    @GetMapping("/location/{locationId}/companies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyLocationResponse>> getCompaniesByLocation(
            @PathVariable Integer locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyLocationResponse> companies = companyLocationService.getCompaniesByLocation(locationId, page, size);
        return ResponseEntity.ok(companies);
    }

    /**
     * Update mapping
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateMapping(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCompanyLocationRequest request) {
        CompanyLocationResponse response = companyLocationService.update(id, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-location mapping updated successfully");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    /**
     * Delete mapping (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteMapping(@PathVariable Integer id) {
        companyLocationService.delete(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-location mapping deleted successfully");

        return ResponseEntity.ok(result);
    }

    /**
     * Batch save/sync company locations
     */
    @PostMapping("/company/{companyId}/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> syncCompanyLocations(
            @PathVariable Integer companyId,
            @Valid @RequestBody BatchMappingRequest request) {
        
        companyLocationService.syncCompanyLocations(companyId, request.getLocationIds());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-location mappings synced successfully");

        return ResponseEntity.ok(result);
    }

    /**
     * Check if location is accessible to company
     */
    @GetMapping("/check-access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkLocationAccess(
            @RequestParam Integer companyId,
            @RequestParam Integer locationId) {
        boolean accessible = companyLocationService.isLocationAccessible(companyId, locationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessible", accessible);
        result.put("message",
                accessible ? "Location is accessible to company" : "Location is not accessible to company");

        return ResponseEntity.ok(result);
    }
}
