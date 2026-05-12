package com.nslindia.procurezone.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for resetting password by admin.
 */
public record ResetPasswordRequest(
        @NotNull(message = "User ID is required") Long userId,

        @NotBlank(message = "New password is required") @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters") String newPassword) {
}
