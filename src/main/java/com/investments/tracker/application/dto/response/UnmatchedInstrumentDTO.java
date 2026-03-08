package com.investments.tracker.application.dto.response;

import java.util.List;

/** Unmatched broker instrument with catalog search suggestions. */
public record UnmatchedInstrumentDTO(
        String brokerName, List<InstrumentSuggestionDTO> suggestions) {}
