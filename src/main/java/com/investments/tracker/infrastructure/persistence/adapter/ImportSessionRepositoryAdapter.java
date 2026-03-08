package com.investments.tracker.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.repository.ImportSessionRepository;
import com.investments.tracker.infrastructure.persistence.entity.ImportSessionJdbcEntity;
import com.investments.tracker.infrastructure.persistence.mapper.ImportSessionPersistenceMapper;
import com.investments.tracker.infrastructure.persistence.repository.ImportSessionJdbcRepository;

/** Spring Data JDBC implementation of the ImportSessionRepository domain port. */
@Repository
public class ImportSessionRepositoryAdapter implements ImportSessionRepository {

    private final ImportSessionJdbcRepository jdbcRepository;
    private final ImportSessionPersistenceMapper mapper;

    public ImportSessionRepositoryAdapter(
            ImportSessionJdbcRepository jdbcRepository, ImportSessionPersistenceMapper mapper) {
        this.jdbcRepository = jdbcRepository;
        this.mapper = mapper;
    }

    @Override
    public ImportSession save(ImportSession session) {
        Long version =
                jdbcRepository
                        .findById(session.id().value())
                        .map(ImportSessionJdbcEntity::version)
                        .orElse(null);
        ImportSessionJdbcEntity entity = mapper.toEntity(session, version);
        ImportSessionJdbcEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ImportSession> findById(ImportSessionId id) {
        return jdbcRepository.findById(id.value()).map(mapper::toDomain);
    }
}
