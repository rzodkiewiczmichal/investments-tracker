package com.investments.tracker.application.dto.response;

import java.math.BigDecimal;

/**
 * Summary DTO for a position (used in list view).
 */
public record PositionSummaryDTO(
        String symbol,
        BigDecimal totalQuantity,
        MoneyDTO averageCost,
        MoneyDTO currentPrice,
        MoneyDTO currentValue,
        MoneyDTO investedAmount,
        MoneyDTO profitLoss,
        BigDecimal returnPercentage) {
}
