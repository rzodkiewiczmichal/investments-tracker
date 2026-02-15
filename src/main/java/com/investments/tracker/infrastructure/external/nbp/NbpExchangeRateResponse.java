package com.investments.tracker.infrastructure.external.nbp;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** DTO for NBP API exchange rate response (Table A single currency). */
record NbpExchangeRateResponse(
        @JsonProperty("table") String tableType,
        @JsonProperty("currency") String currencyName,
        @JsonProperty("code") String currencyCode,
        List<NbpRate> rates) {

    record NbpRate(
            @JsonProperty("no") String rateNumber,
            String effectiveDate,
            @JsonProperty("mid") BigDecimal midMarketRate) {}
}
