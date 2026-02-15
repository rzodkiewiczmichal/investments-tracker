package com.investments.tracker.infrastructure.external.stooq;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * DTO for a single row from the Stooq CSV price endpoint.
 *
 * <p>CSV format: Symbol,Date,Time,Open,High,Low,Close,Volume
 */
record StooqCsvRow(
        String symbol,
        String date,
        String time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume) {

    private static final String NOT_AVAILABLE = "N/D";

    /**
     * Parses a single CSV line into a StooqCsvRow.
     *
     * @param csvLine comma-separated values
     * @return parsed row, or empty if the line contains N/D (no data available)
     * @throws IllegalArgumentException if the line has an unexpected number of fields
     */
    static Optional<StooqCsvRow> parse(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length < 8) {
            throw new IllegalArgumentException(
                    "Expected at least 8 CSV fields, got: " + fields.length);
        }

        // Stooq returns "N/D" for symbols with no data
        if (NOT_AVAILABLE.equals(fields[1].trim()) || NOT_AVAILABLE.equals(fields[6].trim())) {
            return Optional.empty();
        }

        return Optional.of(
                new StooqCsvRow(
                        fields[0].trim().toUpperCase(),
                        fields[1].trim(),
                        fields[2].trim(),
                        new BigDecimal(fields[3].trim()),
                        new BigDecimal(fields[4].trim()),
                        new BigDecimal(fields[5].trim()),
                        new BigDecimal(fields[6].trim()),
                        Long.parseLong(fields[7].trim())));
    }
}
