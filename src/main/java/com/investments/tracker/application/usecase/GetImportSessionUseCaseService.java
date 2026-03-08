package com.investments.tracker.application.usecase;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.tracker.domain.exception.ImportSessionNotFoundException;
import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.value.ImportSessionId;
import com.investments.tracker.domain.repository.ImportSessionRepository;

/** Implementation of the get import session use case. */
@Service
@Transactional(readOnly = true)
public class GetImportSessionUseCaseService implements GetImportSessionUseCase {

    private final ImportSessionRepository importSessionRepository;

    public GetImportSessionUseCaseService(ImportSessionRepository importSessionRepository) {
        this.importSessionRepository = importSessionRepository;
    }

    @Override
    public ImportSession getImportSession(ImportSessionId id) {
        Objects.requireNonNull(id, "id cannot be null");

        return importSessionRepository
                .findById(id)
                .orElseThrow(() -> ImportSessionNotFoundException.byId(id.value()));
    }
}
