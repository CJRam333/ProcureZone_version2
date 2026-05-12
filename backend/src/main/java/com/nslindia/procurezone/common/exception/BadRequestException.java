package com.nslindia.procurezone.common.exception;

/**
 * Exception thrown when a request contains invalid data or violates business
 * rules.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
