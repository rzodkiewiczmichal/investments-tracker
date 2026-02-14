package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.model.AccountHolding;
import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.CostBasis;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentName;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.InstrumentType;
import com.investments.tracker.domain.model.value.Quantity;
import com.investments.tracker.domain.repository.AccountRepository;
import com.investments.tracker.domain.repository.InstrumentRepository;
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
    private final InstrumentRepository instrumentRepository;

    public PositionCommandUseCaseService(
            PositionRepository positionRepository,
            AccountRepository accountRepository,
            InstrumentRepository instrumentRepository) {
        this.positionRepository = positionRepository;
        this.accountRepository = accountRepository;
        this.instrumentRepository = instrumentRepository;
    }

    @Override
    public Position addPosition(InstrumentSymbol symbol, InstrumentName instrumentName,
                                InstrumentType instrumentType, Currency currency,
                                AccountId accountId, Quantity quantity, CostBasis costBasis) {
        Objects.requireNonNull(symbol, "symbol cannot be null");
        Objects.requireNonNull(instrumentName, "instrumentName cannot be null");
        Objects.requireNonNull(instrumentType, "instrumentType cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(costBasis, "costBasis cannot be null");

        // Validate account exists (Layer 2 validation per ADR-011)
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", "id", accountId.value().toString());
        }

        // Create instrument if it doesn't exist, or validate currency matches
        if (!instrumentRepository.existsBySymbol(symbol)) {
            Instrument instrument = new Instrument(symbol, instrumentName, instrumentType, currency);
            instrumentRepository.save(instrument);
        } else {
            Instrument existing = instrumentRepository.findBySymbol(symbol).orElseThrow();
            if (existing.currency() != currency) {
                throw new DomainException(
                        "Currency mismatch: instrument " + symbol.value() +
                        " has currency " + existing.currency().getCode() +
                        " but " + currency.getCode() + " was provided");
            }
        }

        // Find or create position
        Position position = positionRepository.findBySymbol(symbol)
                .map(existing -> existing.addHolding(accountId, quantity, costBasis))
                .orElseGet(() -> new Position(
                        symbol,
                        List.of(new AccountHolding(accountId, quantity, costBasis))));

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
