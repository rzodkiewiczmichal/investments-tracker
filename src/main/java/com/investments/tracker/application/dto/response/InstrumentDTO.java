package com.investments.tracker.application.dto.response;

/** DTO for an instrument. */
public record InstrumentDTO(String symbol, String name, String type, String currency) {}
