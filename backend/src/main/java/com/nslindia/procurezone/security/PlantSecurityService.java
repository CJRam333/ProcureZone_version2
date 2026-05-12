package com.nslindia.procurezone.security;

import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.PlantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A.1 SECURITY FIX: Service to handle plant-level data isolation
 * Ensures users can only see data from their assigned plants.
 * 
 * @author NSL India
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlantSecurityService {

    private final EmployeeRepository employeeRepository;
    private final PlantRepository plantRepository;

    // Roles that bypass plant filtering (can see all data)
    private static final Set<String> GLOBAL_ACCESS_ROLES = Set.of(
            "SUPERADMIN", "ADMIN", "AUDITOR");

    /**
     * Get list of plant IDs the current user has access to.
     * Returns empty list if user has global access (SUPERADMIN, ADMIN, AUDITOR).
     * Returns null if user has no plant mapping (edge case - allow all for
     * backwards compatibility).
     */
    @Transactional(readOnly = true)
    public List<Integer> getAllowedPlantIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found for plant security check");
            return Collections.emptyList();
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return getAllowedPlantIds(principal);
    }

    /**
     * Get list of plant IDs for a specific user principal.
     * Uses Employee.plant (String) field and matches against Plant.code.
     */
    @Transactional(readOnly = true)
    public List<Integer> getAllowedPlantIds(UserPrincipal principal) {
        // Check if user has global access role
        if (hasGlobalAccess(principal)) {
            log.debug("User {} has global access role, returning empty list (no filtering)", principal.username());
            return Collections.emptyList(); // Empty list means no filtering needed
        }

        Integer employeeNumber = principal.employeeNumber();
        if (employeeNumber == null) {
            log.warn("No employee number for user: {}", principal.username());
            return Collections.emptyList();
        }

        // Get employee's plant assignment (String field)
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeNumber);
        if (employeeOpt.isEmpty()) {
            log.warn("Employee not found for employee number: {}", employeeNumber);
            return Collections.emptyList();
        }

        Employee employee = employeeOpt.get();
        String plantCode = employee.getPlant();

        if (plantCode == null || plantCode.trim().isEmpty()) {
            log.warn("No plant assigned to employee: {}. Allowing access to all plants for backwards compatibility.",
                    employeeNumber);
            return Collections.emptyList(); // Allow all for backwards compatibility
        }

        // Match plant code against Plant.code to get Plant ID
        Optional<Plant> plantOpt = plantRepository.findByCode(plantCode.trim());
        if (plantOpt.isEmpty()) {
            log.warn("Plant code '{}' not found in Plant table for employee: {}. Allowing access to all plants.",
                    plantCode, employeeNumber);
            return Collections.emptyList();
        }

        List<Integer> plantIds = List.of(plantOpt.get().getId());
        log.debug("User {} has access to plants: {}", principal.username(), plantIds);
        return plantIds;
    }

    /**
     * Check if user has global access (can see all plants).
     */
    public boolean hasGlobalAccess(UserPrincipal principal) {
        if (principal.roles() == null || principal.roles().isEmpty()) {
            return false;
        }
        return principal.roles().stream()
                .anyMatch(role -> GLOBAL_ACCESS_ROLES.contains(role.toUpperCase()));
    }

    /**
     * Check if current user has access to a specific plant ID.
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToPlant(Integer plantId) {
        if (plantId == null) {
            return true; // No plant restriction on the data
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        if (hasGlobalAccess(principal)) {
            return true;
        }

        List<Integer> allowedPlants = getAllowedPlantIds(principal);
        if (allowedPlants.isEmpty()) {
            // Empty list means no plant assigned = allow all for backwards compatibility
            return true;
        }
        return allowedPlants.contains(plantId);
    }

    /**
     * Get user's department ID (for A.2 department-scoped access).
     */
    @Transactional(readOnly = true)
    public Integer getUserDepartmentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return getUserDepartmentId(principal);
    }

    /**
     * Get department ID for a specific user principal.
     */
    @Transactional(readOnly = true)
    public Integer getUserDepartmentId(UserPrincipal principal) {
        Integer employeeNumber = principal.employeeNumber();
        if (employeeNumber == null) {
            return null;
        }

        Optional<Employee> employee = employeeRepository.findById(employeeNumber);
        return employee.map(e -> e.getDepartment() != null ? e.getDepartment() : null)
                .orElse(null);
    }

    /**
     * Check if user has DEPTHEAD role (for department-scoped filtering).
     */
    public boolean isDeptHead(UserPrincipal principal) {
        if (principal.roles() == null) {
            return false;
        }
        return principal.roles().stream()
                .anyMatch(role -> "DEPTHEAD".equalsIgnoreCase(role));
    }

    /**
     * Get plant code for a specific user.
     * This returns the raw plant code from Employee.plant field.
     */
    @Transactional(readOnly = true)
    public String getUserPlantCode(UserPrincipal principal) {
        Integer employeeNumber = principal.employeeNumber();
        if (employeeNumber == null) {
            return null;
        }

        Optional<Employee> employee = employeeRepository.findById(employeeNumber);
        return employee.map(Employee::getPlant).orElse(null);
    }

    /**
     * Check if a user can access data for a specific plant code (String).
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToPlantCode(String plantCode) {
        if (plantCode == null || plantCode.trim().isEmpty()) {
            return true; // No plant restriction
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        if (hasGlobalAccess(principal)) {
            return true;
        }

        String userPlantCode = getUserPlantCode(principal);
        if (userPlantCode == null || userPlantCode.trim().isEmpty()) {
            return true; // No plant assigned to user = allow all
        }

        return userPlantCode.trim().equalsIgnoreCase(plantCode.trim());
    }
}
