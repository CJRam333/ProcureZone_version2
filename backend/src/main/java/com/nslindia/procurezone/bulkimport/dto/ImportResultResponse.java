package com.nslindia.procurezone.bulkimport.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for bulk import operations.
 * Contains statistics and error details.
 */
public record ImportResultResponse(
        int totalRecords,
        int successCount,
        int failureCount,
        int skippedCount,
        List<ImportError> errors,
        String status,
        LocalDateTime importedAt,
        long processingTimeMs) {

    /**
     * Represents a single import error.
     */
    public record ImportError(
            int rowNumber,
            String code,
            String name,
            String errorMessage,
            ErrorType errorType) {
    }

    /**
     * Types of import errors.
     */
    public enum ErrorType {
        DUPLICATE_CODE,
        VALIDATION_ERROR,
        MISSING_FIELD,
        INVALID_STATUS,
        DATABASE_ERROR,
        PARSING_ERROR
    }

    /**
     * Creates a successful import result.
     */
    public static ImportResultResponse success(int totalRecords, int successCount, long processingTimeMs) {
        return new ImportResultResponse(
                totalRecords,
                successCount,
                0,
                0,
                List.of(),
                "SUCCESS",
                LocalDateTime.now(),
                processingTimeMs);
    }

    /**
     * Creates a partial success import result.
     */
    public static ImportResultResponse partialSuccess(
            int totalRecords,
            int successCount,
            int failureCount,
            List<ImportError> errors,
            long processingTimeMs) {
        return new ImportResultResponse(
                totalRecords,
                successCount,
                failureCount,
                0,
                errors,
                "PARTIAL_SUCCESS",
                LocalDateTime.now(),
                processingTimeMs);
    }

    /**
     * Creates a failed import result.
     */
    public static ImportResultResponse failure(String errorMessage, long processingTimeMs) {
        return new ImportResultResponse(
                0,
                0,
                0,
                0,
                List.of(new ImportError(0, "", "", errorMessage, ErrorType.PARSING_ERROR)),
                "FAILURE",
                LocalDateTime.now(),
                processingTimeMs);
    }
}
