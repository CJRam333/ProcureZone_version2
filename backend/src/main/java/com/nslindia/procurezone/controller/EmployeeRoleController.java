package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.dto.CreateEmployeeRoleRequest;
import com.nslindia.procurezone.dto.EmployeeRoleResponse;
import com.nslindia.procurezone.dto.UpdateEmployeeRoleRequest;
import com.nslindia.procurezone.service.EmployeeRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee-roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'PLANTMANAGER', 'AUDITOR')")
public class EmployeeRoleController {

    private final EmployeeRoleService employeeRoleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createEmployeeRole(
            @Valid @RequestBody CreateEmployeeRoleRequest request) {
        try {
            EmployeeRoleResponse response = employeeRoleService.createEmployeeRole(request);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Employee role assigned successfully");
            responseMap.put("data", response);
            return new ResponseEntity<>(responseMap, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEmployeeRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<EmployeeRoleResponse> employeeRoles = employeeRoleService.getAllEmployeeRoles(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", employeeRoles.getContent());
            response.put("currentPage", employeeRoles.getNumber());
            response.put("totalItems", employeeRoles.getTotalElements());
            response.put("totalPages", employeeRoles.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveEmployeeRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<EmployeeRoleResponse> employeeRoles = employeeRoleService.getActiveEmployeeRoles(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", employeeRoles.getContent());
            response.put("currentPage", employeeRoles.getNumber());
            response.put("totalItems", employeeRoles.getTotalElements());
            response.put("totalPages", employeeRoles.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeeRoleById(@PathVariable Integer id) {
        try {
            EmployeeRoleResponse employeeRole = employeeRoleService.getEmployeeRoleById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", employeeRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/employee/{employeeNumber}/roles")
    public ResponseEntity<Map<String, Object>> getRolesByEmployee(@PathVariable Integer employeeNumber) {
        try {
            List<EmployeeRoleResponse> roles = employeeRoleService.getRolesByEmployee(employeeNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roles);
            response.put("count", roles.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeNumber}/role-ids")
    public ResponseEntity<Map<String, Object>> getRoleIdsByEmployee(@PathVariable Integer employeeNumber) {
        try {
            List<Integer> roleIds = employeeRoleService.getRoleIdsByEmployee(employeeNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roleIds);
            response.put("count", roleIds.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/role/{roleId}/employees")
    public ResponseEntity<Map<String, Object>> getEmployeesWithRole(@PathVariable Integer roleId) {
        try {
            List<Integer> employeeNumbers = employeeRoleService.getEmployeesWithRole(roleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", employeeNumbers);
            response.put("count", employeeNumbers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeNumber}/count")
    public ResponseEntity<Map<String, Object>> countRolesByEmployee(@PathVariable Integer employeeNumber) {
        try {
            long count = employeeRoleService.countRolesByEmployee(employeeNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateEmployeeRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeRoleRequest request) {
        try {
            EmployeeRoleResponse updated = employeeRoleService.updateEmployeeRole(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee role updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> revokeEmployeeRole(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "Role revoked") String reason) {
        try {
            employeeRoleService.revokeEmployeeRole(id, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee role revoked successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/check-role")
    public ResponseEntity<Map<String, Object>> checkRole(
            @RequestParam Integer employeeNumber,
            @RequestParam Integer roleId) {
        try {
            boolean hasRole = employeeRoleService.hasRole(employeeNumber, roleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasRole", hasRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/employee/{employeeNumber}/batch")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> batchAssignRoles(
            @PathVariable Integer employeeNumber,
            @RequestBody Map<String, List<Integer>> payload) {
        try {
            List<Integer> roleIds = payload.get("roleIds");
            employeeRoleService.batchAssignRoles(employeeNumber, roleIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Roles assigned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }
}
