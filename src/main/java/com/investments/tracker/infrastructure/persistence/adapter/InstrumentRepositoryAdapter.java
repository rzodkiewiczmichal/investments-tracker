package com.investments.tracker.infrastructure.persistence.adapter;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.infrastructure.persistence.entity.InstrumentJpaEntity;
import com.investments.tracker.infrastructure.persistence.mapper.InstrumentPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.InstrumentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * JPA-based implementation of the InstrumentRepository domain port.
 */
@Repository
public class InstrumentRepositoryAdapter implements InstrumentRepository {

    private final InstrumentJpaRepository jpaRepository;
    private final InstrumentPersistenceMapper mapper;

    public InstrumentRepositoryAdapter(InstrumentJpaRepository jpaRepository, InstrumentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Instrument> findBySymbol(InstrumentSymbol symbol) {
        return jpaRepository.findById(symbol.value())
                .map(mapper::toDomain);
    }

    @Override
    public Collection<Instrument> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Instrument save(Instrument instrument) {
        InstrumentJpaEntity entity = mapper.toEntity(instrument);
        InstrumentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
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
}
