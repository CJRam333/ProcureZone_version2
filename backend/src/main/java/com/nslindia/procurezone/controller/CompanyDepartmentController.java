package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.dto.CompanyDepartmentResponse;
import com.nslindia.procurezone.dto.CreateCompanyDepartmentRequest;
import com.nslindia.procurezone.dto.UpdateCompanyDepartmentRequest;
import com.nslindia.procurezone.dto.BatchDeptMappingRequest;
import com.nslindia.procurezone.service.CompanyDepartmentService;
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
 * REST Controller for company-department mapping operations
 */
@RestController
@RequestMapping("/api/v1/company-departments")
@RequiredArgsConstructor
public class CompanyDepartmentController {

    private final CompanyDepartmentService companyDepartmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> createMapping(
            @Valid @RequestBody CreateCompanyDepartmentRequest request) {
        CompanyDepartmentResponse response = companyDepartmentService.create(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-department mapping created successfully");
        result.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyDepartmentResponse>> getAllMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<CompanyDepartmentResponse> mappings = companyDepartmentService.getAllMappings(page, size, sortBy, sortDir);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyDepartmentResponse>> getAllActiveMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyDepartmentResponse> mappings = companyDepartmentService.getAllActiveMappings(page, size);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompanyDepartmentResponse> getMappingById(@PathVariable Integer id) {
        CompanyDepartmentResponse mapping = companyDepartmentService.getMappingById(id);
        return ResponseEntity.ok(mapping);
    }

    @GetMapping("/company/{companyId}/departments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyDepartmentResponse>> getDepartmentsByCompany(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyDepartmentResponse> departments = companyDepartmentService.getDepartmentsByCompany(companyId, page,
                size);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/department/{departmentId}/companies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyDepartmentResponse>> getCompaniesByDepartment(
            @PathVariable Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyDepartmentResponse> companies = companyDepartmentService.getCompaniesByDepartment(departmentId,
                page, size);
        return ResponseEntity.ok(companies);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateMapping(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCompanyDepartmentRequest request) {
        CompanyDepartmentResponse response = companyDepartmentService.update(id, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-department mapping updated successfully");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteMapping(@PathVariable Integer id) {
        companyDepartmentService.delete(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-department mapping deleted successfully");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/company/{companyId}/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> syncCompanyDepartments(
            @PathVariable Integer companyId,
            @Valid @RequestBody BatchDeptMappingRequest request) {

        companyDepartmentService.syncCompanyDepartments(companyId, request.getDepartmentIds());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-department mappings synced successfully");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkDepartmentAccess(
            @RequestParam Integer companyId,
            @RequestParam Integer departmentId) {
        boolean accessible = companyDepartmentService.isDepartmentAccessible(companyId, departmentId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessible", accessible);
        result.put("message",
                accessible ? "Department is accessible to company" : "Department is not accessible to company");

        return ResponseEntity.ok(result);
    }
}
