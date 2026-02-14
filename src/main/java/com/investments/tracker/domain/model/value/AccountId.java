package com.investments.tracker.domain.model.value;

import java.util.Objects;

import com.investments.tracker.domain.exception.DomainException;

/**
 * Value object representing an account identifier.
 *
 * <p>Uses Long as surrogate key (database-generated sequence) as per ADR-002.
 */
public record AccountId(Long value) {

    /** Canonical constructor with validation. */
    public AccountId {
        Objects.requireNonNull(value, "Account ID cannot be null");
        if (value <= 0) {
            throw new DomainException("Account ID must be positive, got: " + value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
