package com.investments.tracker.infrastructure.persistence.adapter;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.Currency;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.model.value.Money;
import com.investments.tracker.domain.model.value.Price;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.infrastructure.persistence.entity.PositionJdbcEntity;
import com.investments.tracker.infrastructure.persistence.mapper.PositionPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.InstrumentJdbcRepository;
import com.investments.tracker.infrastructure.persistence.repository.PositionJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Spring Data JDBC implementation of the PositionRepository domain port.
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

    private final PositionJdbcRepository jdbcRepository;
    private final InstrumentJdbcRepository instrumentJdbcRepository;
    private final PositionPersistenceMapper mapper;

    public PositionRepositoryAdapter(PositionJdbcRepository jdbcRepository,
                                     InstrumentJdbcRepository instrumentJdbcRepository,
                                     PositionPersistenceMapper mapper) {
        this.jdbcRepository = jdbcRepository;
        this.instrumentJdbcRepository = instrumentJdbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Position> findBySymbol(InstrumentSymbol symbol) {
        return jdbcRepository.findById(symbol.value())
                .map(entity -> {
                    Price currentPrice = fetchCurrentPrice(symbol.value());
                    return mapper.toDomain(entity, currentPrice);
                });
    }

    @Override
    public Collection<Position> findAll() {
        return jdbcRepository.findAll().stream()
                .map(entity -> {
                    Price currentPrice = fetchCurrentPrice(entity.instrumentSymbol());
                    return mapper.toDomain(entity, currentPrice);
                })
                .toList();
    }

    @Override
    public Position save(Position position) {
        Long version = jdbcRepository.findById(position.symbol().value())
                .map(PositionJdbcEntity::version).orElse(null);
        PositionJdbcEntity entity = mapper.toEntity(position, version);
        PositionJdbcEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved, position.currentPrice());
    }

    @Override
    public void deleteBySymbol(InstrumentSymbol symbol) {
        jdbcRepository.deleteById(symbol.value());
    }

    @Override
    public boolean existsBySymbol(InstrumentSymbol symbol) {
        return jdbcRepository.existsById(symbol.value());
    }

    @Override
    public long count() {
        return jdbcRepository.count();
    }

    /**
     * Fetches the current price from the Instrument entity.
     *
     * @param symbol the instrument symbol
     * @return the current price
     * @throws IllegalStateException if Instrument not found or has no current price
     */
    private Price fetchCurrentPrice(String symbol) {
        return instrumentJdbcRepository.findById(symbol)
                .map(instrument -> {
                    if (instrument.currentPriceAmount() == null) {
                        throw new IllegalStateException(
                                "Instrument " + symbol + " exists but has no current price set"
                        );
                    }
                    return new Price(new Money(
                            instrument.currentPriceAmount(),
                            Currency.valueOf(instrument.currentPriceCurrency())
                    ));
                })
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot reconstruct Position - Instrument " + symbol + " not found in database"
                ));
    }
}
