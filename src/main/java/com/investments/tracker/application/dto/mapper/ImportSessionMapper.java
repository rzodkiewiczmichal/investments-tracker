package com.investments.tracker.application.dto.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.investments.tracker.application.dto.response.ImportSessionResponse;
import com.investments.tracker.application.dto.response.ImportSummaryDTO;
import com.investments.tracker.application.dto.response.InstrumentNeedingPriceDTO;
import com.investments.tracker.application.dto.response.InstrumentSuggestionDTO;
import com.investments.tracker.application.dto.response.UnmatchedInstrumentDTO;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.value.ImportSessionStatus;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.CurrentPriceProvider;
import com.investments.tracker.domain.repository.InstrumentRepository;

/** Mapper for converting ImportSession domain model to response DTOs. */
@Component
public class ImportSessionMapper {

    private final InstrumentRepository instrumentRepository;
    private final CurrentPriceProvider currentPriceProvider;

    public ImportSessionMapper(
            InstrumentRepository instrumentRepository, CurrentPriceProvider currentPriceProvider) {
        this.instrumentRepository = instrumentRepository;
        this.currentPriceProvider = currentPriceProvider;
    }

    public ImportSessionResponse toResponse(ImportSession session) {
        List<InstrumentMapping> unresolved = session.unresolvedMappings();
        long matched =
                session.instrumentMappings().stream().filter(InstrumentMapping::isResolved).count();

        List<UnmatchedInstrumentDTO> unmatchedDetails =
                unresolved.stream().map(this::toUnmatchedInstrument).toList();

        List<InstrumentNeedingPriceDTO> instrumentsNeedingPrices =
                session.status() == ImportSessionStatus.PENDING_PRICES
                        ? buildInstrumentsNeedingPrices(session)
                        : List.of();

        ImportSummaryDTO summary =
                new ImportSummaryDTO(
                        session.transactions().size(),
                        (int) matched,
                        unresolved.size(),
                        unmatchedDetails,
                        instrumentsNeedingPrices);

        return new ImportSessionResponse(
                session.id().toString(),
                session.status().name(),
                session.broker().value(),
                session.accountName().value(),
                summary,
                session.createdAt(),
                session.completedAt());
    }

    private List<InstrumentNeedingPriceDTO> buildInstrumentsNeedingPrices(ImportSession session) {
        Set<InstrumentSymbol> resolvedSymbols =
                session.instrumentMappings().stream()
                        .filter(InstrumentMapping::isResolved)
                        .map(InstrumentMapping::catalogSymbol)
                        .collect(Collectors.toSet());

        Map<InstrumentSymbol, Price> availablePrices =
                currentPriceProvider.getPrices(resolvedSymbols);

        return resolvedSymbols.stream()
                .filter(s -> !availablePrices.containsKey(s))
                .map(
                        symbol -> {
                            Optional<Instrument> instrument =
                                    instrumentRepository.findBySymbol(symbol);
                            String name =
                                    instrument.map(i -> i.name().value()).orElse(symbol.value());
                            String currency =
                                    instrument.map(i -> i.currency().getCode()).orElse("PLN");
                            return new InstrumentNeedingPriceDTO(symbol.value(), name, currency);
                        })
                .toList();
    }

    private UnmatchedInstrumentDTO toUnmatchedInstrument(InstrumentMapping mapping) {
        Collection<Instrument> suggestions =
                instrumentRepository.searchBySymbolOrName(mapping.brokerName().value());

        List<InstrumentSuggestionDTO> suggestionDtos =
                suggestions.stream()
                        .map(i -> new InstrumentSuggestionDTO(i.symbol().value(), i.name().value()))
                        .toList();

        return new UnmatchedInstrumentDTO(mapping.brokerName().value(), suggestionDtos);
    }
}
