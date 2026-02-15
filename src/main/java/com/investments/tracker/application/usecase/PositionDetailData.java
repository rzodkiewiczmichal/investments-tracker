package com.investments.tracker.application.usecase;

import java.util.Map;
import java.util.Objects;

import org.springframework.lang.Nullable;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.Price;

/**
 * Application-layer record bundling a position with its market data and account names for detail
 * display. Used by {@link PositionQueryUseCase#getPositionDetail}.
 */
public record PositionDetailData(
        Position position,
        Instrument instrument,
        @Nullable Price currentPrice,
        Map<AccountId, String> accountNames,
        Map<Currency, ExchangeRate> exchangeRates) {

    public PositionDetailData {
        Objects.requireNonNull(position, "position cannot be null");
        Objects.requireNonNull(instrument, "instrument cannot be null");
        Objects.requireNonNull(accountNames, "accountNames cannot be null");
        Objects.requireNonNull(exchangeRates, "exchangeRates cannot be null");
    }
}
