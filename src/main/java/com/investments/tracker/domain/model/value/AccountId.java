package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing an account identifier.
 * <p>
 * Uses Long as surrogate key (database-generated sequence) as per ADR-002.
 * </p>
 */
public record AccountId(Long value) {

    /**
     * Canonical constructor with validation.
     */
    public AccountId {
        Objects.requireNonNull(value, "Account ID cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("Account ID must be positive, got: " + value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
