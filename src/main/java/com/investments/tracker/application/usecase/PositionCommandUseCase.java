package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;

/**
 * Use case for position commands (create, update).
 */
public interface PositionCommandUseCase {

    /**
     * Creates a new position or adds to existing position.
     *
     * @param symbol the instrument symbol
     * @param accountId the account to add holdings to
     * @param quantity the quantity to add
     * @param costBasis the cost basis for the new shares
     * @param currentPrice the current market price
     * @return the created/updated position
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if account not found
     */
    Position addPosition(InstrumentSymbol symbol, AccountId accountId,
                         Quantity quantity, CostBasis costBasis, Price currentPrice);

    /**
     * Updates an existing position (adds shares).
     *
     * @param symbol the instrument symbol
     * @param accountId the account to update
     * @param quantity the quantity to add
     * @param costBasis the cost basis for the new shares
     * @return the updated position
     * @throws com.investments.tracker.application.exception.ResourceNotFoundException if position or account not found
     */
    Position updatePosition(InstrumentSymbol symbol, AccountId accountId,
                            Quantity quantity, CostBasis costBasis);
}
