package com.investments.tracker.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Detailed response for a single position. */
public record PositionDetailResponse(
        String instrumentSymbol,
        String instrumentName,
        String instrumentType,
        BigDecimal quantity,
        MoneyDTO averageCost,
        MoneyDTO currentPrice,
        MoneyDTO currentValue,
        MoneyDTO investedAmount,
        MoneyDTO profitLoss,
        BigDecimal returnPercentage,
        List<AccountHoldingDTO> holdings) {}
