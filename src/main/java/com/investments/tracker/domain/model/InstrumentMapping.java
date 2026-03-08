package com.investments.tracker.domain.model;

import java.util.Objects;

import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

/**
 * Mapping between a broker's instrument name and a catalog symbol.
 *
 * <p>A {@code null} catalog symbol indicates the instrument is unresolved and requires
 * user-provided mapping before import can complete.
 */
public record InstrumentMapping(BrokerInstrumentName brokerName, InstrumentSymbol catalogSymbol) {

    public InstrumentMapping {
        Objects.requireNonNull(brokerName, "brokerName cannot be null");
    }

    public static InstrumentMapping resolved(
            BrokerInstrumentName brokerName, InstrumentSymbol catalogSymbol) {
        Objects.requireNonNull(catalogSymbol, "catalogSymbol cannot be null for resolved mapping");
        return new InstrumentMapping(brokerName, catalogSymbol);
    }

    public static InstrumentMapping unresolved(BrokerInstrumentName brokerName) {
        return new InstrumentMapping(brokerName, null);
    }

    public boolean isUnresolved() {
        return catalogSymbol == null;
    }

    public boolean isResolved() {
        return catalogSymbol != null;
    }
}
