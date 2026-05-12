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

import com.nslindia.procurezone.masterdata.dto.DepartmentResponse;
import com.nslindia.procurezone.masterdata.dto.CreateDepartmentRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateDepartmentRequest;
import com.nslindia.procurezone.masterdata.service.DepartmentService;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * REST Controller for Department master data management.
 * Provides endpoints for CRUD operations on departments.
 */
@RestController
@RequestMapping("/api/v1/departments")
@Validated
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Get all departments with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return page of departments
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DepartmentResponse>> getAllDepartments(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/departments - Fetching all departments with pagination");
        Page<DepartmentResponse> departments = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(departments);
    }

    /**
     * Get all active departments with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of active departments
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DepartmentResponse>> getActiveDepartments(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/departments/active - Fetching active departments");
        Page<DepartmentResponse> departments = departmentService.getActiveDepartments(pageable);
        return ResponseEntity.ok(departments);
    }

    /**
     * Search departments by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active departments only
     * @param pageable   pagination parameters
     * @return page of matching departments
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DepartmentResponse>> searchDepartments(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/departments/search - searchTerm: {}, activeOnly: {}", searchTerm, activeOnly);
        Page<DepartmentResponse> departments = departmentService.searchDepartments(searchTerm, activeOnly, pageable);
        return ResponseEntity.ok(departments);
    }

    /**
     * Get department by ID.
     * 
     * @param id the department ID
     * @return the department
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable @Min(1) Integer id) {
        logger.info("GET /api/v1/departments/{} - Fetching department by ID", id);
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    /**
     * Create a new department.
     * 
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created department
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("POST /api/v1/departments - Creating department with code: {}", request.code());
        DepartmentResponse department = departmentService.createDepartment(request, principal.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    /**
     * Update an existing department.
     * 
     * @param id        the department ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated department
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable @Min(1) Integer id,
            @Valid @RequestBody UpdateDepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("PUT /api/v1/departments/{} - Updating department", id);
        DepartmentResponse department = departmentService.updateDepartment(id, request, principal.email());
        return ResponseEntity.ok(department);
    }

    /**
     * Delete a department by ID.
     * 
     * @param id        the department ID
     * @param principal the authenticated user
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable @Min(1) Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("DELETE /api/v1/departments/{} - Deleting department", id);
        departmentService.deleteDepartment(id, principal.email());
        return ResponseEntity.noContent().build();
    }
}
