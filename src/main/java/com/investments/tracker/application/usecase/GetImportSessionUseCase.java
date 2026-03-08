package com.investments.tracker.application.usecase;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.value.ImportSessionId;

/** Use case for retrieving an import session's current state. */
public interface GetImportSessionUseCase {

    /**
     * Retrieves an import session by its ID.
     *
     * @param id the import session ID
     * @return the import session
     * @throws com.investments.tracker.domain.exception.ImportSessionNotFoundException if not found
     */
    ImportSession getImportSession(ImportSessionId id);
}
