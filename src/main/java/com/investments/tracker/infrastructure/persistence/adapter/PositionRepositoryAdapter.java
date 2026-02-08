package com.investments.tracker.infrastructure.persistence.adapter;

import com.investments.tracker.domain.model.Position;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.PositionRepository;
import com.investments.tracker.infrastructure.persistence.entity.PositionJdbcEntity;
import com.investments.tracker.infrastructure.persistence.mapper.PositionPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.PositionJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Spring Data JDBC implementation of the PositionRepository domain port.
 * <p>
 * Position aggregate does not contain price data (ADR-023).
 * Price data lives in the Instrument aggregate and is fetched separately.
 * </p>
 */
@Repository
public class PositionRepositoryAdapter implements PositionRepository {

    private final PositionJdbcRepository jdbcRepository;
    private final PositionPersistenceMapper mapper;

    public PositionRepositoryAdapter(PositionJdbcRepository jdbcRepository,
                                     PositionPersistenceMapper mapper) {
        this.jdbcRepository = jdbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Position> findBySymbol(InstrumentSymbol symbol) {
        return jdbcRepository.findById(symbol.value())
                .map(mapper::toDomain);
    }

    @Override
    public Collection<Position> findAll() {
        return jdbcRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Position save(Position position) {
        Long version = jdbcRepository.findById(position.symbol().value())
                .map(PositionJdbcEntity::version).orElse(null);
        PositionJdbcEntity entity = mapper.toEntity(position, version);
        PositionJdbcEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
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
}
