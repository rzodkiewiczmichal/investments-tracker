package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Quantity;

/** Use case for position commands (create, update). */
public interface PositionCommandUseCase {

    /**
     * Creates a new position or adds to existing position. The instrument must already exist in the
     * catalog (ADR-033).
     *
     * @param symbol the instrument symbol (must exist in catalog)
     * @param accountId the account to add holdings to
     * @param quantity the quantity to add
     * @param costBasis the cost basis for the new shares
     * @return the created/updated position
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if instrument
     *     not in catalog or account not found
     */
    Position addPosition(
            InstrumentSymbol symbol, AccountId accountId, Quantity quantity, CostBasis costBasis);

    /**
     * Updates an existing position (adds shares).
     *
     * @param symbol the instrument symbol
     * @param accountId the account to update
     * @param quantity the quantity to add
     * @param costBasis the cost basis for the new shares
     * @return the updated position
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if position
     *     or account not found
     */
    Position updatePosition(
            InstrumentSymbol symbol, AccountId accountId, Quantity quantity, CostBasis costBasis);

    /**
     * Deletes a position. The catalog instrument is preserved (master data).
     *
     * @param symbol the instrument symbol
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if position
     *     not found
     */
    void deletePosition(InstrumentSymbol symbol);
}
