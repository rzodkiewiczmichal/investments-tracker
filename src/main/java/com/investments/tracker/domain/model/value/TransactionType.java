package com.investments.tracker.domain.model.value;

/** Type of broker transaction — buy or sell. */
public enum TransactionType {
    BUY("Buy"),
    SELL("Sell");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
