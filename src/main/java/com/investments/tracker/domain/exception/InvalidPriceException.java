package com.investments.tracker.domain.exception;

/**
 * Exception thrown when a price value is invalid.
 *
 * <p>A price is invalid if it is negative or zero.
 */
public class InvalidPriceException extends DomainException {

    public InvalidPriceException(String message) {
        super(message);
    }

    public static InvalidPriceException negative(String value) {
        return new InvalidPriceException("Price cannot be negative: " + value);
    }

    public static InvalidPriceException zero() {
        return new InvalidPriceException("Price cannot be zero");
    }
}
