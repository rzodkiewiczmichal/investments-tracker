package com.investments.tracker.application.usecase;

import java.util.Map;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;

/**
 * Use case for providing current prices for instruments that lack automatic price data.
 *
 * <p>Called when the import session is in {@code PENDING_PRICES} status. Stores the user-provided
 * prices and completes the import.
 */
public interface ProvideImportPricesUseCase {

    /**
     * Provides prices for instruments missing automatic price data and completes the import.
     *
     * @param id the import session ID (must be in PENDING_PRICES status)
     * @param priceOverrides map of instrument symbol to user-provided current price
     * @return the completed import session
     */
    ImportSession providePrices(ImportSessionId id, Map<InstrumentSymbol, Price> priceOverrides);
}
