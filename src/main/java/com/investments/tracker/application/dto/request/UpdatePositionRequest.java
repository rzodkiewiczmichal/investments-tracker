package com.investments.tracker.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request to update a position by adding more shares. */
public record UpdatePositionRequest(
        @NotNull(message = "Account ID is required") Long accountId,
        @NotNull(message = "Quantity is required")
                @Positive(message = "Quantity must be greater than zero")
                BigDecimal quantity,
        @NotNull(message = "Average cost is required")
                @Positive(message = "Average cost must be greater than zero")
                BigDecimal averageCost) {}
