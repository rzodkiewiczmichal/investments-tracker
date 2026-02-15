package com.investments.tracker.infrastructure.external.stooq;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

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

    /** Stooq uses "N/D" (Nie Dostępne) and "B/D" (Brak Danych) for unavailable data. */
    private static final Set<String> NO_DATA_MARKERS = Set.of("N/D", "B/D");

    /**
     * Parses a single CSV line into a StooqCsvRow.
     *
     * @param csvLine comma-separated values
     * @return parsed row, or empty if the line contains no-data markers (N/D or B/D)
     * @throws IllegalArgumentException if the line has an unexpected number of fields
     */
    static Optional<StooqCsvRow> parse(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length < 8) {
            throw new IllegalArgumentException(
                    "Expected at least 8 CSV fields, got: " + fields.length);
        }

        // Stooq returns "N/D" or "B/D" for symbols with no data
        if (NO_DATA_MARKERS.contains(fields[1].trim())
                || NO_DATA_MARKERS.contains(fields[6].trim())) {
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
