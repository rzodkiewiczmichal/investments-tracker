package com.investments.tracker.application.dto.request;

import jakarta.validation.constraints.NotNull;

/** A single mapping from broker instrument name to catalog symbol. */
public record MappingEntryRequest(@NotNull String brokerName, @NotNull String catalogSymbol) {}
