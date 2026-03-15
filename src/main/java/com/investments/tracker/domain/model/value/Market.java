package com.investments.tracker.domain.model.value;

/** Market where a financial instrument is traded, used for price provider routing. */
public enum Market {
    /** Warsaw Stock Exchange (GPW) — prices from Stooq.pl. */
    GPW("Warsaw Stock Exchange"),

    /** NYSE / NASDAQ — prices from Finnhub. */
    US("NYSE / NASDAQ");

    private final String displayName;

    Market(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
