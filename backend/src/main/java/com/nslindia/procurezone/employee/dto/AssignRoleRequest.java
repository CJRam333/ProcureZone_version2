package com.nslindia.procurezone.employee.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for assigning a role to an employee.
 */
public record AssignRoleRequest(
        @NotNull(message = "Role ID is required") Integer roleId,

        String remarks) {
}
