package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.Price;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Application-layer record bundling a position with its market data
 * for list display. Used by {@link PositionQueryUseCase#listPositionsWithMarketData()}.
 */
public record PositionWithMarketData(
        Position position,
        Instrument instrument,
        @Nullable Price currentPrice,
        Map<Currency, ExchangeRate> exchangeRates) {

    public PositionWithMarketData {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(instrument, "instrument cannot be null");
        Objects.requireNonNull(exchangeRates, "exchangeRates cannot be null");
    }
}
