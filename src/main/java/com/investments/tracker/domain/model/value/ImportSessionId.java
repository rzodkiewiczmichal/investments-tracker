package com.investments.tracker.domain.model.value;

import java.util.Objects;
import java.util.UUID;

/** Value object representing an import session identifier. */
public record ImportSessionId(UUID value) {

    public ImportSessionId {
        Objects.requireNonNull(value, "Import session ID cannot be null");
    }

    public static ImportSessionId generate() {
        return new ImportSessionId(UUID.randomUUID());
    }

    public static ImportSessionId of(UUID value) {
        return new ImportSessionId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
