package com.investments.tracker.application.dto.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.investments.tracker.application.dto.response.AccountHoldingDTO;
import com.investments.tracker.application.dto.response.MoneyDTO;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionListResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.application.usecase.PositionDetailData;
import com.investments.tracker.application.usecase.PositionWithMarketData;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.CurrentValue;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InvestedAmount;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.ProfitAndLoss;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.service.PositionCalculationService;

/**
 * Mapper for Position domain objects to DTOs. All monetary values in the output are converted to
 * PLN.
 */
@Component
public class PositionMapper {

    private final PositionCalculationService positionCalculationService;

    public PositionMapper(PositionCalculationService positionCalculationService) {
        this.positionCalculationService = positionCalculationService;
    }

    /**
     * Maps a Position to PositionSummaryDTO, enriched with Instrument data. All monetary values are
     * converted to PLN.
     */
    public PositionSummaryDTO toSummaryDTO(
            Position position,
            Instrument instrument,
            @Nullable Price currentPrice,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        PositionCalculations calc =
                calculateMetrics(position, currentPrice, exchangeRatesByCurrency);

        return new PositionSummaryDTO(
                position.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                calc.totalQuantity.toBigDecimal(),
                toPlnMoneyDTO(calc.avgCostBasis.money(), calc.exchangeRate),
                calc.currentValue != null ? toMoneyDTO(calc.currentValue.money()) : null,
                toMoneyDTO(calc.investedAmount.money()),
                calc.profitAndLoss != null ? toMoneyDTO(calc.profitAndLoss.amount()) : null,
                calc.profitAndLoss != null ? calc.profitAndLoss.percentage().value() : null);
    }

    /**
     * Maps a list of positions with market data to a sorted list response. Sorted by current value
     * descending (FR-014).
     */
    public PositionListResponse toListResponse(List<PositionWithMarketData> data) {
        List<PositionSummaryDTO> summaries =
                data.stream()
                        .map(
                                d ->
                                        toSummaryDTO(
                                                d.position(),
                                                d.instrument(),
                                                d.currentPrice(),
                                                d.exchangeRates()))
                        .sorted(
                                Comparator.comparing(
                                        (PositionSummaryDTO dto) ->
                                                dto.currentValue() != null
                                                        ? dto.currentValue().amount()
                                                        : null,
                                        Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();
        return new PositionListResponse(summaries, summaries.size());
    }

    /** Maps position detail data to a detail response. All monetary values are converted to PLN. */
    public PositionDetailResponse toDetailResponse(PositionDetailData data) {
        return toDetailResponse(
                data.position(),
                data.instrument(),
                data.currentPrice(),
                data.accountNames(),
                data.exchangeRates());
    }

    /**
     * Maps a Position to PositionDetailResponse, enriched with Instrument data. All monetary values
     * are converted to PLN.
     */
    public PositionDetailResponse toDetailResponse(
            Position position,
            Instrument instrument,
            @Nullable Price currentPrice,
            Map<AccountId, String> accountNames,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        PositionCalculations calc =
                calculateMetrics(position, currentPrice, exchangeRatesByCurrency);

        List<AccountHoldingDTO> holdingDTOs =
                position.holdings().stream()
                        .map(
                                holding ->
                                        toAccountHoldingDTO(
                                                holding, accountNames, exchangeRatesByCurrency))
                        .toList();

        return new PositionDetailResponse(
                position.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                calc.totalQuantity.toBigDecimal(),
                toPlnMoneyDTO(calc.avgCostBasis.money(), calc.exchangeRate),
                currentPrice != null
                        ? toPlnMoneyDTO(currentPrice.money(), calc.exchangeRate)
                        : null,
                calc.currentValue != null ? toMoneyDTO(calc.currentValue.money()) : null,
                toMoneyDTO(calc.investedAmount.money()),
                calc.profitAndLoss != null ? toMoneyDTO(calc.profitAndLoss.amount()) : null,
                calc.profitAndLoss != null ? calc.profitAndLoss.percentage().value() : null,
                holdingDTOs);
    }

    private PositionCalculations calculateMetrics(
            Position position,
            @Nullable Price currentPrice,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        Quantity totalQuantity = position.calculateTotalQuantity();
        CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();
        InvestedAmount nativeInvested = position.calculateInvestedAmount();

        Currency costBasisCurrency = nativeInvested.currency();
        ExchangeRate costBasisRate = exchangeRatesByCurrency.get(costBasisCurrency);
        Objects.requireNonNull(
                costBasisRate, "Missing exchange rate for currency: " + costBasisCurrency);

        InvestedAmount investedAmount =
                new InvestedAmount(nativeInvested.money().convertTo(costBasisRate));

        CurrentValue currentValue = null;
        ProfitAndLoss profitAndLoss = null;

        if (currentPrice != null) {
            Currency priceCurrency = currentPrice.currency();
            ExchangeRate priceRate = exchangeRatesByCurrency.get(priceCurrency);
            Objects.requireNonNull(
                    priceRate, "Missing exchange rate for currency: " + priceCurrency);

            currentValue =
                    positionCalculationService.calculateCurrentValue(
                            position, currentPrice, priceRate);
            profitAndLoss = ProfitAndLoss.calculate(currentValue, investedAmount);
        }

        return new PositionCalculations(
                totalQuantity,
                avgCostBasis,
                currentValue,
                investedAmount,
                profitAndLoss,
                costBasisRate);
    }

    private AccountHoldingDTO toAccountHoldingDTO(
            AccountHolding holding,
            Map<AccountId, String> accountNames,
            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        String accountName = accountNames.getOrDefault(holding.accountId(), "Unknown Account");
        Currency currency = holding.costBasis().currency();
        ExchangeRate rate = exchangeRatesByCurrency.get(currency);
        Objects.requireNonNull(rate, "Missing exchange rate for currency: " + currency);

        return new AccountHoldingDTO(
                holding.accountId().value(),
                accountName,
                holding.quantity().toBigDecimal(),
                toPlnMoneyDTO(holding.costBasis().money(), rate));
    }

    /** Converts Money to PLN using the exchange rate, then to MoneyDTO. */
    private MoneyDTO toPlnMoneyDTO(Money money, ExchangeRate exchangeRate) {
        Money plnMoney = money.convertTo(exchangeRate);
        return new MoneyDTO(plnMoney.amount(), plnMoney.currency().getCode());
    }

    /** Converts Money (already in PLN) to MoneyDTO. */
    private MoneyDTO toMoneyDTO(Money money) {
        return new MoneyDTO(money.amount(), money.currency().getCode());
    }

    private record PositionCalculations(
            Quantity totalQuantity,
            CostBasis avgCostBasis,
            @Nullable CurrentValue currentValue,
            InvestedAmount investedAmount,
            @Nullable ProfitAndLoss profitAndLoss,
            ExchangeRate exchangeRate) {}
}
