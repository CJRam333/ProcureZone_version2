package com.nslindia.procurezone.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nslindia.procurezone.employee.dto.AssignRoleRequest;
import com.nslindia.procurezone.employee.dto.CreateEmployeeRequest;
import com.nslindia.procurezone.employee.dto.EmployeeResponse;
import com.nslindia.procurezone.employee.dto.EmployeeStatisticsResponse;
import com.nslindia.procurezone.employee.dto.EmployeeSummaryResponse;
import com.nslindia.procurezone.employee.dto.ResetPasswordRequest;
import com.nslindia.procurezone.employee.dto.UpdateEmployeeRequest;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;

import java.util.Set;

/**
 * REST controller for managing employees.
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * POST /api/v1/employees - Create new employee.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        EmployeeResponse response = employeeService.createEmployee(request,
                String.valueOf(currentUser.employeeNumber()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/employees/{id} - Update employee.
     * Roles: ADMIN, SUPERADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        EmployeeResponse response = employeeService.updateEmployee(id, request,
                String.valueOf(currentUser.employeeNumber()));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees - List employees with pagination and filtering.
     * Roles: ADMIN, SUPERADMIN, VIEWER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER')")
    public ResponseEntity<Page<EmployeeSummaryResponse>> getAllEmployees(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer locationId,
            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<EmployeeSummaryResponse> response = employeeService.getAllEmployees(status, search, departmentId, locationId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees/active - List active employees.
     * Roles: ADMIN, SUPERADMIN, VIEWER, DEPTHEAD
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER', 'DEPTHEAD', 'USER')")
    public ResponseEntity<Page<EmployeeSummaryResponse>> getActiveEmployees(
            @PageableDefault(size = 100, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {

        // Status 1 is Active in the legacy DB
        Page<EmployeeSummaryResponse> response = employeeService.getAllEmployees(1, null, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees/available-for-user - List employees available to have a user account created.
     * Roles: ADMIN, SUPERADMIN
     */
    @GetMapping("/available-for-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<EmployeeSummaryResponse>> getAvailableForUser(
            @RequestParam(required = false) Integer currentEmployeeId,
            @PageableDefault(size = 100, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Page<EmployeeSummaryResponse> response = employeeService.getAvailableForUser(currentEmployeeId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees/search - Search employees.
     * Roles: ADMIN, SUPERADMIN, VIEWER
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER')")
    public ResponseEntity<Page<EmployeeSummaryResponse>> searchEmployees(
            @RequestParam String query,
            @RequestParam(required = false) Integer status,
            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<EmployeeSummaryResponse> response = employeeService.searchEmployees(query, status, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/employees/{id}/roles - Assign role to employee.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> assignRole(
            @PathVariable Integer id,
            @Valid @RequestBody AssignRoleRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        employeeService.assignRole(id, request, String.valueOf(currentUser.employeeNumber()));

        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/v1/employees/{id}/roles/{roleId} - Remove role from employee.
     * Roles: ADMIN, SUPERADMIN
     */
    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> removeRole(
            @PathVariable Integer id,
            @PathVariable Integer roleId,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        employeeService.removeRole(id, roleId, String.valueOf(currentUser.employeeNumber()));

        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/v1/employees/{id}/activate - Activate employee.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> activateEmployee(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        employeeService.activateEmployee(id, currentUser.username());

        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/v1/employees/{id}/deactivate - Deactivate employee.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        employeeService.deactivateEmployee(id, currentUser.username());

        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/employees/{id}/roles - Get employee roles.
     * Roles: ADMIN, SUPERADMIN, VIEWER
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER')")
    public ResponseEntity<Set<EmployeeResponse.EmployeeRoleInfo>> getEmployeeRoles(
            @PathVariable Integer id) {

        Set<EmployeeResponse.EmployeeRoleInfo> response = employeeService.getEmployeeRoles(id);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees/by-department/{departmentId} - Get employees by
     * department.
     * Roles: ADMIN, DEPTHEAD, SUPERADMIN
     */
    @GetMapping("/by-department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPTHEAD', 'SUPERADMIN')")
    public ResponseEntity<Page<EmployeeSummaryResponse>> getEmployeesByDepartment(
            @PathVariable Integer departmentId,
            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<EmployeeSummaryResponse> response = employeeService.getEmployeesByDepartment(departmentId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees/statistics - Get employee statistics.
     * Roles: ADMIN, SUPERADMIN
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<EmployeeStatisticsResponse> getEmployeeStatistics() {
        EmployeeStatisticsResponse response = employeeService.getEmployeeStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/employees/{id} - Get employee by ID.
     * Roles: ADMIN, SUPERADMIN, VIEWER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Integer id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/employees/{id}/password - Reset employee user-account password.
     * Roles: ADMIN, SUPERADMIN
     */
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody ResetPasswordRequest request) {

        employeeService.resetPassword(id, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
