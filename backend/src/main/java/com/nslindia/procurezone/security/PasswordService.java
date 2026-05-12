package com.nslindia.procurezone.security;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordVerificationResult verifyPassword(String rawPassword, String storedHash) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedHash)) {
            return PasswordVerificationResult.failure();
        }
        if (isBcrypt(storedHash)) {
            boolean matches = passwordEncoder.matches(rawPassword, storedHash);
            return matches ? PasswordVerificationResult.success(false) : PasswordVerificationResult.failure();
        }
        String md5Digest = DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
        if (storedHash.equalsIgnoreCase(md5Digest)) {
            return PasswordVerificationResult.success(true);
        }
        return PasswordVerificationResult.failure();
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private boolean isBcrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    public record PasswordVerificationResult(boolean successful, boolean legacyMatch) {

        public static PasswordVerificationResult success(boolean legacyMatch) {
            return new PasswordVerificationResult(true, legacyMatch);
        }

        public static PasswordVerificationResult failure() {
            return new PasswordVerificationResult(false, false);
        }
    }
}
