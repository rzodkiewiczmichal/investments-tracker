package com.investments.tracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.tracker.application.port.out.TransactionHistoryParser;
import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;
import com.investments.tracker.domain.repository.ImportSessionRepository;
import com.investments.tracker.domain.repository.InstrumentRepository;

@ExtendWith(MockitoExtension.class)
class InitiateImportUseCaseServiceTest {

    @Mock private TransactionHistoryParser parser;

    @Mock private InstrumentRepository instrumentRepository;

    @Mock private ImportSessionRepository importSessionRepository;

    private InitiateImportUseCaseService useCase;

    @BeforeEach
    void setUp() {
        useCase =
                new InitiateImportUseCaseService(
                        List.of(parser), instrumentRepository, importSessionRepository);
    }

    @Test
    void allInstrumentsMatchedSetsReadyToConfirm() {
        when(parser.brokerName()).thenReturn("mBank");
        when(parser.parse(any())).thenReturn(List.of(rawTx("ETFBCASH", TransactionType.BUY, 50)));
        when(instrumentRepository.findBySymbol(new InstrumentSymbol("ETFBCASH")))
                .thenReturn(Optional.of(instrument("ETFBCASH")));
        when(importSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImportSession session =
                useCase.initiateImport(
                        "mBank", AccountName.of("Test"), new ByteArrayInputStream(new byte[0]));

        assertThat(session.status()).isEqualTo(ImportSessionStatus.READY_TO_CONFIRM);
        assertThat(session.instrumentMappings()).hasSize(1);
        assertThat(session.instrumentMappings().getFirst().isResolved()).isTrue();
    }

    @Test
    void unmatchedInstrumentSetsPendingReview() {
        when(parser.brokerName()).thenReturn("mBank");
        when(parser.parse(any())).thenReturn(List.of(rawTx("TORPOL", TransactionType.BUY, 100)));
        when(instrumentRepository.findBySymbol(new InstrumentSymbol("TORPOL")))
                .thenReturn(Optional.empty());
        when(importSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImportSession session =
                useCase.initiateImport(
                        "mBank", AccountName.of("Test"), new ByteArrayInputStream(new byte[0]));

        assertThat(session.status()).isEqualTo(ImportSessionStatus.PENDING_REVIEW);
        assertThat(session.unresolvedMappings()).hasSize(1);
        assertThat(session.unresolvedMappings().getFirst().brokerName().value())
                .isEqualTo("TORPOL");
    }

    @Test
    void fullySoldInstrumentsAreSkipped() {
        when(parser.brokerName()).thenReturn("mBank");
        when(parser.parse(any()))
                .thenReturn(
                        List.of(
                                rawTx("ALLEGRO", TransactionType.BUY, 100),
                                rawTx("ALLEGRO", TransactionType.SELL, 100)));
        when(importSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImportSession session =
                useCase.initiateImport(
                        "mBank", AccountName.of("Test"), new ByteArrayInputStream(new byte[0]));

        assertThat(session.instrumentMappings()).isEmpty();
        assertThat(session.status()).isEqualTo(ImportSessionStatus.READY_TO_CONFIRM);
    }

    @Test
    void throwsForUnsupportedBroker() {
        when(parser.brokerName()).thenReturn("mBank");

        assertThatThrownBy(
                        () ->
                                useCase.initiateImport(
                                        "Unknown",
                                        AccountName.of("Test"),
                                        new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(ImportParsingException.class)
                .hasMessageContaining("No parser available");
    }

    private RawTransaction rawTx(String instrumentName, TransactionType type, int qty) {
        return new RawTransaction(
                BrokerInstrumentName.of(instrumentName),
                type,
                Quantity.of(qty),
                Price.of(Money.pln("10.00")),
                Commission.pln("1.00"),
                Currency.PLN);
    }

    private Instrument instrument(String symbol) {
        return new Instrument(
                new InstrumentSymbol(symbol),
                new InstrumentName(symbol),
                InstrumentType.STOCK,
                Currency.PLN);
    }
}
