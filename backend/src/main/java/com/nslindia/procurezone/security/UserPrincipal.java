package com.nslindia.procurezone.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record UserPrincipal(
        Long userId,
        Integer employeeNumber,
        String employeeId,
        String username,
        String displayName,
        String email,
        Set<String> roles,
        boolean canView,
        boolean canAdd,
        boolean canEdit,
        boolean canDelete,
        Integer deptId,
        Integer locationId,
        List<Integer> companyIds) {

    public UserPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        companyIds = companyIds == null ? List.of() : List.copyOf(companyIds);
    }

    public Collection<? extends GrantedAuthority> authorities() {
        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // Map granular permissions to virtual roles for controller compatibility
        if (canView) {
            authorities.add(new SimpleGrantedAuthority("ROLE_VIEWER"));
            // VIEWER is the only synthetic authority from canView.
            // All other role codes come from tbl_roles_master.role_code via RoleNormalizer.
        }
        if (canAdd || canEdit || canDelete) {
            // Most management endpoints require ADMIN or SPECIFIC roles.
            // For now, we add ROLE_EDITOR if it's used, or map to a generic role.
            // Based on IndentController/EmployeeController, many write ops needs specific roles.
            authorities.add(new SimpleGrantedAuthority("ROLE_USER_PERMITTED"));
        }

        return authorities;
    }
}
