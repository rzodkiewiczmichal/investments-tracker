package com.investments.tracker.domain.exception;

/**
 * Exception thrown when an instrument symbol value is invalid.
 * <p>
 * A symbol is invalid if it is blank, null, or does not match
 * either a ticker (1-10 uppercase alphanumeric) or ISIN format.
 * </p>
 */
public class InvalidSymbolException extends DomainException {

    public InvalidSymbolException(String message) {
        super(message);
    }

    public static InvalidSymbolException blank() {
        return new InvalidSymbolException("Instrument symbol cannot be blank");
    }

    public static InvalidSymbolException invalidFormat(String value) {
        return new InvalidSymbolException("Invalid instrument symbol format: " + value);
    }
}
