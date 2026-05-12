package com.nslindia.procurezone.common.web;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.nslindia.procurezone.auth.exception.InactiveUserException;
import com.nslindia.procurezone.auth.exception.InvalidCredentialsException;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.DuplicateResourceException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCredentials(InvalidCredentialsException ex) {
        return ApiError.of("AUTH_INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(InactiveUserException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleInactiveUser(InactiveUserException ex) {
        return ApiError.of("AUTH_INACTIVE_USER", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex) {
        return ApiError.of("ACCESS_DENIED", "You do not have permission to perform this action");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return ApiError.of("VALIDATION_FAILED", message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        HttpStatus resolved = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError payload = ApiError.of("HTTP_" + resolved.value(),
                ex.getReason() != null ? ex.getReason() : ex.getMessage());
        return ResponseEntity.status(resolved).body(payload);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiError.of("RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(EntityNotFoundException ex) {
        return ApiError.of("ENTITY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(BadRequestException ex) {
        return ApiError.of("BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateResource(DuplicateResourceException ex) {
        return ApiError.of("DUPLICATE_RESOURCE", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalState(IllegalStateException ex) {
        return ApiError.of("INVALID_STATE", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex) {
        return ApiError.of("INVALID_ARGUMENT", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        return ApiError.of("DATA_INTEGRITY_VIOLATION",
                message != null ? message : "Database constraint violation");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiError handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ApiError.of("METHOD_NOT_ALLOWED", ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoHandlerFound(NoHandlerFoundException ex) {
        return ApiError.of("NOT_FOUND", "Resource not found: " + ex.getRequestURL());
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResourceFound(NoResourceFoundException ex) {
        return ApiError.of("NOT_FOUND", "Resource not found: " + ex.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception ex) {
        // Log the exception for debugging
        ex.printStackTrace(); // Added for debugging backend issues
        return ApiError.of("INTERNAL_ERROR", "An unexpected error occurred: " + ex.getMessage());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + (error.getDefaultMessage() != null ? error.getDefaultMessage() : "is invalid");
    }
}
