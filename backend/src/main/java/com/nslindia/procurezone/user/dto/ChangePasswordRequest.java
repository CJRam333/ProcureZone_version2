package com.nslindia.procurezone.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for changing password.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required") String currentPassword,

        @NotBlank(message = "New password is required") @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters") String newPassword,

        @NotBlank(message = "Confirm password is required") String confirmPassword) {
}
