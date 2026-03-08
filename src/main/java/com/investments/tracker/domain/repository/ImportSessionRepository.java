package com.investments.tracker.domain.repository;

import java.util.Optional;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.value.ImportSessionId;

/** Repository for the ImportSession aggregate. */
public interface ImportSessionRepository {

    ImportSession save(ImportSession session);

    Optional<ImportSession> findById(ImportSessionId id);
}
