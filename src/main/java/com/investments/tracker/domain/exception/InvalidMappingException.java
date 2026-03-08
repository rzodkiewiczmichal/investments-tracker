package com.investments.tracker.domain.exception;

/**
 * Exception thrown when a user-provided instrument mapping references a catalog symbol that does
 * not exist.
 */
public class InvalidMappingException extends DomainException {

    public InvalidMappingException(String message) {
        super(message);
    }

    public static InvalidMappingException unknownCatalogSymbol(String catalogSymbol) {
        return new InvalidMappingException("Catalog symbol does not exist: " + catalogSymbol);
    }
}
