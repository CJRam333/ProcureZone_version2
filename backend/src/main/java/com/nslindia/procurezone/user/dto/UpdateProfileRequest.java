package com.nslindia.procurezone.user.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile.
 */
public record UpdateProfileRequest(
        @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters") String username) {
}
