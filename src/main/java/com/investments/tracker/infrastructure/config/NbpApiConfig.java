package com.investments.tracker.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Configuration for the NBP (Narodowy Bank Polski) API client. */
@Configuration
public class NbpApiConfig {

    @Bean
    public RestClient nbpRestClient(@Value("${app.nbp.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
