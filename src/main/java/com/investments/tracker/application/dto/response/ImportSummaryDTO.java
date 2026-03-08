package com.investments.tracker.application.dto.response;

import java.util.List;

/** Summary of an import session's parsing results. */
public record ImportSummaryDTO(
        int totalTransactions,
        int matchedInstruments,
        int unmatchedInstruments,
        List<UnmatchedInstrumentDTO> unmatchedDetails,
        List<InstrumentNeedingPriceDTO> instrumentsNeedingPrices) {}
