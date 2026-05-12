package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.mapping.dto.CompanyPlantMaterialRequest;
import com.nslindia.procurezone.mapping.dto.CompanyPlantMaterialResponse;
import com.nslindia.procurezone.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Company-Plant-Material Mapping operations
 * Provides endpoints for managing material availability at plants
 */
@RestController
@RequestMapping("/api/v1/plant-materials")
@RequiredArgsConstructor
// CORS handled by WebConfig
public class CompanyPlantMaterialController {

    private final CompanyPlantMaterialService service;

    /** Helper: extract current user id from JWT principal */
    private Integer getUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal up) {
            return up.employeeNumber();
        }
        return 1; // fallback
    }

    @GetMapping
    public ResponseEntity<Page<CompanyPlantMaterialResponse>> getAllMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAllMappings(PageRequest.of(page, size)));
    }

    /**
     * Create a new Company-Plant-Material mapping
     */
    @PostMapping
    public ResponseEntity<CompanyPlantMaterialResponse> createMapping(
            @Valid @RequestBody CompanyPlantMaterialRequest request,
            Authentication authentication) {
        CompanyPlantMaterialResponse response = service.createMapping(request, getUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing Company-Plant-Material mapping
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyPlantMaterialResponse> updateMapping(
            @PathVariable Integer id,
            @Valid @RequestBody CompanyPlantMaterialRequest request,
            Authentication authentication) {
        CompanyPlantMaterialResponse response = service.updateMapping(id, request, getUserId(authentication));
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a Company-Plant-Material mapping
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateMapping(
            @PathVariable Integer id,
            Authentication authentication) {
        service.deactivateMapping(id, getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all active mappings for a specific plant
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<CompanyPlantMaterialResponse>> getMappingsByPlant(
            @PathVariable Integer plantId) {
        List<CompanyPlantMaterialResponse> mappings = service.getActiveMappingsByPlant(plantId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get all active mappings for a specific company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CompanyPlantMaterialResponse>> getMappingsByCompany(
            @PathVariable Integer companyId) {
        List<CompanyPlantMaterialResponse> mappings = service.getActiveMappingsByCompany(companyId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get all materials available at a specific company-plant combination
     */
    @GetMapping("/materials")
    public ResponseEntity<List<CompanyPlantMaterialResponse>> getMaterialsByCompanyAndPlant(
            @RequestParam Integer companyId,
            @RequestParam Integer plantId) {
        List<CompanyPlantMaterialResponse> materials = service.getMaterialsByCompanyAndPlant(companyId, plantId);
        return ResponseEntity.ok(materials);
    }

    /**
     * Check if a material is available at a specific company-plant combination
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkMaterialAvailability(
            @RequestParam Integer companyId,
            @RequestParam Integer plantId,
            @RequestParam Integer materialId) {
        boolean available = service.isMaterialAvailable(companyId, plantId, materialId);
        return ResponseEntity.ok(available);
    }

    /**
     * Get materials that need reordering at a specific plant
     */
    @GetMapping("/reorder")
    public ResponseEntity<List<CompanyPlantMaterialResponse>> getMaterialsForReorder(
            @RequestParam Integer plantId) {
        List<CompanyPlantMaterialResponse> materials = service.getMaterialsForReorder(plantId);
        return ResponseEntity.ok(materials);
    }

    /**
     * Get a specific mapping by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyPlantMaterialResponse> getMappingById(@PathVariable Integer id) {
        CompanyPlantMaterialResponse mapping = service.getMappingById(id);
        return ResponseEntity.ok(mapping);
    }
}
