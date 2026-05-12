package com.nslindia.procurezone.auth.dto;

import java.time.Instant;

public record LogoutResponse(
        String message,
        Instant logoutTime) {

    public static LogoutResponse success() {
        return new LogoutResponse("Logged out successfully", Instant.now());
    }
}
