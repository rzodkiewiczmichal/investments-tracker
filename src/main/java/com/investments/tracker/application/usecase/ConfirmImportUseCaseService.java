package com.investments.tracker.application.usecase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.domain.exception.ImportSessionNotFoundException;
import com.investments.tracker.domain.exception.IncompleteMappingsException;
import com.investments.tracker.domain.exception.InvalidMappingException;
import com.investments.tracker.domain.model.Account;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.BrokerInstrumentMapping;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.RawTransaction;
import com.investments.tracker.domain.model.Transaction;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.BrokerName;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.domain.repository.BrokerInstrumentMappingRepository;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ImportSessionRepository;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.domain.service.ImportCalculationService;

/** Implementation of the confirm import use case. */
@Service
@Transactional
public class ConfirmImportUseCaseService implements ConfirmImportUseCase {

    private final ImportSessionRepository importSessionRepository;
    private final InstrumentRepository instrumentRepository;
    private final AccountRepository accountRepository;
    private final PositionRepository positionRepository;
    private final ImportCalculationService importCalculationService;
    private final CurrentPriceProvider currentPriceProvider;
    private final BrokerInstrumentMappingRepository brokerMappingRepository;

    public ConfirmImportUseCaseService(
            ImportSessionRepository importSessionRepository,
            InstrumentRepository instrumentRepository,
            AccountRepository accountRepository,
            PositionRepository positionRepository,
            ImportCalculationService importCalculationService,
            CurrentPriceProvider currentPriceProvider,
            BrokerInstrumentMappingRepository brokerMappingRepository) {
        this.importSessionRepository = importSessionRepository;
        this.instrumentRepository = instrumentRepository;
        this.accountRepository = accountRepository;
        this.positionRepository = positionRepository;
        this.importCalculationService = importCalculationService;
        this.currentPriceProvider = currentPriceProvider;
        this.brokerMappingRepository = brokerMappingRepository;
    }

    @Override
    public ImportSession confirmImport(ImportSessionId id, List<InstrumentMapping> userMappings) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(userMappings, "userMappings cannot be null");

        ImportSession session =
                importSessionRepository
                        .findById(id)
                        .orElseThrow(() -> ImportSessionNotFoundException.byId(id.value()));

        List<InstrumentMapping> mergedMappings = mergeMappings(session, userMappings);
        validateMappingsComplete(mergedMappings);
        validateCatalogSymbolsExist(userMappings);
        persistUserMappings(session.broker(), userMappings);

        Set<InstrumentSymbol> resolvedSymbols =
                mergedMappings.stream()
                        .filter(InstrumentMapping::isResolved)
                        .map(InstrumentMapping::catalogSymbol)
                        .collect(Collectors.toSet());

        Map<InstrumentSymbol, Price> availablePrices =
                currentPriceProvider.getPrices(resolvedSymbols);

        Set<InstrumentSymbol> missingPrices =
                resolvedSymbols.stream()
                        .filter(s -> !availablePrices.containsKey(s))
                        .collect(Collectors.toSet());

        if (!missingPrices.isEmpty()) {
            ImportSession pendingPrices =
                    new ImportSession(
                            session.id(),
                            ImportSessionStatus.PENDING_PRICES,
                            session.broker(),
                            session.accountName(),
                            session.transactions(),
                            mergedMappings,
                            session.createdAt(),
                            null);

            return importSessionRepository.save(pendingPrices);
        }

        return completeImport(session, mergedMappings);
    }

    /**
     * Completes the import by resolving transactions, computing holdings, and replacing positions.
     * Package-private so {@link ProvideImportPricesUseCaseService} can reuse it.
     */
    ImportSession completeImport(ImportSession session, List<InstrumentMapping> mergedMappings) {
        List<Transaction> resolvedTransactions =
                resolveTransactions(session.transactions(), mergedMappings);

        Account account = findOrCreateAccount(session);
        AccountId accountId = account.id();

        Map<InstrumentSymbol, AccountHolding> computedHoldings =
                importCalculationService.computeHoldings(resolvedTransactions, accountId);

        replaceHoldings(accountId, computedHoldings);

        ImportSession completed =
                new ImportSession(
                        session.id(),
                        ImportSessionStatus.COMPLETED,
                        session.broker(),
                        session.accountName(),
                        session.transactions(),
                        mergedMappings,
                        session.createdAt(),
                        LocalDateTime.now());

        return importSessionRepository.save(completed);
    }

    private List<InstrumentMapping> mergeMappings(
            ImportSession session, List<InstrumentMapping> userMappings) {
        Map<BrokerInstrumentName, InstrumentSymbol> userMappingMap =
                userMappings.stream()
                        .filter(InstrumentMapping::isResolved)
                        .collect(
                                Collectors.toMap(
                                        InstrumentMapping::brokerName,
                                        InstrumentMapping::catalogSymbol));

        List<InstrumentMapping> merged = new ArrayList<>();
        for (InstrumentMapping existing : session.instrumentMappings()) {
            if (existing.isResolved()) {
                merged.add(existing);
            } else {
                InstrumentSymbol userSymbol = userMappingMap.get(existing.brokerName());
                if (userSymbol != null) {
                    merged.add(InstrumentMapping.resolved(existing.brokerName(), userSymbol));
                } else {
                    merged.add(existing);
                }
            }
        }
        return merged;
    }

    private void validateMappingsComplete(List<InstrumentMapping> mappings) {
        Set<String> unresolved =
                mappings.stream()
                        .filter(InstrumentMapping::isUnresolved)
                        .map(m -> m.brokerName().value())
                        .collect(Collectors.toSet());

        if (!unresolved.isEmpty()) {
            throw IncompleteMappingsException.of(unresolved);
        }
    }

    private void validateCatalogSymbolsExist(List<InstrumentMapping> userMappings) {
        for (InstrumentMapping mapping : userMappings) {
            if (mapping.isResolved()
                    && !instrumentRepository.existsBySymbol(mapping.catalogSymbol())) {
                throw InvalidMappingException.unknownCatalogSymbol(mapping.catalogSymbol().value());
            }
        }
    }

    private List<Transaction> resolveTransactions(
            List<RawTransaction> rawTransactions, List<InstrumentMapping> mappings) {
        Map<BrokerInstrumentName, InstrumentSymbol> symbolMap =
                mappings.stream()
                        .filter(InstrumentMapping::isResolved)
                        .collect(
                                Collectors.toMap(
                                        InstrumentMapping::brokerName,
                                        InstrumentMapping::catalogSymbol));

        return rawTransactions.stream()
                .filter(tx -> symbolMap.containsKey(tx.brokerInstrumentName()))
                .map(
                        tx -> {
                            InstrumentSymbol catalogSymbol =
                                    symbolMap.get(tx.brokerInstrumentName());
                            Currency currency = resolveInstrumentCurrency(catalogSymbol, tx);
                            Price unitPrice =
                                    Price.of(new Money(tx.unitPrice().money().amount(), currency));
                            return new Transaction(
                                    catalogSymbol,
                                    tx.type(),
                                    tx.quantity(),
                                    unitPrice,
                                    tx.commission(),
                                    currency,
                                    tx.transactionDate());
                        })
                .toList();
    }

    private Currency resolveInstrumentCurrency(InstrumentSymbol symbol, RawTransaction fallback) {
        return instrumentRepository
                .findBySymbol(symbol)
                .map(instrument -> instrument.currency())
                .orElse(fallback.currency());
    }

    private Account findOrCreateAccount(ImportSession session) {
        return accountRepository
                .findByName(session.accountName())
                .orElseGet(() -> accountRepository.create(session.accountName(), session.broker()));
    }

    private void persistUserMappings(BrokerName broker, List<InstrumentMapping> userMappings) {
        for (InstrumentMapping mapping : userMappings) {
            if (mapping.isResolved()) {
                brokerMappingRepository.save(
                        new BrokerInstrumentMapping(
                                broker, mapping.brokerName(), mapping.catalogSymbol()));
            }
        }
    }

    private void replaceHoldings(
            AccountId accountId, Map<InstrumentSymbol, AccountHolding> computedHoldings) {
        for (Position position : positionRepository.findAll()) {
            Optional<AccountHolding> existingHolding = position.findHolding(accountId);
            if (existingHolding.isPresent()) {
                if (position.getAccountCount() == 1) {
                    positionRepository.deleteBySymbol(position.symbol());
                } else {
                    Position updated = position.removeHolding(accountId);
                    positionRepository.save(updated);
                }
            }
        }

        for (var entry : computedHoldings.entrySet()) {
            InstrumentSymbol symbol = entry.getKey();
            AccountHolding holding = entry.getValue();

            Optional<Position> existingPosition = positionRepository.findBySymbol(symbol);
            if (existingPosition.isPresent()) {
                Position updated =
                        existingPosition
                                .get()
                                .addHolding(accountId, holding.quantity(), holding.costBasis());
                positionRepository.save(updated);
            } else {
                Position newPosition = new Position(symbol, List.of(holding));
                positionRepository.save(newPosition);
            }
        }
    }
}
