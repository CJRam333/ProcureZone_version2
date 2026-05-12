package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.mapping.dto.CompanyLocationMaterialRequest;
import com.nslindia.procurezone.mapping.dto.CompanyLocationMaterialResponse;
import com.nslindia.procurezone.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-location-materials")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'STOREKEEPER')")
public class CompanyLocationMaterialController {

    private final CompanyLocationMaterialService service;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<CompanyLocationMaterialResponse>> getAllMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAllMappings(org.springframework.data.domain.PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<CompanyLocationMaterialResponse> createMapping(
            @Valid @RequestBody CompanyLocationMaterialRequest request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createMapping(request, userPrincipal.employeeNumber()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyLocationMaterialResponse> updateMapping(
            @PathVariable Integer id,
            @Valid @RequestBody CompanyLocationMaterialRequest request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(service.updateMapping(id, request, userPrincipal.employeeNumber()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateMapping(
            @PathVariable Integer id,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        service.deactivateMapping(id, userPrincipal.employeeNumber());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<CompanyLocationMaterialResponse>> getMappingsByLocation(
            @PathVariable Integer locationId) {
        return ResponseEntity.ok(service.getActiveMappingsByLocation(locationId));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CompanyLocationMaterialResponse>> getMappingsByCompany(
            @PathVariable Integer companyId) {
        return ResponseEntity.ok(service.getActiveMappingsByCompany(companyId));
    }

    @GetMapping("/materials")
    public ResponseEntity<List<CompanyLocationMaterialResponse>> getMaterialsByCompanyAndLocation(
            @RequestParam Integer companyId,
            @RequestParam Integer locationId) {
        return ResponseEntity.ok(service.getMaterialsByCompanyAndLocation(companyId, locationId));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkMaterialAvailability(
            @RequestParam Integer companyId,
            @RequestParam Integer locationId,
            @RequestParam Integer materialId) {
        return ResponseEntity.ok(service.isMaterialAvailable(companyId, locationId, materialId));
    }

    @GetMapping("/reorder")
    public ResponseEntity<List<CompanyLocationMaterialResponse>> getMaterialsForReorder(
            @RequestParam(required = false) Integer companyId,
            @RequestParam Integer locationId) {
        return ResponseEntity.ok(service.getMaterialsForReorder(locationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyLocationMaterialResponse> getMappingById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getMappingById(id));
    }
}
