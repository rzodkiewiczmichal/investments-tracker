package com.investments.tracker.domain.model;

import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;

import java.util.Objects;

/**
 * A financial instrument (stock or ETF).
 * <p>
 * Polish government bonds are not included here - they will be handled separately
 * in a future version with different valuation logic.
 * </p>
 * <p>
 * Instrument is pure reference data identified by its symbol (natural key).
 * Current prices are managed separately via {@link com.investments.tracker.domain.repository.CurrentPriceProvider}.
 * </p>
 */
public record Instrument(
        InstrumentSymbol symbol,
        InstrumentName name,
        InstrumentType type,
        Currency currency) {

    public Instrument {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
    }

    /**
     * Identity-based equality. Instruments are equal if they have the same symbol.
     * This overrides record's default structural equality to follow DDD entity semantics.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instrument other)) return false;
        return symbol.equals(other.symbol);
    }

    /**
     * Hash code based on identity (symbol) only.
     */
    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
}
