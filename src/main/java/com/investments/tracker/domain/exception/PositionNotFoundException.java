package com.investments.tracker.domain.exception;

import com.investments.tracker.domain.model.value.InstrumentSymbol;

/** Exception thrown when a position cannot be found. */
public class PositionNotFoundException extends DomainException {

    private final InstrumentSymbol symbol;

    public PositionNotFoundException(InstrumentSymbol symbol) {
        super("Position not found for instrument: " + symbol.value());
        this.symbol = symbol;
    }

    public InstrumentSymbol getSymbol() {
        return symbol;
    }
}
