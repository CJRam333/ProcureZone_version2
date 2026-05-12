package com.nslindia.procurezone.bulkimport;

import com.nslindia.procurezone.bulkimport.dto.ImportResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * I.1 FIX: In-memory cache for import results
 * Stores import results temporarily for error download functionality.
 * Results expire after 24 hours.
 * 
 * @author NSL India
 * @version 1.0
 */
@Component
@Slf4j
public class ImportResultCache {

    private static final int MAX_CACHE_SIZE = 1000;
    private static final int EXPIRY_HOURS = 24;

    private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

    /**
     * Store import result and return a unique ID for later retrieval.
     */
    public String store(ImportResultResponse result) {
        // Clean up expired entries
        cleanupExpired();

        // Check cache size limit
        if (cache.size() >= MAX_CACHE_SIZE) {
            log.warn("Import result cache is full, removing oldest entries");
            removeOldest();
        }

        String importId = UUID.randomUUID().toString();
        cache.put(importId, new CachedResult(result, LocalDateTime.now()));
        log.debug("Stored import result with ID: {}", importId);
        return importId;
    }

    /**
     * Retrieve import result by ID.
     */
    public ImportResultResponse get(String importId) {
        CachedResult cached = cache.get(importId);
        if (cached == null) {
            return null;
        }

        // Check if expired
        if (isExpired(cached)) {
            cache.remove(importId);
            return null;
        }

        return cached.result;
    }

    /**
     * Remove import result from cache.
     */
    public void remove(String importId) {
        cache.remove(importId);
    }

    /**
     * Check if a cached result is expired.
     */
    private boolean isExpired(CachedResult cached) {
        return cached.storedAt.plusHours(EXPIRY_HOURS).isBefore(LocalDateTime.now());
    }

    /**
     * Clean up expired entries.
     */
    private void cleanupExpired() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    /**
     * Remove oldest entries when cache is full.
     */
    private void removeOldest() {
        cache.entrySet().stream()
                .sorted((a, b) -> a.getValue().storedAt.compareTo(b.getValue().storedAt))
                .limit(cache.size() / 10) // Remove 10% oldest
                .forEach(entry -> cache.remove(entry.getKey()));
    }

    /**
     * Internal class to hold cached result with timestamp.
     */
    private record CachedResult(ImportResultResponse result, LocalDateTime storedAt) {
    }
}
