package com.investments.tracker.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Configuration for the Stooq.pl price API client. */
@Configuration
public class StooqApiConfig {

    @Bean
    public RestClient stooqRestClient(@Value("${app.stooq.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
