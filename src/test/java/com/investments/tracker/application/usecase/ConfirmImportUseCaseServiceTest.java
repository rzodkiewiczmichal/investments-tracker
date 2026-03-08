package com.investments.tracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investments.tracker.domain.exception.ImportSessionNotFoundException;
import com.investments.tracker.domain.exception.IncompleteMappingsException;
import com.investments.tracker.domain.exception.InvalidMappingException;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ImportSessionRepository;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.ImportCalculationService;

@ExtendWith(MockitoExtension.class)
class ConfirmImportUseCaseServiceTest {

    @Mock private ImportSessionRepository importSessionRepository;

    @Mock private InstrumentRepository instrumentRepository;

    @Mock private AccountRepository accountRepository;

    @Mock private PositionRepository positionRepository;

    @Mock private ImportCalculationService importCalculationService;

    @Mock private CurrentPriceProvider currentPriceProvider;

    @InjectMocks private ConfirmImportUseCaseService useCase;

    private final ImportSessionId sessionId = ImportSessionId.of(UUID.randomUUID());

    @Test
    void throwsWhenSessionNotFound() {
        when(importSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.confirmImport(sessionId, List.of()))
                .isInstanceOf(ImportSessionNotFoundException.class);
    }

    @Test
    void throwsWhenMappingsIncomplete() {
        ImportSession session =
                sessionWith(
                        List.of(rawTx("TORPOL")),
                        List.of(InstrumentMapping.unresolved(BrokerInstrumentName.of("TORPOL"))),
                        ImportSessionStatus.PENDING_REVIEW);
        when(importSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> useCase.confirmImport(sessionId, List.of()))
                .isInstanceOf(IncompleteMappingsException.class);
    }

    @Test
    void throwsWhenCatalogSymbolDoesNotExist() {
        ImportSession session =
                sessionWith(
                        List.of(rawTx("TORPOL")),
                        List.of(InstrumentMapping.unresolved(BrokerInstrumentName.of("TORPOL"))),
                        ImportSessionStatus.PENDING_REVIEW);
        when(importSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(instrumentRepository.existsBySymbol(new InstrumentSymbol("TOR"))).thenReturn(false);

        List<InstrumentMapping> userMappings =
                List.of(
                        InstrumentMapping.resolved(
                                BrokerInstrumentName.of("TORPOL"), new InstrumentSymbol("TOR")));

        assertThatThrownBy(() -> useCase.confirmImport(sessionId, userMappings))
                .isInstanceOf(InvalidMappingException.class)
                .hasMessageContaining("TOR");
    }

    @Test
    void confirmsReadyToConfirmSessionWithEmptyMappings() {
        ImportSession session =
                sessionWith(
                        List.of(rawTx("ETFBCASH")),
                        List.of(
                                InstrumentMapping.resolved(
                                        BrokerInstrumentName.of("ETFBCASH"),
                                        new InstrumentSymbol("ETFBCASH"))),
                        ImportSessionStatus.READY_TO_CONFIRM);
        when(importSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(currentPriceProvider.getPrices(any()))
                .thenReturn(
                        java.util.Map.of(
                                new InstrumentSymbol("ETFBCASH"), Price.of(Money.pln("10.00"))));
        when(accountRepository.findByName(AccountName.of("Test")))
                .thenReturn(
                        Optional.of(
                                new Account(
                                        new AccountId(1L),
                                        AccountName.of("Test"),
                                        BrokerName.of("mBank"))));
        when(importCalculationService.computeHoldings(any(), any())).thenReturn(java.util.Map.of());
        when(positionRepository.findAll()).thenReturn(List.of());
        when(importSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImportSession result = useCase.confirmImport(sessionId, List.of());

        assertThat(result.status()).isEqualTo(ImportSessionStatus.COMPLETED);
        assertThat(result.completedAt()).isNotNull();
    }

    @Test
    void createsAccountIfNotFound() {
        ImportSession session =
                sessionWith(
                        List.of(rawTx("ETFBCASH")),
                        List.of(
                                InstrumentMapping.resolved(
                                        BrokerInstrumentName.of("ETFBCASH"),
                                        new InstrumentSymbol("ETFBCASH"))),
                        ImportSessionStatus.READY_TO_CONFIRM);
        when(importSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(currentPriceProvider.getPrices(any()))
                .thenReturn(
                        java.util.Map.of(
                                new InstrumentSymbol("ETFBCASH"), Price.of(Money.pln("10.00"))));
        when(accountRepository.findByName(AccountName.of("Test"))).thenReturn(Optional.empty());
        when(accountRepository.create(AccountName.of("Test"), BrokerName.of("mBank")))
                .thenReturn(
                        new Account(
                                new AccountId(1L), AccountName.of("Test"), BrokerName.of("mBank")));
        when(importCalculationService.computeHoldings(any(), any())).thenReturn(java.util.Map.of());
        when(positionRepository.findAll()).thenReturn(List.of());
        when(importSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.confirmImport(sessionId, List.of());

        verify(accountRepository).create(AccountName.of("Test"), BrokerName.of("mBank"));
    }

    private ImportSession sessionWith(
            List<RawTransaction> transactions,
            List<InstrumentMapping> mappings,
            ImportSessionStatus status) {
        return new ImportSession(
                sessionId,
                status,
                BrokerName.of("mBank"),
                AccountName.of("Test"),
                transactions,
                mappings,
                LocalDateTime.now(),
                null);
    }

    private RawTransaction rawTx(String instrumentName) {
        return new RawTransaction(
                BrokerInstrumentName.of(instrumentName),
                TransactionType.BUY,
                Quantity.of(100),
                Price.of(Money.pln("10.00")),
                Commission.pln("1.00"),
                Currency.PLN);
    }
}
