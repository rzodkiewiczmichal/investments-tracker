package com.investments.tracker.domain.model.value;

/**
 * Supported currencies for the Investment Tracker.
 * <p>
 * For v0.1, only PLN (Polish Zloty) is supported.
 * Multi-currency support is planned for v2.0+.
 * </p>
 */
public enum Currency {
    /**
     * Polish Zloty - the default and only supported currency in v0.1.
     */
    PLN("PLN", "Polish Zloty", 2);

    private final String code;
    private final String displayName;
    private final int decimalPlaces;

    Currency(String code, String displayName, int decimalPlaces) {
        this.code = code;
        this.displayName = displayName;
        this.decimalPlaces = decimalPlaces;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }
}
