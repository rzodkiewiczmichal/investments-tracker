package com.investments.tracker.application.dto.response;

import java.math.BigDecimal;

/**
 * DTO representing a monetary amount with currency.
 */
public record MoneyDTO(BigDecimal amount, String currency) {
}
