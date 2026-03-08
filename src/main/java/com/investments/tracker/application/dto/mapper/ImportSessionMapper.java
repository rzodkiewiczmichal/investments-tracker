package com.investments.tracker.application.dto.mapper;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.investments.tracker.application.dto.response.ImportSessionResponse;
import com.investments.tracker.application.dto.response.ImportSummaryDTO;
import com.investments.tracker.application.dto.response.InstrumentSuggestionDTO;
import com.investments.tracker.application.dto.response.UnmatchedInstrumentDTO;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.repository.InstrumentRepository;

/** Mapper for converting ImportSession domain model to response DTOs. */
@Component
public class ImportSessionMapper {

    private final InstrumentRepository instrumentRepository;

    public ImportSessionMapper(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public ImportSessionResponse toResponse(ImportSession session) {
        List<InstrumentMapping> unresolved = session.unresolvedMappings();
        long matched =
                session.instrumentMappings().stream().filter(InstrumentMapping::isResolved).count();

        List<UnmatchedInstrumentDTO> unmatchedDetails =
                unresolved.stream().map(this::toUnmatchedInstrument).toList();

        ImportSummaryDTO summary =
                new ImportSummaryDTO(
                        session.transactions().size(),
                        (int) matched,
                        unresolved.size(),
                        unmatchedDetails);

        return new ImportSessionResponse(
                session.id().toString(),
                session.status().name(),
                session.broker().value(),
                session.accountName().value(),
                summary,
                session.createdAt(),
                session.completedAt());
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
