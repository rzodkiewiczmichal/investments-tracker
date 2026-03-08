package com.investments.tracker.infrastructure.external.mbank;

import java.math.BigDecimal;

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

    static MBankCsvRow parse(String line, int lineNumber) {
        String[] fields = line.split(";", -1);
        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw ImportParsingException.invalidFormat(
                    lineNumber,
                    "expected " + EXPECTED_FIELD_COUNT + " fields, got " + fields.length);
        }

        try {
            return new MBankCsvRow(
                    fields[0].trim(),
                    fields[1].trim(),
                    fields[2].trim(),
                    fields[3].trim(),
                    parsePolishNumber(fields[4]),
                    parsePolishNumber(fields[5]),
                    parseCurrency(fields[6].trim(), lineNumber),
                    parsePolishNumber(fields[7]),
                    parseCurrency(fields[8].trim(), lineNumber));
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

    private static Currency parseCurrency(String value, int lineNumber) {
        try {
            return Currency.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw ImportParsingException.invalidFormat(
                    lineNumber, "unsupported currency: " + value);
        }
    }
}
