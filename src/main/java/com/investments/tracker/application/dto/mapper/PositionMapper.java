package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.AccountHoldingDTO;
import com.investments.tracker.application.dto.response.MoneyDTO;
import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mapper for Position domain objects to DTOs.
 */
@Component
public class PositionMapper {

    private final PositionCalculationService positionCalculationService;

    public PositionMapper(PositionCalculationService positionCalculationService) {
        this.positionCalculationService = positionCalculationService;
    }

    /**
     * Maps a Position to PositionSummaryDTO, enriched with Instrument data.
     * Aggregated monetary values (invested amount, current value, P&L) are converted to PLN.
     *
     * @param position the position domain object
     * @param instrument the instrument providing name, type, and current price
     * @param exchangeRatesByCurrency exchange rates to PLN keyed by source currency
     * @return the position summary DTO
     */
    public PositionSummaryDTO toSummaryDTO(Position position, Instrument instrument,
                                            Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        PositionCalculations calc = calculateMetrics(position, instrument.currentPrice(), exchangeRatesByCurrency);

        return new PositionSummaryDTO(
                position.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                calc.totalQuantity.toBigDecimal(),
                toMoneyDTO(calc.avgCostBasis.money()),
                calc.currentValue != null ? toMoneyDTO(calc.currentValue.money()) : null,
                toMoneyDTO(calc.investedAmount.money()),
                calc.profitAndLoss != null ? toMoneyDTO(calc.profitAndLoss.amount()) : null,
                calc.profitAndLoss != null ? calc.profitAndLoss.percentage().value() : null);
    }

    /**
     * Maps a Position to PositionDetailResponse, enriched with Instrument data.
     * Aggregated monetary values (invested amount, current value, P&L) are converted to PLN.
     *
     * @param position the position domain object
     * @param instrument the instrument providing name, type, and current price
     * @param accountNames map of account IDs to names for display
     * @param exchangeRatesByCurrency exchange rates to PLN keyed by source currency
     * @return the position detail response
     */
    public PositionDetailResponse toDetailResponse(Position position, Instrument instrument,
                                                   Map<AccountId, String> accountNames,
                                                   Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        Price currentPrice = instrument.currentPrice();
        PositionCalculations calc = calculateMetrics(position, currentPrice, exchangeRatesByCurrency);

        List<AccountHoldingDTO> holdingDTOs = position.holdings().stream()
                .map(holding -> toAccountHoldingDTO(holding, accountNames))
                .toList();

        return new PositionDetailResponse(
                position.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                calc.totalQuantity.toBigDecimal(),
                toMoneyDTO(calc.avgCostBasis.money()),
                currentPrice != null ? toMoneyDTO(currentPrice.money()) : null,
                calc.currentValue != null ? toMoneyDTO(calc.currentValue.money()) : null,
                toMoneyDTO(calc.investedAmount.money()),
                calc.profitAndLoss != null ? toMoneyDTO(calc.profitAndLoss.amount()) : null,
                calc.profitAndLoss != null ? calc.profitAndLoss.percentage().value() : null,
                holdingDTOs);
    }

    private PositionCalculations calculateMetrics(Position position, @Nullable Price currentPrice,
                                                   Map<Currency, ExchangeRate> exchangeRatesByCurrency) {
        Quantity totalQuantity = position.calculateTotalQuantity();
        CostBasis avgCostBasis = position.calculateWeightedAverageCostBasis();
        InvestedAmount nativeInvested = position.calculateInvestedAmount();

        Currency nativeCurrency = nativeInvested.currency();
        ExchangeRate exchangeRate = exchangeRatesByCurrency.get(nativeCurrency);
        Objects.requireNonNull(exchangeRate,
                "Missing exchange rate for currency: " + nativeCurrency);

        InvestedAmount investedAmount = new InvestedAmount(nativeInvested.money().convertTo(exchangeRate));

        CurrentValue currentValue = null;
        ProfitAndLoss profitAndLoss = null;

        if (currentPrice != null) {
            currentValue = positionCalculationService.calculateCurrentValue(position, currentPrice, exchangeRate);
            profitAndLoss = positionCalculationService.calculateProfitAndLoss(position, currentPrice, exchangeRate);
        }

        return new PositionCalculations(
                totalQuantity, avgCostBasis, currentValue, investedAmount, profitAndLoss);
    }

    private AccountHoldingDTO toAccountHoldingDTO(AccountHolding holding, Map<AccountId, String> accountNames) {
        String accountName = accountNames.getOrDefault(holding.accountId(), "Unknown Account");

        return new AccountHoldingDTO(
                holding.accountId().value(),
                accountName,
                holding.quantity().toBigDecimal(),
                toMoneyDTO(holding.costBasis().money()));
    }

    /** Converts Money to MoneyDTO. */
    private MoneyDTO toMoneyDTO(Money money) {
        return new MoneyDTO(money.amount(), money.currency().getCode());
    }

    private record PositionCalculations(
            Quantity totalQuantity,
            CostBasis avgCostBasis,
            @Nullable CurrentValue currentValue,
            InvestedAmount investedAmount,
            @Nullable ProfitAndLoss profitAndLoss) {
    }
}
