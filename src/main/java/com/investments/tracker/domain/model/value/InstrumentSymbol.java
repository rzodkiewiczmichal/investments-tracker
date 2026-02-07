package com.investments.tracker.domain.model.value;

import com.investments.tracker.domain.exception.InvalidSymbolException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an instrument identifier.
 * <p>
 * Can be either a ticker symbol (e.g., "AAPL", "CDR") or an ISIN
 * (e.g., "US0378331005", "PLPKO0000016").
 * </p>
 * <p>
 * Used as natural key for Position and Instrument entities.
 * </p>
 */
public record InstrumentSymbol(String value) {

    /**
     * Pattern for ISIN validation (2 letters + 9 alphanumeric + 1 check digit).
     */
    private static final Pattern ISIN_PATTERN = Pattern.compile("^[A-Z]{2}[A-Z0-9]{9}[0-9]$");

    /**
     * Pattern for ticker symbol validation (1-10 uppercase letters and optionally digits).
     */
    private static final Pattern TICKER_PATTERN = Pattern.compile("^[A-Z0-9]{1,10}$");

    /**
     * Canonical constructor with validation.
     */
    public InstrumentSymbol {
        Objects.requireNonNull(value, "value cannot be null");
        if (value.isBlank()) {
            throw InvalidSymbolException.blank();
        }
        value = value.trim().toUpperCase();
        if (!isValidSymbol(value)) {
            throw InvalidSymbolException.invalidFormat(value);
        }
    }

    /**
     * Creates an InstrumentSymbol from a string.
     *
     * @param value the symbol string
     * @return new InstrumentSymbol
     */
    public static InstrumentSymbol of(String value) {
        return new InstrumentSymbol(value);
    }

    /**
     * Checks if this symbol is an ISIN.
     *
     * @return true if this is an ISIN
     */
    public boolean isIsin() {
        return ISIN_PATTERN.matcher(value).matches();
    }

    /**
     * Checks if this symbol is a ticker symbol.
     *
     * @return true if this is a ticker symbol (not an ISIN)
     */
    public boolean isTicker() {
        return !isIsin() && TICKER_PATTERN.matcher(value).matches();
    }

    private static boolean isValidSymbol(String symbol) {
        return ISIN_PATTERN.matcher(symbol).matches() || TICKER_PATTERN.matcher(symbol).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
