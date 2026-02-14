package com.investments.tracker.domain.model.value;

/** Types of financial instruments supported by the Investment Tracker. */
public enum InstrumentType {
    /** Individual stock (equity). */
    STOCK("Stock"),

    /** Exchange-Traded Fund (can be stock ETF or bond ETF). */
    ETF("ETF"),

    /** Polish government bond. */
    BOND("Bond");

    private final String displayName;

    InstrumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
