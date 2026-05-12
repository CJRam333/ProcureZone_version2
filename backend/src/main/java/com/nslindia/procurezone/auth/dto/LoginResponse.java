package com.nslindia.procurezone.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        AuthenticatedUser user) {
    public static LoginResponse bearer(String token, Instant expiresAt, AuthenticatedUser user) {
        return new LoginResponse(token, "Bearer", expiresAt, user);
    }
}
