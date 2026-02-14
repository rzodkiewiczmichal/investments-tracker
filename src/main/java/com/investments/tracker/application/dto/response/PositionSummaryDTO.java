package com.investments.tracker.application.dto.response;

import java.math.BigDecimal;

/** Summary DTO for a position (used in list view). */
public record PositionSummaryDTO(
        String instrumentSymbol,
        String instrumentName,
        String instrumentType,
        BigDecimal quantity,
        MoneyDTO averageCost,
        MoneyDTO currentValue,
        MoneyDTO investedAmount,
        MoneyDTO profitLoss,
        BigDecimal returnPercentage) {}
