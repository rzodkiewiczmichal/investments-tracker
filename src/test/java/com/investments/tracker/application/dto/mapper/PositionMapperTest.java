package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
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
import com.investments.tracker.domain.service.PositionCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PositionMapper")
class PositionMapperTest {

    private static final Map<Currency, ExchangeRate> PLN_RATES =
            Map.of(Currency.PLN, ExchangeRate.identity(Currency.PLN));

    private PositionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PositionMapper(new PositionCalculationService());
    }

    @Nested
    @DisplayName("toSummaryDTO")
    class ToSummaryDTO {

        @Test
        @DisplayName("should map position to summary DTO with price")
        void shouldMapPositionToSummaryDTOWithPrice() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK);
            Price price = new Price(Money.pln(new BigDecimal("175.00")));

            // When
            PositionSummaryDTO dto = mapper.toSummaryDTO(position, instrument, price, PLN_RATES);

            // Then
            assertThat(dto.instrumentSymbol()).isEqualTo("AAPL");
            assertThat(dto.instrumentName()).isEqualTo("Apple Inc.");
            assertThat(dto.instrumentType()).isEqualTo("STOCK");
            assertThat(dto.quantity()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(dto.averageCost().amount()).isEqualByComparingTo(new BigDecimal("150.0000"));
            assertThat(dto.averageCost().currency()).isEqualTo("PLN");
            assertThat(dto.currentValue().amount()).isPositive();
            assertThat(dto.investedAmount().amount()).isPositive();
            assertThat(dto.profitLoss().amount()).isPositive();
            assertThat(dto.returnPercentage()).isPositive();
        }

        @Test
        @DisplayName("should map position to summary DTO with null price-dependent fields when no price")
        void shouldMapPositionToSummaryDTOWithNullFieldsWhenNoPrice() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK);

            // When
            PositionSummaryDTO dto = mapper.toSummaryDTO(position, instrument, null, PLN_RATES);

            // Then
            assertThat(dto.instrumentSymbol()).isEqualTo("AAPL");
            assertThat(dto.quantity()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(dto.investedAmount().amount()).isPositive();
            assertThat(dto.currentValue()).isNull();
            assertThat(dto.profitLoss()).isNull();
            assertThat(dto.returnPercentage()).isNull();
        }
    }

    @Nested
    @DisplayName("toDetailResponse")
    class ToDetailResponse {

        @Test
        @DisplayName("should map position to detail response with holdings")
        void shouldMapPositionToDetailResponseWithHoldings() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK);
            Price price = new Price(Money.pln(new BigDecimal("175.00")));
            Map<AccountId, String> accountNames = Map.of(new AccountId(1L), "My Account");

            // When
            PositionDetailResponse dto = mapper.toDetailResponse(position, instrument, price, accountNames, PLN_RATES);

            // Then
            assertThat(dto.instrumentSymbol()).isEqualTo("AAPL");
            assertThat(dto.instrumentName()).isEqualTo("Apple Inc.");
            assertThat(dto.instrumentType()).isEqualTo("STOCK");
            assertThat(dto.quantity()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(dto.holdings()).hasSize(1);
            assertThat(dto.holdings().getFirst().accountId()).isEqualTo(1L);
            assertThat(dto.holdings().getFirst().accountName()).isEqualTo("My Account");
        }

        @Test
        @DisplayName("should use 'Unknown Account' when account not in map")
        void shouldUseUnknownAccountWhenAccountNotInMap() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK);
            Price price = new Price(Money.pln(new BigDecimal("175.00")));
            Map<AccountId, String> accountNames = Map.of(); // Empty map

            // When
            PositionDetailResponse dto = mapper.toDetailResponse(position, instrument, price, accountNames, PLN_RATES);

            // Then
            assertThat(dto.holdings().getFirst().accountName()).isEqualTo("Unknown Account");
        }

        @Test
        @DisplayName("should have null price-dependent fields when no price")
        void shouldHaveNullFieldsWhenNoPrice() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK);
            Map<AccountId, String> accountNames = Map.of(new AccountId(1L), "My Account");

            // When
            PositionDetailResponse dto = mapper.toDetailResponse(position, instrument, null, accountNames, PLN_RATES);

            // Then
            assertThat(dto.currentPrice()).isNull();
            assertThat(dto.currentValue()).isNull();
            assertThat(dto.profitLoss()).isNull();
            assertThat(dto.returnPercentage()).isNull();
            assertThat(dto.investedAmount().amount()).isPositive();
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

    private Instrument createInstrument(String symbol, String name, InstrumentType type) {
        return new Instrument(
                new InstrumentSymbol(symbol),
                new InstrumentName(name),
                type,
                Currency.PLN);
    }
}
