package com.investments.tracker.infrastructure.web.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.investments.tracker.application.dto.mapper.InstrumentMapper;
import com.investments.tracker.application.dto.response.InstrumentDTO;
import com.investments.tracker.application.dto.response.InstrumentListResponse;
import com.investments.tracker.application.usecase.InstrumentQueryUseCase;
import com.investments.tracker.domain.model.Instrument;

/** REST controller for instrument catalog operations. */
@RestController
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentQueryUseCase instrumentQueryUseCase;
    private final InstrumentMapper instrumentMapper;

    public InstrumentController(
            InstrumentQueryUseCase instrumentQueryUseCase, InstrumentMapper instrumentMapper) {
        this.instrumentQueryUseCase = instrumentQueryUseCase;
        this.instrumentMapper = instrumentMapper;
    }

    /**
     * Searches instruments by symbol or name. Returns all instruments if no query is provided.
     *
     * @param query the search query (optional)
     * @return instrument list response
     */
    @GetMapping
    public InstrumentListResponse searchInstruments(
            @RequestParam(name = "q", required = false, defaultValue = "") String query) {
        Collection<Instrument> instruments = instrumentQueryUseCase.searchInstruments(query);
        List<InstrumentDTO> dtos = instruments.stream().map(instrumentMapper::toDTO).toList();
        return new InstrumentListResponse(dtos, dtos.size());
    }
}
