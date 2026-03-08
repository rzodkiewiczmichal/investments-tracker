package com.investments.tracker.application.dto.response;

/** An instrument that needs a user-provided current price. */
public record InstrumentNeedingPriceDTO(String symbol, String name, String currency) {}
