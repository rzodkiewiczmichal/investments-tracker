package com.investments.tracker.domain.model.value;

/** Supported currencies for the Investment Tracker. */
public enum Currency {
    PLN("PLN", "Polish Zloty", 2),
    EUR("EUR", "Euro", 2),
    GBP("GBP", "British Pound Sterling", 2),
    USD("USD", "US Dollar", 2);

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
