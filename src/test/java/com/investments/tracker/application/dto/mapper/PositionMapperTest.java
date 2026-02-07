package com.investments.tracker.application.dto.mapper;

import com.investments.tracker.application.dto.response.PositionDetailResponse;
import com.investments.tracker.application.dto.response.PositionSummaryDTO;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
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

    private PositionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PositionMapper();
    }

    @Nested
    @DisplayName("toSummaryDTO")
    class ToSummaryDTO {

        @Test
        @DisplayName("should map position to summary DTO")
        void shouldMapPositionToSummaryDTO() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00", "175.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK, "175.00");

            // When
            PositionSummaryDTO dto = mapper.toSummaryDTO(position, instrument);

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
    }

    @Nested
    @DisplayName("toDetailResponse")
    class ToDetailResponse {

        @Test
        @DisplayName("should map position to detail response with holdings")
        void shouldMapPositionToDetailResponseWithHoldings() {
            // Given
            Position position = createPosition("AAPL", 100, "150.00", "175.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK, "175.00");
            Map<AccountId, String> accountNames = Map.of(new AccountId(1L), "My Account");

            // When
            PositionDetailResponse dto = mapper.toDetailResponse(position, instrument, accountNames);

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
            Position position = createPosition("AAPL", 100, "150.00", "175.00");
            Instrument instrument = createInstrument("AAPL", "Apple Inc.", InstrumentType.STOCK, "175.00");
            Map<AccountId, String> accountNames = Map.of(); // Empty map

            // When
            PositionDetailResponse dto = mapper.toDetailResponse(position, instrument, accountNames);

            // Then
            assertThat(dto.holdings().getFirst().accountName()).isEqualTo("Unknown Account");
        }
    }

    private Position createPosition(String symbol, int qty, String costBasis, String price) {
        return new Position(
                InstrumentSymbol.of(symbol),
                List.of(new AccountHolding(
                        new AccountId(1L),
                        Quantity.of(qty),
                        CostBasis.of(Money.pln(costBasis)))),
                new Price(Money.pln(price)));
    }

    private Instrument createInstrument(String symbol, String name, InstrumentType type, String price) {
        return new Instrument(
                new InstrumentSymbol(symbol),
                new InstrumentName(name),
                type,
                new Price(Money.pln(new BigDecimal(price))));
    }
}
