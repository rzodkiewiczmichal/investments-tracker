package com.investments.tracker.application.port.out;

import java.io.InputStream;

/**
 * Port interface for parsing broker-specific transaction history files.
 *
 * <p>Each broker adapter implements this interface to parse its export format into raw transactions
 * and optional ticker hints. Symbol resolution is not the responsibility of the parser — it returns
 * raw broker instrument names as-is.
 */
public interface TransactionHistoryParser {

    /**
     * Parses a broker transaction history file into raw transactions and ticker hints.
     *
     * @param file the input stream of the broker export file
     * @return parse result containing transactions and optional ticker hints
     * @throws com.investments.tracker.domain.exception.ImportParsingException if the file cannot be
     *     parsed
     */
    ParseResult parse(InputStream file);

    /**
     * Returns the broker name this parser handles.
     *
     * @return the broker identifier (e.g., "mBank")
     */
    String brokerName();
}
