package com.investments.tracker.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;

/**
 * Result of parsing a broker transaction history file.
 *
 * <p>Contains parsed transactions and optional ticker hints extracted from the file (e.g., XTB
 * Closed Positions sheet provides instrument name → ticker mappings).
 *
 * @param transactions parsed raw transactions
 * @param tickerHints broker instrument name → catalog symbol hints extracted from the file
 */
public record ParseResult(
        List<RawTransaction> transactions,
        Map<BrokerInstrumentName, InstrumentSymbol> tickerHints) {

    public ParseResult {
        Objects.requireNonNull(transactions, "transactions cannot be null");
        Objects.requireNonNull(tickerHints, "tickerHints cannot be null");
        transactions = List.copyOf(transactions);
        tickerHints = Map.copyOf(tickerHints);
    }
}
