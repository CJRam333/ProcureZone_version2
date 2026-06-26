package com.nslindia.procurezone.auth.dto;

import java.util.Set;

public record AuthenticatedUser(
                Long userId,
                Integer employeeNumber,
                String employeeId,
                String displayName,
                String email,
                Set<String> roles,
                boolean canView,
                boolean canAdd,
                boolean canEdit,
                boolean canDelete,
                String departmentName,
                String companyName,
                String locationName,
                String designation) {
}
