package com.investments.tracker.application.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Request to provide current prices for instruments that lack automatic price data. */
public record PriceOverrideRequest(@NotNull @Valid List<PriceEntryRequest> prices) {

    /** A single price entry: symbol and its current price. */
    public record PriceEntryRequest(
            @NotNull String symbol, @NotNull String price, @NotNull String currency) {}
}
