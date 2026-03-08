package com.investments.tracker.application.usecase;

import java.util.List;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.InstrumentMapping;
import com.investments.tracker.domain.model.value.ImportSessionId;

/**
 * Use case for confirming an import session with user-provided instrument mappings.
 *
 * <p>Validates mappings, applies them, computes holdings from the stored transactions, and performs
 * full replacement of positions for the target account.
 */
public interface ConfirmImportUseCase {

    /**
     * Confirms the import with user-provided mappings for unmatched instruments.
     *
     * @param id the import session ID
     * @param userMappings user-provided mappings for unmatched instruments (may be empty for
     *     READY_TO_CONFIRM sessions)
     * @return the completed import session
     */
    ImportSession confirmImport(ImportSessionId id, List<InstrumentMapping> userMappings);
}
