package com.investments.tracker.application.usecase;

import com.investments.tracker.application.exception.ResourceNotFoundException;
import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;

/**
 * Implementation of position query use case.
 */
@Service
@Transactional(readOnly = true)
public class PositionQueryUseCaseService implements PositionQueryUseCase {

    private final PositionRepository positionRepository;

    public PositionQueryUseCaseService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Override
    public Collection<Position> listPositions() {
        return positionRepository.findAll();
    }

    @Override
    public Position getPosition(InstrumentSymbol symbol) {
        Objects.requireNonNull(symbol, "symbol cannot be null");

        return positionRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Position", "symbol", symbol.value()));
    }
}
