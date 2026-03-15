package com.investments.tracker.domain.model;

import java.util.Objects;

import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

/**
 * Persistent mapping between a broker-specific instrument name and a catalog symbol.
 *
 * <p>Unlike {@link InstrumentMapping} which is scoped to a single import session, this entity
 * persists across imports so that previously resolved mappings can be reused automatically.
 *
 * <p>Identity is the composite of (broker, brokerInstrumentName).
 */
public record BrokerInstrumentMapping(
        BrokerName broker,
        BrokerInstrumentName brokerInstrumentName,
        InstrumentSymbol catalogSymbol) {

    public BrokerInstrumentMapping {
        Objects.requireNonNull(broker, "broker cannot be null");
        Objects.requireNonNull(brokerInstrumentName, "brokerInstrumentName cannot be null");
        Objects.requireNonNull(catalogSymbol, "catalogSymbol cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrokerInstrumentMapping other)) return false;
        return broker.equals(other.broker)
                && brokerInstrumentName.equals(other.brokerInstrumentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(broker, brokerInstrumentName);
    }
}
