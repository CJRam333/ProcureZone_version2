package com.nslindia.procurezone.service;

import com.nslindia.procurezone.dto.CreateEmployeeRoleRequest;
import com.nslindia.procurezone.dto.EmployeeRoleResponse;
import com.nslindia.procurezone.dto.UpdateEmployeeRoleRequest;
import com.nslindia.procurezone.entity.EmployeeRole;
import com.nslindia.procurezone.repository.EmployeeRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeRoleService {

    private final EmployeeRoleMappingRepository employeeRoleRepository;

    @Transactional
    public EmployeeRoleResponse createEmployeeRole(CreateEmployeeRoleRequest request) {
        // Check for duplicate active role assignment
        if (employeeRoleRepository.hasActiveRole(request.getEmployeeNumber(), request.getRoleId())) {
            throw new RuntimeException("Employee already has this role assigned");
        }

        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeNumber(request.getEmployeeNumber());
        employeeRole.setRoleId(request.getRoleId());
        employeeRole.setStatus(request.getStatus());
        employeeRole.setLastModifiedDate(LocalDate.now());
        employeeRole.setLastModifiedBy(getCurrentUserId());

        // Set assignment metadata
        if (request.getStatus() == 1) {
            employeeRole.activate(getCurrentUserId());
        }

        EmployeeRole savedRole = employeeRoleRepository.save(employeeRole);
        return new EmployeeRoleResponse(savedRole);
    }

    public Page<EmployeeRoleResponse> getAllEmployeeRoles(Pageable pageable) {
        return employeeRoleRepository.findAll(pageable)
                .map(EmployeeRoleResponse::new);
    }

    public Page<EmployeeRoleResponse> getActiveEmployeeRoles(Pageable pageable) {
        return employeeRoleRepository.findByStatus(1, pageable)
                .map(EmployeeRoleResponse::new);
    }

    public EmployeeRoleResponse getEmployeeRoleById(Integer id) {
        EmployeeRole employeeRole = employeeRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee role not found with id: " + id));
        return new EmployeeRoleResponse(employeeRole);
    }

    public List<EmployeeRoleResponse> getRolesByEmployee(Integer employeeNumber) {
        return employeeRoleRepository.findActiveRolesByEmployee(employeeNumber)
                .stream()
                .map(EmployeeRoleResponse::new)
                .collect(Collectors.toList());
    }

    public List<Integer> getRoleIdsByEmployee(Integer employeeNumber) {
        return employeeRoleRepository.findActiveRoleIdsByEmployee(employeeNumber);
    }

    public List<Integer> getEmployeesWithRole(Integer roleId) {
        return employeeRoleRepository.findEmployeeNumbersByRole(roleId);
    }

    @Transactional
    public EmployeeRoleResponse updateEmployeeRole(Integer id, UpdateEmployeeRoleRequest request) {
        EmployeeRole employeeRole = employeeRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee role not found with id: " + id));

        if (request.getStatus() != null) {
            if (request.getStatus() == 1 && employeeRole.getStatus() == 0) {
                // Reactivating role
                employeeRole.activate(getCurrentUserId());
            } else if (request.getStatus() == 0 && employeeRole.getStatus() == 1) {
                // Deactivating role
                employeeRole.deactivate(getCurrentUserId(), request.getRemarks());
            }
            employeeRole.setStatus(request.getStatus());
        }

        employeeRole.setLastModifiedDate(LocalDate.now());
        employeeRole.setLastModifiedBy(getCurrentUserId());

        EmployeeRole updatedRole = employeeRoleRepository.save(employeeRole);
        return new EmployeeRoleResponse(updatedRole);
    }

    @Transactional
    public void revokeEmployeeRole(Integer id, String reason) {
        EmployeeRole employeeRole = employeeRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee role not found with id: " + id));

        employeeRole.deactivate(getCurrentUserId(), reason);
        employeeRole.setLastModifiedDate(LocalDate.now());
        employeeRole.setLastModifiedBy(getCurrentUserId());

        employeeRoleRepository.save(employeeRole);
    }

    public boolean hasRole(Integer employeeNumber, Integer roleId) {
        return employeeRoleRepository.hasActiveRole(employeeNumber, roleId);
    }

    public long countRolesByEmployee(Integer employeeNumber) {
        return employeeRoleRepository.countActiveRolesByEmployee(employeeNumber);
    }

    @Transactional
    public void batchAssignRoles(Integer employeeNumber, List<Integer> roleIds) {
        List<Integer> targetRoleIds = roleIds != null ? roleIds : List.of();
        List<Integer> currentActive = getRoleIdsByEmployee(employeeNumber);
        
        // Roles to add/activate
        for (Integer roleId : targetRoleIds) {
            if (!currentActive.contains(roleId)) {
                EmployeeRole existing = employeeRoleRepository.findByEmployeeNumberAndRoleId(employeeNumber, roleId).orElse(null);
                if (existing != null) {
                    existing.activate(getCurrentUserId());
                    existing.setStatus(1);
                    existing.setLastModifiedDate(LocalDate.now());
                    existing.setLastModifiedBy(getCurrentUserId());
                    employeeRoleRepository.save(existing);
                } else {
                    CreateEmployeeRoleRequest req = new CreateEmployeeRoleRequest();
                    req.setEmployeeNumber(employeeNumber);
                    req.setRoleId(roleId);
                    req.setStatus(1);
                    createEmployeeRole(req);
                }
            }
        }
        
        // Roles to remove/deactivate
        for (Integer activeRoleId : currentActive) {
            if (!targetRoleIds.contains(activeRoleId)) {
                EmployeeRole existing = employeeRoleRepository.findByEmployeeNumberAndRoleId(employeeNumber, activeRoleId).orElse(null);
                if (existing != null) {
                    existing.deactivate(getCurrentUserId(), "Batch update");
                    existing.setStatus(0);
                    existing.setLastModifiedDate(LocalDate.now());
                    existing.setLastModifiedBy(getCurrentUserId());
                    employeeRoleRepository.save(existing);
                }
            }
        }
    }

    public long countEmployeesWithRole(Integer roleId) {
        return employeeRoleRepository.countEmployeesWithRole(roleId);
    }

    private Integer getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.nslindia.procurezone.security.UserPrincipal) {
                    return ((com.nslindia.procurezone.security.UserPrincipal) principal).employeeNumber();
                }
            }
            return 1; // Fallback for system operations
        } catch (Exception e) {
            return 1; // Fallback on error
        }
    }
}
