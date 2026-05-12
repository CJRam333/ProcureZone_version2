package com.nslindia.procurezone.vendor;

import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.vendor.dto.CreateVendorRequest;
import com.nslindia.procurezone.vendor.dto.UpdateVendorRequest;
import com.nslindia.procurezone.vendor.dto.VendorPerformanceResponse;
import com.nslindia.procurezone.vendor.dto.VendorResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for vendor management operations.
 */
@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * Creates a new vendor.
     * Endpoint: POST /api/v1/vendors
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> createVendor(
            @Valid @RequestBody CreateVendorRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        VendorResponse vendor = vendorService.createVendor(request, currentEmpId);

        return ResponseEntity.status(HttpStatus.CREATED).body(vendor);
    }

    /**
     * Lists all vendors with optional status filter and pagination.
     * Endpoint: GET /api/v1/vendors?status={status}&page={page}&size={size}
     * Roles: All authenticated users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Page<VendorResponse>> listVendors(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VendorResponse> vendors = vendorService.listVendors(status, page, size);

        return ResponseEntity.ok(vendors);
    }

    /**
     * Gets vendor details by ID.
     * Endpoint: GET /api/v1/vendors/{id}
     * Roles: All authenticated users
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Integer id) {

        VendorResponse vendor = vendorService.getVendorById(id);

        return ResponseEntity.ok(vendor);
    }

    /**
     * Updates an existing vendor.
     * Endpoint: PUT /api/v1/vendors/{id}
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateVendorRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        VendorResponse vendor = vendorService.updateVendor(id, request, currentEmpId);

        return ResponseEntity.ok(vendor);
    }

    /**
     * Soft deletes a vendor.
     * Endpoint: DELETE /api/v1/vendors/{id}
     * Roles: ADMIN, SUPERADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deleteVendor(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        vendorService.deleteVendor(id, currentEmpId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all active vendors (status = 1).
     * Endpoint: GET /api/v1/vendors/active
     * Roles: All authenticated users
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Page<VendorResponse>> listActiveVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VendorResponse> vendors = vendorService.listActiveVendors(page, size);

        return ResponseEntity.ok(vendors);
    }

    /**
     * Searches vendors by keyword (name, code, contact, city).
     * Endpoint: GET
     * /api/v1/vendors/search?keyword={keyword}&page={page}&size={size}
     * Roles: All authenticated users
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<Page<VendorResponse>> searchVendors(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VendorResponse> vendors = vendorService.searchVendors(keyword, page, size);

        return ResponseEntity.ok(vendors);
    }

    /**
     * Updates vendor rating.
     * Endpoint: PUT /api/v1/vendors/{id}/rating
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @PutMapping("/{id}/rating")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> updateVendorRating(
            @PathVariable Integer id,
            @RequestParam Double rating,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        VendorResponse vendor = vendorService.updateVendorRating(id, rating, currentEmpId);

        return ResponseEntity.ok(vendor);
    }

    /**
     * Gets vendor performance metrics.
     * Endpoint: GET /api/v1/vendors/{id}/performance
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @GetMapping("/{id}/performance")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN', 'AUDITOR')")
    public ResponseEntity<VendorPerformanceResponse> getVendorPerformance(
            @PathVariable Integer id) {

        VendorPerformanceResponse performance = vendorService.getVendorPerformance(id);

        return ResponseEntity.ok(performance);
    }
}
