package com.investments.tracker.domain.model.value;

import java.util.Objects;

/**
 * Value object representing an instrument name.
 */
public record InstrumentName(String value) {

    public InstrumentName {
        Objects.requireNonNull(value, "Instrument name cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Instrument name cannot be blank");
        }
        value = value.trim();
    }

    public static InstrumentName of(String value) {
        return new InstrumentName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
