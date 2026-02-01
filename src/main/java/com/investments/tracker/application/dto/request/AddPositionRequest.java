package com.investments.tracker.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request to add a new position or add shares to existing position.
 */
public record AddPositionRequest(
        @NotBlank(message = "Instrument symbol is required")
        String instrumentSymbol,

        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @NotNull(message = "Cost basis is required")
        @Positive(message = "Average cost must be greater than zero")
        BigDecimal costBasis,

        @NotNull(message = "Current price is required")
        @Positive(message = "Current price must be greater than zero")
        BigDecimal currentPrice) {
}
