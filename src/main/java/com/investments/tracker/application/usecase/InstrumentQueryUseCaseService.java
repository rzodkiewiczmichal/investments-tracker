package com.investments.tracker.application.usecase;

import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.InstrumentRepository;

/** Implementation of instrument query use case. */
@Service
@Transactional(readOnly = true)
public class InstrumentQueryUseCaseService implements InstrumentQueryUseCase {

    private final InstrumentRepository instrumentRepository;

    public InstrumentQueryUseCaseService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Override
    public Instrument getInstrument(InstrumentSymbol symbol) {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        return instrumentRepository
                .findBySymbol(symbol)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Instrument", "symbol", symbol.value()));
    }

    @Override
    public Collection<Instrument> listInstruments() {
        return instrumentRepository.findAll();
    }

    @Override
    public Collection<Instrument> searchInstruments(String query) {
        Objects.requireNonNull(query, "query cannot be null");
        if (query.isBlank()) {
            return instrumentRepository.findAll();
        }
        return instrumentRepository.searchBySymbolOrName(query);
    }
}
