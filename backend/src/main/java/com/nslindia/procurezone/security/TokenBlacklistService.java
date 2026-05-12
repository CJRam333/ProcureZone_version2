package com.nslindia.procurezone.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service to manage blacklisted JWT tokens (logged out tokens).
 * In production, consider using Redis for distributed token blacklist.
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    // Map: token -> expiration time
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist.
     * 
     * @param token          the JWT token to blacklist
     * @param expirationTime the expiration time of the token
     */
    public void blacklistToken(String token, Instant expirationTime) {
        blacklistedTokens.put(token, expirationTime);
        log.debug("Token blacklisted. Total blacklisted tokens: {}", blacklistedTokens.size());
    }

    /**
     * Check if a token is blacklisted.
     * 
     * @param token the JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Clean up expired tokens from the blacklist every hour.
     * This prevents memory leaks by removing tokens that have already expired.
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int initialSize = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));

        int removedCount = initialSize - blacklistedTokens.size();
        if (removedCount > 0) {
            log.info("Cleaned up {} expired tokens from blacklist. Remaining: {}",
                    removedCount, blacklistedTokens.size());
        }
    }

    /**
     * Get the number of blacklisted tokens (for monitoring).
     * 
     * @return count of blacklisted tokens
     */
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }
}
