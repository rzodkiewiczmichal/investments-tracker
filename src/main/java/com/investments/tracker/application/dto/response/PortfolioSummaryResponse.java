package com.investments.tracker.application.dto.response;

import java.math.BigDecimal;

/** Response containing aggregated portfolio metrics. */
public record PortfolioSummaryResponse(
        MoneyDTO totalCurrentValue,
        MoneyDTO totalInvestedAmount,
        MoneyDTO totalProfitLoss,
        BigDecimal totalReturnPercentage,
        int positionsCount,
        String message) {}
