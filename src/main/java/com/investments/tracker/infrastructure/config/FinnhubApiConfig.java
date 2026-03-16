package com.investments.tracker.infrastructure.config;

import java.net.http.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the Finnhub price API client.
 *
 * <p>Only creates the RestClient bean when the API key is configured. Without the key, the {@link
 * com.investments.tracker.infrastructure.external.finnhub.FinnhubPriceClient} will not be usable
 * and US price fetching will be disabled.
 *
 * @see <a href="../../docs/adr/ADR-031-instrument-price-providers.md">ADR-031</a>
 */
@Configuration
public class FinnhubApiConfig {

    @Bean
    @ConditionalOnProperty(name = "app.finnhub.api-key")
    public RestClient finnhubRestClient(
            @Value("${app.finnhub.base-url}") String baseUrl,
            @Value("${app.finnhub.api-key}") String apiKey) {
        HttpClient httpClient =
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("X-Finnhub-Token", apiKey)
                .build();
    }
}
