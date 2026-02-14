package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("PositionCommandUseCaseService")
@ExtendWith(MockitoExtension.class)
class PositionCommandUseCaseServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    private PositionCommandUseCaseService positionCommandUseCaseService;

    @BeforeEach
    void setUp() {
        positionCommandUseCaseService = new PositionCommandUseCaseService(
                positionRepository, accountRepository, instrumentRepository);
    }

    @Nested
    @DisplayName("addPosition")
    class AddPosition {

        @Test
        @DisplayName("should create new position and instrument when none exists")
        void shouldCreateNewPositionAndInstrumentWhenNoneExists() {
            // Given
            AccountId accountId = new AccountId(1L);
            when(accountRepository.existsById(accountId)).thenReturn(true);
            when(instrumentRepository.existsBySymbol(any())).thenReturn(false);
            when(positionRepository.findBySymbol(any())).thenReturn(Optional.empty());
            when(positionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
            InstrumentName instrumentName = new InstrumentName("Apple Inc.");
            InstrumentType instrumentType = InstrumentType.STOCK;
            Currency currency = Currency.PLN;
            Quantity quantity = Quantity.of(100);
            CostBasis costBasis = CostBasis.of(Money.pln("150.00"));

            // When
            Position result = positionCommandUseCaseService.addPosition(
                    symbol, instrumentName, instrumentType, currency, accountId, quantity, costBasis);

            // Then
            assertThat(result.symbol().value()).isEqualTo("AAPL");
            assertThat(result.calculateTotalQuantity().value()).isEqualByComparingTo("100");

            ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
            verify(positionRepository).save(captor.capture());
            Position savedPosition = captor.getValue();
            assertThat(savedPosition.symbol().value()).isEqualTo("AAPL");

            // Verify instrument was created as pure reference data
            ArgumentCaptor<Instrument> instrumentCaptor = ArgumentCaptor.forClass(Instrument.class);
            verify(instrumentRepository).save(instrumentCaptor.capture());
            Instrument savedInstrument = instrumentCaptor.getValue();
            assertThat(savedInstrument.symbol().value()).isEqualTo("AAPL");
            assertThat(savedInstrument.name().value()).isEqualTo("Apple Inc.");
            assertThat(savedInstrument.type()).isEqualTo(InstrumentType.STOCK);
            assertThat(savedInstrument.currency()).isEqualTo(Currency.PLN);
        }

        @Test
        @DisplayName("should add to existing position without creating instrument")
        void shouldAddToExistingPositionWithoutCreatingInstrument() {
            // Given
            Position existingPosition = createPosition("AAPL", 50, "140.00");
            Instrument existingInstrument = new Instrument(
                    InstrumentSymbol.of("AAPL"), new InstrumentName("Apple Inc."),
                    InstrumentType.STOCK, Currency.PLN);
            AccountId accountId = new AccountId(1L);
            when(accountRepository.existsById(accountId)).thenReturn(true);
            when(instrumentRepository.existsBySymbol(any())).thenReturn(true);
            when(instrumentRepository.findBySymbol(any())).thenReturn(Optional.of(existingInstrument));
            when(positionRepository.findBySymbol(any())).thenReturn(Optional.of(existingPosition));
            when(positionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
            InstrumentName instrumentName = new InstrumentName("Apple Inc.");
            InstrumentType instrumentType = InstrumentType.STOCK;
            Currency currency = Currency.PLN;
            Quantity quantity = Quantity.of(100);
            CostBasis costBasis = CostBasis.of(Money.pln("150.00"));

            // When
            Position result = positionCommandUseCaseService.addPosition(
                    symbol, instrumentName, instrumentType, currency, accountId, quantity, costBasis);

            // Then
            assertThat(result.symbol().value()).isEqualTo("AAPL");
            // 50 + 100 = 150 shares
            assertThat(result.calculateTotalQuantity().value()).isEqualByComparingTo("150");

            // Verify instrument was NOT created
            verify(instrumentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when account not found")
        void shouldThrowResourceNotFoundExceptionWhenAccountNotFound() {
            // Given
            AccountId accountId = new AccountId(999L);
            when(accountRepository.existsById(accountId)).thenReturn(false);

            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
            InstrumentName instrumentName = new InstrumentName("Apple Inc.");
            InstrumentType instrumentType = InstrumentType.STOCK;
            Currency currency = Currency.PLN;
            Quantity quantity = Quantity.of(100);
            CostBasis costBasis = CostBasis.of(Money.pln("150.00"));

            // When/Then
            assertThatThrownBy(() -> positionCommandUseCaseService.addPosition(
                    symbol, instrumentName, instrumentType, currency, accountId, quantity, costBasis))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("updatePosition")
    class UpdatePosition {

        @Test
        @DisplayName("should update existing position")
        void shouldUpdateExistingPosition() {
            // Given
            Position existingPosition = createPosition("AAPL", 50, "140.00");
            AccountId accountId = new AccountId(1L);
            when(accountRepository.existsById(accountId)).thenReturn(true);
            when(positionRepository.findBySymbol(any())).thenReturn(Optional.of(existingPosition));
            when(positionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
            Quantity quantity = Quantity.of(100);
            CostBasis costBasis = CostBasis.of(Money.pln("160.00"));

            // When
            Position result = positionCommandUseCaseService.updatePosition(
                    symbol, accountId, quantity, costBasis);

            // Then
            assertThat(result.symbol().value()).isEqualTo("AAPL");
            assertThat(result.calculateTotalQuantity().value()).isEqualByComparingTo("150");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when position not found")
        void shouldThrowResourceNotFoundExceptionWhenPositionNotFound() {
            // Given
            AccountId accountId = new AccountId(1L);
            when(accountRepository.existsById(accountId)).thenReturn(true);
            when(positionRepository.findBySymbol(any())).thenReturn(Optional.empty());

            InstrumentSymbol symbol = InstrumentSymbol.of("UNKNOWN");
            Quantity quantity = Quantity.of(100);
            CostBasis costBasis = CostBasis.of(Money.pln("160.00"));

            // When/Then
            assertThatThrownBy(() -> positionCommandUseCaseService.updatePosition(
                    symbol, accountId, quantity, costBasis))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Position")
                    .hasMessageContaining("UNKNOWN");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when account not found")
        void shouldThrowResourceNotFoundExceptionWhenAccountNotFound() {
            // Given
            AccountId accountId = new AccountId(999L);
            when(accountRepository.existsById(accountId)).thenReturn(false);

            InstrumentSymbol symbol = InstrumentSymbol.of("AAPL");
            Quantity quantity = Quantity.of(100);
            CostBasis costBasis = CostBasis.of(Money.pln("160.00"));

            // When/Then
            assertThatThrownBy(() -> positionCommandUseCaseService.updatePosition(
                    symbol, accountId, quantity, costBasis))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account")
                    .hasMessageContaining("999");
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
