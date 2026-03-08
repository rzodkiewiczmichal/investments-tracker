package com.investments.tracker.domain.model;

import java.util.Objects;

import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Ephemeral record representing a resolved broker transaction.
 *
 * <p>Uses resolved {@link InstrumentSymbol}, not raw broker names. Not persisted — used only during
 * import calculation.
 */
public record Transaction(
        InstrumentSymbol symbol,
        TransactionType type,
        Quantity quantity,
        Price unitPrice,
        Commission commission,
        Currency currency) {

    public Transaction {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        Objects.requireNonNull(commission, "commission cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
    }

    /** Calculates total cost: (unitPrice * quantity) + commission. */
    public Money totalCost() {
        Money value = unitPrice.money().multiply(quantity.toBigDecimal());
        return value.add(commission.money());
    }
}
