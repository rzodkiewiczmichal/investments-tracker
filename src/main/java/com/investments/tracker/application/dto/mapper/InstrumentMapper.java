package com.investments.tracker.application.dto.mapper;

import org.springframework.stereotype.Component;

import com.investments.tracker.application.dto.response.InstrumentDTO;
import com.investments.tracker.domain.model.Instrument;

/** Mapper for Instrument domain objects to DTOs. */
@Component
public class InstrumentMapper {

    /**
     * Maps an Instrument to InstrumentDTO.
     *
     * @param instrument the instrument domain object
     * @return the instrument DTO
     */
    public InstrumentDTO toDTO(Instrument instrument) {
        return new InstrumentDTO(
                instrument.symbol().value(),
                instrument.name().value(),
                instrument.type().name(),
                instrument.currency().getCode());
    }
}
