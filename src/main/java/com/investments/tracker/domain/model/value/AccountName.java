package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing an account name.
 */
public record AccountName(String value) {

    public AccountName {
        Objects.requireNonNull(value, "Account name cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be blank");
        }
        value = value.trim();
    }

    public static AccountName of(String value) {
        return new AccountName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
