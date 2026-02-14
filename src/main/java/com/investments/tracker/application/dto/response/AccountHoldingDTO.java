package com.investments.tracker.application.dto.response;

import java.math.BigDecimal;

/** DTO for an account holding within a position. */
public record AccountHoldingDTO(
        Long accountId, String accountName, BigDecimal quantity, MoneyDTO costBasis) {}
