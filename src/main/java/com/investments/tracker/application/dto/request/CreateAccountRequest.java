package com.investments.tracker.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Request to create a new brokerage account. */
public record CreateAccountRequest(
        @NotBlank(message = "Account name is required") String name,
        @NotBlank(message = "Broker name is required") String broker) {}
