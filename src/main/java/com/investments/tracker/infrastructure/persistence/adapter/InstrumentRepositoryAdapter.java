package com.investments.tracker.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.investments.tracker.domain.model.Instrument;
import com.investments.tracker.domain.model.value.InstrumentSymbol;
import com.investments.tracker.domain.repository.InstrumentRepository;
import com.investments.tracker.infrastructure.persistence.entity.InstrumentJdbcEntity;
import com.investments.tracker.infrastructure.persistence.mapper.InstrumentPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.InstrumentJdbcRepository;

/** Spring Data JDBC implementation of the InstrumentRepository domain port. */
@Repository
public class InstrumentRepositoryAdapter implements InstrumentRepository {

    private final InstrumentJdbcRepository jdbcRepository;
    private final InstrumentPersistenceMapper mapper;

    public InstrumentRepositoryAdapter(
            InstrumentJdbcRepository jdbcRepository, InstrumentPersistenceMapper mapper) {
        this.jdbcRepository = jdbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Instrument> findBySymbol(InstrumentSymbol symbol) {
        return jdbcRepository.findById(symbol.value()).map(mapper::toDomain);
    }

    @Override
    public Collection<Instrument> findAll() {
        return jdbcRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Instrument save(Instrument instrument) {
        Long version =
                jdbcRepository
                        .findById(instrument.symbol().value())
                        .map(InstrumentJdbcEntity::version)
                        .orElse(null);
        InstrumentJdbcEntity entity = mapper.toEntity(instrument, version);
        InstrumentJdbcEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Collection<Instrument> saveAll(Collection<Instrument> instruments) {
        if (instruments.isEmpty()) {
            return List.of();
        }
        List<String> symbols = instruments.stream().map(i -> i.symbol().value()).toList();
        Map<String, Long> existingVersions =
                jdbcRepository.findAllById(symbols).stream()
                        .collect(
                                Collectors.toMap(
                                        InstrumentJdbcEntity::symbol,
                                        InstrumentJdbcEntity::version));
        List<InstrumentJdbcEntity> entities =
                instruments.stream()
                        .map(
                                instrument ->
                                        mapper.toEntity(
                                                instrument,
                                                existingVersions.get(instrument.symbol().value())))
                        .toList();
        List<InstrumentJdbcEntity> saved = jdbcRepository.saveAll(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Collection<Instrument> searchBySymbolOrName(String query) {
        String pattern = "%" + query + "%";
        return jdbcRepository.searchBySymbolOrName(pattern).stream().map(mapper::toDomain).toList();
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
