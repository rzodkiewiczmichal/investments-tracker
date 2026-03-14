package com.investments.tracker.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Configuration for the Finnhub API client. */
@Configuration
public class FinnhubApiConfig {

    @Bean
    public RestClient finnhubRestClient(@Value("${app.finnhub.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
