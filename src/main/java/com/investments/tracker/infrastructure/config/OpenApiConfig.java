package com.investments.tracker.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI investmentTrackerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Investment Tracker API")
                .description("REST API for tracking investments across broker accounts")
                .version("v0.1.0"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development")
            ));
    }
}
