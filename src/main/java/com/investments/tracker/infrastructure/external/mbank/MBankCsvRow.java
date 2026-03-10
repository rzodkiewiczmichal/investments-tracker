package com.investments.tracker.infrastructure.external.mbank;

import java.math.BigDecimal;
import java.util.Optional;

import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.value.Currency;

/**
 * Package-private record representing a single parsed row from an mBank CSV export.
 *
 * <p>Handles semicolon-delimited fields and Polish number format (comma decimal separator, space
 * thousands separator).
 */
record MBankCsvRow(
        String timestamp,
        String instrumentName,
        String exchange,
        String side,
        BigDecimal quantity,
        BigDecimal unitPrice,
        Currency priceCurrency,
        BigDecimal commission,
        Currency commissionCurrency) {

    private static final int EXPECTED_FIELD_COUNT = 11;

    /**
     * Parses a CSV line into a row. Returns empty if the row uses an unsupported currency (e.g.,
     * PKT for futures contracts), as these instruments are outside the tracking scope.
     */
    static Optional<MBankCsvRow> parse(String line, int lineNumber) {
        String[] fields = line.split(";", -1);
        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw ImportParsingException.invalidFormat(
                    lineNumber,
                    "expected " + EXPECTED_FIELD_COUNT + " fields, got " + fields.length);
        }

        try {
            Optional<Currency> priceCurrency = parseCurrency(fields[6].trim());
            Optional<Currency> commissionCurrency = parseCurrency(fields[8].trim());

            if (priceCurrency.isEmpty() || commissionCurrency.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(
                    new MBankCsvRow(
                            fields[0].trim(),
                            fields[1].trim(),
                            fields[2].trim(),
                            fields[3].trim(),
                            parsePolishNumber(fields[4]),
                            parsePolishNumber(fields[5]),
                            priceCurrency.get(),
                            parsePolishNumber(fields[7]),
                            commissionCurrency.get()));
        } catch (ImportParsingException e) {
            throw e;
        } catch (Exception e) {
            throw ImportParsingException.invalidFormat(lineNumber, e.getMessage());
        }
    }

    /**
     * Parses a Polish-formatted number: removes space thousands separator, replaces comma decimal
     * separator with dot.
     */
    static BigDecimal parsePolishNumber(String value) {
        String normalized = value.trim().replace(" ", "").replace(",", ".");
        return new BigDecimal(normalized);
    }

    private static Optional<Currency> parseCurrency(String value) {
        try {
            return Optional.of(Currency.valueOf(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
