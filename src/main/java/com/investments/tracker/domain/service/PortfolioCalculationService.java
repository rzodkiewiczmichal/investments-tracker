package com.investments.tracker.domain.service;

import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.PortfolioMetrics;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain service for portfolio-level calculations.
 * <p>
 * Aggregates metrics from individual positions to provide
 * portfolio-level totals and summaries. All monetary values are
 * converted to PLN using provided exchange rates.
 * </p>
 * <p>
 * This is a stateless domain service with no dependencies on infrastructure.
 * </p>
 */
public class PortfolioCalculationService {

    private final PositionCalculationService positionCalculationService;

    public PortfolioCalculationService(PositionCalculationService positionCalculationService) {
        this.positionCalculationService = Objects.requireNonNull(
                positionCalculationService, "positionCalculationService cannot be null");
    }

    /**
     * Creates a Portfolio from positions, prices, and exchange rates.
     * All monetary metrics are calculated in PLN using current exchange rates.
     *
     * @param positions the list of positions
     * @param currentPrices map of instrument symbol to current price (in native currency)
     * @param exchangeRatesByCurrency map of currency to exchange rate to PLN
     * @return the Portfolio with PLN-denominated metrics
     */
    public Portfolio createPortfolio(
            List<Position> positions,
            Map<InstrumentSymbol, Price> currentPrices,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {

        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(currentPrices, "currentPrices cannot be null");
        Objects.requireNonNull(exchangeRatesByCurrency, "exchangeRatesByCurrency cannot be null");

        if (positions.isEmpty()) {
            return new Portfolio(List.of(), PortfolioMetrics.empty());
        }

        PortfolioMetrics metrics = calculateMetrics(positions, currentPrices, exchangeRatesByCurrency);
        return new Portfolio(positions, metrics);
    }

    /**
     * Calculates total invested amount across all positions in PLN.
     *
     * @param positions the list of positions
     * @param exchangeRatesByCurrency map of currency to exchange rate to PLN
     * @return optional invested amount in PLN (empty if no positions)
     */
    public Optional<InvestedAmount> calculateTotalInvestedAmount(
            List<Position> positions,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {

        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(exchangeRatesByCurrency, "exchangeRatesByCurrency cannot be null");

        if (positions.isEmpty()) {
            return Optional.empty();
        }

        return positions.stream()
                .map(p -> {
                    InvestedAmount nativeAmount = p.calculateInvestedAmount();
                    Currency currency = nativeAmount.currency();
                    ExchangeRate rate = exchangeRatesByCurrency.get(currency);
                    Objects.requireNonNull(rate,
                            "Missing exchange rate for currency: " + currency);
                    Money plnMoney = nativeAmount.money().convertTo(rate);
                    return new InvestedAmount(plnMoney);
                })
                .reduce(InvestedAmount::add);
    }

    /**
     * Calculates total current value across all positions in PLN.
     * Returns empty if any position lacks a current price.
     *
     * @param positions the list of positions
     * @param currentPrices map of instrument symbol to current price
     * @param exchangeRatesByCurrency map of currency to exchange rate to PLN
     * @return optional current value in PLN (empty if any position lacks price)
     */
    public Optional<CurrentValue> calculateTotalCurrentValue(
            List<Position> positions,
            Map<InstrumentSymbol, Price> currentPrices,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {

        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(currentPrices, "currentPrices cannot be null");
        Objects.requireNonNull(exchangeRatesByCurrency, "exchangeRatesByCurrency cannot be null");

        if (positions.isEmpty()) {
            return Optional.empty();
        }

        boolean allPricesAvailable = positions.stream()
                .allMatch(p -> currentPrices.containsKey(p.symbol()));

        if (!allPricesAvailable) {
            return Optional.empty();
        }

        return positions.stream()
                .map(p -> {
                    Price price = currentPrices.get(p.symbol());
                    Currency currency = price.currency();
                    ExchangeRate rate = exchangeRatesByCurrency.get(currency);
                    Objects.requireNonNull(rate,
                            "Missing exchange rate for currency: " + currency);
                    return positionCalculationService.calculateCurrentValue(p, price, rate);
                })
                .reduce(CurrentValue::add);
    }

    /**
     * Calculates total P&L across all positions in PLN.
     * Returns empty if any position lacks a current price.
     *
     * @param positions the list of positions
     * @param currentPrices map of instrument symbol to current price
     * @param exchangeRatesByCurrency map of currency to exchange rate to PLN
     * @return optional P&L in PLN (empty if prices are incomplete)
     */
    public Optional<ProfitAndLoss> calculateTotalProfitAndLoss(
            List<Position> positions,
            Map<InstrumentSymbol, Price> currentPrices,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {

        Objects.requireNonNull(positions, "positions cannot be null");
        Objects.requireNonNull(currentPrices, "currentPrices cannot be null");
        Objects.requireNonNull(exchangeRatesByCurrency, "exchangeRatesByCurrency cannot be null");

        Optional<InvestedAmount> totalInvested = calculateTotalInvestedAmount(positions, exchangeRatesByCurrency);
        Optional<CurrentValue> totalCurrentValue = calculateTotalCurrentValue(
                positions, currentPrices, exchangeRatesByCurrency);

        if (totalInvested.isEmpty() || totalCurrentValue.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ProfitAndLoss.calculate(totalCurrentValue.get(), totalInvested.get()));
    }

    private PortfolioMetrics calculateMetrics(
            List<Position> positions,
            Map<InstrumentSymbol, Price> currentPrices,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {

        InvestedAmount totalInvested = calculateTotalInvestedAmount(
                positions, exchangeRatesByCurrency).orElse(null);
        CurrentValue totalCurrentValue = calculateTotalCurrentValue(
                positions, currentPrices, exchangeRatesByCurrency).orElse(null);

        ProfitAndLoss profitAndLoss = null;
        if (totalInvested != null && totalCurrentValue != null) {
            profitAndLoss = ProfitAndLoss.calculate(totalCurrentValue, totalInvested);
        }

        return new PortfolioMetrics(
                totalInvested,
                totalCurrentValue,
                profitAndLoss,
                positions.size());
    }
}
