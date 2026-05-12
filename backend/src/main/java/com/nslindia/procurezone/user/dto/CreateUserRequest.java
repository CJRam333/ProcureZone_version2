package com.nslindia.procurezone.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user account.
 */
public record CreateUserRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters") String username,

        @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password,

        @NotNull(message = "Employee number is required") Integer employeeId) {
}
