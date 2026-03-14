package com.investments.tracker.infrastructure.external.finnhub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Finnhub stock symbol API response element. */
@JsonIgnoreProperties(ignoreUnknown = true)
record FinnhubSymbolResponse(
        @JsonProperty("symbol") String symbol,
        @JsonProperty("description") String description,
        @JsonProperty("currency") String currency,
        @JsonProperty("type") String type) {}
