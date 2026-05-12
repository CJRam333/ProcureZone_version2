package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.dto.CompanyEmployeeResponse;
import com.nslindia.procurezone.dto.CreateCompanyEmployeeRequest;
import com.nslindia.procurezone.dto.UpdateCompanyEmployeeRequest;
import com.nslindia.procurezone.service.CompanyEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/company-employees")
@RequiredArgsConstructor
public class CompanyEmployeeController {

    private final CompanyEmployeeService companyEmployeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> createMapping(
            @Valid @RequestBody CreateCompanyEmployeeRequest request) {
        CompanyEmployeeResponse response = companyEmployeeService.create(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-employee mapping created successfully");
        result.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyEmployeeResponse>> getAllMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<CompanyEmployeeResponse> mappings = companyEmployeeService.getAllMappings(page, size, sortBy, sortDir);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyEmployeeResponse>> getAllActiveMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyEmployeeResponse> mappings = companyEmployeeService.getAllActiveMappings(page, size);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompanyEmployeeResponse> getMappingById(@PathVariable Integer id) {
        CompanyEmployeeResponse mapping = companyEmployeeService.getMappingById(id);
        return ResponseEntity.ok(mapping);
    }

    @GetMapping("/company/{companyId}/employees")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyEmployeeResponse>> getEmployeesByCompany(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyEmployeeResponse> employees = companyEmployeeService.getEmployeesByCompany(companyId, page, size);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/employee/{employeeNumber}/companies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CompanyEmployeeResponse>> getCompaniesByEmployee(
            @PathVariable Integer employeeNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CompanyEmployeeResponse> companies = companyEmployeeService.getCompaniesByEmployee(employeeNumber, page,
                size);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/company/{companyId}/employee-numbers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getActiveEmployeeNumbers(@PathVariable Integer companyId) {
        List<Integer> employeeNumbers = companyEmployeeService.getActiveEmployeeNumbers(companyId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("companyId", companyId);
        result.put("employeeNumbers", employeeNumbers);
        result.put("count", employeeNumbers.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/company/{companyId}/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> countActiveEmployees(@PathVariable Integer companyId) {
        long count = companyEmployeeService.countActiveEmployees(companyId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("companyId", companyId);
        result.put("count", count);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> updateMapping(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCompanyEmployeeRequest request) {
        CompanyEmployeeResponse response = companyEmployeeService.update(id, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-employee mapping updated successfully");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deleteMapping(@PathVariable Integer id) {
        companyEmployeeService.delete(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Company-employee mapping deleted successfully");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-assignment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkEmployeeAssignment(
            @RequestParam Integer companyId,
            @RequestParam Integer employeeNumber) {
        boolean assigned = companyEmployeeService.isEmployeeAssigned(companyId, employeeNumber);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("assigned", assigned);
        result.put("message", assigned ? "Employee is assigned to company" : "Employee is not assigned to company");

        return ResponseEntity.ok(result);
    }
}
