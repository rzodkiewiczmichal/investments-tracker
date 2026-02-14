package com.investments.tracker.domain.exception;

/**
 * Base exception for all domain-specific exceptions.
 *
 * <p>Domain exceptions represent business rule violations or invalid domain operations.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
