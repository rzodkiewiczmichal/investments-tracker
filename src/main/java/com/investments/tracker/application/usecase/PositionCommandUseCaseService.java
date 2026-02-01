package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.domain.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of position command use case.
 */
@Service
@Transactional
public class PositionCommandUseCaseService implements PositionCommandUseCase {

    private final PositionRepository positionRepository;
    private final AccountRepository accountRepository;

    public PositionCommandUseCaseService(
            PositionRepository positionRepository,
            AccountRepository accountRepository) {
        this.positionRepository = positionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Position addPosition(InstrumentSymbol symbol, AccountId accountId,
                                Quantity quantity, CostBasis costBasis, Price currentPrice) {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(costBasis, "costBasis cannot be null");
        Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

        // Validate account exists (Layer 2 validation per ADR-011)
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", "id", accountId.value().toString());
        }

        // Find or create position
        Position position = positionRepository.findBySymbol(symbol)
                .map(existing -> existing.addHolding(accountId, quantity, costBasis))
                .orElseGet(() -> new Position(
                        symbol,
                        List.of(new AccountHolding(accountId, quantity, costBasis)),
                        currentPrice));

        return positionRepository.save(position);
    }

    @Override
    public Position updatePosition(InstrumentSymbol symbol, AccountId accountId,
                                   Quantity quantity, CostBasis costBasis) {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(costBasis, "costBasis cannot be null");

        // Validate account exists (Layer 2 validation per ADR-011)
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", "id", accountId.value().toString());
        }

        // Find existing position
        Position position = positionRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Position", "symbol", symbol.value()));

        // Add shares to position
        Position updated = position.addHolding(accountId, quantity, costBasis);

        return positionRepository.save(updated);
    }
}
