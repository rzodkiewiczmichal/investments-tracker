package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.MoneyDTO;
import com.investments.tracker.application.dto.response.PortfolioSummaryResponse;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.PortfolioMetrics;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for Portfolio domain objects to DTOs.
 */
@Component
public class PortfolioMapper {

    private static final String DEFAULT_CURRENCY = Currency.PLN.getCode();

    /**
     * Maps a Portfolio to PortfolioSummaryResponse.
     *
     * @param portfolio the portfolio domain object
     * @return the portfolio summary response
     */
    public PortfolioSummaryResponse toSummaryResponse(Portfolio portfolio) {
        PortfolioMetrics metrics = portfolio.metrics();

        MoneyDTO totalCurrentValue = toMoneyDTO(metrics.totalCurrentValue().money());

        // PortfolioMetrics allows null for empty portfolios (by design)
        MoneyDTO totalInvestedAmount = metrics.totalInvestedAmount() != null
                ? toMoneyDTO(metrics.totalInvestedAmount().money())
                : new MoneyDTO(BigDecimal.ZERO, DEFAULT_CURRENCY);

        MoneyDTO totalProfitLoss = metrics.profitAndLoss() != null
                ? toMoneyDTO(metrics.profitAndLoss().amount())
                : new MoneyDTO(BigDecimal.ZERO, DEFAULT_CURRENCY);

        BigDecimal returnPercentage = metrics.profitAndLoss() != null
                ? metrics.profitAndLoss().percentage().value()
                : BigDecimal.ZERO;

        String message = metrics.totalPositions() == 0 ? "No positions found" : null;

        return new PortfolioSummaryResponse(
                totalCurrentValue,
                totalInvestedAmount,
                totalProfitLoss,
                returnPercentage,
                metrics.totalPositions(),
                message);
    }

    /** Converts Money to MoneyDTO. */
    private MoneyDTO toMoneyDTO(Money money) {
        return new MoneyDTO(money.amount(), money.currency().getCode());
    }
}
