package com.investments.tracker.infrastructure.persistence.adapter;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.infrastructure.persistence.entity.PositionJpaEntity;
import com.investments.tracker.infrastructure.persistence.mapper.PositionPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.InstrumentJpaRepository;
import com.investments.tracker.infrastructure.persistence.repository.PositionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * JPA-based implementation of the PositionRepository domain port.
 * <p>
 * This adapter has special requirements:
 * <ul>
 *   <li>findBySymbol and findAll must fetch currentPrice from Instrument</li>
 *   <li>save() does NOT create Instrument - that's the use case's responsibility</li>
 * </ul>
 * </p>
 */
@Repository
public class PositionRepositoryAdapter implements PositionRepository {

    private final PositionJpaRepository jpaRepository;
    private final InstrumentJpaRepository instrumentJpaRepository;
    private final PositionPersistenceMapper mapper;

    public PositionRepositoryAdapter(PositionJpaRepository jpaRepository,
                                     InstrumentJpaRepository instrumentJpaRepository,
                                     PositionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.instrumentJpaRepository = instrumentJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Position> findBySymbol(InstrumentSymbol symbol) {
        return jpaRepository.findById(symbol.value())
                .map(entity -> {
                    Price currentPrice = fetchCurrentPrice(symbol.value());
                    return mapper.toDomain(entity, currentPrice);
                });
    }

    @Override
    public Collection<Position> findAll() {
        return jpaRepository.findAll().stream()
                .map(entity -> {
                    Price currentPrice = fetchCurrentPrice(entity.getInstrumentSymbol());
                    return mapper.toDomain(entity, currentPrice);
                })
                .toList();
    }

    @Override
    public Position save(Position position) {
        // Repository only handles Position aggregate
        // Instrument MUST already exist (use case responsibility)
        PositionJpaEntity entity = mapper.toEntity(position);
        PositionJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved, position.currentPrice());
    }

    @Override
    public void deleteBySymbol(InstrumentSymbol symbol) {
        jpaRepository.deleteById(symbol.value());
    }

    @Override
    public boolean existsBySymbol(InstrumentSymbol symbol) {
        return jpaRepository.existsById(symbol.value());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    /**
     * Fetches the current price from the Instrument entity.
     *
     * @param symbol the instrument symbol
     * @return the current price
     * @throws IllegalStateException if Instrument not found or has no current price
     */
    private Price fetchCurrentPrice(String symbol) {
        return instrumentJpaRepository.findById(symbol)
                .map(instrument -> {
                    if (instrument.getCurrentPriceAmount() == null) {
                        throw new IllegalStateException(
                                "Instrument " + symbol + " exists but has no current price set"
                        );
                    }
                    return new Price(new Money(
                            instrument.getCurrentPriceAmount(),
                            Currency.valueOf(instrument.getCurrentPriceCurrency())
                    ));
                })
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot reconstruct Position - Instrument " + symbol + " not found in database"
                ));
    }
}
