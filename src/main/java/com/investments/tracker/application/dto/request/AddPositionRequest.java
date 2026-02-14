package com.investments.tracker.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request to add a new position or add shares to existing position. */
public record AddPositionRequest(
        @NotBlank(message = "Instrument symbol is required") String instrumentSymbol,
        @NotBlank(message = "Instrument name is required") String instrumentName,
        @NotBlank(message = "Instrument type is required") String instrumentType,
        @NotBlank(message = "Currency is required") String currency,
        @NotNull(message = "Account ID is required") Long accountId,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive")
                BigDecimal quantity,
        @NotNull(message = "Average cost is required")
                @Positive(message = "Average cost must be greater than zero")
                BigDecimal averageCost) {}
