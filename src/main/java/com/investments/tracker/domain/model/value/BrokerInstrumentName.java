package com.investments.tracker.domain.model.value;

import java.util.Objects;

import com.investments.tracker.domain.exception.DomainException;

/**
 * Value object representing a raw instrument name as provided by the broker.
 *
 * <p>This is the unprocessed value from the broker's export file (e.g., mBank "Papier" column). It
 * may contain spaces, hyphens, and other characters that are not valid in {@link InstrumentSymbol}.
 * Resolution to a catalog symbol is done by the user during import.
 */
public record BrokerInstrumentName(String value) {

    public BrokerInstrumentName {
        Objects.requireNonNull(value, "Broker instrument name cannot be null");
        if (value.isBlank()) {
            throw new DomainException("Broker instrument name cannot be blank");
        }
        value = value.trim();
    }

    public static BrokerInstrumentName of(String value) {
        return new BrokerInstrumentName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
