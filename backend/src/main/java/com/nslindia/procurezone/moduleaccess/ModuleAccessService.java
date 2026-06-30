package com.nslindia.procurezone.moduleaccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.identity.EmployeeRoleRepository;
import com.nslindia.procurezone.moduleaccess.dto.EmpModuleEntry;
import com.nslindia.procurezone.moduleaccess.dto.ModuleResponse;
import com.nslindia.procurezone.moduleaccess.dto.UpdateModuleAccessRequest;
import com.nslindia.procurezone.security.RoleNormalizer;
import com.nslindia.procurezone.security.UserPrincipal;

@Service
@Transactional(readOnly = true)
public class ModuleAccessService {

    private static final String ADMINISTRATION = "ADMINISTRATION";

    private final ModuleMasterRepository moduleMasterRepository;
    private final EmpModuleAccessRepository empModuleAccessRepository;
    private final EmployeeRoleRepository employeeRoleRepository;

    public ModuleAccessService(ModuleMasterRepository moduleMasterRepository,
            EmpModuleAccessRepository empModuleAccessRepository,
            EmployeeRoleRepository employeeRoleRepository) {
        this.moduleMasterRepository = moduleMasterRepository;
        this.empModuleAccessRepository = empModuleAccessRepository;
        this.employeeRoleRepository = employeeRoleRepository;
    }

    public List<String> getMyModuleCodes() {
        UserPrincipal cu = currentUser();
        List<ModuleMaster> allModules = moduleMasterRepository.findAllOrdered();

        Map<String, Boolean> customAccess = empModuleAccessRepository
                .findByEmpNumber(cu.employeeNumber())
                .stream()
                .collect(Collectors.toMap(EmpModuleAccess::getModuleCode, EmpModuleAccess::isEnabled));

        Set<String> roles = cu.roles();

        return allModules.stream()
                .filter(m -> {
                    if (customAccess.containsKey(m.getModuleCode())) {
                        return customAccess.get(m.getModuleCode());
                    }
                    return isAllowedByRole(m, roles);
                })
                .map(ModuleMaster::getModuleCode)
                .collect(Collectors.toList());
    }

    public List<EmpModuleEntry> getEmployeeModules(Integer empNumber) {
        List<ModuleMaster> allModules = moduleMasterRepository.findAllOrdered();
        Map<String, Boolean> customAccess = empModuleAccessRepository
                .findByEmpNumber(empNumber)
                .stream()
                .collect(Collectors.toMap(EmpModuleAccess::getModuleCode, EmpModuleAccess::isEnabled));

        Set<String> empRoles = getEmpNormalizedRoles(empNumber);

        return allModules.stream()
                .map(m -> {
                    boolean hasCustom = customAccess.containsKey(m.getModuleCode());
                    boolean enabled = hasCustom
                            ? customAccess.get(m.getModuleCode())
                            : isAllowedByRole(m, empRoles);
                    return new EmpModuleEntry(
                            m.getModuleCode(),
                            m.getModuleName(),
                            enabled,
                            m.isFuture(),
                            hasCustom);
                })
                .collect(Collectors.toList());
    }

    public List<ModuleResponse> getAllModules() {
        return moduleMasterRepository.findAllOrdered().stream()
                .map(m -> new ModuleResponse(
                        m.getModuleCode(),
                        m.getModuleName(),
                        m.isModuleStatus(),
                        m.isFuture(),
                        m.getDefaultRoles()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public void updateEmployeeModules(Integer empNumber, UpdateModuleAccessRequest request) {
        UserPrincipal cu = currentUser();
        Set<String> roles = cu.roles();

        boolean isSuperAdmin = roles.contains("SUPERADMIN");
        boolean isAdmin = roles.contains("ADMIN");

        if (!isSuperAdmin && !isAdmin) {
            throw new AccessDeniedException("Only ADMIN or SUPERADMIN can modify module access");
        }
        if (cu.employeeNumber() != null && cu.employeeNumber().equals(empNumber)) {
            throw new AccessDeniedException("You cannot modify your own module access");
        }

        List<EmpModuleAccess> toSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (UpdateModuleAccessRequest.ModuleUpdate update : request.moduleCodes()) {
            String code = update.code();

            // ADMIN cannot modify ADMINISTRATION in any direction — only SUPERADMIN can
            if (!isSuperAdmin && ADMINISTRATION.equals(code)) {
                continue;
            }

            EmpModuleAccess row = empModuleAccessRepository
                    .findByEmpNumberAndModuleCode(empNumber, code)
                    .orElseGet(() -> new EmpModuleAccess(empNumber, code, update.enabled(), cu.employeeNumber()));

            row.setEnabled(update.enabled());
            row.setGrantedBy(cu.employeeNumber());
            row.setGrantedAt(now);
            toSave.add(row);
        }

        empModuleAccessRepository.saveAllAndFlush(toSave);
    }

    private Set<String> getEmpNormalizedRoles(Integer empNumber) {
        return employeeRoleRepository
                .findByEmployee_EmployeeNumberAndStatus(empNumber, 1)
                .stream()
                .map(er -> RoleNormalizer.normalize(er.getRole().getCode()))
                .collect(Collectors.toSet());
    }

    private boolean isAllowedByRole(ModuleMaster m, Set<String> roles) {
        if (!m.isModuleStatus() || m.isFuture()) {
            return false;
        }
        String defaultRoles = m.getDefaultRoles();
        if (defaultRoles == null || defaultRoles.isBlank()) return false;

        Set<String> allowed = Arrays.stream(defaultRoles.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        // "ALL" anywhere in the list means every authenticated user gets this module
        if (allowed.contains("ALL")) return true;

        return roles.stream()
                .map(String::toUpperCase)
                .anyMatch(allowed::contains);
    }

    private UserPrincipal currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal cu)) {
            throw new AccessDeniedException("No authenticated user");
        }
        return cu;
    }
}
