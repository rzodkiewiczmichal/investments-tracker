package com.investments.tracker.application.usecase;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.exception.ImportSessionNotFoundException;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.ImportSessionRepository;
import com.investments.tracker.domain.repository.ManualPriceProvider;

/**
 * Implementation of the provide import prices use case.
 *
 * <p>Stores user-provided prices for instruments that lack automatic price data, then completes the
 * import.
 */
@Service
@Transactional
public class ProvideImportPricesUseCaseService implements ProvideImportPricesUseCase {

    private final ImportSessionRepository importSessionRepository;
    private final ManualPriceProvider manualPriceProvider;
    private final CurrentPriceProvider currentPriceProvider;
    private final ConfirmImportUseCaseService confirmImportUseCaseService;

    public ProvideImportPricesUseCaseService(
            ImportSessionRepository importSessionRepository,
            ManualPriceProvider manualPriceProvider,
            CurrentPriceProvider currentPriceProvider,
            ConfirmImportUseCaseService confirmImportUseCaseService) {
        this.importSessionRepository = importSessionRepository;
        this.manualPriceProvider = manualPriceProvider;
        this.currentPriceProvider = currentPriceProvider;
        this.confirmImportUseCaseService = confirmImportUseCaseService;
    }

    @Override
    public ImportSession providePrices(
            ImportSessionId id, Map<InstrumentSymbol, Price> priceOverrides) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(priceOverrides, "priceOverrides cannot be null");

        ImportSession session =
                importSessionRepository
                        .findById(id)
                        .orElseThrow(() -> ImportSessionNotFoundException.byId(id.value()));

        if (session.status() != ImportSessionStatus.PENDING_PRICES) {
            throw new DomainException(
                    "Import session is not in PENDING_PRICES status: " + session.status());
        }

        // Determine which instruments still need prices
        Set<InstrumentSymbol> resolvedSymbols =
                session.instrumentMappings().stream()
                        .filter(InstrumentMapping::isResolved)
                        .map(InstrumentMapping::catalogSymbol)
                        .collect(Collectors.toSet());

        Map<InstrumentSymbol, Price> availablePrices =
                currentPriceProvider.getPrices(resolvedSymbols);

        Set<InstrumentSymbol> stillMissing =
                resolvedSymbols.stream()
                        .filter(s -> !availablePrices.containsKey(s))
                        .filter(s -> !priceOverrides.containsKey(s))
                        .collect(Collectors.toSet());

        if (!stillMissing.isEmpty()) {
            throw new DomainException(
                    "Missing prices for instruments: "
                            + stillMissing.stream()
                                    .map(InstrumentSymbol::value)
                                    .collect(Collectors.joining(", ")));
        }

        // Store user-provided prices in cache
        priceOverrides.forEach(manualPriceProvider::putPrice);

        return confirmImportUseCaseService.completeImport(
                session, session.instrumentMappings().stream().toList());
    }
}
