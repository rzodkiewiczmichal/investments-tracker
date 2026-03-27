package com.investments.tracker.domain.model.value;

/** Market where a financial instrument is traded, used for price provider routing. */
public enum Market {
    /** Warsaw Stock Exchange (GPW) — prices from Stooq.pl. */
    GPW("Warsaw Stock Exchange"),

    /** NYSE / NASDAQ — prices from Finnhub. */
    US("NYSE / NASDAQ"),

    /** London Stock Exchange — no automatic price provider yet. */
    UK("London Stock Exchange"),

    /** XETRA (Deutsche Börse) — no automatic price provider yet. */
    DE("XETRA");

    private final String displayName;

    Market(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
