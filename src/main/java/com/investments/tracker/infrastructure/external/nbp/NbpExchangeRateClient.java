package com.investments.tracker.infrastructure.external.nbp;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.investments.tracker.domain.exception.ExchangeRateNotFoundException;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.ExchangeRate;

/**
 * HTTP client for the NBP (Narodowy Bank Polski) exchange rate API.
 *
 * <p>Fetches mid-market exchange rates from NBP Table A. Each call retrieves the latest available
 * rate for a single currency against PLN.
 *
 * @see <a href="https://api.nbp.pl/en.html">NBP API documentation</a>
 */
@Component
public class NbpExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(NbpExchangeRateClient.class);

    private final RestClient restClient;
    private final String ratesPath;

    public NbpExchangeRateClient(
            RestClient nbpRestClient, @Value("${app.nbp.rates-path}") String ratesPath) {
        this.restClient = nbpRestClient;
        this.ratesPath = ratesPath;
    }

    /**
     * Fetches the latest exchange rate for the given currency to PLN from NBP API.
     *
     * @param currency the source currency (must not be PLN)
     * @return exchange rate from currency to PLN
     * @throws ExchangeRateNotFoundException if NBP returns 404 (no rate available)
     * @throws RestClientException if a network or server error occurs
     */
    public ExchangeRate fetchRate(Currency currency) {
        if (currency == Currency.PLN) {
            return ExchangeRate.identity(Currency.PLN);
        }

        String currencyCode = currency.getCode().toLowerCase();
        log.debug("Fetching exchange rate from NBP for {}", currency.getCode());

        try {
            NbpExchangeRateResponse response =
                    restClient
                            .get()
                            .uri(ratesPath, currencyCode)
                            .retrieve()
                            .body(NbpExchangeRateResponse.class);

            if (response == null || response.rates() == null || response.rates().isEmpty()) {
                throw new ExchangeRateNotFoundException(currency, Currency.PLN);
            }

            BigDecimal midMarketRate = response.rates().getFirst().midMarketRate();
            if (midMarketRate == null || midMarketRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ExchangeRateNotFoundException(currency, Currency.PLN);
            }

            ExchangeRate rate = new ExchangeRate(currency, Currency.PLN, midMarketRate);
            log.debug("NBP rate for {}: {}", currency.getCode(), rate.rate());
            return rate;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("NBP returned 404 for {} — no rate available", currency.getCode());
            throw new ExchangeRateNotFoundException(currency, Currency.PLN);
        }
    }
}
