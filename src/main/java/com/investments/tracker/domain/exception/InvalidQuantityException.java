package com.investments.tracker.domain.exception;

/**
 * Exception thrown when a quantity value is invalid.
 *
 * <p>A quantity is invalid if it is negative, zero, or non-numeric.
 */
public class InvalidQuantityException extends DomainException {

    public InvalidQuantityException(String message) {
        super(message);
    }

    public static InvalidQuantityException negative(String value) {
        return new InvalidQuantityException("Quantity cannot be negative: " + value);
    }

    public static InvalidQuantityException zero() {
        return new InvalidQuantityException("Quantity cannot be zero");
    }

    public static InvalidQuantityException nonNumeric(String value) {
        return new InvalidQuantityException("Quantity must be numeric: " + value);
    }

    public static InvalidQuantityException exceedsScale(int actualScale, int maxScale) {
        return new InvalidQuantityException(
                "Quantity cannot exceed " + maxScale + " decimal places, got: " + actualScale);
    }
}
