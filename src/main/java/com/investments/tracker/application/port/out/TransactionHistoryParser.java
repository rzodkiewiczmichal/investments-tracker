package com.investments.tracker.application.port.out;

import java.io.InputStream;
import java.util.List;

import com.investments.tracker.domain.model.RawTransaction;

/**
 * Port interface for parsing broker-specific transaction history files.
 *
 * <p>Each broker adapter implements this interface to parse its export format into a list of raw
 * transactions. Symbol resolution is not the responsibility of the parser — it returns raw broker
 * instrument names as-is.
 */
public interface TransactionHistoryParser {

    /**
     * Parses a broker transaction history file into raw transactions.
     *
     * @param file the input stream of the broker export file
     * @return list of parsed raw transactions
     * @throws com.investments.tracker.domain.exception.ImportParsingException if the file cannot be
     *     parsed
     */
    List<RawTransaction> parse(InputStream file);

    /**
     * Returns the broker name this parser handles.
     *
     * @return the broker identifier (e.g., "mBank")
     */
    String brokerName();
}
