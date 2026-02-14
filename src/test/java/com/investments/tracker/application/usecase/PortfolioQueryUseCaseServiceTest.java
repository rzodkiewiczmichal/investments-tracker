package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Portfolio;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.ExchangeRateProvider;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.PortfolioCalculationService;
import com.investments.tracker.domain.service.PositionCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("PortfolioQueryUseCaseService")
@ExtendWith(MockitoExtension.class)
class PortfolioQueryUseCaseServiceTest {

    private static final Map<Currency, ExchangeRate> PLN_RATES = Map.of(
            Currency.PLN, ExchangeRate.identity(Currency.PLN));

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    private PortfolioCalculationService portfolioCalculationService;
    private PortfolioQueryUseCaseService portfolioQueryUseCaseService;

    @BeforeEach
    void setUp() {
        portfolioCalculationService = new PortfolioCalculationService(new PositionCalculationService());
        portfolioQueryUseCaseService = new PortfolioQueryUseCaseService(
                positionRepository, instrumentRepository, exchangeRateProvider, portfolioCalculationService);
    }

    @Nested
    @DisplayName("getPortfolio")
    class GetPortfolio {

        @Test
        @DisplayName("should return empty portfolio when no positions")
        void shouldReturnEmptyPortfolioWhenNoPositions() {
            // Given
            when(positionRepository.findAll()).thenReturn(List.of());
            when(instrumentRepository.findAll()).thenReturn(List.of());
            when(exchangeRateProvider.getExchangeRatesToPln(any())).thenReturn(PLN_RATES);

            // When
            Portfolio portfolio = portfolioQueryUseCaseService.getPortfolio();

            // Then
            assertThat(portfolio.positions()).isEmpty();
            assertThat(portfolio.metrics().totalPositions()).isZero();
            assertThat(portfolio.getTotalCurrentValue()).isEmpty();
        }

        @Test
        @DisplayName("should return portfolio with positions and prices")
        void shouldReturnPortfolioWithPositionsAndPrices() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = new Instrument(
                    InstrumentSymbol.of("AAPL"),
                    new InstrumentName("Apple Inc."),
                    InstrumentType.STOCK,
                    Currency.PLN,
                    new Price(Money.pln("175.00")));
            when(positionRepository.findAll()).thenReturn(List.of(position));
            when(instrumentRepository.findAll()).thenReturn(List.of(instrument));
            when(exchangeRateProvider.getExchangeRatesToPln(any())).thenReturn(PLN_RATES);

            // When
            Portfolio portfolio = portfolioQueryUseCaseService.getPortfolio();

            // Then
            assertThat(portfolio.positions()).hasSize(1);
            assertThat(portfolio.metrics().totalPositions()).isEqualTo(1);
            assertThat(portfolio.getTotalCurrentValue()).isPresent();
            assertThat(portfolio.getTotalCurrentValue().get().money().amount()).isPositive();
            assertThat(portfolio.getTotalInvestedAmount()).isPresent();
            assertThat(portfolio.getTotalInvestedAmount().get().money().amount()).isPositive();
        }

        @Test
        @DisplayName("should return portfolio without current value when prices unknown")
        void shouldReturnPortfolioWithoutCurrentValueWhenPricesUnknown() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = new Instrument(
                    InstrumentSymbol.of("AAPL"),
                    new InstrumentName("Apple Inc."),
                    InstrumentType.STOCK,
                    Currency.PLN,
                    null);
            when(positionRepository.findAll()).thenReturn(List.of(position));
            when(instrumentRepository.findAll()).thenReturn(List.of(instrument));
            when(exchangeRateProvider.getExchangeRatesToPln(any())).thenReturn(PLN_RATES);

            // When
            Portfolio portfolio = portfolioQueryUseCaseService.getPortfolio();

            // Then
            assertThat(portfolio.positions()).hasSize(1);
            assertThat(portfolio.getTotalInvestedAmount()).isPresent();
            assertThat(portfolio.getTotalCurrentValue()).isEmpty();
            assertThat(portfolio.getTotalProfitAndLoss()).isEmpty();
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(
                        new AccountId(1L),
                        Quantity.of(qty),
                        CostBasis.of(Money.pln(costBasis)))));
    }
}
