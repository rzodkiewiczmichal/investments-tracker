package com.investments.tracker.domain.model;

import java.util.Objects;

import com.investments.tracker.domain.model.value.BrokerInstrumentName;
import com.investments.tracker.domain.model.value.Commission;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.model.value.TransactionType;

/**
 * Raw broker transaction data before symbol resolution.
 *
 * <p>Contains the unprocessed broker instrument name (e.g., mBank "Papier" value) which may not
 * match catalog symbols. Resolution to a catalog symbol is performed by the user during the import
 * flow.
 *
 * <p>Stored within {@link ImportSession} aggregate so parsed data does not need to be re-parsed
 * from the original file.
 */
public record RawTransaction(
        BrokerInstrumentName brokerInstrumentName,
        TransactionType type,
        Quantity quantity,
        Price unitPrice,
        Commission commission,
        Currency currency) {

    public RawTransaction {
        Objects.requireNonNull(brokerInstrumentName, "brokerInstrumentName cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        Objects.requireNonNull(commission, "commission cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
    }
}
